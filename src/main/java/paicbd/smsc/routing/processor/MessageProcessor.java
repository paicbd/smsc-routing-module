package paicbd.smsc.routing.processor;

import com.paicbd.smsc.dto.MessageEvent;
import com.paicbd.smsc.dto.MessagePart;
import com.paicbd.smsc.dto.UtilsRecords;
import com.paicbd.smsc.utils.Converter;
import com.paicbd.smsc.utils.Watcher;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsmpp.bean.ESMClass;
import org.jsmpp.bean.GSMSpecificFeature;
import org.jsmpp.bean.MessageMode;
import org.jsmpp.bean.MessageType;
import org.jsmpp.bean.OptionalParameter;
import org.springframework.stereotype.Component;
import paicbd.smsc.routing.util.AppProperties;
import paicbd.smsc.routing.util.RoutingHelper;
import paicbd.smsc.routing.util.StaticMethods;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import redis.clients.jedis.JedisCluster;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageProcessor implements RoutingProcessor {
    private final AtomicInteger processedMessages = new AtomicInteger(0);
    private final AtomicInteger idPartMessages = new AtomicInteger(0);

    private final JedisCluster jedisCluster;
    private final AppProperties appProperties;
    private final RoutingHelper routingHelper;
    private final CommonProcessor commonProcessor;

    @PostConstruct
    public void init() {
        log.info("Message Processor Initialized Successfully");
        Thread.startVirtualThread(() -> new Watcher(appProperties.getPreMessageList(), processedMessages, 1));
        this.start();
    }

    public void start() {
        CompletableFuture.runAsync(() -> Flux.interval(Duration.ofSeconds(1))
                .doOnNext(val -> routingHelper.processEventsFlux(fetchAllMessages()))
                .subscribe()
        );
    }

    @Override
    public Flux<List<MessageEvent>> fetchAllMessages() {
        var redisListSize = (int) jedisCluster.llen(appProperties.getPreMessageList());
        if (redisListSize <= 0) {
            return Flux.empty();
        }

        int itemsToProcess = appProperties.getPreMessageItemsToProcess();
        int workers = appProperties.getPreMessageWorkers();
        int batchSize = this.routingHelper.calculateBatchPerWorker(itemsToProcess, redisListSize, workers);

        return Flux.range(0, workers)
                .flatMap(worker -> preProcessBatch(batchSize))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public void prepareMessage(MessageEvent messageEvent) {
        this.commonProcessor.setUpInitialSettings(messageEvent);

        boolean treatLikeDlr = Boolean.TRUE.equals(messageEvent.getCheckSubmitSmResponse());
        if (treatLikeDlr) {
            this.commonProcessor.processDlr(messageEvent);
            return;
        }

        this.commonProcessor.processMessage(messageEvent);
    }

    private Flux<List<MessageEvent>> preProcessBatch(int batchSize) {
        List<String> batch = jedisCluster.lpop(this.appProperties.getPreMessageList(), batchSize);
        if (batch == null || batch.isEmpty()) {
            return Flux.empty();
        }

        List<MessageEvent> messageEvents = batch.stream()
                .map(StaticMethods::stringAsEvent)
                .filter(Objects::nonNull)
                .toList();
        messageEvents.parallelStream().forEach(this::prepareMessage);

        List<MessageEvent> mainList = messageEvents.parallelStream()
                .filter(MessageEvent::notApplyForLongMessage)
                .collect(Collectors.toList());
        List<MessageEvent> applicableForLongMessages = messageEvents.parallelStream()
                .filter(MessageEvent::applyForLongMessage)
                .toList();

        if (applicableForLongMessages.isEmpty()) {
            processedMessages.addAndGet(messageEvents.size());
            return Flux.just(mainList);
        }

        applicableForLongMessages.parallelStream().forEach(this::processCandidatesForLongMessages);
        mainList.addAll(applicableForLongMessages);
        processedMessages.addAndGet(messageEvents.size());
        return Flux.just(mainList);
    }

    private void processCandidatesForLongMessages(MessageEvent messageEvent) {
        CommonProcessor.MessagePart partMessage = commonProcessor.getPartsOfMessage(messageEvent);
        int parts = partMessage.parts();
        if (parts <= 1) {
            int minLength = Math.min(messageEvent.getShortMessage().length(), partMessage.messageLength());
            messageEvent.setShortMessage(messageEvent.getShortMessage().substring(0, minLength));
            return;
        }
        String currentMessage = messageEvent.getShortMessage();
        int length = partMessage.messageLength();
        String stringReference = String.valueOf(idPartMessages.incrementAndGet());
        int intReference = idPartMessages.get();
        List<MessagePart> messagePartList = new ArrayList<>();
        for (int i = 0; i < parts; i++) {
            MessagePart messagePartEvent = new MessagePart();
            int partNumber = i + 1;
            int startMessagePart = i * length;
            int endMessagePart = Math.min((i + 1) * length, currentMessage.length());
            String messagePart = currentMessage.substring(startMessagePart, endMessagePart);
            log.debug("Part {} of {}, message: {}", i + 1, parts, messagePart);
            messagePartEvent.setMessageId(System.currentTimeMillis() + "-" + System.nanoTime());
            if (partMessage.isUdh()) {
                byte[] udh = Converter.paramsToUdhBytes(messagePart, intReference, parts, partNumber);
                String udhAsJson = Converter.udhMapToJson(Converter.bytesToUdhMap(udh));
                messagePartEvent.setUdhJson(udhAsJson);
            } else {
                OptionalParameter[] opts = Converter.convertToOptionalParameters(intReference, parts, partNumber);
                messagePartEvent.setOptionalParameters(fromOptionalParameters(opts));
            }
            ESMClass esm = new ESMClass(MessageMode.STORE_AND_FORWARD, MessageType.DEFAULT,
                    partMessage.isUdh() ? GSMSpecificFeature.UDHI : GSMSpecificFeature.DEFAULT);
            messageEvent.setEsmClass((int) esm.value());
            messagePartEvent.setShortMessage(messagePart);
            messagePartEvent.setSegmentSequence(i + 1);
            messagePartEvent.setTotalSegment(parts);
            messagePartEvent.setMsgReferenceNumber(stringReference);
            messagePartList.add(messagePartEvent);
        }
        messageEvent.setMessageParts(messagePartList);
        log.debug("Message {} has {} parts and length {}", messageEvent.getMessageId(), parts, length);
    }

    private static List<UtilsRecords.OptionalParameter> fromOptionalParameters(OptionalParameter[] opts) {
        List<UtilsRecords.OptionalParameter> opList = new ArrayList<>(opts.length);
        for (OptionalParameter opt : opts) {
            opList.add(new UtilsRecords.OptionalParameter(opt.tag, opt.toString()));
        }
        return opList;
    }
}

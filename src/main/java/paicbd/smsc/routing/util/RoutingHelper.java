package paicbd.smsc.routing.util;

import com.paicbd.smsc.cdr.CdrProcessor;
import com.paicbd.smsc.dto.MessageEvent;
import com.paicbd.smsc.dto.UtilsRecords;
import com.paicbd.smsc.utils.UtilsEnum;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import redis.clients.jedis.JedisCluster;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoutingHelper {
    private final ExecutorService dlrExecutorService = Executors.newVirtualThreadPerTaskExecutor();
    private final ExecutorService rPushExecutorService = Executors.newVirtualThreadPerTaskExecutor();

    private final JedisCluster jedisCluster;
    private final CdrProcessor cdrProcessor;
    private final CreditHandler creditHandler;
    private final AppProperties appProperties;

    @PostConstruct
    public void init() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    public void processEventsFlux(Flux<List<MessageEvent>> data) {
        data.subscribeOn(Schedulers.boundedElastic())
                .flatMap(messageEventList -> {
                    List<MessageEvent> itemsToProcessLikeMessage =
                            messageEventList.stream().filter(smEvent -> !smEvent.isDlr()).toList();
                    List<MessageEvent> itemsToProcessLikeDlr =
                            messageEventList.stream().filter(MessageEvent::isProcess).filter(MessageEvent::isDlr).toList();

                    this.processPush(itemsToProcessLikeMessage, itemsToProcessLikeDlr);
                    return Mono.empty();
                }).subscribe();
    }

    public List<MessageEvent> stringListAsEventList(List<String> stringList) {
        return stringList.parallelStream()
                .map(StaticMethods::stringAsEvent)
                .filter(Objects::nonNull)
                .toList();
    }

    public void relocateMessageEvent(List<MessageEvent> messageList) {
        if (!messageList.isEmpty()) {
            this.rPushExecutorService.execute(
                    this.putMessageListInRedis(this.appProperties.getSmppMessageList(), "SMPP", messageList)
            );
            this.rPushExecutorService.execute(
                    this.putMessageListInRedis(this.appProperties.getHttpMessageList(), "HTTP", messageList)
            );
            this.rPushExecutorService.execute(
                    this.putMessageListInRedis(this.appProperties.getSs7MessageList(), "SS7", messageList)
            );
        }
    }

    public Runnable putMessageListInRedis(String queueName, String protocol, List<MessageEvent> messageEventList) {
        return () -> {
            var filteredList = messageEventList
                    .parallelStream()
                    .filter(x -> protocol.equalsIgnoreCase(x.getDestProtocol()))
                    .toList();

            if (!filteredList.isEmpty()) {
                this.jedisCluster.rpush(queueName, filteredList.stream().map(event -> {
                    if ("SP".equalsIgnoreCase(event.getOriginNetworkType())) {
                        creditHandler.incrementCreditUsed(
                                event.getOriginNetworkId(),
                                (Objects.isNull(event.getMessageParts()) ? 1 : event.getMessageParts().size()));
                    }
                    return event.toString();
                }).toList().toArray(new String[0]));
            }
        };
    }

    public void relocateDlrEvent(List<MessageEvent> dlrList) {
        if (!dlrList.isEmpty()) {
            this.dlrExecutorService.execute(this.putDlrListInRedis(dlrList));
        }
    }

    public Runnable putDlrListInRedis(List<MessageEvent> dlrList) {
        return () -> Flux.fromIterable(dlrList)
                .subscribeOn(Schedulers.boundedElastic())
                .groupBy(MessageEvent::getDestProtocol)
                .flatMap(groupedFlux -> groupedFlux.collectList()
                        .doOnNext(listOfMessages -> {
                            switch (groupedFlux.key().toUpperCase()) {
                                case "SMPP" ->
                                        this.insertDlrOnRedis(this.appProperties.getSmppDlrList(), listOfMessages);
                                case "HTTP" ->
                                        this.insertDlrOnRedis(this.appProperties.getHttpDlrList(), listOfMessages);
                                case "SS7" -> this.rPushExecutorService.execute(
                                        this.putMessageListInRedis(this.appProperties.getSs7MessageList(), "SS7", listOfMessages)
                                );
                                default -> log.error("Unknown destination protocol of DLRs in SP {}", listOfMessages);
                            }
                        })).subscribe();
    }

    public void processPush(List<MessageEvent> itemsToProcessLikeMessage, List<MessageEvent> itemsToProcessLikeDlr) {
        this.relocateMessageEvent(itemsToProcessLikeMessage);
        this.relocateDlrEvent(itemsToProcessLikeDlr);
    }

    private void insertDlrOnRedis(String listName, List<MessageEvent> dlrList) {
        this.jedisCluster.rpush(listName, dlrList
                .parallelStream().map(MessageEvent::toString)
                .toList().toArray(new String[0]));
    }

    public void prepareCdr(
            MessageEvent event, UtilsEnum.CdrStatus status, String comment, boolean createCdr) {
        UtilsRecords.CdrDetail cdrDetail = toCdrDetail(event, status, comment);
        cdrProcessor.putCdrDetailOnRedis(cdrDetail);
        if (createCdr) {
            cdrProcessor.createCdr(event.getMessageId());
        }
    }

    public UtilsRecords.CdrDetail toCdrDetail(
            MessageEvent event, UtilsEnum.CdrStatus status, String comment) {
        return event.toCdrDetail(UtilsEnum.Module.ROUTING, StaticMethods.getMessageType(event), status, comment);
    }

    private void shutdown() {
        this.dlrExecutorService.shutdown();
        this.rPushExecutorService.shutdown();
    }

    public int calculateBatchPerWorker(int itemsToProcess, int listSize, int workers) {
        int min = Math.min(itemsToProcess, listSize);
        var bpw = min / workers;
        return bpw > 0 ? bpw : 1;
    }
}

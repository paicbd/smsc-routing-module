package paicbd.smsc.routing.processor;

import com.paicbd.smsc.dto.MessageEvent;
import com.paicbd.smsc.utils.Watcher;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import paicbd.smsc.routing.util.AppProperties;
import paicbd.smsc.routing.util.RoutingHelper;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import redis.clients.jedis.JedisCluster;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliverProcessor implements RoutingProcessor {
    private final AtomicInteger processedDeliveries = new AtomicInteger(0);

    private final JedisCluster jedisCluster;
    private final AppProperties appProperties;
    private final RoutingHelper routingHelper;
    private final CommonProcessor commonProcessor;

    @PostConstruct
    public void init() {
        log.info("Deliver Processor Initialized Successfully");
        Thread.startVirtualThread(() -> new Watcher(appProperties.getPreDeliverList(), processedDeliveries, 1));
        this.start();
    }

    public void start() {
        CompletableFuture.runAsync(() -> Flux.interval(Duration.ofSeconds(1))
                .doOnNext(val -> routingHelper.processEventsFlux(fetchAllMessages()))
                .subscribe());
        log.info("Deliver Processor Started Successfully");
    }

    @Override
    public Flux<List<MessageEvent>> fetchAllMessages() {
        var redisListSize = (int) jedisCluster.llen(appProperties.getPreDeliverList());
        if (redisListSize <= 0) {
            return Flux.empty();
        }

        int itemsToProcess = appProperties.getPreDeliverItemsToProcess();
        int workers = appProperties.getPreDeliverWorkers();
        int batchSize = this.routingHelper.calculateBatchPerWorker(itemsToProcess, redisListSize, workers);
        return Flux.range(0, workers)
                .flatMap(deliverWorker -> {
                    List<String> batch = jedisCluster.lpop(appProperties.getPreDeliverList(), batchSize);
                    if (Objects.isNull(batch) || batch.isEmpty()) {
                        return Flux.empty();
                    }

                    List<MessageEvent> deliverEvents =  routingHelper.stringListAsEventList(batch);
                    deliverEvents.parallelStream().forEach(this::prepareMessage);
                    processedDeliveries.addAndGet(deliverEvents.size());
                    return Flux.just(deliverEvents);
                }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public void prepareMessage(MessageEvent messageEvent) {
        this.commonProcessor.setUpInitialSettings(messageEvent);

        boolean treatLikeMessage = Boolean.FALSE.equals(messageEvent.getCheckSubmitSmResponse());
        if (treatLikeMessage) {
            this.commonProcessor.processMessage(messageEvent);
            return;
        }

        this.commonProcessor.processDlr(messageEvent);
    }
}

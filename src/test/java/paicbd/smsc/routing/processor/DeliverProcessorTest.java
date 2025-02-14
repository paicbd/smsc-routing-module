package paicbd.smsc.routing.processor;


import com.paicbd.smsc.dto.MessageEvent;
import com.paicbd.smsc.utils.RequestDelivery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import paicbd.smsc.routing.util.AppProperties;
import paicbd.smsc.routing.component.RoutingHelper;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import redis.clients.jedis.JedisCluster;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliverProcessorTest {
    @Mock
    JedisCluster jedisCluster;

    @Mock
    AppProperties appProperties;

    @Mock
    RoutingHelper routingHelper;

    @Mock
    CommonProcessor commonProcessor;

    @InjectMocks
    DeliverProcessor deliverProcessor;

    @Test
    @DisplayName("Fetch all messages with empty list then return empty flux")
    void testFetchAllMessagesWithEmptyListThenReturnEmptyFlux() {
        when(appProperties.getPreDeliverList()).thenReturn("preDeliver");
        when(jedisCluster.llen("preDeliver")).thenReturn(0L);
        Flux<List<MessageEvent>> result = deliverProcessor.fetchAllMessages();
        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();

        verify(jedisCluster, times(1)).llen("preDeliver");
    }

    @Test
    @DisplayName("Fetch all messages with non empty list")
    void testFetchAllMessages_NonEmptyList() {
        when(appProperties.getPreDeliverList()).thenReturn("preDeliver");
        when(jedisCluster.llen("preDeliver")).thenReturn(1L);
        when(appProperties.getPreDeliverItemsToProcess()).thenReturn(1);
        when(appProperties.getPreDeliverWorkers()).thenReturn(1);
        when(routingHelper.calculateBatchPerWorker(1, 1, 1)).thenReturn(1);

        MessageEvent dlrToProcess =  MessageEvent.builder()
                .id("1722442489766-7788604799226")
                .messageId("1722442489770-7788608933795")
                .deliverSmId("1722442489766-7788604799227")
                .systemId("smpp_sp")
                .commandStatus(0)
                .segmentSequence(2)
                .sourceAddr("50588888888")
                .destinationAddr("50599999999")
                .validityPeriod(0)
                .registeredDelivery(RequestDelivery.NON_REQUEST_DLR.getValue())
                .shortMessage("Hello ..!")
                .originNetworkType("SP")
                .originProtocol("SMPP")
                .originNetworkId(1)
                .destNetworkType("GW")
                .destProtocol("SMPP")
                .destNetworkId(4)
                .dataCoding(0)
                .build();

        List<String> batch = List.of(dlrToProcess.toString());
        when(jedisCluster.lpop(eq("preDeliver"), anyInt())).thenReturn(batch);

        List<MessageEvent> deliverEvents = List.of(
                dlrToProcess
        );
        when(routingHelper.stringListAsEventList(batch)).thenReturn(deliverEvents);

        Flux<List<MessageEvent>> result = deliverProcessor.fetchAllMessages();

        StepVerifier.create(result)
                .expectNext(deliverEvents)
                .verifyComplete();

        verify(jedisCluster, times(1)).llen("preDeliver");
        verify(jedisCluster, times(1)).lpop("preDeliver", 1);
        verify(routingHelper, times(1)).stringListAsEventList(batch);
        verify(commonProcessor, times(1)).setUpInitialSettings(any(MessageEvent.class));
    }

    @Test
    void testPrepareMessage_TreatLikeMessage() {
        MessageEvent messageEvent = new MessageEvent();
        messageEvent.setCheckSubmitSmResponse(false);

        deliverProcessor.prepareMessage(messageEvent);

        verify(commonProcessor, times(1)).setUpInitialSettings(messageEvent);
        verify(commonProcessor, times(1)).processMessage(messageEvent);
        verify(commonProcessor, never()).processDlr(messageEvent);
    }

    @Test
    void testPrepareMessage_TreatLikeDlr() {
        MessageEvent messageEvent = new MessageEvent();
        messageEvent.setCheckSubmitSmResponse(true);

        deliverProcessor.prepareMessage(messageEvent);

        verify(commonProcessor, times(1)).setUpInitialSettings(messageEvent);
        verify(commonProcessor, never()).processMessage(messageEvent);
        verify(commonProcessor, times(1)).processDlr(messageEvent);
    }
}

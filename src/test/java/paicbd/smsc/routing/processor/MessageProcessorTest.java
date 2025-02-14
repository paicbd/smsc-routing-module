package paicbd.smsc.routing.processor;

import com.paicbd.smsc.dto.MessageEvent;
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

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageProcessorTest {
    @Mock
    private JedisCluster jedisCluster;

    @Mock
    private AppProperties appProperties;

    @Mock
    private RoutingHelper routingHelper;

    @Mock
    private CommonProcessor commonProcessor;

    @InjectMocks
    private MessageProcessor messageProcessor;


    @Test
    void testFetchAllMessages_EmptyList() {
        when(appProperties.getPreMessageList()).thenReturn("preMessageList");
        when(jedisCluster.llen("preMessageList")).thenReturn(0L);

        Flux<List<MessageEvent>> result = messageProcessor.fetchAllMessages();

        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();

        verify(jedisCluster, times(1)).llen("preMessageList");
    }

    @Test
    void testPrepareMessage_TreatLikeDlr() {
        MessageEvent messageEvent = new MessageEvent();
        messageEvent.setCheckSubmitSmResponse(true);

        messageProcessor.prepareMessage(messageEvent);

        verify(commonProcessor, times(1)).setUpInitialSettings(messageEvent);
        verify(commonProcessor, times(1)).processDlr(messageEvent);
        verify(commonProcessor, never()).processMessage(messageEvent);
    }

    @Test
    void testPrepareMessage_TreatLikeMessage() {
        MessageEvent messageEvent = new MessageEvent();
        messageEvent.setCheckSubmitSmResponse(false);

        messageProcessor.prepareMessage(messageEvent);

        verify(commonProcessor, times(1)).setUpInitialSettings(messageEvent);
        verify(commonProcessor, never()).processDlr(messageEvent);
        verify(commonProcessor, times(1)).processMessage(messageEvent);
    }

}
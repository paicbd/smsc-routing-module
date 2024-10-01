package paicbd.smsc.routing.processor;

import com.paicbd.smsc.dto.MessageEvent;
import reactor.core.publisher.Flux;

import java.util.List;

public interface RoutingProcessor {
    Flux<List<MessageEvent>> fetchAllMessages();
    void prepareMessage(MessageEvent messageEvent);
}

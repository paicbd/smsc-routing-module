package paicbd.smsc.routing.component;

import com.paicbd.smsc.dto.MessageEvent;
import com.paicbd.smsc.dto.RoutingRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RoutingMatcherTest {
    @Mock
    private ConcurrentMap<Integer, List<RoutingRule>> routingRules;

    @InjectMocks
    private RoutingMatcher routingMatcher;

    @Test
    void getRoutingWhenDoesNotExistRulesForNetworkId() {
        MessageEvent messageEvent = MessageEvent.builder()
                .originNetworkId(1)
                .build();

        RoutingRule foundRoutingRule = routingMatcher.getRouting(messageEvent);
        verify(routingRules).get(1);
        assertNull(foundRoutingRule);
    }

    @Test
    void getRoutingWhenRoutingHasNotFilterRules() {
        RoutingRule.Destination destination = new RoutingRule.Destination();
        destination.setPriority(1);
        destination.setNetworkId(2);
        destination.setProtocol("SMPP");
        destination.setNetworkType("GW");

        RoutingRule routingRule = RoutingRule.builder()
                .originNetworkId(1)
                .destination(List.of(destination))
                .hasFilterRules(false)
                .build();

        MessageEvent messageEvent = MessageEvent.builder()
                .originNetworkId(1)
                .originProtocol("SMPP")
                .build();

        routingRules = spy(new ConcurrentHashMap<>());
        routingRules.put(1, List.of(routingRule));
        routingMatcher = new RoutingMatcher(routingRules);

        RoutingRule foundRoutingRule = routingMatcher.getRouting(messageEvent);
        verify(routingRules, times(2)).get(1);

        assertNotNull(foundRoutingRule);
        assertEquals(1, foundRoutingRule.getDestination().size());
    }

    @ParameterizedTest
    @MethodSource("allRoutingVariations")
    void getRouting(MessageEvent messageEvent, RoutingRule routingRule, boolean expectedNull) {
        routingRules = spy(new ConcurrentHashMap<>());
        routingRules.put(1, List.of(routingRule));
        routingMatcher = new RoutingMatcher(routingRules);

        RoutingRule foundRoutingRule = routingMatcher.getRouting(messageEvent);
        verify(routingRules, atLeast(1)).get(1);

        if (expectedNull) {
            assertNull(foundRoutingRule);
            return;
        }

        assertNotNull(foundRoutingRule);
        assertEquals(1, foundRoutingRule.getDestination().size());

        RoutingRule.Destination destination = foundRoutingRule.getDestination().getFirst();
        assertEquals(1, destination.getPriority());
        assertEquals(2, destination.getNetworkId());
        assertEquals("SMPP", destination.getProtocol());
        assertEquals("GW", destination.getNetworkType());
    }

    static Stream<Arguments> allRoutingVariations() {
        RoutingRule.Destination destination = new RoutingRule.Destination();
        destination.setPriority(1);
        destination.setNetworkId(2);
        destination.setProtocol("SMPP");
        destination.setNetworkType("GW");
        List<RoutingRule.Destination> destinations = List.of(destination);

        // MessageEvent, RoutingRule, ExpectedNullWhileFoundRoutingRule
        return Stream.of(
                Arguments.of(
                        MessageEvent.builder()
                                .originNetworkId(1)
                                .originProtocol("SMPP")
                                .sourceAddr("50580808080")
                                .sourceAddrTon(1)
                                .sourceAddrNpi(1)
                                .destinationAddr("50581818181")
                                .destAddrTon(1)
                                .destAddrNpi(1)
                                .imsi("4400127890123")
                                .networkNodeNumber("23")
                                .sccpCallingPartyAddress("0123456789")
                                .sriResponse(true)
                                .build(),
                        RoutingRule.builder()
                                .originRegexSourceAddr("^505\\d*")
                                .originRegexSourceAddrTon("^1\\d*")
                                .originRegexSourceAddrNpi("^1\\d*")
                                .originRegexDestinationAddr("^505\\d*")
                                .originRegexDestAddrTon("^1\\d*")
                                .originRegexDestAddrNpi("^1\\d*")
                                .regexImsiDigitsMask("^44\\d*")
                                .regexNetworkNodeNumber("^23\\d*")
                                .regexCallingPartyAddress("^012\\d*")
                                .sriResponse(true)
                                .destination(destinations)
                                .hasFilterRules(true)
                                .build(),
                        false
                ),
                Arguments.of(
                        MessageEvent.builder()
                                .originNetworkId(1)
                                .originProtocol("SMPP")
                                .sourceAddr("50580808080")
                                .sourceAddrTon(1)
                                .sourceAddrNpi(1)
                                .destinationAddr("50581818181")
                                .destAddrTon(1)
                                .destAddrNpi(1)
                                .imsi("4400127890123")
                                .networkNodeNumber("23")
                                .sccpCallingPartyAddress("0123456789")
                                .sriResponse(true)
                                .build(),
                        RoutingRule.builder()
                                .originRegexSourceAddr("^505\\d*")
                                .originRegexSourceAddrTon("^1\\d*")
                                .originRegexSourceAddrNpi("^1\\d*")
                                .originRegexDestinationAddr("^506\\d*")
                                .originRegexDestAddrTon("^1\\d*")
                                .originRegexDestAddrNpi("^1\\d*")
                                .regexImsiDigitsMask("^44\\d*")
                                .regexNetworkNodeNumber("^23\\d*")
                                .regexCallingPartyAddress("^012\\d*")
                                .sriResponse(true)
                                .destination(destinations)
                                .hasFilterRules(true)
                                .build(),
                        true
                ),
                Arguments.of(
                        MessageEvent.builder()
                                .originNetworkId(1)
                                .originProtocol("SMPP")
                                .sourceAddr("50580808080")
                                .sourceAddrTon(1)
                                .sourceAddrNpi(1)
                                .destinationAddr("50581818181")
                                .destAddrTon(1)
                                .destAddrNpi(1)
                                .imsi("4400127890123")
                                .networkNodeNumber("23")
                                .sccpCallingPartyAddress("0123456789")
                                .sriResponse(true)
                                .build(),
                        RoutingRule.builder()
                                .originRegexSourceAddr("")
                                .originRegexSourceAddrTon("^1\\d*")
                                .originRegexSourceAddrNpi("^1\\d*")
                                .originRegexDestinationAddr("^506\\d*")
                                .originRegexDestAddrTon("^1\\d*")
                                .originRegexDestAddrNpi("^1\\d*")
                                .regexImsiDigitsMask("^44\\d*")
                                .regexNetworkNodeNumber("^23\\d*")
                                .regexCallingPartyAddress("^012\\d*")
                                .sriResponse(true)
                                .destination(destinations)
                                .hasFilterRules(true)
                                .build(),
                        true
                )
        );
    }
}
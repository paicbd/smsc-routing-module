package paicbd.smsc.routing.component;

import com.paicbd.smsc.cdr.CdrProcessor;
import com.paicbd.smsc.dto.MessageEvent;
import com.paicbd.smsc.dto.MessagePart;
import com.paicbd.smsc.utils.UtilsEnum;
import lombok.SneakyThrows;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import paicbd.smsc.routing.util.AppProperties;
import reactor.core.publisher.Flux;
import redis.clients.jedis.JedisCluster;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoutingHelperTest {
    @Mock
    private JedisCluster jedisCluster;

    @Mock
    private CdrProcessor cdrProcessor;

    @Mock
    private CreditHandler creditHandler;

    @Mock
    private AppProperties appProperties;

    @Spy
    @InjectMocks
    private RoutingHelper routingHelper;

    // In this step, the routing info has been completed according to the routing rules in MessageProcessor or DeliverProcessor
    // that's why the routing helper only needs to process the events and push them to the corresponding list
    @Test
    @SneakyThrows
    void httpDestinationProtocol() {
        MessageEvent eventToHttp = MessageEvent.builder()
                .originNetworkId(1)
                .originProtocol("HTTP")
                .destProtocol("HTTP")
                .originNetworkType("SP")
                .sourceAddrTon(1)
                .sourceAddrNpi(1)
                .sourceAddr("80808080")
                .destAddrTon(1)
                .destAddrNpi(1)
                .destinationAddr("90909090")
                .shortMessage("Hello")
                .routingId(1) // simulating the routingId assigned in MessageProcessor
                .isDlr(false)
                .build();

        Flux<List<MessageEvent>> data = Flux.just(List.of(eventToHttp));

        when(appProperties.getHttpMessageList()).thenReturn("httpMessageList");
        ArgumentCaptor<String> listName = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String[]> argument = ArgumentCaptor.forClass(String[].class);

        routingHelper.processEventsFlux(data);
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> verify(jedisCluster).rpush(listName.capture(), argument.capture()));

        assertNotNull(listName.getValue());
        assertNotNull(argument.getValue());

        assertEquals("httpMessageList", listName.getValue());
        String[] messageEventList = argument.getValue();
        assertEquals(1, messageEventList.length);
        verify(creditHandler).incrementCreditUsed(1, 1);
    }

    @Test
    @SneakyThrows
    void smppDestinationProtocolMultipartMessage() {
        MessageEvent eventToSmpp = MessageEvent.builder()
                .originNetworkId(2)
                .originProtocol("HTTP")
                .destProtocol("SMPP")
                .originNetworkType("SP")
                .sourceAddrTon(1)
                .sourceAddrNpi(1)
                .sourceAddr("80808080")
                .destAddrTon(1)
                .destAddrNpi(1)
                .destinationAddr("91919191")
                .messageParts(
                        List.of(
                                MessagePart.builder()
                                        .messageId("mp1")
                                        .shortMessage("Hello from part 1")
                                        .msgReferenceNumber("1")
                                        .totalSegment(2)
                                        .segmentSequence(1)
                                        .build(),
                                MessagePart.builder()
                                        .messageId("mp2")
                                        .shortMessage("Hello from part 2")
                                        .msgReferenceNumber("2")
                                        .totalSegment(2)
                                        .segmentSequence(2)
                                        .build()
                        )
                )
                .routingId(2) // simulating the routingId assigned in MessageProcessor
                .isDlr(false)
                .build();

        Flux<List<MessageEvent>> data = Flux.just(List.of(eventToSmpp));

        when(appProperties.getSmppMessageList()).thenReturn("smppMessageList");
        ArgumentCaptor<String> listName = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String[]> argument = ArgumentCaptor.forClass(String[].class);

        routingHelper.processEventsFlux(data);
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> verify(jedisCluster).rpush(listName.capture(), argument.capture()));

        assertNotNull(listName.getValue());
        assertNotNull(argument.getValue());

        assertEquals("smppMessageList", listName.getValue());
        String[] messageEventList = argument.getValue();
        assertEquals(1, messageEventList.length);
        verify(creditHandler).incrementCreditUsed(2, 2);
    }

    @Test
    @SneakyThrows
    void ss7DestinationProtocolSingleMessage() {
        MessageEvent eventToSs7 = MessageEvent.builder()
                .originNetworkId(3)
                .originProtocol("HTTP")
                .destProtocol("SS7")
                .originNetworkType("SP")
                .sourceAddrTon(1)
                .sourceAddrNpi(1)
                .sourceAddr("80808080")
                .destAddrTon(1)
                .destAddrNpi(1)
                .imsi("100223311")
                .destinationAddr("92929292")
                .shortMessage("Hello")
                .routingId(3) // simulating the routingId assigned in MessageProcessor
                .isDlr(false)
                .build();

        Flux<List<MessageEvent>> data = Flux.just(List.of(eventToSs7));

        when(appProperties.getSs7MessageList()).thenReturn("ss7MessageList");
        ArgumentCaptor<String> listName = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String[]> argument = ArgumentCaptor.forClass(String[].class);

        routingHelper.processEventsFlux(data);
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> verify(jedisCluster).rpush(listName.capture(), argument.capture()));

        assertNotNull(listName.getValue());
        assertNotNull(argument.getValue());

        assertEquals("ss7MessageList", listName.getValue());
        String[] messageEventList = argument.getValue();
        assertEquals(1, messageEventList.length);
        verify(creditHandler).incrementCreditUsed(3, 1);
    }

    @Test
    @SneakyThrows
    void smppDestinationProtocolSingleMessageButOriginNetworkTypeNotSP() {
        MessageEvent eventToSmpp = MessageEvent.builder()
                .originNetworkId(2)
                .originProtocol("HTTP")
                .destProtocol("SMPP")
                .originNetworkType("GW")
                .sourceAddrTon(1)
                .sourceAddrNpi(1)
                .sourceAddr("80808080")
                .destAddrTon(1)
                .destAddrNpi(1)
                .destinationAddr("91919191")
                .shortMessage("Hello")
                .routingId(2) // simulating the routingId assigned in MessageProcessor
                .isDlr(false)
                .build();

        Flux<List<MessageEvent>> data = Flux.just(List.of(eventToSmpp));

        when(appProperties.getSmppMessageList()).thenReturn("smppMessageList");
        ArgumentCaptor<String> listName = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String[]> argument = ArgumentCaptor.forClass(String[].class);

        routingHelper.processEventsFlux(data);
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> verify(jedisCluster).rpush(listName.capture(), argument.capture()));

        assertNotNull(listName.getValue());
        assertNotNull(argument.getValue());

        assertEquals("smppMessageList", listName.getValue());
        String[] messageEventList = argument.getValue();
        assertEquals(1, messageEventList.length);
        verifyNoInteractions(creditHandler);
    }

    @ParameterizedTest
    @MethodSource("messageEventsForDlrs")
    void putCdrInRedisWhenIsDlrThenDoIt(MessageEvent messageEvent, UtilsEnum.CdrStatus status, String comment, boolean createCdr) {
        assertDoesNotThrow(() -> routingHelper.prepareCdr(messageEvent, status, comment, createCdr));
        if (createCdr) {
            ArgumentCaptor<String> idEvent = ArgumentCaptor.forClass(String.class);
            verify(cdrProcessor).createCdr(idEvent.capture());
            assertEquals(messageEvent.getId(), idEvent.getValue());
        }
    }

    static Stream<Arguments> messageEventsForDlrs() {
        return Stream.of(
                Arguments.of(
                        MessageEvent.builder()
                                .id("1722442615535-7914373631079")
                                .messageId("1722442615535-7914373631079")
                                .originNetworkId(1)
                                .originProtocol("HTTP")
                                .destProtocol("HTTP")
                                .originNetworkType("SP")
                                .sourceAddrTon(1)
                                .sourceAddrNpi(1)
                                .sourceAddr("80808080")
                                .destAddrTon(1)
                                .destAddrNpi(1)
                                .destinationAddr("90909090")
                                .shortMessage("Hello")
                                .routingId(1) // simulating the routingId assigned in MessageProcessor
                                .isDlr(true)
                                .build(),
                        UtilsEnum.CdrStatus.ENQUEUE,
                        "ENQUEUE",
                        false
                ),
                Arguments.of(
                        MessageEvent.builder()
                                .id("1722442615535-7914373631080")
                                .messageId("1722442615535-7914373631080")
                                .originNetworkId(1)
                                .originProtocol("HTTP")
                                .destProtocol("HTTP")
                                .originNetworkType("SP")
                                .sourceAddrTon(1)
                                .sourceAddrNpi(1)
                                .sourceAddr("80808080")
                                .destAddrTon(1)
                                .destAddrNpi(1)
                                .destinationAddr("90909090")
                                .shortMessage("Hello")
                                .routingId(1) // simulating the routingId assigned in MessageProcessor
                                .isDlr(true)
                                .build(),
                        UtilsEnum.CdrStatus.FAILED,
                        "Anything rule matched",
                        true
                )
        );
    }

    @ParameterizedTest
    @MethodSource("paramsForCalculateBatch")
    void calculateBatch(int itemsToProcess, int listSize, int workers, int expected) {
        assertEquals(expected, routingHelper.calculateBatchPerWorker(itemsToProcess, listSize, workers));
    }

    static Stream<Arguments> paramsForCalculateBatch() {
        return Stream.of(
                Arguments.of(10, 10, 1, 10),
                Arguments.of(10, 10, 2, 5),
                Arguments.of(10, 10, 3, 3),
                Arguments.of(10, 10, 4, 2),
                Arguments.of(10, 10, 5, 2),
                Arguments.of(10, 10, 6, 1),
                Arguments.of(10, 10, 7, 1),
                Arguments.of(10, 10, 8, 1),
                Arguments.of(10, 10, 9, 1),
                Arguments.of(10, 10, 10, 1),
                Arguments.of(10, 10, 11, 1),
                Arguments.of(10, 10, 12, 1),
                Arguments.of(10, 10, 13, 1),
                Arguments.of(10, 10, 14, 1),
                Arguments.of(10, 10, 15, 1),
                Arguments.of(10, 10, 16, 1),
                Arguments.of(10, 10, 17, 1),
                Arguments.of(10, 10, 18, 1),
                Arguments.of(10, 10, 19, 1),
                Arguments.of(10, 10, 20, 1),
                Arguments.of(10, 10, 21, 1),
                Arguments.of(10, 10, 22, 1),
                Arguments.of(10, 10, 23, 1),
                Arguments.of(10, 10, 24, 1),
                Arguments.of(10, 10, 25, 1),
                Arguments.of(10, 10, 26, 1),
                Arguments.of(10, 10, 27, 1),
                Arguments.of(10, 10, 28, 1),
                Arguments.of(10, 10, 29, 1),
                Arguments.of(10, 10, 30, 1),
                Arguments.of(10, 10, 31, 1),
                Arguments.of(10, 10, 32, 1),
                Arguments.of(10, 10, 33, 1));
    }

    @Test
    void stringListAsEventList() {
        List<String> ls = List.of(
                MessageEvent.builder()
                        .id("1722442615535-7914373631079")
                        .messageId("1722442615535-7914373631079")
                        .originNetworkId(1)
                        .originProtocol("HTTP")
                        .destProtocol("HTTP")
                        .originNetworkType("SP")
                        .sourceAddrTon(1)
                        .sourceAddrNpi(1)
                        .sourceAddr("80808080")
                        .destAddrTon(1)
                        .destAddrNpi(1)
                        .destinationAddr("90909090")
                        .shortMessage("Hello")
                        .routingId(1)
                        .isDlr(true)
                        .build().toString(),
                MessageEvent.builder()
                        .id("1722442615535-7914373631080")
                        .messageId("1722442615535-7914373631080")
                        .originNetworkId(1)
                        .originProtocol("HTTP")
                        .destProtocol("HTTP")
                        .originNetworkType("SP")
                        .sourceAddrTon(1)
                        .sourceAddrNpi(1)
                        .sourceAddr("80808080")
                        .destAddrTon(1)
                        .destAddrNpi(1)
                        .destinationAddr("90909090")
                        .shortMessage("Hello")
                        .routingId(1)
                        .isDlr(true)
                        .build().toString()
                , "null"
        );

        var result = routingHelper.stringListAsEventList(ls);
        assertEquals(2, result.size());
    }

    @ParameterizedTest
    @MethodSource("dlrTestParams")
    void dlrEventsProcess(MessageEvent event, String redisList) {
        Flux<List<MessageEvent>> data = Flux.just(List.of(event));

        String listNameToMock = switch (event.getDestProtocol()) {
            case "HTTP" -> appProperties.getHttpDlrList();
            case "SMPP" -> appProperties.getSmppDlrList();
            case "SS7" -> appProperties.getSs7MessageList();
            default -> null;
        };

        if (listNameToMock != null) {
            when(listNameToMock).thenReturn(redisList);

            ArgumentCaptor<String> listNameCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String[]> argumentCaptor = ArgumentCaptor.forClass(String[].class);

            routingHelper.processEventsFlux(data);

            Awaitility.await()
                    .atMost(5, TimeUnit.SECONDS)
                    .untilAsserted(() -> verify(jedisCluster).rpush(listNameCaptor.capture(), argumentCaptor.capture()));

            assertNotNull(listNameCaptor.getValue());
            assertNotNull(argumentCaptor.getValue());
            assertEquals(redisList, listNameCaptor.getValue());
            assertEquals(1, argumentCaptor.getValue().length);
        } else {
            routingHelper.processEventsFlux(data);
            verifyNoInteractions(jedisCluster);
        }
    }

    static Stream<Arguments> dlrTestParams() {
        return Stream.of(
                Arguments.of(
                        MessageEvent.builder()
                                .id("1722442615535-7914373631079")
                                .destProtocol("HTTP")
                                .isDlr(true)
                                .process(true)
                                .build(), "http_dlr"),
                Arguments.of(
                        MessageEvent.builder()
                                .id("1722442615535-7914373631079")
                                .destProtocol("SMPP")
                                .isDlr(true)
                                .process(true)
                                .build(), "smpp_dlr"),
                Arguments.of(
                        MessageEvent.builder()
                                .id("1722442615535-7914373631079")
                                .destProtocol("SS7")
                                .isDlr(true)
                                .process(true)
                                .build(), "ss7_messages"),
                Arguments.of(
                        MessageEvent.builder()
                                .id("1722442615535-7914373631079")
                                .destProtocol("UNKNOWN")
                                .isDlr(true)
                                .process(true)
                                .build(), "unknown_dlr")
        );
    }

    @Test
    void init() {
        // No functionality to test, just checking if it throws an exception
        assertDoesNotThrow(() -> routingHelper.init());
    }

    @Test
    void shutdown() {
        // No functionality to test, just checking if it throws an exception
        assertDoesNotThrow(() -> routingHelper.shutdown());
    }
}
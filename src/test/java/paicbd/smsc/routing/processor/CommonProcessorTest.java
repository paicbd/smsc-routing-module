package paicbd.smsc.routing.processor;

import com.paicbd.smsc.dto.Gateway;
import com.paicbd.smsc.dto.GeneralSettings;
import com.paicbd.smsc.dto.MessageEvent;
import com.paicbd.smsc.dto.RoutingRule;
import com.paicbd.smsc.dto.Ss7Settings;
import com.paicbd.smsc.dto.UtilsRecords;
import com.paicbd.smsc.utils.RequestDelivery;
import com.paicbd.smsc.utils.SmppEncoding;
import com.paicbd.smsc.utils.UtilsEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import paicbd.smsc.routing.loaders.SettingsLoader;
import paicbd.smsc.routing.util.AppProperties;
import paicbd.smsc.routing.component.RoutingHelper;
import paicbd.smsc.routing.component.RoutingMatcher;
import redis.clients.jedis.JedisCluster;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class CommonProcessorTest {
    @Mock
    SettingsLoader settingsLoader;

    @Mock
    JedisCluster jedisCluster;

    @Mock
    AppProperties appProperties;

    @Mock
    RoutingHelper routingHelper;

    @Mock
    RoutingMatcher routingMatcher;

    @Mock
    ConcurrentMap<Integer, Gateway> gateways;

    @InjectMocks
    CommonProcessor commonProcessor;


    @ParameterizedTest
    @MethodSource("getMessageToTestInitialSettings")
    @DisplayName("Test SetUpInitialSettings and set the values of generalSetting to the MessageEvent")
    void setUpInitialSettingsAndSetTheValuesOfGeneralSettingToTheMessageEvent(MessageEvent messageEvent) {
        GeneralSettings generalSettings = GeneralSettings.builder()
                .validityPeriod(120)
                .maxValidityPeriod(240)
                .id(1)
                .destAddrNpi(1)
                .destAddrTon(1)
                .sourceAddrNpi(1)
                .sourceAddrTon(1)
                .encodingGsm7(0)
                .encodingUcs2(2)
                .encodingIso88591(3)
                .build();

        when(settingsLoader.getSmppHttpSettings()).thenReturn(generalSettings);

        commonProcessor.setUpInitialSettings(messageEvent);

        if (Objects.isNull(messageEvent.getSourceAddrTon()))
            assertEquals(generalSettings.getSourceAddrTon(), messageEvent.getSourceAddrTon());

        if (Objects.isNull(messageEvent.getSourceAddrNpi()))
            assertEquals(generalSettings.getSourceAddrNpi(), messageEvent.getSourceAddrNpi());

        if (Objects.isNull(messageEvent.getDestAddrNpi()))
            assertEquals(generalSettings.getDestAddrNpi(), messageEvent.getDestAddrNpi());

        if (Objects.isNull(messageEvent.getDestAddrTon()))
            assertEquals(generalSettings.getDestAddrTon(), messageEvent.getDestAddrTon());

        if (messageEvent.getValidityPeriod() == 0)
            assertEquals(generalSettings.getValidityPeriod(), messageEvent.getValidityPeriod());

        assertNotNull(messageEvent.getStringValidityPeriod());
        assertTrue(messageEvent.getValidityPeriod() > 0);
    }

    static Stream<MessageEvent> getMessageToTestInitialSettings() {
        return Stream.of(
                MessageEvent.builder()
                        .id("1722442489766-7788604799226")
                        .messageId("1722442489770-7788608933795")
                        .systemId("http_sp")
                        .commandStatus(0)
                        .segmentSequence(2)
                        .sourceAddr("50588888888")
                        .destinationAddr("50599999999")
                        .validityPeriod(0)
                        .registeredDelivery(RequestDelivery.REQUEST_DLR.getValue())
                        .shortMessage("Hello ..!")
                        .originNetworkType("SP")
                        .originProtocol("HTTP")
                        .originNetworkId(1)
                        .destNetworkType("GW")
                        .destProtocol("SMPP")
                        .destNetworkId(4)
                        .build(),

                MessageEvent.builder()
                        .id("1722442489766-7788604799226")
                        .messageId("1722442489770-7788608933795")
                        .systemId("smpp_sp")
                        .commandStatus(0)
                        .segmentSequence(2)
                        .sourceAddr("50588888888")
                        .destinationAddr("50599999999")
                        .validityPeriod(60)
                        .registeredDelivery(RequestDelivery.REQUEST_DLR.getValue())
                        .dataCoding(0)
                        .shortMessage("Hello ..!")
                        .originNetworkType("SP")
                        .originProtocol("SMPP")
                        .originNetworkId(1)
                        .destNetworkType("GW")
                        .destProtocol("SMPP")
                        .destNetworkId(4)
                        .destAddrNpi(1)
                        .destAddrTon(1)
                        .sourceAddrNpi(1)
                        .sourceAddrTon(1)
                        .esmClass(3)
                        .build(),

                MessageEvent.builder()
                        .id("1722442489766-7788604799226")
                        .messageId("1722442489770-7788608933795")
                        .systemId("smpp_sp")
                        .commandStatus(0)
                        .segmentSequence(2)
                        .sourceAddr("50588888888")
                        .destinationAddr("50599999999")
                        .validityPeriod(60)
                        .stringValidityPeriod("000000000200000R")
                        .registeredDelivery(RequestDelivery.REQUEST_DLR.getValue())
                        .dataCoding(0)
                        .shortMessage("Hello ..!")
                        .originNetworkType("SP")
                        .originProtocol("SMPP")
                        .originNetworkId(1)
                        .destNetworkType("GW")
                        .destProtocol("SMPP")
                        .destNetworkId(4)
                        .destAddrNpi(1)
                        .destAddrTon(1)
                        .sourceAddrNpi(1)
                        .sourceAddrTon(1)
                        .esmClass(3)
                        .build()
        );
    }

    @Test
    @DisplayName("Test processMessage without rule then it write fail cdr")
    void processMessageWithoutRuleThenItWriteFailCdr() {
        MessageEvent messageEvent = MessageEvent.builder()
                .id("1722442489766-7788604799226")
                .messageId("1722442489770-7788608933795")
                .systemId("smpp_sp")
                .commandStatus(0)
                .segmentSequence(2)
                .sourceAddr("50588888888")
                .destinationAddr("50599999999")
                .validityPeriod(60)
                .registeredDelivery(RequestDelivery.REQUEST_DLR.getValue())
                .dataCoding(0)
                .shortMessage("Hello ..!")
                .originNetworkType("SP")
                .originProtocol("SMPP")
                .originNetworkId(1)
                .destNetworkType("GW")
                .destProtocol("SMPP")
                .destNetworkId(4)
                .destAddrNpi(1)
                .destAddrTon(1)
                .sourceAddrNpi(1)
                .sourceAddrTon(1)
                .esmClass(3)
                .build();
        when(routingMatcher.getRouting(any())).thenReturn(null);
        commonProcessor.processMessage(messageEvent);
        ArgumentCaptor<MessageEvent> messageEventArgumentCaptor = ArgumentCaptor.forClass(MessageEvent.class);
        ArgumentCaptor<UtilsEnum.CdrStatus> cdrStatusArgumentCaptor = ArgumentCaptor.forClass(UtilsEnum.CdrStatus.class);
        ArgumentCaptor<String> commentArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Boolean> createCdrArgumentCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(routingHelper).prepareCdr(messageEventArgumentCaptor.capture(), cdrStatusArgumentCaptor.capture(), commentArgumentCaptor.capture(), createCdrArgumentCaptor.capture());
        assertEquals(messageEvent, messageEventArgumentCaptor.getValue());
        assertEquals(UtilsEnum.CdrStatus.FAILED, cdrStatusArgumentCaptor.getValue());
        assertEquals("NO ROUTING", commentArgumentCaptor.getValue());
        assertEquals(true, createCdrArgumentCaptor.getValue());
    }

    @Test
    @DisplayName("Test processMessage without destination then it write fail cdr")
    void processMessageWithoutDestinationThenItWriteFailCdr() {
        MessageEvent messageEvent = MessageEvent.builder()
                .id("1722442489766-7788604799226")
                .messageId("1722442489770-7788608933795")
                .systemId("smpp_sp")
                .commandStatus(0)
                .segmentSequence(2)
                .sourceAddr("50588888888")
                .destinationAddr("50599999999")
                .validityPeriod(60)
                .registeredDelivery(RequestDelivery.REQUEST_DLR.getValue())
                .dataCoding(0)
                .shortMessage("Hello ..!")
                .originNetworkType("SP")
                .originProtocol("SMPP")
                .originNetworkId(1)
                .destNetworkType("GW")
                .destProtocol("SMPP")
                .destNetworkId(4)
                .destAddrNpi(1)
                .destAddrTon(1)
                .sourceAddrNpi(1)
                .sourceAddrTon(1)
                .esmClass(3)
                .build();

        RoutingRule routingRule = RoutingRule.builder()
                .id(1)
                .originNetworkId(1)
                .destination(List.of())
                .build();

        when(routingMatcher.getRouting(any())).thenReturn(routingRule);
        commonProcessor.processMessage(messageEvent);
        ArgumentCaptor<MessageEvent> messageEventArgumentCaptor = ArgumentCaptor.forClass(MessageEvent.class);
        ArgumentCaptor<UtilsEnum.CdrStatus> cdrStatusArgumentCaptor = ArgumentCaptor.forClass(UtilsEnum.CdrStatus.class);
        ArgumentCaptor<String> commentArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Boolean> createCdrArgumentCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(routingHelper).prepareCdr(messageEventArgumentCaptor.capture(), cdrStatusArgumentCaptor.capture(), commentArgumentCaptor.capture(), createCdrArgumentCaptor.capture());
        assertEquals(messageEvent, messageEventArgumentCaptor.getValue());
        assertEquals(UtilsEnum.CdrStatus.FAILED, cdrStatusArgumentCaptor.getValue());
        assertEquals("NO DEST", commentArgumentCaptor.getValue());
        assertEquals(true, createCdrArgumentCaptor.getValue());
    }

    @ParameterizedTest
    @MethodSource("provideTestCasesForProcessMessage")
    @DisplayName("Test processMessage with rule and destination then it enqueues the message")
    void processMessageWithRuleAndDestinationThenItEnqueuesTheMessage(MessageEvent messageEvent, RoutingRule routingRule, Gateway gateway, Ss7Settings ss7Settings) {
        when(routingMatcher.getRouting(any())).thenReturn(routingRule);
        if (Objects.nonNull(gateway)) {
            when(gateways.get(anyInt())).thenReturn(gateway);
        }

        if (Objects.nonNull(ss7Settings)) {
            when(settingsLoader.getSs7Settings(anyInt())).thenReturn(ss7Settings);
        }

        int requestDlr = messageEvent.getRegisteredDelivery();
        commonProcessor = new CommonProcessor(settingsLoader, jedisCluster, appProperties, routingHelper, routingMatcher, gateways);
        commonProcessor.processMessage(messageEvent);
        this.checkMessageWithRule(messageEvent, routingRule);

        if (Objects.nonNull(gateway)) {
            // check the Request DLR values
            if (RequestDelivery.TRANSPARENT.getValue() != gateway.getRequestDLR()) {
                assertEquals(gateway.getRequestDLR(), messageEvent.getRegisteredDelivery());
            } else {
                assertEquals(requestDlr, messageEvent.getRegisteredDelivery());
            }

        }
        this.checkEnqueuedMessage(messageEvent);
    }

    private void checkMessageWithRule(MessageEvent messageEvent, RoutingRule routingRule) {
        RoutingRule.Destination destination = routingRule.getDestination().stream().min(Comparator.comparingInt(RoutingRule.Destination::getPriority)).orElse(null);
        assertNotNull(destination);
        assertEquals(routingRule.getId(), messageEvent.getRoutingId());
        assertEquals(destination.getNetworkType(), messageEvent.getDestNetworkType());
        assertEquals(destination.getProtocol(), messageEvent.getDestProtocol());
        assertEquals(destination.getNetworkId(), messageEvent.getDestNetworkId());

        if (routingRule.isHasActionRules()) {
            //Check Source Address
            if (!routingRule.getNewSourceAddr().isEmpty()) {
                assertEquals(routingRule.getNewSourceAddr(), messageEvent.getSourceAddr());
            }
            if (routingRule.getNewSourceAddrTon() > -1) {
                assertEquals(routingRule.getNewSourceAddrTon(), messageEvent.getSourceAddrTon());
            }
            if (routingRule.getNewSourceAddrNpi() > -1) {
                assertEquals(routingRule.getNewSourceAddrNpi(), messageEvent.getSourceAddrNpi());
            }
            if (!routingRule.getRemoveSourceAddrPrefix().isEmpty()) {
                assertFalse(messageEvent.getSourceAddr().startsWith(routingRule.getRemoveSourceAddrPrefix()));
            }
            if (!routingRule.getAddSourceAddrPrefix().isEmpty()) {
                assertTrue(messageEvent.getSourceAddr().startsWith(routingRule.getAddSourceAddrPrefix()));
            }

            //Check Destination Address
            if (!routingRule.getNewDestinationAddr().isEmpty()) {
                assertEquals(routingRule.getNewDestinationAddr(), messageEvent.getDestinationAddr());
            }
            if (routingRule.getNewDestAddrTon() > -1) {
                assertEquals(routingRule.getNewDestAddrTon(), messageEvent.getDestAddrTon());
            }
            if (routingRule.getNewDestAddrNpi() > -1) {
                assertEquals(routingRule.getNewDestAddrNpi(), messageEvent.getDestAddrNpi());
            }
            if (!routingRule.getRemoveDestAddrPrefix().isEmpty()) {
                assertFalse(messageEvent.getDestinationAddr().startsWith(routingRule.getRemoveDestAddrPrefix()));
            }
            if (!routingRule.getAddDestAddrPrefix().isEmpty()) {
                assertTrue(messageEvent.getDestinationAddr().startsWith(routingRule.getAddDestAddrPrefix()));
            }
        }
    }

    private void checkEnqueuedMessage(MessageEvent messageEvent) {
        ArgumentCaptor<MessageEvent> messageEventArgumentCaptor = ArgumentCaptor.forClass(MessageEvent.class);
        ArgumentCaptor<UtilsEnum.CdrStatus> cdrStatusArgumentCaptor = ArgumentCaptor.forClass(UtilsEnum.CdrStatus.class);
        ArgumentCaptor<String> commentArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Boolean> createCdrArgumentCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(routingHelper).prepareCdr(messageEventArgumentCaptor.capture(), cdrStatusArgumentCaptor.capture(), commentArgumentCaptor.capture(), createCdrArgumentCaptor.capture());
        assertEquals(messageEvent, messageEventArgumentCaptor.getValue());
        assertEquals(UtilsEnum.CdrStatus.ENQUEUE, cdrStatusArgumentCaptor.getValue());
        assertEquals("", commentArgumentCaptor.getValue());
        assertEquals(false, createCdrArgumentCaptor.getValue());
    }

    private static Stream<Arguments> provideTestCasesForProcessMessage() {
        return Stream.of(
                // Test case for destination SMPP and GW with REQUEST_DLR
                Arguments.of(
                        getSingleMessage(),
                        getRuleWithActionsAndSmppDestination(),
                        Gateway.builder()
                                .name("GW")
                                .requestDLR(RequestDelivery.REQUEST_DLR.getValue())
                                .build(),
                        null
                ),
                // Test case for destination SMPP and GW with NON_REQUEST_DLR
                Arguments.of(
                        getSingleMessage(),
                        getRuleWithActionsAndSmppDestination(),
                        Gateway.builder()
                                .name("GW")
                                .requestDLR(RequestDelivery.NON_REQUEST_DLR.getValue())
                                .build(),
                        null
                ),
                // Test case for destination SMPP and GW with TRANSPARENT
                Arguments.of(
                        getSingleMessage(),
                        getRuleWithActionsAndSmppDestination(),
                        Gateway.builder()
                                .name("GW")
                                .requestDLR(RequestDelivery.TRANSPARENT.getValue())
                                .build(),
                        null
                ),
                // Test case for destination SS7
                Arguments.of(
                        getSingleMessage(),
                        getRuleWithActionsAndSs7Destination(),
                        null,
                        Ss7Settings.builder()
                                .name("ss7")
                                .protocol("SS7")
                                .networkId(2)
                                .globalTitle("50588655545")
                                .globalTitleIndicator(UtilsEnum.GlobalTitleIndicator.GLOBAL_TITLE_INCLUDES_TRANSLATION_TYPE_NUMBERING_PLAN_ENCODING_SCHEME_AND_NATURE_OF_ADDRESS)
                                .translationType(0)
                                .smscSsn(8)
                                .hlrSsn(6)
                                .mscSsn(8)
                                .mapVersion(3)
                                .splitMessage(true)
                                .build()
                )
        );
    }

    public static MessageEvent getSingleMessage() {
        return MessageEvent.builder()
                .id("1722442489766-778860479922")
                .parentId("1722442489766-7788604799226")
                .messageId("1722442489766-7788604799226")
                .systemId("smpp_sp")
                .commandStatus(0)
                .segmentSequence(2)
                .sourceAddr("50588888888")
                .destinationAddr("50599999999")
                .validityPeriod(60)
                .registeredDelivery(RequestDelivery.NON_REQUEST_DLR.getValue())
                .dataCoding(0)
                .shortMessage("Hello I'm message with destination SMPP")
                .originNetworkId(1)
                .originNetworkType("SP")
                .originProtocol("SMPP")
                .destAddrNpi(1)
                .destAddrTon(1)
                .sourceAddrNpi(1)
                .sourceAddrTon(1)
                .esmClass(3)
                .build();
    }

    public static RoutingRule getRuleWithActionsAndSmppDestination() {
        RoutingRule.Destination destinationGwSmpp = new RoutingRule.Destination();
        destinationGwSmpp.setPriority(1);
        destinationGwSmpp.setNetworkId(2);
        destinationGwSmpp.setProtocol("SMPP");
        destinationGwSmpp.setNetworkType("GW");
        return RoutingRule.builder()
                .id(11)
                .originNetworkId(1)
                .originNetworkType("SP")
                .originProtocol("SMPP")
                .newSourceAddr("")
                .newSourceAddrTon(2)
                .newSourceAddrNpi(2)
                .newDestinationAddr("")
                .addSourceAddrPrefix("")
                .removeSourceAddrPrefix("")
                .newDestAddrTon(2)
                .newDestAddrNpi(2)
                .hasActionRules(true)
                .addDestAddrPrefix("")
                .removeDestAddrPrefix("")
                .newGtSccpAddrMt("")
                .destination(List.of(destinationGwSmpp))
                .build();
    }

    public static RoutingRule getRuleWithActionsAndSs7Destination() {
        RoutingRule.Destination destinationGwSmpp = new RoutingRule.Destination();
        destinationGwSmpp.setPriority(1);
        destinationGwSmpp.setNetworkId(2);
        destinationGwSmpp.setProtocol("SS7");
        destinationGwSmpp.setNetworkType("GW");
        return RoutingRule.builder()
                .id(11)
                .originNetworkId(1)
                .originNetworkType("SP")
                .originProtocol("SMPP")
                .newSourceAddr("")
                .newSourceAddrTon(2)
                .newSourceAddrNpi(2)
                .newDestinationAddr("")
                .addSourceAddrPrefix("")
                .removeSourceAddrPrefix("")
                .newDestAddrTon(2)
                .newDestAddrNpi(2)
                .hasActionRules(true)
                .addDestAddrPrefix("")
                .removeDestAddrPrefix("")
                .newGtSccpAddrMt("")
                .destination(List.of(destinationGwSmpp))
                .build();
    }


    static String longMessage = """
                                Java is a powerful, object-oriented programming language widely used
                                for building enterprise applications, web services, and mobile apps.
                                With features like platform independence, strong memory management,
                                and multithreading capabilities, Java remains a top choice for developers.
                                It supports frameworks like Spring Boot, Hibernate, and Jakarta EE,
                                enabling efficient backend development. Additionally, Java's rich ecosystem,
                                including tools like Maven and Gradle, simplifies project management.
                                The language's compatibility with modern cloud technologies and microservices
                                architecture makes it essential for scalable software solutions.
                                Whether for Android development, big data, or distributed systems,
                                Java continues to be highly relevant in the tech industry
                                """;


    @ParameterizedTest
    @MethodSource("provideTestCasesForGetPartsOfMessageWithSmppDestination")
    @DisplayName("Test getPartsOfMessage with SMPP destination")
    void getPartsOfMessageWithSmppDestination(
            String message, int dataCoding, boolean splitMessage, String splitSmppType, CommonProcessor.MessagePart expectedResult) {
        Gateway gateway = Gateway.builder()
                .name("GW")
                .splitMessage(splitMessage)
                .splitSmppType(splitSmppType)
                .encodingGsm7(SmppEncoding.GSM7)
                .encodingUcs2(SmppEncoding.UCS2)
                .encodingIso88591(SmppEncoding.ISO88591)
                .build();
        when(gateways.get(anyInt())).thenReturn(gateway);
        MessageEvent messageEvent = MessageEvent.builder()
                .id("1722442489766-7788604799226")
                .messageId("1722442489770-7788608933795")
                .systemId("http_sp")
                .commandStatus(0)
                .segmentSequence(2)
                .sourceAddr("50588888888")
                .destinationAddr("50599999999")
                .validityPeriod(0)
                .registeredDelivery(RequestDelivery.REQUEST_DLR.getValue())
                .originNetworkType("SP")
                .originProtocol("HTTP")
                .originNetworkId(1)
                .destNetworkType("GW")
                .destProtocol("SMPP")
                .destNetworkId(4)
                .build();

        messageEvent.setShortMessage(message);
        messageEvent.setDataCoding(dataCoding);

        commonProcessor = new CommonProcessor(settingsLoader, jedisCluster, appProperties, routingHelper, routingMatcher, gateways);
        CommonProcessor.MessagePart result = commonProcessor.getPartsOfMessage(messageEvent);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    private static Stream<Arguments> provideTestCasesForGetPartsOfMessageWithSmppDestination() {
        return Stream.of(
                // Test case for long message with data coding GSM7 and split true using UDHprovideTestCasesForGetPartsOfMessageWithSmppDestination
                Arguments.of(
                        longMessage,
                        0,
                        true,
                        "UDH",
                        new CommonProcessor.MessagePart(
                                4,
                                248,
                                0,
                                true)
                ),
                // Test case for long message with data coding GSM7 and split true using TLV
                Arguments.of(
                        longMessage,
                        0,
                        true,
                        "TLV",
                        new CommonProcessor.MessagePart(
                                4,
                                254,
                                0,
                                false)
                ),
                // Test case for long message with data coding GSM7 and split false using UDH
                Arguments.of(
                        longMessage,
                        0,
                        false,
                        "UDH",
                        new CommonProcessor.MessagePart(
                                1,
                                764,
                                0,
                                true)
                ),
                // Test case for long message with data coding UCS2 and split true using UDH
                Arguments.of(
                        longMessage,
                        8,
                        true,
                        "UDH",
                        new CommonProcessor.MessagePart(
                                7,
                                124,
                                2,
                                true)
                ),
                // Test case for long message with data coding UCS2 and split true using TLV
                Arguments.of(
                        longMessage,
                        8,
                        true,
                        "TLV",
                        new CommonProcessor.MessagePart(
                                7,
                                127,
                                2,
                                false)
                ),
                // Test case for long message with data coding UCS2 and split false using UDH
                Arguments.of(
                        longMessage,
                        8,
                        false,
                        "UDH",
                        new CommonProcessor.MessagePart(
                                1,
                                764,
                                2,
                                true)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("provideTestCasesForGetPartsOfMessageWithSs7Destination")
    @DisplayName("Test getPartsOfMessage with SS7 destination")
    void getPartsOfMessageWithSs7Destination(
            String message, int dataCoding, boolean splitMessage, CommonProcessor.MessagePart expectedResult) {

        Ss7Settings ss7Settings = Ss7Settings.builder()
                .splitMessage(splitMessage)
                .build();

        when(settingsLoader.getSs7Settings(anyInt())).thenReturn(ss7Settings);

        MessageEvent messageEvent = MessageEvent.builder()
                .id("1722442489766-7788604799226")
                .messageId("1722442489770-7788608933795")
                .systemId("http_sp")
                .commandStatus(0)
                .segmentSequence(2)
                .sourceAddr("50588888888")
                .destinationAddr("50599999999")
                .validityPeriod(0)
                .registeredDelivery(RequestDelivery.REQUEST_DLR.getValue())
                .originNetworkType("SP")
                .originProtocol("HTTP")
                .originNetworkId(1)
                .destNetworkType("GW")
                .destProtocol("SS7")
                .build();

        messageEvent.setShortMessage(message);
        messageEvent.setDataCoding(dataCoding);

        commonProcessor = new CommonProcessor(settingsLoader, jedisCluster, appProperties, routingHelper, routingMatcher, gateways);
        CommonProcessor.MessagePart result = commonProcessor.getPartsOfMessage(messageEvent);
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    private static Stream<Arguments> provideTestCasesForGetPartsOfMessageWithSs7Destination() {
        return Stream.of(
                // Test case for long message with data coding GSM7 and split true using UDH
                Arguments.of(
                        longMessage,
                        0,
                        true,
                        new CommonProcessor.MessagePart(
                                5,
                                153,
                                0,
                                true)
                ),
                // Test case for long message with data coding GSM7 and split false using UDH
                Arguments.of(
                        longMessage,
                        0,
                        false,
                        new CommonProcessor.MessagePart(
                                1,
                                764,
                                0,
                                true)
                ),
                // Test case for long message with data coding UCS2 and split true using UDH
                Arguments.of(
                        longMessage,
                        8,
                        true,
                        new CommonProcessor.MessagePart(
                                12,
                                67,
                                2,
                                true)
                )
        );
    }


    @ParameterizedTest
    @MethodSource("provideTestCasesForProcessDlr")
    void processDlrWithDestinationSmpp(MessageEvent messageEvent, UtilsRecords.SubmitSmResponseEvent submitSmResponseEvent) {
        when(appProperties.getSmppResult()).thenReturn("smpp_dlr");
        when(jedisCluster.hget("smpp_dlr", messageEvent.getDeliverSmId())).thenReturn(submitSmResponseEvent.toString());
        commonProcessor = new CommonProcessor(settingsLoader, jedisCluster, appProperties, routingHelper, routingMatcher, gateways);
        commonProcessor.processDlr(messageEvent);
        if (messageEvent.getCheckSubmitSmResponse()) {
            this.checkEnqueuedMessage(messageEvent);
        } else {
            verify(routingHelper, never()).prepareCdr(any(MessageEvent.class), any(UtilsEnum.CdrStatus.class), any(String.class), anyBoolean());
        }
    }


    private static Stream<Arguments> provideTestCasesForProcessDlr() {
        return Stream.of(
                // Test case for destination SMPP
                Arguments.of(
                        MessageEvent.builder()
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
                                .shortMessage("id:1 sub:001 dlvrd:001 submit date:2101010000 done date:2101010000 stat:DELIVRD err:000 text:Test Message")
                                .delReceipt("id:1 sub:001 dlvrd:001 submit date:2101010000 done date:2101010000 stat:DELIVRD err:000 text:Test Message")
                                .status("DELIVRD")
                                .originNetworkType("SP")
                                .originProtocol("SMPP")
                                .originNetworkId(1)
                                .destNetworkType("GW")
                                .destProtocol("SMPP")
                                .destNetworkId(4)
                                .dataCoding(0)
                                .checkSubmitSmResponse(true)
                                .build(),
                        new UtilsRecords.SubmitSmResponseEvent(
                                "",
                                "1722442489766-7788604799226",
                                "smpp_sp",
                                "1722442489766-7788604799227",
                                "1722442489766-7788604799226",
                                "SMPP",
                                1,
                                "SP",
                                "",
                                0,
                                0,
                                "1722442489766-7788604799226"
                                )
                )
        );
    }
}
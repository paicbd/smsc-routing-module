package paicbd.smsc.routing.processor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.paicbd.smsc.dto.Gateway;
import com.paicbd.smsc.dto.GeneralSettings;
import com.paicbd.smsc.dto.MessageEvent;
import com.paicbd.smsc.dto.RoutingRule;
import com.paicbd.smsc.dto.Ss7Settings;
import com.paicbd.smsc.dto.UtilsRecords;
import com.paicbd.smsc.exception.RTException;
import com.paicbd.smsc.utils.Converter;
import com.paicbd.smsc.utils.Generated;
import com.paicbd.smsc.utils.UtilsEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsmpp.bean.DeliveryReceipt;
import org.jsmpp.util.InvalidDeliveryReceiptException;
import org.springframework.stereotype.Component;
import paicbd.smsc.routing.loaders.LoadSettings;
import paicbd.smsc.routing.util.AppProperties;
import paicbd.smsc.routing.util.RoutingMatcher;
import paicbd.smsc.routing.util.RoutingHelper;
import redis.clients.jedis.JedisCluster;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommonProcessor {
    private static final Set<String> DELIVERY_STATUS = Set.of("ENROUTE", "DELIVRD", "ACCEPTD");
    private static final int SUCCESS_STATUS = 0;
    private static final int FAILURE_STATUS = 1;

    private final LoadSettings loadSettings;
    private final JedisCluster jedisCluster;
    private final AppProperties appProperties;
    private final RoutingHelper routingHelper;
    private final RoutingMatcher routingMatcher;
    private final ConcurrentMap<Integer, Gateway> gateways;
    private final ConcurrentMap<Integer, Ss7Settings> ss7SettingsMap;

    /**
     * This variable represents a mapping between different message types and their corresponding lengths.
     *
     * <p><strong>Variable Values:</strong>
     * <ul>
     *   <li><strong>SMPP_UDH_2:</strong> Value 77. This involves only 'encoding' 2 (UNICODE) and 'splitSmppType' UDH. </li>
     *   <li><strong>SMPP_UDH_DEFAULT:</strong> Value 154. This involves 'encoding' 0 (GSM7), 1 (UTF-8), 3 (ISO-8859-1) and 'splitSmppType' UDH.</li>
     *   <li><strong>SMPP_TLV_2:</strong> Value 80. This involves only 'encoding' 2 (UNICODE) and 'splitSmppType' TLV.</li>
     *   <li><strong>SMPP_TLV_DEFAULT:</strong> Value 160. This involves 'encoding' 0 (GSM7), 1 (UTF-8), 3 (ISO-8859-1) and 'splitSmppType' TLV.</li>
     *   <li><strong>SS7_2:</strong> Value 67. This involves only 'encoding' 2 (UNICODE), SS7 only supports UDH, so there is no need to check 'splitSmppType'.</li>
     *   <li><strong>SS7_DEFAULT:</strong> Value 152. This involves 'encoding' 0 (GSM7), 1 (UTF-8), 3 (ISO-8859-1), SS7 only supports UDH, so there is no need to check 'splitSmppType'.</li>
     * </ul>
     * </p>
     */
    private static final Map<String, Integer> LENGTH_BY_PROTOCOL_AND_ENCODING = Map.of(
            "SMPP_UDH_2", 77,
            "SMPP_UDH_DEFAULT", 154,
            "SMPP_TLV_2", 80,
            "SMPP_TLV_DEFAULT", 160,
            "SS7_2", 67,
            "SS7_DEFAULT", 135
    );

    public void setUpInitialSettings(MessageEvent event) {
        GeneralSettings smppHttpSettings = this.loadSettings.getSmppHttpSettings();
        event.setSourceAddrTon(Objects.isNull(event.getSourceAddrTon()) ? smppHttpSettings.getSourceAddrTon() : event.getSourceAddrTon());
        event.setSourceAddrNpi(Objects.isNull(event.getSourceAddrNpi()) ? smppHttpSettings.getSourceAddrNpi() : event.getSourceAddrNpi());
        event.setDestAddrTon(Objects.isNull(event.getDestAddrTon()) ? smppHttpSettings.getDestAddrTon() : event.getDestAddrTon());
        event.setDestAddrNpi(Objects.isNull(event.getDestAddrNpi()) ? smppHttpSettings.getDestAddrNpi() : event.getDestAddrNpi());
        event.setDataCoding(Objects.isNull(event.getDataCoding()) ? smppHttpSettings.getEncodingGsm7() : event.getDataCoding());
        event.setEsmClass(Objects.isNull(event.getEsmClass()) ? 0 : event.getEsmClass());
        if (Objects.isNull(event.getValidityPeriod())) {
            event.setValidityPeriod(String.valueOf(smppHttpSettings.getValidityPeriod()));
            return;
        }
        event.setValidityPeriod(Integer.parseInt(event.getValidityPeriod()) <= smppHttpSettings.getMaxValidityPeriod() ?
                event.getValidityPeriod() : String.valueOf(smppHttpSettings.getMaxValidityPeriod()));
    }

    public void processMessage(MessageEvent messageEvent) {
        RoutingRule routing = this.routingMatcher.getRouting(messageEvent);
        if (Objects.isNull(routing)) {
            log.warn("Not routing found for message when origin network id is {}", messageEvent.getOriginNetworkId());
            this.routingHelper.prepareCdr(messageEvent, UtilsEnum.CdrStatus.FAILED, "NO ROUTING", true);
            return;
        }

        Optional<RoutingRule.Destination> optionalDestination = routing.getDestination().stream().min(Comparator.comparingInt(RoutingRule.Destination::getPriority));
        if (optionalDestination.isEmpty()) {
            log.warn("No destination found for routing rule with id {}", routing.getId());
            this.routingHelper.prepareCdr(messageEvent, UtilsEnum.CdrStatus.FAILED, "NO DEST", true);
            return;
        }

        RoutingRule.Destination destination = optionalDestination.get();
        messageEvent.setRoutingId(routing.getId());
        messageEvent.setDestNetworkId(destination.getNetworkId());
        messageEvent.setDestProtocol(destination.getProtocol());
        messageEvent.setDestNetworkType(destination.getNetworkType());
        messageEvent.setDlr("SP".equalsIgnoreCase(destination.getNetworkType()));
        if (messageEvent.isDlr()) {
            messageEvent.setEsmClass(3);
            messageEvent.setDelReceipt(messageEvent.getShortMessage());
            messageEvent.setSystemId(null);
        }

        if ("SS7".equalsIgnoreCase(messageEvent.getDestProtocol())) {
            setSS7Settings(messageEvent);
        }
        this.applyRoutingRulesActions(messageEvent, routing);
        this.routingHelper.prepareCdr(messageEvent, UtilsEnum.CdrStatus.ENQUEUE, "", false);
    }

    private void applyRoutingRulesActions(MessageEvent event, RoutingRule routing) {
        if (routing.isHasActionRules()) {
            this.replaceSourceAddrIfNeeded(event, routing);
            this.replaceDestinationAddrIfNeeded(event, routing);
        }
        this.setSS7RulesConfiguration(event, routing);
    }

    private void replaceSourceAddrIfNeeded(MessageEvent event, RoutingRule routing) {
        if (!routing.getNewSourceAddr().isEmpty()) {
            log.debug("Replace source address form {} to {}", event.getSourceAddr(), routing.getNewSourceAddr());
            event.setSourceAddr(routing.getNewSourceAddr());
        }
        if (routing.getNewSourceAddrTon() > -1) {
            log.debug("Replace source address TON form {} to {}", event.getSourceAddrTon(), routing.getNewSourceAddrTon());
            event.setSourceAddrTon(routing.getNewSourceAddrTon());
        }
        if (routing.getNewSourceAddrNpi() > -1) {
            log.debug("Replace source address NPI form {} to {}", event.getSourceAddrNpi(), routing.getNewSourceAddrNpi());
            event.setSourceAddrNpi(routing.getNewSourceAddrNpi());
        }
        if (!routing.getRemoveSourceAddrPrefix().isEmpty()) {
            log.debug("Removing source address prefix if needed, {}", routing.getRemoveSourceAddrPrefix());
            event.setSourceAddr(event.getSourceAddr().replaceFirst(routing.getRemoveSourceAddrPrefix(), ""));
        }
        if (!routing.getAddSourceAddrPrefix().isEmpty()) {
            log.debug("Adding source address prefix, {}", routing.getAddSourceAddrPrefix());
            event.setSourceAddr(routing.getAddSourceAddrPrefix().concat(event.getSourceAddr()));
        }
    }

    private void replaceDestinationAddrIfNeeded(MessageEvent event, RoutingRule routing) {
        if (!routing.getNewDestinationAddr().isEmpty()) {
            log.debug("Replace destination address form {} to {}", event.getDestinationAddr(), routing.getNewDestinationAddr());
            event.setDestinationAddr(routing.getNewDestinationAddr());
        }
        if (routing.getNewDestAddrTon() > -1) {
            log.debug("Replace destination address TON form {} to {}", event.getDestAddrTon(), routing.getNewDestAddrTon());
            event.setDestAddrTon(routing.getNewDestAddrTon());
        }
        if (routing.getNewDestAddrNpi() > -1) {
            log.debug("Replace destination address NPI form {} to {}", event.getDestAddrNpi(), routing.getNewDestAddrNpi());
            event.setDestAddrNpi(routing.getNewDestAddrNpi());
        }
        if (!routing.getRemoveDestAddrPrefix().isEmpty()) {
            log.debug("Removing destination prefix if needed, {}", routing.getRemoveDestAddrPrefix());
            event.setDestinationAddr(event.getDestinationAddr().replaceFirst(routing.getRemoveDestAddrPrefix(), ""));
        }
        if (!routing.getAddDestAddrPrefix().isEmpty()) {
            log.debug("Adding destination prefix, {}", routing.getAddDestAddrPrefix());
            event.setDestinationAddr(routing.getAddDestAddrPrefix().concat(event.getDestinationAddr()));
        }
    }

    private void setSS7RulesConfiguration(MessageEvent event, RoutingRule rule) {
        event.setDropMapSri(rule.isDropMapSri());
        event.setNetworkIdToMapSri(rule.getNetworkIdToMapSri());
        event.setNetworkIdToPermanentFailure(rule.getNetworkIdToPermanentFailure());
        event.setDropTempFailure(rule.isDropTempFailure());
        event.setNetworkIdTempFailure(rule.getNetworkIdTempFailure());
        event.setSriResponse(rule.isSriResponse());
        event.setCheckSriResponse(rule.isCheckSriResponse());
        if (!event.isMoMessage() && !rule.getNewGtSccpAddrMt().isEmpty()) {
            log.debug("Replace SMSC Global title  form {} to {}", event.getGlobalTitle(), rule.getNewGtSccpAddrMt());
            event.setGlobalTitle(rule.getNewGtSccpAddrMt());
        }
    }

    private void setSS7Settings(MessageEvent event) {
        Ss7Settings ss7Config = this.loadSettings.getSs7Settings(event.getDestNetworkId());
        if (Objects.isNull(ss7Config)) {
            log.error("No SS7 settings found for network id {}", event.getDestNetworkId());
            return;
        }
        event.setMsisdn(event.getDestinationAddr());
        event.setAddressNatureMsisdn(event.getDestAddrTon());
        event.setNumberingPlanMsisdn(event.getDestAddrNpi());
        event.setGlobalTitle(ss7Config.getGlobalTitle());
        event.setGlobalTitleIndicator(ss7Config.getGlobalTitleIndicator().toString());
        event.setTranslationType(ss7Config.getTranslationType());
        event.setSmscSsn(ss7Config.getSmscSsn());
        event.setHlrSsn(ss7Config.getHlrSsn());
        event.setMscSsn(ss7Config.getMscSsn());
        event.setMapVersion(ss7Config.getMapVersion());
    }

    public MessagePart getPartsOfMessage(MessageEvent messageEvent) {
        String destinationProtocol = messageEvent.getDestProtocol();
        int dataCoding = messageEvent.getDataCoding(); // 0: GSM7, 8: UCS2, 3: ISO-8859-1
        int encoding;
        boolean splitMessage;
        String splitBy;
        if ("SS7".equalsIgnoreCase(destinationProtocol)) {
            Ss7Settings ss7Settings = ss7SettingsMap.get(messageEvent.getDestNetworkId());
            Objects.requireNonNull(ss7Settings, "No SS7 gateway found for network id " + messageEvent.getDestNetworkId());
            splitMessage = ss7Settings.isSplitMessage();
            splitBy = "UDH";
            encoding = dataCoding == 0 ? 0 : 2;
        } else {
            Gateway gateway = this.gateways.get(messageEvent.getDestNetworkId());
            Objects.requireNonNull(gateway, "No SMPP/HTTP gateway found for network id " + messageEvent.getDestNetworkId());
            splitMessage = gateway.isSplitMessage();
            splitBy = gateway.getSplitSmppType().toUpperCase();
            encoding = getEncoding(dataCoding, gateway.getEncodingGsm7(), gateway.getEncodingUcs2(), gateway.getEncodingIso88591());
        }
        int lengthForPartByRule = calculateLengthByRule(destinationProtocol, splitBy, encoding);
        int longMessageLength = messageEvent.getShortMessage().length();
        int totalParts = (longMessageLength + lengthForPartByRule - 1) / lengthForPartByRule;
        return new MessagePart(
                splitMessage ? totalParts : 1,
                lengthForPartByRule,
                encoding,
                "UDH".equalsIgnoreCase(splitBy)
        );
    }

    private int getEncoding(int dataCoding, int gsm7Encoding, int ucs2Encoding, int isoEncoding) {
        return switch (dataCoding) {
            case 0 -> gsm7Encoding;
            case 8 -> ucs2Encoding;
            case 3 -> isoEncoding;
            default -> 0;
        };
    }

    private static int calculateLengthByRule(String destinationProtocol, String splitCriteria, int encodingType) {
        String protocolKey = destinationProtocol.toUpperCase();
        String smppSplitCriteria = "SMPP".equalsIgnoreCase(destinationProtocol) ? splitCriteria : null;
        String encodingKey = encodingType == 2 ? "2" : "DEFAULT";
        String protocolEncodingKey = Objects.isNull(smppSplitCriteria)
                ? String.format("%s_%s", protocolKey, encodingKey)
                : String.format("%s_%s_%s", protocolKey, smppSplitCriteria, encodingKey);

        return LENGTH_BY_PROTOCOL_AND_ENCODING.get(protocolEncodingKey);
    }

    public void processDlr(MessageEvent messageEvent) {
        if (Boolean.TRUE.equals(messageEvent.getCheckSubmitSmResponse())) {
            String key = getKeyFromMessageEvent(messageEvent);
            String resultListName = chooseResultListNameBasedOnProtocol(messageEvent);
            String resultInRawFormat = jedisCluster.hget(resultListName, key);
            handleResultAvailability(resultInRawFormat, key, messageEvent);

            UtilsRecords.SubmitSmResponseEvent result = Converter.stringToObject(resultInRawFormat, new TypeReference<>() {
            });

            jedisCluster.hdel(resultListName, key);
            if ("GW".equalsIgnoreCase(result.originNetworkType()) && "SMPP".equalsIgnoreCase(result.originProtocol())) {
                log.warn("This message is ignored because origin is SMPP and network type is Gateway");
                messageEvent.setProcess(false);
                this.routingHelper.prepareCdr(
                        messageEvent, UtilsEnum.CdrStatus.FAILED, "IGNORED DUE ORIGIN IS SMPP AND ORIGIN NETWORK TYPE IS GATEWAY", true);
                return;
            }

            messageEvent.setDestProtocol(result.originProtocol());
            messageEvent.setDestNetworkId(result.originNetworkId());
            messageEvent.setTotalSegment(result.totalSegment());
            messageEvent.setSegmentSequence(result.segmentSequence());
            messageEvent.setMsgReferenceNumber(result.msgReferenceNumber());
            messageEvent.setMessageId(result.submitSmServerId());
            handleReceiptDelivery(messageEvent, result);
            setDetailsForMessageEventAsPerOriginProtocol(messageEvent, result);
            this.routingHelper.prepareCdr(messageEvent, UtilsEnum.CdrStatus.ENQUEUE, "", false);
        }
    }

    private String getKeyFromMessageEvent(MessageEvent messageEvent) {
        return messageEvent.getDeliverSmId();
    }

    private String chooseResultListNameBasedOnProtocol(MessageEvent messageEvent) {
        return "SMPP".equalsIgnoreCase(messageEvent.getOriginProtocol()) ?
                appProperties.getSmppResult() : appProperties.getHttpResult();
    }

    private void handleResultAvailability(String resultInRawFormat, String key, MessageEvent messageEvent) {
        if (Objects.isNull(resultInRawFormat)) {
            log.error("Getting null for sending deliverSm for key {}. This DLR will be retried", key);
            var retryListName = chooseRetryListNameBasedOnProtocol(messageEvent);
            jedisCluster.rpush(retryListName, messageEvent.toString());
            throw new RTException("SubmitSmResponseEvent not found for key " + key);
        }
    }

    private String chooseRetryListNameBasedOnProtocol(MessageEvent messageEvent) {
        return "SMPP".equalsIgnoreCase(messageEvent.getOriginProtocol()) ?
                appProperties.getSmppDlrRetryList() : appProperties.getHttpDlrRetryList();
    }

    private void handleReceiptDelivery(MessageEvent messageEvent, UtilsRecords.SubmitSmResponseEvent result) {
        if (Objects.nonNull(messageEvent.getDelReceipt())) {
            try {
                DeliveryReceipt deliveryReceipt = new DeliveryReceipt(messageEvent.getDelReceipt());
                deliveryReceipt.setId(result.submitSmServerId());
                messageEvent.setDelReceipt(deliveryReceipt.toString());
                messageEvent.setShortMessage(deliveryReceipt.toString());
            } catch (InvalidDeliveryReceiptException e) {
                log.info("Error casting deliveryReceipt for {}", messageEvent.getDelReceipt());
            }
        }
    }

    private void setDetailsForMessageEventAsPerOriginProtocol(MessageEvent messageEvent, UtilsRecords.SubmitSmResponseEvent result) {
        if ("SS7".equalsIgnoreCase(result.originProtocol())) {
            configureMessageForSS7Protocol(messageEvent, result);
            return;
        }
        configureMessageForOtherProtocol(messageEvent, result);
    }

    private void configureMessageForSS7Protocol(MessageEvent messageEvent, UtilsRecords.SubmitSmResponseEvent result) {
        messageEvent.setDestNetworkType("GW");
        messageEvent.setDlr(true);
        messageEvent.setDeliverSmId(result.submitSmServerId());
        setSS7Settings(messageEvent);
        setCommandIdForDeliveryStatus(messageEvent);
    }

    private void setCommandIdForDeliveryStatus(MessageEvent event) {
        boolean isDelivered = DELIVERY_STATUS.contains(event.getStatus().toUpperCase());
        if (isDelivered) {
            event.setCommandStatus(SUCCESS_STATUS);
            return;
        }
        event.setCommandStatus(FAILURE_STATUS);
    }

    private void configureMessageForOtherProtocol(MessageEvent messageEvent, UtilsRecords.SubmitSmResponseEvent result) {
        messageEvent.setDestNetworkType("SP");
        messageEvent.setDeliverSmServerId(result.submitSmServerId());
        messageEvent.setSystemId(result.systemId());
        messageEvent.setDlr(true);
    }

    @Generated
    public record MessagePart(
            int parts,
            int messageLength,
            int encoding,
            boolean isUdh
    ) {
    }
}

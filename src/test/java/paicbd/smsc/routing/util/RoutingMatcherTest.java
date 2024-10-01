package paicbd.smsc.routing.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.paicbd.smsc.dto.MessageEvent;
import com.paicbd.smsc.dto.RoutingRule;
import com.paicbd.smsc.utils.Converter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class RoutingMatcherTest {
    ConcurrentMap<Integer, List<RoutingRule>> routingRules = new ConcurrentHashMap<>();
    RoutingMatcher routingMatcher = new RoutingMatcher(routingRules);

    @BeforeEach
    void setUp() {
        String routingRuleRaw = "[{\"id\":1,\"origin_network_id\":3,\"regex_source_addr\":\"\",\"regex_source_addr_ton\":\"\",\"regex_source_addr_npi\":\"\",\"regex_destination_addr\":\"\",\"regex_dest_addr_ton\":\"\",\"regex_dest_addr_npi\":\"\",\"regex_imsi_digits_mask\":\"\",\"regex_network_node_number\":\"\",\"regex_calling_party_address\":\"\",\"is_sri_response\":false,\"destination\":[{\"priority\":1,\"network_id\":1,\"dest_protocol\":\"SMPP\",\"network_type\":\"GW\"},{\"priority\":2,\"network_id\":2,\"dest_protocol\":\"HTTP\",\"network_type\":\"GW\"}],\"new_source_addr\":\"\",\"new_source_addr_ton\":-1,\"new_source_addr_npi\":-1,\"new_destination_addr\":\"\",\"new_dest_addr_ton\":-1,\"new_dest_addr_npi\":-1,\"add_source_addr_prefix\":\"\",\"add_dest_addr_prefix\":\"\",\"remove_source_addr_prefix\":\"\",\"remove_dest_addr_prefix\":\"\",\"new_gt_sccp_addr_mt\":\"\",\"drop_map_sri\":false,\"network_id_to_map_sri\":-1,\"network_id_to_permanent_failure\":-1,\"drop_temp_failure\":false,\"network_id_temp_failure\":-1,\"check_sri_response\":false,\"origin_protocol\":\"SMPP\",\"origin_network_type\":\"SP\",\"has_filter_rules\":false,\"has_action_rules\":false}]";
        List<RoutingRule> routingRulesCasted = Converter.stringToObject(routingRuleRaw, new TypeReference<>() {
        });
        routingRules.put(3, routingRulesCasted);
    }

    @Test
    void getRouting_Null() {
        String msgWithOriginNetworkIdNull = "{\"msisdn\":null,\"id\":\"1719422148745-11322463364554\",\"message_id\":\"1719422148745-11322463364554\",\"system_id\":\"httpsp01\",\"deliver_sm_id\":null,\"deliver_sm_server_id\":null,\"command_status\":0,\"sequence_number\":0,\"source_addr_ton\":1,\"source_addr_npi\":1,\"source_addr\":\"50510201020\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"50582368999\",\"esm_class\":0,\"validity_period\":\"60\",\"registered_delivery\":1,\"data_coding\":0,\"sm_default_msg_id\":0,\"short_message\":\"Prueba\",\"delivery_receipt\":null,\"status\":null,\"error_code\":null,\"check_submit_sm_response\":null,\"optional_parameters\":null,\"origin_network_type\":\"SP\",\"origin_protocol\":\"HTTP\",\"origin_network_id\":1,\"dest_network_type\":\"GW\",\"dest_protocol\":\"HTTP\",\"dest_network_id\":5,\"routing_id\":1,\"address_nature_msisdn\":null,\"numbering_plan_msisdn\":null,\"remote_dialog_id\":null,\"local_dialog_id\":null,\"sccp_called_party_address_pc\":null,\"sccp_called_party_address_ssn\":null,\"sccp_called_party_address\":null,\"sccp_calling_party_address_pc\":null,\"sccp_calling_party_address_ssn\":null,\"sccp_calling_party_address\":null,\"global_title\":null,\"global_title_indicator\":null,\"translation_type\":null,\"smsc_ssn\":null,\"hlr_ssn\":null,\"msc_ssn\":null,\"map_version\":null,\"is_retry\":false,\"retry_dest_network_id\":null,\"retry_number\":null,\"is_last_retry\":false,\"is_network_notify_error\":false,\"due_delay\":0,\"accumulated_time\":0,\"drop_map_sri\":false,\"network_id_to_map_sri\":-1,\"network_id_to_permanent_failure\":-1,\"drop_temp_failure\":false,\"network_id_temp_failure\":-1,\"imsi\":null,\"network_node_number\":null,\"network_node_number_nature_of_address\":null,\"network_node_number_numbering_plan\":null,\"mo_message\":false,\"is_sri_response\":false,\"check_sri_response\":false,\"msg_reference_number\":null,\"total_segment\":null,\"segment_sequence\":null,\"originator_sccp_address\":null,\"udhi\":null,\"udh_json\":null,\"parent_id\":null,\"is_dlr\":false,\"message_parts\":null}";
        MessageEvent messageEvent = Converter.stringToObject(msgWithOriginNetworkIdNull, new TypeReference<>() {
        });
        messageEvent.setOriginNetworkId(4);
        assertNull(routingMatcher.getRouting(messageEvent));
    }

    @Test
    void getRouting_NonNull() {
        this.routingMatcher = new RoutingMatcher(routingRules);
        String msgWithOriginNetworkIdNull = "{\"msisdn\":null,\"id\":\"1719422148745-11322463364554\",\"message_id\":\"1719422148745-11322463364554\",\"system_id\":\"httpsp01\",\"deliver_sm_id\":null,\"deliver_sm_server_id\":null,\"command_status\":0,\"sequence_number\":0,\"source_addr_ton\":1,\"source_addr_npi\":1,\"source_addr\":\"50510201020\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"50582368999\",\"esm_class\":0,\"validity_period\":\"60\",\"registered_delivery\":1,\"data_coding\":0,\"sm_default_msg_id\":0,\"short_message\":\"Prueba\",\"delivery_receipt\":null,\"status\":null,\"error_code\":null,\"check_submit_sm_response\":null,\"optional_parameters\":null,\"origin_network_type\":\"SP\",\"origin_protocol\":\"HTTP\",\"origin_network_id\":1,\"dest_network_type\":\"GW\",\"dest_protocol\":\"HTTP\",\"dest_network_id\":5,\"routing_id\":1,\"address_nature_msisdn\":null,\"numbering_plan_msisdn\":null,\"remote_dialog_id\":null,\"local_dialog_id\":null,\"sccp_called_party_address_pc\":null,\"sccp_called_party_address_ssn\":null,\"sccp_called_party_address\":null,\"sccp_calling_party_address_pc\":null,\"sccp_calling_party_address_ssn\":null,\"sccp_calling_party_address\":null,\"global_title\":null,\"global_title_indicator\":null,\"translation_type\":null,\"smsc_ssn\":null,\"hlr_ssn\":null,\"msc_ssn\":null,\"map_version\":null,\"is_retry\":false,\"retry_dest_network_id\":null,\"retry_number\":null,\"is_last_retry\":false,\"is_network_notify_error\":false,\"due_delay\":0,\"accumulated_time\":0,\"drop_map_sri\":false,\"network_id_to_map_sri\":-1,\"network_id_to_permanent_failure\":-1,\"drop_temp_failure\":false,\"network_id_temp_failure\":-1,\"imsi\":null,\"network_node_number\":null,\"network_node_number_nature_of_address\":null,\"network_node_number_numbering_plan\":null,\"mo_message\":false,\"is_sri_response\":false,\"check_sri_response\":false,\"msg_reference_number\":null,\"total_segment\":null,\"segment_sequence\":null,\"originator_sccp_address\":null,\"udhi\":null,\"udh_json\":null,\"parent_id\":null,\"is_dlr\":false,\"message_parts\":null}";
        MessageEvent messageEvent = Converter.stringToObject(msgWithOriginNetworkIdNull, new TypeReference<>() {
        });
        messageEvent.setOriginNetworkId(3);
        assertNotNull(routingMatcher.getRouting(messageEvent));
    }

    @Test
    void matchesRoutingCriteria_RoutingNull() {
        RoutingRule rr = routingRules.get(3).getFirst();
        rr.setNewSourceAddr("50510201020");
        rr.setHasFilterRules(true);
        List<RoutingRule> internalRr = List.of(rr);
        routingRules.put(3, internalRr);
        routingMatcher = new RoutingMatcher(routingRules);

        MessageEvent messageEvent = new MessageEvent();
        messageEvent.setSourceAddr("50510201020");

        messageEvent.setOriginNetworkId(5);
        assertNull(routingMatcher.getRouting(messageEvent));
    }

    @Test
    void matchesRoutingCriteria_SourceAddress() {
        RoutingRule rr = routingRules.get(3).getFirst();
        rr.setNewSourceAddr("50510201020");
        rr.setHasFilterRules(true);
        List<RoutingRule> internalRr = List.of(rr);
        routingRules.put(3, internalRr);
        routingMatcher = new RoutingMatcher(routingRules);

        MessageEvent messageEvent = new MessageEvent();
        messageEvent.setSourceAddr("50510201020");

        messageEvent.setOriginNetworkId(3);
        assertNotNull(routingMatcher.getRouting(messageEvent));

        rr.setNewSourceAddr("CLARO-CAMPAIGN");
        internalRr = List.of(rr);
        routingRules.put(3, internalRr);
        routingMatcher = new RoutingMatcher(routingRules);

        RoutingRule routingRule = routingMatcher.getRouting(messageEvent);
        String currentSourceAddr = messageEvent.getSourceAddr();
        String newSourceAddr = routingRule.getNewSourceAddr();
        assertNotEquals(newSourceAddr, currentSourceAddr);

        rr.setOriginRegexSourceAddr("^505\\d*$");
        internalRr = List.of(rr);
        routingRules.put(3, internalRr);
        routingMatcher = new RoutingMatcher(routingRules);

        messageEvent.setSourceAddr("50510201020");
        assertNotNull(routingMatcher.getRouting(messageEvent));

        messageEvent.setSourceAddr("82368999");
        assertNull(routingMatcher.getRouting(messageEvent));
    }

    @Test
    void matchesRoutingCriteria_RegexSourceAddressTON() {
        RoutingRule rr = routingRules.get(3).getFirst();
        rr.setOriginRegexSourceAddrTon("^1$");
        rr.setHasFilterRules(true);
        List<RoutingRule> internalRr = List.of(rr);
        routingRules.put(3, internalRr);
        routingMatcher = new RoutingMatcher(routingRules);

        MessageEvent messageEvent = new MessageEvent();
        messageEvent.setOriginNetworkId(3);
        messageEvent.setSourceAddrTon(1);

        RoutingRule routingRule = routingMatcher.getRouting(messageEvent);
        assertNotNull(routingRule);

        messageEvent.setSourceAddrTon(2);
        assertNull(routingMatcher.getRouting(messageEvent));
    }

    @Test
    void matchesRoutingCriteria_RegexSourceAddressNPI() {
        RoutingRule rr = routingRules.get(3).getFirst();
        rr.setOriginRegexSourceAddrNpi("^1$");
        rr.setHasFilterRules(true);
        List<RoutingRule> internalRr = List.of(rr);
        routingRules.put(3, internalRr);
        routingMatcher = new RoutingMatcher(routingRules);

        MessageEvent messageEvent = new MessageEvent();
        messageEvent.setOriginNetworkId(3);
        messageEvent.setSourceAddrNpi(1);

        RoutingRule routingRule = routingMatcher.getRouting(messageEvent);
        assertNotNull(routingRule);

        messageEvent.setSourceAddrNpi(2);
        assertNull(routingMatcher.getRouting(messageEvent));
    }

    @Test
    void matchesRoutingCriteria_DestAddress() {
        RoutingRule rr = routingRules.get(3).getFirst();
        rr.setNewDestinationAddr("DEST");
        rr.setHasFilterRules(true);
        List<RoutingRule> internalRr = List.of(rr);
        routingRules.put(3, internalRr);
        routingMatcher = new RoutingMatcher(routingRules);

        MessageEvent messageEvent = new MessageEvent();
        messageEvent.setOriginNetworkId(3);
        messageEvent.setDestinationAddr("82368999");

        RoutingRule routingRule = routingMatcher.getRouting(messageEvent);
        String currentDestinationAddr = messageEvent.getDestinationAddr();
        String newDestinationAddr = routingRule.getNewDestinationAddr();
        assertNotEquals(newDestinationAddr, currentDestinationAddr);
    }

    @Test
    void matchesRoutingCriteria_RegexDestAddressNPI() {
        RoutingRule rr = routingRules.get(3).getFirst();
        rr.setOriginRegexDestAddrNpi("^1$");
        rr.setHasFilterRules(true);
        List<RoutingRule> internalRr = List.of(rr);
        routingRules.put(3, internalRr);
        routingMatcher = new RoutingMatcher(routingRules);

        MessageEvent messageEvent = new MessageEvent();
        messageEvent.setOriginNetworkId(3);
        messageEvent.setDestAddrNpi(1);

        RoutingRule routingRule = routingMatcher.getRouting(messageEvent);
        assertNotNull(routingRule);

        messageEvent.setDestAddrNpi(2);
        assertNull(routingMatcher.getRouting(messageEvent));
    }

    @Test
    void matchesRoutingCriteria_DestAddressRegex() {
        RoutingRule rr = routingRules.get(3).getFirst();
        rr.setOriginRegexDestinationAddr("^505\\d*$");
        rr.setHasFilterRules(true);
        List<RoutingRule> internalRr = List.of(rr);
        routingRules.put(3, internalRr);
        routingMatcher = new RoutingMatcher(routingRules);

        MessageEvent messageEvent = new MessageEvent();
        messageEvent.setOriginNetworkId(3);
        messageEvent.setDestinationAddr("50510201020");

        RoutingRule routingRule = routingMatcher.getRouting(messageEvent);
        assertNotNull(routingRule);

        messageEvent.setDestinationAddr("82368999");
        assertNull(routingMatcher.getRouting(messageEvent));
    }

    @Test
    void matchesRoutingCriteria_DestAddressTON() {
        RoutingRule rr = routingRules.get(3).getFirst();
        rr.setOriginRegexDestAddrTon("^1$");
        rr.setHasFilterRules(true);
        List<RoutingRule> internalRr = List.of(rr);
        routingRules.put(3, internalRr);
        routingMatcher = new RoutingMatcher(routingRules);

        MessageEvent messageEvent = new MessageEvent();
        messageEvent.setOriginNetworkId(3);
        messageEvent.setDestAddrTon(1);

        RoutingRule routingRule = routingMatcher.getRouting(messageEvent);
        assertNotNull(routingRule);

        messageEvent.setDestAddrTon(2);
        assertNull(routingMatcher.getRouting(messageEvent));
    }

    @Test
    void matchesRoutingCriteria_IMSI() {
        RoutingRule rr = routingRules.get(3).getFirst();
        rr.setRegexImsiDigitsMask("^\\d{15}$");
        rr.setHasFilterRules(true);
        List<RoutingRule> internalRr = List.of(rr);
        routingRules.put(3, internalRr);
        routingMatcher = new RoutingMatcher(routingRules);

        MessageEvent messageEvent = new MessageEvent();
        messageEvent.setOriginNetworkId(3);
        messageEvent.setImsi("123456789012345");

        RoutingRule routingRule = routingMatcher.getRouting(messageEvent);
        assertNotNull(routingRule);

        messageEvent.setImsi("12345678901234");
        assertNull(routingMatcher.getRouting(messageEvent));
    }

    @Test
    void matchesRoutingCriteria_NNN() {
        RoutingRule rr = routingRules.get(3).getFirst();
        rr.setRegexNetworkNodeNumber("^\\d{15}$");
        rr.setHasFilterRules(true);
        List<RoutingRule> internalRr = List.of(rr);
        routingRules.put(3, internalRr);
        routingMatcher = new RoutingMatcher(routingRules);

        MessageEvent messageEvent = new MessageEvent();
        messageEvent.setOriginNetworkId(3);
        messageEvent.setNetworkNodeNumber("123456789012345");

        RoutingRule routingRule = routingMatcher.getRouting(messageEvent);
        assertNotNull(routingRule);

        messageEvent.setNetworkNodeNumber("12345678901234");
        assertNull(routingMatcher.getRouting(messageEvent));
    }

    @Test
    void matchesRoutingCriteria_SRI() {
        RoutingRule rr = routingRules.get(3).getFirst();
        rr.setRegexCallingPartyAddress("^\\d{15}$");
        rr.setHasFilterRules(true);
        rr.setSriResponse(false);
        List<RoutingRule> internalRr = List.of(rr);
        routingRules.put(3, internalRr);
        routingMatcher = new RoutingMatcher(routingRules);

        MessageEvent messageEvent = new MessageEvent();
        messageEvent.setOriginNetworkId(3);
        messageEvent.setSriResponse(false);
        messageEvent.setSccpCallingPartyAddress("123456789012345");

        RoutingRule routingRule = routingMatcher.getRouting(messageEvent);
        assertNotNull(routingRule);

        messageEvent.setSriResponse(true);
        assertNull(routingMatcher.getRouting(messageEvent));
    }

    @Test
    void matchesRoutingCriteria_SCP() {
        RoutingRule rr = routingRules.get(3).getFirst();
        rr.setRegexCallingPartyAddress("^\\d{15}$");
        rr.setHasFilterRules(true);
        List<RoutingRule> internalRr = List.of(rr);
        routingRules.put(3, internalRr);
        routingMatcher = new RoutingMatcher(routingRules);

        MessageEvent messageEvent = new MessageEvent();
        messageEvent.setOriginNetworkId(3);
        messageEvent.setSccpCallingPartyAddress("123456789012345");

        RoutingRule routingRule = routingMatcher.getRouting(messageEvent);
        assertNotNull(routingRule);

        messageEvent.setSccpCallingPartyAddress("12345678901234");
        assertNull(routingMatcher.getRouting(messageEvent));
    }
}
package paicbd.smsc.routing.processor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.paicbd.smsc.dto.Gateway;
import com.paicbd.smsc.dto.GeneralSettings;
import com.paicbd.smsc.dto.MessageEvent;
import com.paicbd.smsc.dto.RoutingRule;
import com.paicbd.smsc.dto.Ss7Settings;
import com.paicbd.smsc.exception.RTException;
import com.paicbd.smsc.utils.Converter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import paicbd.smsc.routing.loaders.LoadSettings;
import paicbd.smsc.routing.util.AppProperties;
import paicbd.smsc.routing.util.RoutingHelper;
import paicbd.smsc.routing.util.RoutingMatcher;
import paicbd.smsc.routing.util.StaticMethods;
import redis.clients.jedis.JedisCluster;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommonProcessorTest {
    @Mock
    LoadSettings loadSettings;
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
    @Mock
    ConcurrentMap<Integer, Ss7Settings> ss7SettingsMap;

    @InjectMocks
    CommonProcessor commonProcessor;

    @Test
    void setUpInitialSettings() {
        String stringEvent = "{\"msisdn\":null,\"id\":\"1722442489770-7788608933795\",\"message_id\":\"1722442489766-7788604799226\",\"system_id\":\"smppsp\",\"deliver_sm_id\":null,\"deliver_sm_server_id\":null,\"command_status\":0,\"sequence_number\":2,\"source_addr_ton\":1,\"source_addr_npi\":1,\"source_addr\":\"6666\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"5555\",\"esm_class\":3,\"validity_period\":\"60\",\"registered_delivery\":1,\"data_coding\":0,\"sm_default_msg_id\":0,\"short_message\":\"Hello!\",\"delivery_receipt\":null,\"status\":null,\"error_code\":null,\"check_submit_sm_response\":null,\"optional_parameters\":null,\"origin_network_type\":\"SP\",\"origin_protocol\":\"SMPP\",\"origin_network_id\":3,\"dest_network_type\":\"GW\",\"dest_protocol\":\"SMPP\",\"dest_network_id\":1,\"routing_id\":1,\"address_nature_msisdn\":null,\"numbering_plan_msisdn\":null,\"remote_dialog_id\":null,\"local_dialog_id\":null,\"sccp_called_party_address_pc\":null,\"sccp_called_party_address_ssn\":null,\"sccp_called_party_address\":null,\"sccp_calling_party_address_pc\":null,\"sccp_calling_party_address_ssn\":null,\"sccp_calling_party_address\":null,\"global_title\":null,\"global_title_indicator\":null,\"translation_type\":null,\"smsc_ssn\":null,\"hlr_ssn\":null,\"msc_ssn\":null,\"map_version\":null,\"is_retry\":false,\"retry_dest_network_id\":\"\",\"retry_number\":null,\"is_last_retry\":false,\"is_network_notify_error\":false,\"due_delay\":0,\"accumulated_time\":0,\"drop_map_sri\":false,\"network_id_to_map_sri\":-1,\"network_id_to_permanent_failure\":-1,\"drop_temp_failure\":false,\"network_id_temp_failure\":-1,\"imsi\":null,\"network_node_number\":null,\"network_node_number_nature_of_address\":null,\"network_node_number_numbering_plan\":null,\"mo_message\":false,\"is_sri_response\":false,\"check_sri_response\":false,\"msg_reference_number\":null,\"total_segment\":null,\"segment_sequence\":null,\"originator_sccp_address\":null,\"udhi\":\"0\",\"udh_json\":null,\"parent_id\":null,\"is_dlr\":false,\"message_parts\":null}";
        MessageEvent event = StaticMethods.stringAsEvent(stringEvent);
        GeneralSettings generalSettings = new GeneralSettings();
        generalSettings.setValidityPeriod(120);
        generalSettings.setMaxValidityPeriod(240);
        when(loadSettings.getSmppHttpSettings()).thenReturn(generalSettings);
        assertNotNull(event);
        assertDoesNotThrow(() -> commonProcessor.setUpInitialSettings(event));

        event.setSourceAddrTon(null);
        event.setSourceAddrNpi(null);
        event.setDestAddrTon(null);
        event.setDestAddrNpi(null);
        event.setEsmClass(null);
        event.setDataCoding(null);
        event.setRegisteredDelivery(null);
        assertDoesNotThrow(() -> commonProcessor.setUpInitialSettings(event));

        event.setValidityPeriod(0);
        assertDoesNotThrow(() -> commonProcessor.setUpInitialSettings(event));

        assertEquals(0, event.getSourceAddrTon());
        assertEquals(0, event.getSourceAddrNpi());
        assertEquals(0, event.getDestAddrTon());
        assertEquals(60, event.getValidityPeriod());

        event.setStringValidityPeriod(null);
        event.setValidityPeriod(0);
        assertDoesNotThrow(() -> commonProcessor.setUpInitialSettings(event));

        event.setValidityPeriod(120);
        assertDoesNotThrow(() -> commonProcessor.setUpInitialSettings(event));
    }

    @Test
    void processMessage_RoutingNull() {
        String stringEvent = "{\"msisdn\":null,\"id\":\"1722442489770-7788608933795\",\"message_id\":\"1722442489766-7788604799226\",\"system_id\":\"smppsp\",\"deliver_sm_id\":null,\"deliver_sm_server_id\":null,\"command_status\":0,\"sequence_number\":2,\"source_addr_ton\":1,\"source_addr_npi\":1,\"source_addr\":\"6666\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"5555\",\"esm_class\":3,\"validity_period\":\"60\",\"registered_delivery\":1,\"data_coding\":0,\"sm_default_msg_id\":0,\"short_message\":\"Hello!\",\"delivery_receipt\":null,\"status\":null,\"error_code\":null,\"check_submit_sm_response\":null,\"optional_parameters\":null,\"origin_network_type\":\"SP\",\"origin_protocol\":\"SMPP\",\"origin_network_id\":3,\"dest_network_type\":\"GW\",\"dest_protocol\":\"SMPP\",\"dest_network_id\":1,\"routing_id\":1,\"address_nature_msisdn\":null,\"numbering_plan_msisdn\":null,\"remote_dialog_id\":null,\"local_dialog_id\":null,\"sccp_called_party_address_pc\":null,\"sccp_called_party_address_ssn\":null,\"sccp_called_party_address\":null,\"sccp_calling_party_address_pc\":null,\"sccp_calling_party_address_ssn\":null,\"sccp_calling_party_address\":null,\"global_title\":null,\"global_title_indicator\":null,\"translation_type\":null,\"smsc_ssn\":null,\"hlr_ssn\":null,\"msc_ssn\":null,\"map_version\":null,\"is_retry\":false,\"retry_dest_network_id\":\"\",\"retry_number\":null,\"is_last_retry\":false,\"is_network_notify_error\":false,\"due_delay\":0,\"accumulated_time\":0,\"drop_map_sri\":false,\"network_id_to_map_sri\":-1,\"network_id_to_permanent_failure\":-1,\"drop_temp_failure\":false,\"network_id_temp_failure\":-1,\"imsi\":null,\"network_node_number\":null,\"network_node_number_nature_of_address\":null,\"network_node_number_numbering_plan\":null,\"mo_message\":false,\"is_sri_response\":false,\"check_sri_response\":false,\"msg_reference_number\":null,\"total_segment\":null,\"segment_sequence\":null,\"originator_sccp_address\":null,\"udhi\":\"0\",\"udh_json\":null,\"parent_id\":null,\"is_dlr\":false,\"message_parts\":null}";
        MessageEvent event = StaticMethods.stringAsEvent(stringEvent);
        assertNotNull(event);
        when(routingMatcher.getRouting(event)).thenReturn(null);
        assertDoesNotThrow(() -> commonProcessor.processMessage(event));
    }

    @Test
    void processMessage_DestinationEmpty() {
        String stringEvent = "{\"msisdn\":null,\"id\":\"1722442489770-7788608933795\",\"message_id\":\"1722442489766-7788604799226\",\"system_id\":\"smppsp\",\"deliver_sm_id\":null,\"deliver_sm_server_id\":null,\"command_status\":0,\"sequence_number\":2,\"source_addr_ton\":1,\"source_addr_npi\":1,\"source_addr\":\"6666\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"5555\",\"esm_class\":3,\"validity_period\":\"60\",\"registered_delivery\":1,\"data_coding\":0,\"sm_default_msg_id\":0,\"short_message\":\"Hello!\",\"delivery_receipt\":null,\"status\":null,\"error_code\":null,\"check_submit_sm_response\":null,\"optional_parameters\":null,\"origin_network_type\":\"SP\",\"origin_protocol\":\"SMPP\",\"origin_network_id\":3,\"dest_network_type\":\"GW\",\"dest_protocol\":\"SMPP\",\"dest_network_id\":1,\"routing_id\":1,\"address_nature_msisdn\":null,\"numbering_plan_msisdn\":null,\"remote_dialog_id\":null,\"local_dialog_id\":null,\"sccp_called_party_address_pc\":null,\"sccp_called_party_address_ssn\":null,\"sccp_called_party_address\":null,\"sccp_calling_party_address_pc\":null,\"sccp_calling_party_address_ssn\":null,\"sccp_calling_party_address\":null,\"global_title\":null,\"global_title_indicator\":null,\"translation_type\":null,\"smsc_ssn\":null,\"hlr_ssn\":null,\"msc_ssn\":null,\"map_version\":null,\"is_retry\":false,\"retry_dest_network_id\":\"\",\"retry_number\":null,\"is_last_retry\":false,\"is_network_notify_error\":false,\"due_delay\":0,\"accumulated_time\":0,\"drop_map_sri\":false,\"network_id_to_map_sri\":-1,\"network_id_to_permanent_failure\":-1,\"drop_temp_failure\":false,\"network_id_temp_failure\":-1,\"imsi\":null,\"network_node_number\":null,\"network_node_number_nature_of_address\":null,\"network_node_number_numbering_plan\":null,\"mo_message\":false,\"is_sri_response\":false,\"check_sri_response\":false,\"msg_reference_number\":null,\"total_segment\":null,\"segment_sequence\":null,\"originator_sccp_address\":null,\"udhi\":\"0\",\"udh_json\":null,\"parent_id\":null,\"is_dlr\":false,\"message_parts\":null}";
        MessageEvent event = StaticMethods.stringAsEvent(stringEvent);
        assertNotNull(event);

        String routingRuleString = "{\"id\":2,\"origin_network_id\":4,\"regex_source_addr\":\"\",\"regex_source_addr_ton\":\"\",\"regex_source_addr_npi\":\"\",\"regex_destination_addr\":\"\",\"regex_dest_addr_ton\":\"\",\"regex_dest_addr_npi\":\"\",\"regex_imsi_digits_mask\":\"\",\"regex_network_node_number\":\"\",\"regex_calling_party_address\":\"\",\"is_sri_response\":false,\"destination\":[]}";
        RoutingRule routingRule = Converter.stringToObject(routingRuleString, new TypeReference<>() {
        });
        when(routingMatcher.getRouting(event)).thenReturn(routingRule);
        assertDoesNotThrow(() -> commonProcessor.processMessage(event));
    }

    @Test
    void processMessage_DLR_False() {
        String stringEvent = "{\"msisdn\":null,\"id\":\"1722442489770-7788608933795\",\"message_id\":\"1722442489766-7788604799226\",\"system_id\":\"smppsp\",\"deliver_sm_id\":null,\"deliver_sm_server_id\":null,\"command_status\":0,\"sequence_number\":2,\"source_addr_ton\":1,\"source_addr_npi\":1,\"source_addr\":\"6666\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"5555\",\"esm_class\":3,\"validity_period\":\"60\",\"registered_delivery\":1,\"data_coding\":0,\"sm_default_msg_id\":0,\"short_message\":\"Hello!\",\"delivery_receipt\":null,\"status\":null,\"error_code\":null,\"check_submit_sm_response\":null,\"optional_parameters\":null,\"origin_network_type\":\"SP\",\"origin_protocol\":\"SMPP\",\"origin_network_id\":3,\"dest_network_type\":\"GW\",\"dest_protocol\":\"SMPP\",\"dest_network_id\":1,\"routing_id\":1,\"address_nature_msisdn\":null,\"numbering_plan_msisdn\":null,\"remote_dialog_id\":null,\"local_dialog_id\":null,\"sccp_called_party_address_pc\":null,\"sccp_called_party_address_ssn\":null,\"sccp_called_party_address\":null,\"sccp_calling_party_address_pc\":null,\"sccp_calling_party_address_ssn\":null,\"sccp_calling_party_address\":null,\"global_title\":null,\"global_title_indicator\":null,\"translation_type\":null,\"smsc_ssn\":null,\"hlr_ssn\":null,\"msc_ssn\":null,\"map_version\":null,\"is_retry\":false,\"retry_dest_network_id\":\"\",\"retry_number\":null,\"is_last_retry\":false,\"is_network_notify_error\":false,\"due_delay\":0,\"accumulated_time\":0,\"drop_map_sri\":false,\"network_id_to_map_sri\":-1,\"network_id_to_permanent_failure\":-1,\"drop_temp_failure\":false,\"network_id_temp_failure\":-1,\"imsi\":null,\"network_node_number\":null,\"network_node_number_nature_of_address\":null,\"network_node_number_numbering_plan\":null,\"mo_message\":false,\"is_sri_response\":false,\"check_sri_response\":false,\"msg_reference_number\":null,\"total_segment\":null,\"segment_sequence\":null,\"originator_sccp_address\":null,\"udhi\":\"0\",\"udh_json\":null,\"parent_id\":null,\"is_dlr\":false,\"message_parts\":null}";
        MessageEvent event = StaticMethods.stringAsEvent(stringEvent);
        assertNotNull(event);
        String routingRuleString = "{\"id\":2,\"origin_network_id\":4,\"regex_source_addr\":\"\",\"regex_source_addr_ton\":\"\",\"regex_source_addr_npi\":\"\",\"regex_destination_addr\":\"\",\"regex_dest_addr_ton\":\"\",\"regex_dest_addr_npi\":\"\",\"regex_imsi_digits_mask\":\"\",\"regex_network_node_number\":\"\",\"regex_calling_party_address\":\"\",\"is_sri_response\":false,\"destination\":[{\"priority\":1,\"network_id\":6,\"dest_protocol\":\"SMPP\",\"network_type\":\"GW\"}],\"new_source_addr\":\"\",\"new_source_addr_ton\":-1,\"new_source_addr_npi\":-1,\"new_destination_addr\":\"\",\"new_dest_addr_ton\":-1,\"new_dest_addr_npi\":-1,\"add_source_addr_prefix\":\"\",\"add_dest_addr_prefix\":\"\",\"remove_source_addr_prefix\":\"\",\"remove_dest_addr_prefix\":\"\",\"new_gt_sccp_addr_mt\":\"\",\"drop_map_sri\":false,\"network_id_to_map_sri\":-1,\"network_id_to_permanent_failure\":-1,\"drop_temp_failure\":false,\"network_id_temp_failure\":-1,\"check_sri_response\":false,\"origin_protocol\":\"SMPP\",\"origin_network_type\":\"SP\",\"has_filter_rules\":false,\"has_action_rules\":false}";
        RoutingRule routingRule = Converter.stringToObject(routingRuleString, new TypeReference<>() {
        });
        when(routingMatcher.getRouting(event)).thenReturn(routingRule);
        assertDoesNotThrow(() -> commonProcessor.processMessage(event));
    }

    @Test
    void processMessage_DLR_True() {
        String stringEvent = "{\"msisdn\":null,\"id\":\"1722615370526-9949308027644\",\"message_id\":\"1722615339113-9917894629494\",\"system_id\":\"smppsp\",\"deliver_sm_id\":\"455a4020-0269-4a0d-b1ed-bd45601ea881\",\"deliver_sm_server_id\":\"1722615339113-9917894629494\",\"command_status\":0,\"sequence_number\":13,\"source_addr_ton\":1,\"source_addr_npi\":1,\"source_addr\":\"5555\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"6666\",\"esm_class\":4,\"validity_period\":\"60\",\"registered_delivery\":1,\"data_coding\":0,\"sm_default_msg_id\":0,\"short_message\":\"id:1722615339113-9917894629494 sub:001 dlvrd:001 submit date:2408021016 done date:2408021016 stat:DELIVRD err:000 text:\",\"delivery_receipt\":\"id:1722615339113-9917894629494 sub:001 dlvrd:001 submit date:2408021016 done date:2408021016 stat:DELIVRD err:000 text:\",\"status\":\"DELIVRD\",\"error_code\":\"000\",\"check_submit_sm_response\":true,\"optional_parameters\":[{\"tag\":30,\"value\":\"455a4020-0269-4a0d-b1ed-bd45601ea881\"},{\"tag\":1063,\"value\":\"1\"}],\"origin_network_type\":\"GW\",\"origin_protocol\":\"SMPP\",\"origin_network_id\":6,\"dest_network_type\":\"SP\",\"dest_protocol\":\"SS7\",\"dest_network_id\":3,\"routing_id\":0,\"address_nature_msisdn\":null,\"numbering_plan_msisdn\":null,\"remote_dialog_id\":null,\"local_dialog_id\":null,\"sccp_called_party_address_pc\":null,\"sccp_called_party_address_ssn\":null,\"sccp_called_party_address\":null,\"sccp_calling_party_address_pc\":null,\"sccp_calling_party_address_ssn\":null,\"sccp_calling_party_address\":null,\"global_title\":null,\"global_title_indicator\":null,\"translation_type\":null,\"smsc_ssn\":null,\"hlr_ssn\":null,\"msc_ssn\":null,\"map_version\":null,\"is_retry\":false,\"retry_dest_network_id\":null,\"retry_number\":null,\"is_last_retry\":false,\"is_network_notify_error\":false,\"due_delay\":0,\"accumulated_time\":0,\"drop_map_sri\":false,\"network_id_to_map_sri\":0,\"network_id_to_permanent_failure\":0,\"drop_temp_failure\":false,\"network_id_temp_failure\":0,\"imsi\":null,\"network_node_number\":null,\"network_node_number_nature_of_address\":null,\"network_node_number_numbering_plan\":null,\"mo_message\":false,\"is_sri_response\":false,\"check_sri_response\":false,\"msg_reference_number\":null,\"total_segment\":null,\"segment_sequence\":null,\"originator_sccp_address\":null,\"udhi\":null,\"udh_json\":null,\"parent_id\":null,\"is_dlr\":true,\"message_parts\":null}";
        MessageEvent event = StaticMethods.stringAsEvent(stringEvent);
        assertNotNull(event);
        String routingRuleString = "{\"id\":2,\"origin_network_id\":6,\"regex_source_addr\":\"\",\"regex_source_addr_ton\":\"\",\"regex_source_addr_npi\":\"\",\"regex_destination_addr\":\"\",\"regex_dest_addr_ton\":\"\",\"regex_dest_addr_npi\":\"\",\"regex_imsi_digits_mask\":\"\",\"regex_network_node_number\":\"\",\"regex_calling_party_address\":\"\",\"is_sri_response\":false,\"destination\":[{\"priority\":1,\"network_id\":3,\"dest_protocol\":\"SS7\",\"network_type\":\"SP\"}],\"new_source_addr\":\"\",\"new_source_addr_ton\":-1,\"new_source_addr_npi\":-1,\"new_destination_addr\":\"\",\"new_dest_addr_ton\":-1,\"new_dest_addr_npi\":-1,\"add_source_addr_prefix\":\"\",\"add_dest_addr_prefix\":\"\",\"remove_source_addr_prefix\":\"\",\"remove_dest_addr_prefix\":\"\",\"new_gt_sccp_addr_mt\":\"\",\"drop_map_sri\":false,\"network_id_to_map_sri\":-1,\"network_id_to_permanent_failure\":-1,\"drop_temp_failure\":false,\"network_id_temp_failure\":-1,\"check_sri_response\":false,\"origin_protocol\":\"SMPP\",\"origin_network_type\":\"SP\",\"has_filter_rules\":false,\"has_action_rules\":false}";
        RoutingRule routingRule = Converter.stringToObject(routingRuleString, new TypeReference<>() {
        });
        when(routingMatcher.getRouting(event)).thenReturn(routingRule);
        assertDoesNotThrow(() -> commonProcessor.processMessage(event)); // SS7 Settings Null

        String ss7SettingString = "{\"name\":\"ss7gw\",\"enabled\":0,\"status\":\"STARTED\",\"protocol\":\"SS7\",\"network_id\":3,\"mno_id\":1,\"global_title\":\"22220\",\"global_title_indicator\":\"GT0100\",\"translation_type\":0,\"smsc_ssn\":8,\"hlr_ssn\":6,\"msc_ssn\":8,\"map_version\":3,\"split_message\":false}";
        Ss7Settings ss7Settings = Converter.stringToObject(ss7SettingString, new TypeReference<>() {
        });
        when(loadSettings.getSs7Settings(anyInt())).thenReturn(ss7Settings);
        assertDoesNotThrow(() -> commonProcessor.processMessage(event));

        event.setOriginProtocol("SS7");
        assertDoesNotThrow(() -> commonProcessor.processMessage(event));

        routingRule.setNewGtSccpAddrMt("10010");
        event.setMoMessage(false);
        assertDoesNotThrow(() -> commonProcessor.processMessage(event));

        event.setMoMessage(true);
        assertDoesNotThrow(() -> commonProcessor.processMessage(event));
    }

    @Test
    void applyingRoutingRuleActions() {
        String stringEvent = "{\"msisdn\":null,\"id\":\"1722442489770-7788608933795\",\"message_id\":\"1722442489766-7788604799226\",\"system_id\":\"smppsp\",\"deliver_sm_id\":null,\"deliver_sm_server_id\":null,\"command_status\":0,\"sequence_number\":2,\"source_addr_ton\":1,\"source_addr_npi\":1,\"source_addr\":\"6666\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"5555\",\"esm_class\":3,\"validity_period\":\"60\",\"registered_delivery\":1,\"data_coding\":0,\"sm_default_msg_id\":0,\"short_message\":\"Hello!\",\"delivery_receipt\":null,\"status\":null,\"error_code\":null,\"check_submit_sm_response\":null,\"optional_parameters\":null,\"origin_network_type\":\"SP\",\"origin_protocol\":\"SMPP\",\"origin_network_id\":3,\"dest_network_type\":\"GW\",\"dest_protocol\":\"SMPP\",\"dest_network_id\":1,\"routing_id\":1,\"address_nature_msisdn\":null,\"numbering_plan_msisdn\":null,\"remote_dialog_id\":null,\"local_dialog_id\":null,\"sccp_called_party_address_pc\":null,\"sccp_called_party_address_ssn\":null,\"sccp_called_party_address\":null,\"sccp_calling_party_address_pc\":null,\"sccp_calling_party_address_ssn\":null,\"sccp_calling_party_address\":null,\"global_title\":null,\"global_title_indicator\":null,\"translation_type\":null,\"smsc_ssn\":null,\"hlr_ssn\":null,\"msc_ssn\":null,\"map_version\":null,\"is_retry\":false,\"retry_dest_network_id\":\"\",\"retry_number\":null,\"is_last_retry\":false,\"is_network_notify_error\":false,\"due_delay\":0,\"accumulated_time\":0,\"drop_map_sri\":false,\"network_id_to_map_sri\":-1,\"network_id_to_permanent_failure\":-1,\"drop_temp_failure\":false,\"network_id_temp_failure\":-1,\"imsi\":null,\"network_node_number\":null,\"network_node_number_nature_of_address\":null,\"network_node_number_numbering_plan\":null,\"mo_message\":false,\"is_sri_response\":false,\"check_sri_response\":false,\"msg_reference_number\":null,\"total_segment\":null,\"segment_sequence\":null,\"originator_sccp_address\":null,\"udhi\":\"0\",\"udh_json\":null,\"parent_id\":null,\"is_dlr\":false,\"message_parts\":null}";
        MessageEvent event = StaticMethods.stringAsEvent(stringEvent);
        assertNotNull(event);
        String routingRuleString = "{\"id\":2,\"origin_network_id\":6,\"regex_source_addr\":\"\",\"regex_source_addr_ton\":\"\",\"regex_source_addr_npi\":\"\",\"regex_destination_addr\":\"\",\"regex_dest_addr_ton\":\"\",\"regex_dest_addr_npi\":\"\",\"regex_imsi_digits_mask\":\"\",\"regex_network_node_number\":\"\",\"regex_calling_party_address\":\"\",\"is_sri_response\":false,\"destination\":[{\"priority\":1,\"network_id\":3,\"dest_protocol\":\"SMPP\",\"network_type\":\"GW\"}],\"new_source_addr\":\"\",\"new_source_addr_ton\":-1,\"new_source_addr_npi\":-1,\"new_destination_addr\":\"\",\"new_dest_addr_ton\":-1,\"new_dest_addr_npi\":-1,\"add_source_addr_prefix\":\"\",\"add_dest_addr_prefix\":\"\",\"remove_source_addr_prefix\":\"\",\"remove_dest_addr_prefix\":\"\",\"new_gt_sccp_addr_mt\":\"\",\"drop_map_sri\":false,\"network_id_to_map_sri\":-1,\"network_id_to_permanent_failure\":-1,\"drop_temp_failure\":false,\"network_id_temp_failure\":-1,\"check_sri_response\":false,\"origin_protocol\":\"SMPP\",\"origin_network_type\":\"SP\",\"has_filter_rules\":true,\"has_action_rules\":true}";
        RoutingRule routingRule = Converter.stringToObject(routingRuleString, new TypeReference<>() {
        });
        when(routingMatcher.getRouting(any())).thenReturn(routingRule);
        assertDoesNotThrow(() -> commonProcessor.processMessage(event));

        // Source Address
        routingRule.setNewSourceAddr("newSourceAddr");
        routingRule.setNewSourceAddrTon(1);
        routingRule.setNewSourceAddrNpi(1);
        routingRule.setRemoveSourceAddrPrefix("removeSourceAddrPrefix");
        routingRule.setAddSourceAddrPrefix("addSourceAddrPrefix");
        assertDoesNotThrow(() -> commonProcessor.processMessage(event));

        // Destination Address
        routingRule.setNewDestinationAddr("newDestinationAddr");
        routingRule.setNewDestAddrTon(1);
        routingRule.setNewDestAddrNpi(1);
        routingRule.setRemoveDestAddrPrefix("removeDestAddrPrefix");
        routingRule.setAddDestAddrPrefix("addDestAddrPrefix");
        assertDoesNotThrow(() -> commonProcessor.processMessage(event));
    }

    @Test
    void processDlr_checkResult() {
        String stringDlr = "{\"msisdn\":null,\"id\":\"1722615370526-9949307382958\",\"message_id\":\"1722615339493-9918274592992\",\"system_id\":\"smppsp\",\"deliver_sm_id\":\"29bb38a6-8d0d-43a0-9586-7b97f8d2c479\",\"deliver_sm_server_id\":\"1722615339493-9918274592992\",\"command_status\":0,\"sequence_number\":12,\"source_addr_ton\":1,\"source_addr_npi\":1,\"source_addr\":\"5555\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"6666\",\"esm_class\":4,\"validity_period\":\"60\",\"registered_delivery\":1,\"data_coding\":0,\"sm_default_msg_id\":0,\"short_message\":\"id:1722615339493-9918274592992 sub:001 dlvrd:001 submit date:2408021016 done date:2408021016 stat:DELIVRD err:000 text:\",\"delivery_receipt\":\"id:1722615339493-9918274592992 sub:001 dlvrd:001 submit date:2408021016 done date:2408021016 stat:DELIVRD err:000 text:\",\"status\":\"DELIVRD\",\"error_code\":\"000\",\"check_submit_sm_response\":true,\"optional_parameters\":[{\"tag\":30,\"value\":\"29bb38a6-8d0d-43a0-9586-7b97f8d2c479\"},{\"tag\":1063,\"value\":\"1\"}],\"origin_network_type\":\"GW\",\"origin_protocol\":\"SMPP\",\"origin_network_id\":6,\"dest_network_type\":\"SP\",\"dest_protocol\":\"SMPP\",\"dest_network_id\":4,\"routing_id\":0,\"address_nature_msisdn\":null,\"numbering_plan_msisdn\":null,\"remote_dialog_id\":null,\"local_dialog_id\":null,\"sccp_called_party_address_pc\":null,\"sccp_called_party_address_ssn\":null,\"sccp_called_party_address\":null,\"sccp_calling_party_address_pc\":null,\"sccp_calling_party_address_ssn\":null,\"sccp_calling_party_address\":null,\"global_title\":null,\"global_title_indicator\":null,\"translation_type\":null,\"smsc_ssn\":null,\"hlr_ssn\":null,\"msc_ssn\":null,\"map_version\":null,\"is_retry\":false,\"retry_dest_network_id\":null,\"retry_number\":null,\"is_last_retry\":false,\"is_network_notify_error\":false,\"due_delay\":0,\"accumulated_time\":0,\"drop_map_sri\":false,\"network_id_to_map_sri\":0,\"network_id_to_permanent_failure\":0,\"drop_temp_failure\":false,\"network_id_temp_failure\":0,\"imsi\":null,\"network_node_number\":null,\"network_node_number_nature_of_address\":null,\"network_node_number_numbering_plan\":null,\"mo_message\":false,\"is_sri_response\":false,\"check_sri_response\":false,\"msg_reference_number\":null,\"total_segment\":null,\"segment_sequence\":null,\"originator_sccp_address\":null,\"udhi\":null,\"udh_json\":null,\"parent_id\":null,\"is_dlr\":true,\"message_parts\":null}";
        MessageEvent dlr = StaticMethods.stringAsEvent(stringDlr);
        assertNotNull(dlr);
        String submitSmResultString = "{\"hash_id\":\"1722615339493-9918274592992\",\"id\":\"1722615339493-9918274592992\",\"system_id\":\"smppsp\",\"submit_sm_id\":\"29bb38a6-8d0d-43a0-9586-7b97f8d2c479\",\"submit_sm_server_id\":\"1722615339493-9918274592992\",\"origin_protocol\":\"SMPP\",\"origin_network_id\":6,\"origin_network_type\":\"GW\",\"msg_reference_number\":\"1\",\"total_segment\":1,\"segment_sequence\":1}";
        when(appProperties.getSmppResult()).thenReturn("smpp_result");
        when(appProperties.getHttpResult()).thenReturn("http_result");
        when(jedisCluster.hget(anyString(), anyString())).thenReturn(submitSmResultString);
        assertDoesNotThrow(() -> commonProcessor.processDlr(dlr));

        dlr.setOriginProtocol("HTTP");
        assertDoesNotThrow(() -> commonProcessor.processDlr(dlr));

        dlr.setSystemId(null);
        String resultString = "{\"hash_id\":\"1722615339493-9918274592992\",\"id\":\"1722615339493-9918274592992\",\"system_id\":\"smppsp\",\"submit_sm_id\":\"29bb38a6-8d0d-43a0-9586-7b97f8d2c479\",\"submit_sm_server_id\":\"1722615339493-9918274592992\",\"origin_protocol\":\"HTTP\",\"origin_network_id\":6,\"origin_network_type\":\"SP\",\"msg_reference_number\":\"1\",\"total_segment\":1,\"segment_sequence\":1}";
        when(jedisCluster.hget(anyString(), anyString())).thenReturn(resultString);
        assertDoesNotThrow(() -> commonProcessor.processDlr(dlr));

        // handle receipt Delivery catch
        dlr.setDelReceipt("{}");
        assertDoesNotThrow(() -> commonProcessor.processDlr(dlr));

        dlr.setDelReceipt(null);
        assertDoesNotThrow(() -> commonProcessor.processDlr(dlr));
    }

    @Test
    void processDlr_checkResultFalse() {
        String stringDlr = "{\"msisdn\":null,\"id\":\"1722615370526-9949307382958\",\"message_id\":\"1722615339493-9918274592992\",\"system_id\":\"smppsp\",\"deliver_sm_id\":\"29bb38a6-8d0d-43a0-9586-7b97f8d2c479\",\"deliver_sm_server_id\":\"1722615339493-9918274592992\",\"command_status\":0,\"sequence_number\":12,\"source_addr_ton\":1,\"source_addr_npi\":1,\"source_addr\":\"5555\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"6666\",\"esm_class\":4,\"validity_period\":\"60\",\"registered_delivery\":1,\"data_coding\":0,\"sm_default_msg_id\":0,\"short_message\":\"id:1722615339493-9918274592992 sub:001 dlvrd:001 submit date:2408021016 done date:2408021016 stat:DELIVRD err:000 text:\",\"delivery_receipt\":\"id:1722615339493-9918274592992 sub:001 dlvrd:001 submit date:2408021016 done date:2408021016 stat:DELIVRD err:000 text:\",\"status\":\"DELIVRD\",\"error_code\":\"000\",\"check_submit_sm_response\":true,\"optional_parameters\":[{\"tag\":30,\"value\":\"29bb38a6-8d0d-43a0-9586-7b97f8d2c479\"},{\"tag\":1063,\"value\":\"1\"}],\"origin_network_type\":\"GW\",\"origin_protocol\":\"SMPP\",\"origin_network_id\":6,\"dest_network_type\":\"SP\",\"dest_protocol\":\"SMPP\",\"dest_network_id\":4,\"routing_id\":0,\"address_nature_msisdn\":null,\"numbering_plan_msisdn\":null,\"remote_dialog_id\":null,\"local_dialog_id\":null,\"sccp_called_party_address_pc\":null,\"sccp_called_party_address_ssn\":null,\"sccp_called_party_address\":null,\"sccp_calling_party_address_pc\":null,\"sccp_calling_party_address_ssn\":null,\"sccp_calling_party_address\":null,\"global_title\":null,\"global_title_indicator\":null,\"translation_type\":null,\"smsc_ssn\":null,\"hlr_ssn\":null,\"msc_ssn\":null,\"map_version\":null,\"is_retry\":false,\"retry_dest_network_id\":null,\"retry_number\":null,\"is_last_retry\":false,\"is_network_notify_error\":false,\"due_delay\":0,\"accumulated_time\":0,\"drop_map_sri\":false,\"network_id_to_map_sri\":0,\"network_id_to_permanent_failure\":0,\"drop_temp_failure\":false,\"network_id_temp_failure\":0,\"imsi\":null,\"network_node_number\":null,\"network_node_number_nature_of_address\":null,\"network_node_number_numbering_plan\":null,\"mo_message\":false,\"is_sri_response\":false,\"check_sri_response\":false,\"msg_reference_number\":null,\"total_segment\":null,\"segment_sequence\":null,\"originator_sccp_address\":null,\"udhi\":null,\"udh_json\":null,\"parent_id\":null,\"is_dlr\":true,\"message_parts\":null}";
        MessageEvent dlr = StaticMethods.stringAsEvent(stringDlr);
        assertNotNull(dlr);
        dlr.setCheckSubmitSmResponse(false);
        assertDoesNotThrow(() -> commonProcessor.processDlr(dlr));
    }

    @Test
    void processDlr_ResultNotAvailable() {
        String stringDlr = "{\"msisdn\":null,\"id\":\"1722615370526-9949307382958\",\"message_id\":\"1722615339493-9918274592992\",\"system_id\":\"smppsp\",\"deliver_sm_id\":\"29bb38a6-8d0d-43a0-9586-7b97f8d2c479\",\"deliver_sm_server_id\":\"1722615339493-9918274592992\",\"command_status\":0,\"sequence_number\":12,\"source_addr_ton\":1,\"source_addr_npi\":1,\"source_addr\":\"5555\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"6666\",\"esm_class\":4,\"validity_period\":\"60\",\"registered_delivery\":1,\"data_coding\":0,\"sm_default_msg_id\":0,\"short_message\":\"id:1722615339493-9918274592992 sub:001 dlvrd:001 submit date:2408021016 done date:2408021016 stat:DELIVRD err:000 text:\",\"delivery_receipt\":\"id:1722615339493-9918274592992 sub:001 dlvrd:001 submit date:2408021016 done date:2408021016 stat:DELIVRD err:000 text:\",\"status\":\"DELIVRD\",\"error_code\":\"000\",\"check_submit_sm_response\":true,\"optional_parameters\":[{\"tag\":30,\"value\":\"29bb38a6-8d0d-43a0-9586-7b97f8d2c479\"},{\"tag\":1063,\"value\":\"1\"}],\"origin_network_type\":\"GW\",\"origin_protocol\":\"SMPP\",\"origin_network_id\":6,\"dest_network_type\":\"SP\",\"dest_protocol\":\"SMPP\",\"dest_network_id\":4,\"routing_id\":0,\"address_nature_msisdn\":null,\"numbering_plan_msisdn\":null,\"remote_dialog_id\":null,\"local_dialog_id\":null,\"sccp_called_party_address_pc\":null,\"sccp_called_party_address_ssn\":null,\"sccp_called_party_address\":null,\"sccp_calling_party_address_pc\":null,\"sccp_calling_party_address_ssn\":null,\"sccp_calling_party_address\":null,\"global_title\":null,\"global_title_indicator\":null,\"translation_type\":null,\"smsc_ssn\":null,\"hlr_ssn\":null,\"msc_ssn\":null,\"map_version\":null,\"is_retry\":false,\"retry_dest_network_id\":null,\"retry_number\":null,\"is_last_retry\":false,\"is_network_notify_error\":false,\"due_delay\":0,\"accumulated_time\":0,\"drop_map_sri\":false,\"network_id_to_map_sri\":0,\"network_id_to_permanent_failure\":0,\"drop_temp_failure\":false,\"network_id_temp_failure\":0,\"imsi\":null,\"network_node_number\":null,\"network_node_number_nature_of_address\":null,\"network_node_number_numbering_plan\":null,\"mo_message\":false,\"is_sri_response\":false,\"check_sri_response\":false,\"msg_reference_number\":null,\"total_segment\":null,\"segment_sequence\":null,\"originator_sccp_address\":null,\"udhi\":null,\"udh_json\":null,\"parent_id\":null,\"is_dlr\":true,\"message_parts\":null}";
        MessageEvent dlr = StaticMethods.stringAsEvent(stringDlr);
        assertNotNull(dlr);

        when(appProperties.getSmppResult()).thenReturn("smpp_result");
        when(jedisCluster.hget(anyString(), anyString())).thenReturn(null);
        assertThrows(RTException.class, () -> commonProcessor.processDlr(dlr));

        dlr.setOriginProtocol("HTTP");
        when(appProperties.getHttpResult()).thenReturn("http_result");
        assertThrows(RTException.class, () -> commonProcessor.processDlr(dlr));
    }

    @Test
    void processDlr_setDetailsForMessageEventAsPerOriginProtocol() {
        String stringDlr = "{\"msisdn\":null,\"id\":\"1722615370526-9949307382958\",\"message_id\":\"1722615339493-9918274592992\",\"system_id\":\"smppsp\",\"deliver_sm_id\":\"29bb38a6-8d0d-43a0-9586-7b97f8d2c479\",\"deliver_sm_server_id\":\"1722615339493-9918274592992\",\"command_status\":0,\"sequence_number\":12,\"source_addr_ton\":1,\"source_addr_npi\":1,\"source_addr\":\"5555\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"6666\",\"esm_class\":4,\"validity_period\":\"60\",\"registered_delivery\":1,\"data_coding\":0,\"sm_default_msg_id\":0,\"short_message\":\"id:1722615339493-9918274592992 sub:001 dlvrd:001 submit date:2408021016 done date:2408021016 stat:DELIVRD err:000 text:\",\"delivery_receipt\":\"id:1722615339493-9918274592992 sub:001 dlvrd:001 submit date:2408021016 done date:2408021016 stat:DELIVRD err:000 text:\",\"status\":\"DELIVRD\",\"error_code\":\"000\",\"check_submit_sm_response\":true,\"optional_parameters\":[{\"tag\":30,\"value\":\"29bb38a6-8d0d-43a0-9586-7b97f8d2c479\"},{\"tag\":1063,\"value\":\"1\"}],\"origin_network_type\":\"GW\",\"origin_protocol\":\"SMPP\",\"origin_network_id\":6,\"dest_network_type\":\"SP\",\"dest_protocol\":\"SMPP\",\"dest_network_id\":4,\"routing_id\":0,\"address_nature_msisdn\":null,\"numbering_plan_msisdn\":null,\"remote_dialog_id\":null,\"local_dialog_id\":null,\"sccp_called_party_address_pc\":null,\"sccp_called_party_address_ssn\":null,\"sccp_called_party_address\":null,\"sccp_calling_party_address_pc\":null,\"sccp_calling_party_address_ssn\":null,\"sccp_calling_party_address\":null,\"global_title\":null,\"global_title_indicator\":null,\"translation_type\":null,\"smsc_ssn\":null,\"hlr_ssn\":null,\"msc_ssn\":null,\"map_version\":null,\"is_retry\":false,\"retry_dest_network_id\":null,\"retry_number\":null,\"is_last_retry\":false,\"is_network_notify_error\":false,\"due_delay\":0,\"accumulated_time\":0,\"drop_map_sri\":false,\"network_id_to_map_sri\":0,\"network_id_to_permanent_failure\":0,\"drop_temp_failure\":false,\"network_id_temp_failure\":0,\"imsi\":null,\"network_node_number\":null,\"network_node_number_nature_of_address\":null,\"network_node_number_numbering_plan\":null,\"mo_message\":false,\"is_sri_response\":false,\"check_sri_response\":false,\"msg_reference_number\":null,\"total_segment\":null,\"segment_sequence\":null,\"originator_sccp_address\":null,\"udhi\":null,\"udh_json\":null,\"parent_id\":null,\"is_dlr\":true,\"message_parts\":null}";
        MessageEvent dlr = StaticMethods.stringAsEvent(stringDlr);
        assertNotNull(dlr);
        String submitSmResultString = "{\"hash_id\":\"1722615339493-9918274592992\",\"id\":\"1722615339493-9918274592992\",\"system_id\":\"smppsp\",\"submit_sm_id\":\"29bb38a6-8d0d-43a0-9586-7b97f8d2c479\",\"submit_sm_server_id\":\"1722615339493-9918274592992\",\"origin_protocol\":\"SS7\",\"origin_network_id\":6,\"origin_network_type\":\"GW\",\"msg_reference_number\":\"1\",\"total_segment\":1,\"segment_sequence\":1}";
        when(appProperties.getSmppResult()).thenReturn("smpp_result");
        when(jedisCluster.hget(anyString(), anyString())).thenReturn(submitSmResultString);
        String ss7SettingString = "{\"name\":\"ss7gw\",\"enabled\":0,\"status\":\"STARTED\",\"protocol\":\"SS7\",\"network_id\":3,\"mno_id\":1,\"global_title\":\"22220\",\"global_title_indicator\":\"GT0100\",\"translation_type\":0,\"smsc_ssn\":8,\"hlr_ssn\":6,\"msc_ssn\":8,\"map_version\":3,\"split_message\":false}";
        Ss7Settings ss7Settings = Converter.stringToObject(ss7SettingString, new TypeReference<>() {
        });
        when(loadSettings.getSs7Settings(anyInt())).thenReturn(ss7Settings);
        assertDoesNotThrow(() -> commonProcessor.processDlr(dlr));

        dlr.setStatus("UNDELIV");
        assertDoesNotThrow(() -> commonProcessor.processDlr(dlr));
    }

    @Test
    void getPartsOfMessageTest_NoSS7Dest() {
        String stringEvent = "{\"msisdn\":null,\"id\":\"1722442489770-7788608933795\",\"message_id\":\"1722442489766-7788604799226\",\"system_id\":\"smppsp\",\"deliver_sm_id\":null,\"deliver_sm_server_id\":null,\"command_status\":0,\"sequence_number\":2,\"source_addr_ton\":1,\"source_addr_npi\":1,\"source_addr\":\"6666\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"5555\",\"esm_class\":3,\"validity_period\":\"60\",\"registered_delivery\":1,\"data_coding\":0,\"sm_default_msg_id\":0,\"short_message\":\"Use the SMSC simulator service from smpp.org for free to test SMS submission by your application. Your application can connect to the SMSC simulator, submit SMS, and receive message IDs for each message submitted. SMS delivery is simulated, with no messages delivered to mobiles, and delivery receipts indicating successful delivery are returned by the SMSC simulator.\",\"delivery_receipt\":null,\"status\":null,\"error_code\":null,\"check_submit_sm_response\":null,\"optional_parameters\":null,\"origin_network_type\":\"SP\",\"origin_protocol\":\"HTTP\",\"origin_network_id\":1,\"dest_network_type\":\"GW\",\"dest_protocol\":\"SMPP\",\"dest_network_id\":1,\"routing_id\":1,\"address_nature_msisdn\":null,\"numbering_plan_msisdn\":null,\"remote_dialog_id\":null,\"local_dialog_id\":null,\"sccp_called_party_address_pc\":null,\"sccp_called_party_address_ssn\":null,\"sccp_called_party_address\":null,\"sccp_calling_party_address_pc\":null,\"sccp_calling_party_address_ssn\":null,\"sccp_calling_party_address\":null,\"global_title\":null,\"global_title_indicator\":null,\"translation_type\":null,\"smsc_ssn\":null,\"hlr_ssn\":null,\"msc_ssn\":null,\"map_version\":null,\"is_retry\":false,\"retry_dest_network_id\":\"\",\"retry_number\":null,\"is_last_retry\":false,\"is_network_notify_error\":false,\"due_delay\":0,\"accumulated_time\":0,\"drop_map_sri\":false,\"network_id_to_map_sri\":-1,\"network_id_to_permanent_failure\":-1,\"drop_temp_failure\":false,\"network_id_temp_failure\":-1,\"imsi\":null,\"network_node_number\":null,\"network_node_number_nature_of_address\":null,\"network_node_number_numbering_plan\":null,\"mo_message\":false,\"is_sri_response\":false,\"check_sri_response\":false,\"msg_reference_number\":null,\"total_segment\":null,\"segment_sequence\":null,\"originator_sccp_address\":null,\"udhi\":\"0\",\"udh_json\":null,\"parent_id\":null,\"is_dlr\":false,\"message_parts\":null}";
        MessageEvent event = StaticMethods.stringAsEvent(stringEvent);
        Objects.requireNonNull(event, "Event is null");

        String stringSmppGw = "{\"network_id\":6,\"name\":\"smppgw\",\"system_id\":\"smppgw\",\"password\":\"1234\",\"ip\":\"192.168.100.20\",\"port\":2777,\"bind_type\":\"TRANSCEIVER\",\"system_type\":\"\",\"interface_version\":\"IF_50\",\"sessions_number\":1,\"address_ton\":0,\"address_npi\":0,\"address_range\":null,\"tps\":10,\"status\":\"STOPPED\",\"enabled\":1,\"enquire_link_period\":30000,\"enquire_link_timeout\":0,\"request_dlr\":true,\"no_retry_error_code\":\"\",\"retry_alternate_destination_error_code\":\"\",\"bind_timeout\":5000,\"bind_retry_period\":10000,\"pdu_timeout\":5000,\"pdu_degree\":1,\"thread_pool_size\":100,\"mno_id\":1,\"tlv_message_receipt_id\":false,\"message_id_decimal_format\":false,\"active_sessions_numbers\":0,\"protocol\":\"SMPP\",\"auto_retry_error_code\":\"\",\"encoding_iso88591\":3,\"encoding_gsm7\":0,\"encoding_ucs2\":2,\"split_message\":false,\"split_smpp_type\":\"TLV\"}";
        Gateway smppGw = Converter.stringToObject(stringSmppGw, new TypeReference<>() {
        });

        gateways = new ConcurrentHashMap<>();
        gateways.put(event.getDestNetworkId(), smppGw);
        commonProcessor = new CommonProcessor(loadSettings, jedisCluster, appProperties, routingHelper, routingMatcher, gateways, ss7SettingsMap);
        assertDoesNotThrow(() -> commonProcessor.getPartsOfMessage(event));

        event.setDataCoding(8);
        assertDoesNotThrow(() -> commonProcessor.getPartsOfMessage(event));

        event.setDataCoding(3);
        assertDoesNotThrow(() -> commonProcessor.getPartsOfMessage(event));

        smppGw.setSplitMessage(true);
        commonProcessor = new CommonProcessor(loadSettings, jedisCluster, appProperties, routingHelper, routingMatcher, gateways, ss7SettingsMap);
        event.setDataCoding(19);
        assertDoesNotThrow(() -> commonProcessor.getPartsOfMessage(event));
    }

    @Test
    void getPartsOfMessageTest_SS7Dest() {
        String stringEvent = "{\"msisdn\":null,\"id\":\"1722442489770-7788608933795\",\"message_id\":\"1722442489766-7788604799226\",\"system_id\":\"smppsp\",\"deliver_sm_id\":null,\"deliver_sm_server_id\":null,\"command_status\":0,\"sequence_number\":2,\"source_addr_ton\":1,\"source_addr_npi\":1,\"source_addr\":\"6666\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"5555\",\"esm_class\":3,\"validity_period\":\"60\",\"registered_delivery\":1,\"data_coding\":0,\"sm_default_msg_id\":0,\"short_message\":\"Use the SMSC simulator service from smpp.org for free to test SMS submission by your application. Your application can connect to the SMSC simulator, submit SMS, and receive message IDs for each message submitted. SMS delivery is simulated, with no messages delivered to mobiles, and delivery receipts indicating successful delivery are returned by the SMSC simulator.\",\"delivery_receipt\":null,\"status\":null,\"error_code\":null,\"check_submit_sm_response\":null,\"optional_parameters\":null,\"origin_network_type\":\"SP\",\"origin_protocol\":\"HTTP\",\"origin_network_id\":1,\"dest_network_type\":\"GW\",\"dest_protocol\":\"SMPP\",\"dest_network_id\":1,\"routing_id\":1,\"address_nature_msisdn\":null,\"numbering_plan_msisdn\":null,\"remote_dialog_id\":null,\"local_dialog_id\":null,\"sccp_called_party_address_pc\":null,\"sccp_called_party_address_ssn\":null,\"sccp_called_party_address\":null,\"sccp_calling_party_address_pc\":null,\"sccp_calling_party_address_ssn\":null,\"sccp_calling_party_address\":null,\"global_title\":null,\"global_title_indicator\":null,\"translation_type\":null,\"smsc_ssn\":null,\"hlr_ssn\":null,\"msc_ssn\":null,\"map_version\":null,\"is_retry\":false,\"retry_dest_network_id\":\"\",\"retry_number\":null,\"is_last_retry\":false,\"is_network_notify_error\":false,\"due_delay\":0,\"accumulated_time\":0,\"drop_map_sri\":false,\"network_id_to_map_sri\":-1,\"network_id_to_permanent_failure\":-1,\"drop_temp_failure\":false,\"network_id_temp_failure\":-1,\"imsi\":null,\"network_node_number\":null,\"network_node_number_nature_of_address\":null,\"network_node_number_numbering_plan\":null,\"mo_message\":false,\"is_sri_response\":false,\"check_sri_response\":false,\"msg_reference_number\":null,\"total_segment\":null,\"segment_sequence\":null,\"originator_sccp_address\":null,\"udhi\":\"0\",\"udh_json\":null,\"parent_id\":null,\"is_dlr\":false,\"message_parts\":null}";
        MessageEvent event = StaticMethods.stringAsEvent(stringEvent);
        Objects.requireNonNull(event, "Event is null");

        String stringSs7Settings = "{\"name\":\"ss7gw\",\"enabled\":0,\"status\":\"STARTED\",\"protocol\":\"SS7\",\"network_id\":3,\"mno_id\":1,\"global_title\":\"22220\",\"global_title_indicator\":\"GT0100\",\"translation_type\":0,\"smsc_ssn\":8,\"hlr_ssn\":6,\"msc_ssn\":8,\"map_version\":3,\"split_message\":false}";
        Ss7Settings ss7Settings = Converter.stringToObject(stringSs7Settings, new TypeReference<>() {
        });
        event.setDestProtocol("SS7");
        ss7SettingsMap = new ConcurrentHashMap<>();
        ss7SettingsMap.put(event.getDestNetworkId(), ss7Settings);
        commonProcessor = new CommonProcessor(loadSettings, jedisCluster, appProperties, routingHelper, routingMatcher, gateways, ss7SettingsMap);
        assertDoesNotThrow(() -> commonProcessor.getPartsOfMessage(event));

        event.setDataCoding(2);
        commonProcessor = new CommonProcessor(loadSettings, jedisCluster, appProperties, routingHelper, routingMatcher, gateways, ss7SettingsMap);
        assertDoesNotThrow(() -> commonProcessor.getPartsOfMessage(event));
    }
}
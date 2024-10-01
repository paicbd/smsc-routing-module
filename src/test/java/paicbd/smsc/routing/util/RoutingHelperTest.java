package paicbd.smsc.routing.util;

import com.paicbd.smsc.cdr.CdrProcessor;
import com.paicbd.smsc.dto.MessageEvent;
import com.paicbd.smsc.utils.UtilsEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import redis.clients.jedis.JedisCluster;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class RoutingHelperTest {
    @Mock
    JedisCluster jedisCluster;
    @Mock
    CdrProcessor cdrProcessor;
    @Mock
    CreditHandler creditHandler;
    @Mock
    AppProperties appProperties;

    @InjectMocks
    RoutingHelper routingHelper;

    @Test
    void initTest() {
        assertDoesNotThrow(() -> routingHelper.init());
    }

    @Test
    void processEventsFlux() {
        List<String> eventsList = getMessages();
        List<MessageEvent> messageEvents = routingHelper.stringListAsEventList(eventsList);
        assertDoesNotThrow(() -> routingHelper.processEventsFlux(Flux.fromIterable(List.of(messageEvents))));
    }

    @Test
    void stringListAsEventList() {
        List<String> eventsList = getMessages();
        List<MessageEvent> messageEvents = routingHelper.stringListAsEventList(eventsList);
        assertEquals(4, messageEvents.size());
    }

    @Test
    void relocateMessageEvent() {
        List<String> eventsList = getMessages();
        List<MessageEvent> messageEvents = routingHelper.stringListAsEventList(eventsList);
        assertDoesNotThrow(() -> routingHelper.relocateMessageEvent(messageEvents));
        assertEquals(4, messageEvents.size());

        List<MessageEvent> emptyList = List.of();
        assertDoesNotThrow(() -> routingHelper.relocateMessageEvent(emptyList));
    }

    @Test
    void putMessageListInRedis_EmptyList() {
        List<String> list = List.of(
                "{\"msisdn\":null,\"id\":\"1722442489770-7788608933795\",\"message_id\":\"1722442489766-7788604799226\",\"system_id\":\"smppsp\",\"deliver_sm_id\":null,\"deliver_sm_server_id\":null,\"command_status\":0,\"sequence_number\":2,\"source_addr_ton\":1,\"source_addr_npi\":1,\"source_addr\":\"6666\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"5555\",\"esm_class\":3,\"validity_period\":\"60\",\"registered_delivery\":1,\"data_coding\":0,\"sm_default_msg_id\":0,\"short_message\":\"Hello!\",\"delivery_receipt\":null,\"status\":null,\"error_code\":null,\"check_submit_sm_response\":null,\"optional_parameters\":null,\"origin_network_type\":\"SP\",\"origin_protocol\":\"XHD\",\"origin_network_id\":3,\"dest_network_type\":\"GW\",\"dest_protocol\":\"SMPP\",\"dest_network_id\":1,\"routing_id\":1,\"address_nature_msisdn\":null,\"numbering_plan_msisdn\":null,\"remote_dialog_id\":null,\"local_dialog_id\":null,\"sccp_called_party_address_pc\":null,\"sccp_called_party_address_ssn\":null,\"sccp_called_party_address\":null,\"sccp_calling_party_address_pc\":null,\"sccp_calling_party_address_ssn\":null,\"sccp_calling_party_address\":null,\"global_title\":null,\"global_title_indicator\":null,\"translation_type\":null,\"smsc_ssn\":null,\"hlr_ssn\":null,\"msc_ssn\":null,\"map_version\":null,\"is_retry\":false,\"retry_dest_network_id\":\"\",\"retry_number\":null,\"is_last_retry\":false,\"is_network_notify_error\":false,\"due_delay\":0,\"accumulated_time\":0,\"drop_map_sri\":false,\"network_id_to_map_sri\":-1,\"network_id_to_permanent_failure\":-1,\"drop_temp_failure\":false,\"network_id_temp_failure\":-1,\"imsi\":null,\"network_node_number\":null,\"network_node_number_nature_of_address\":null,\"network_node_number_numbering_plan\":null,\"mo_message\":false,\"is_sri_response\":false,\"check_sri_response\":false,\"msg_reference_number\":null,\"total_segment\":null,\"segment_sequence\":null,\"originator_sccp_address\":null,\"udhi\":\"0\",\"udh_json\":null,\"parent_id\":null,\"is_dlr\":false,\"message_parts\":null}"
        );
        List<MessageEvent> messageEvents = routingHelper.stringListAsEventList(list);
        assertDoesNotThrow(() -> routingHelper.relocateMessageEvent(messageEvents));
    }

    @Test
    void testRelocateMessageEvent() {
        List<MessageEvent> emptyList = List.of();
        assertDoesNotThrow(() -> routingHelper.relocateDlrEvent(emptyList));

        List<String> dlrList = getDlrs();
        List<MessageEvent> dlrEvents = routingHelper.stringListAsEventList(dlrList);
        assertDoesNotThrow(() -> routingHelper.relocateDlrEvent(dlrEvents));
    }

    @Test
    void prepareCdr() {
        String stringEvent = "{\"msisdn\":null,\"id\":\"1722442489770-7788608933795\",\"message_id\":\"1722442489766-7788604799226\",\"system_id\":\"smppsp\",\"deliver_sm_id\":null,\"deliver_sm_server_id\":null,\"command_status\":0,\"sequence_number\":2,\"source_addr_ton\":1,\"source_addr_npi\":1,\"source_addr\":\"6666\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"5555\",\"esm_class\":3,\"validity_period\":\"60\",\"registered_delivery\":1,\"data_coding\":0,\"sm_default_msg_id\":0,\"short_message\":\"Hello!\",\"delivery_receipt\":null,\"status\":null,\"error_code\":null,\"check_submit_sm_response\":null,\"optional_parameters\":null,\"origin_network_type\":\"SP\",\"origin_protocol\":\"SMPP\",\"origin_network_id\":3,\"dest_network_type\":\"GW\",\"dest_protocol\":\"SMPP\",\"dest_network_id\":1,\"routing_id\":1,\"address_nature_msisdn\":null,\"numbering_plan_msisdn\":null,\"remote_dialog_id\":null,\"local_dialog_id\":null,\"sccp_called_party_address_pc\":null,\"sccp_called_party_address_ssn\":null,\"sccp_called_party_address\":null,\"sccp_calling_party_address_pc\":null,\"sccp_calling_party_address_ssn\":null,\"sccp_calling_party_address\":null,\"global_title\":null,\"global_title_indicator\":null,\"translation_type\":null,\"smsc_ssn\":null,\"hlr_ssn\":null,\"msc_ssn\":null,\"map_version\":null,\"is_retry\":false,\"retry_dest_network_id\":\"\",\"retry_number\":null,\"is_last_retry\":false,\"is_network_notify_error\":false,\"due_delay\":0,\"accumulated_time\":0,\"drop_map_sri\":false,\"network_id_to_map_sri\":-1,\"network_id_to_permanent_failure\":-1,\"drop_temp_failure\":false,\"network_id_temp_failure\":-1,\"imsi\":null,\"network_node_number\":null,\"network_node_number_nature_of_address\":null,\"network_node_number_numbering_plan\":null,\"mo_message\":false,\"is_sri_response\":false,\"check_sri_response\":false,\"msg_reference_number\":null,\"total_segment\":null,\"segment_sequence\":null,\"originator_sccp_address\":null,\"udhi\":\"0\",\"udh_json\":null,\"parent_id\":null,\"is_dlr\":false,\"message_parts\":null}";
        MessageEvent event = StaticMethods.stringAsEvent(stringEvent);
        assertDoesNotThrow(() -> routingHelper.prepareCdr(event, UtilsEnum.CdrStatus.FAILED, "comment", false));
        assertDoesNotThrow(() -> routingHelper.prepareCdr(event, UtilsEnum.CdrStatus.ENQUEUE, "comment", true));
    }

    @Test
    void calculateBatchPerWorker() {
        int itemsToProcess = 10;
        int listSize = 10;
        int workers = 10;
        int result = routingHelper.calculateBatchPerWorker(itemsToProcess, listSize, workers);
        assertEquals(1, result);

        listSize = 3;
        result = routingHelper.calculateBatchPerWorker(itemsToProcess, listSize, workers);
        assertEquals(1, result);
    }

    private List<String> getMessages() {
        return List.of(
                "{\"msisdn\":null,\"id\":\"1722442489770-7788608933795\",\"message_id\":\"1722442489766-7788604799226\",\"system_id\":\"smppsp\",\"deliver_sm_id\":null,\"deliver_sm_server_id\":null,\"command_status\":0,\"sequence_number\":2,\"source_addr_ton\":1,\"source_addr_npi\":1,\"source_addr\":\"6666\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"5555\",\"esm_class\":3,\"validity_period\":\"60\",\"registered_delivery\":1,\"data_coding\":0,\"sm_default_msg_id\":0,\"short_message\":\"Hello!\",\"delivery_receipt\":null,\"status\":null,\"error_code\":null,\"check_submit_sm_response\":null,\"optional_parameters\":null,\"origin_network_type\":\"SP\",\"origin_protocol\":\"SMPP\",\"origin_network_id\":3,\"dest_network_type\":\"GW\",\"dest_protocol\":\"SMPP\",\"dest_network_id\":1,\"routing_id\":1,\"address_nature_msisdn\":null,\"numbering_plan_msisdn\":null,\"remote_dialog_id\":null,\"local_dialog_id\":null,\"sccp_called_party_address_pc\":null,\"sccp_called_party_address_ssn\":null,\"sccp_called_party_address\":null,\"sccp_calling_party_address_pc\":null,\"sccp_calling_party_address_ssn\":null,\"sccp_calling_party_address\":null,\"global_title\":null,\"global_title_indicator\":null,\"translation_type\":null,\"smsc_ssn\":null,\"hlr_ssn\":null,\"msc_ssn\":null,\"map_version\":null,\"is_retry\":false,\"retry_dest_network_id\":\"\",\"retry_number\":null,\"is_last_retry\":false,\"is_network_notify_error\":false,\"due_delay\":0,\"accumulated_time\":0,\"drop_map_sri\":false,\"network_id_to_map_sri\":-1,\"network_id_to_permanent_failure\":-1,\"drop_temp_failure\":false,\"network_id_temp_failure\":-1,\"imsi\":null,\"network_node_number\":null,\"network_node_number_nature_of_address\":null,\"network_node_number_numbering_plan\":null,\"mo_message\":false,\"is_sri_response\":false,\"check_sri_response\":false,\"msg_reference_number\":null,\"total_segment\":null,\"segment_sequence\":null,\"originator_sccp_address\":null,\"udhi\":\"0\",\"udh_json\":null,\"parent_id\":null,\"is_dlr\":false,\"message_parts\":null}",
                "{\"msisdn\":null,\"id\":\"1722442615535-7914373631079\",\"message_id\":\"1722442615535-7914373573310\",\"system_id\":\"smppsp\",\"deliver_sm_id\":null,\"deliver_sm_server_id\":null,\"command_status\":0,\"sequence_number\":4,\"source_addr_ton\":1,\"source_addr_npi\":1,\"source_addr\":\"6666\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"5555\",\"esm_class\":3,\"validity_period\":\"60\",\"registered_delivery\":1,\"data_coding\":0,\"sm_default_msg_id\":0,\"short_message\":\"Hello!\",\"delivery_receipt\":null,\"status\":null,\"error_code\":null,\"check_submit_sm_response\":null,\"optional_parameters\":null,\"origin_network_type\":\"SP\",\"origin_protocol\":\"SMPP\",\"origin_network_id\":3,\"dest_network_type\":\"GW\",\"dest_protocol\":\"HTTP\",\"dest_network_id\":3,\"routing_id\":1,\"address_nature_msisdn\":null,\"numbering_plan_msisdn\":null,\"remote_dialog_id\":null,\"local_dialog_id\":null,\"sccp_called_party_address_pc\":null,\"sccp_called_party_address_ssn\":null,\"sccp_called_party_address\":null,\"sccp_calling_party_address_pc\":null,\"sccp_calling_party_address_ssn\":null,\"sccp_calling_party_address\":null,\"global_title\":null,\"global_title_indicator\":null,\"translation_type\":null,\"smsc_ssn\":null,\"hlr_ssn\":null,\"msc_ssn\":null,\"map_version\":null,\"is_retry\":false,\"retry_dest_network_id\":\"\",\"retry_number\":null,\"is_last_retry\":false,\"is_network_notify_error\":false,\"due_delay\":0,\"accumulated_time\":0,\"drop_map_sri\":false,\"network_id_to_map_sri\":-1,\"network_id_to_permanent_failure\":-1,\"drop_temp_failure\":false,\"network_id_temp_failure\":-1,\"imsi\":null,\"network_node_number\":null,\"network_node_number_nature_of_address\":null,\"network_node_number_numbering_plan\":null,\"mo_message\":false,\"is_sri_response\":false,\"check_sri_response\":false,\"msg_reference_number\":null,\"total_segment\":null,\"segment_sequence\":null,\"originator_sccp_address\":null,\"udhi\":\"0\",\"udh_json\":null,\"parent_id\":null,\"is_dlr\":false,\"message_parts\":null}",
                "{\"msisdn\":null,\"id\":\"1722446896082-12194920127675\",\"message_id\":\"1722446896081-12194920043917\",\"system_id\":\"smppsp\",\"deliver_sm_id\":null,\"deliver_sm_server_id\":null,\"command_status\":0,\"sequence_number\":5,\"source_addr_ton\":1,\"source_addr_npi\":1,\"source_addr\":\"6666\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"5555\",\"esm_class\":3,\"validity_period\":\"60\",\"registered_delivery\":1,\"data_coding\":0,\"sm_default_msg_id\":0,\"short_message\":\"Hello!\",\"delivery_receipt\":null,\"status\":null,\"error_code\":null,\"check_submit_sm_response\":null,\"optional_parameters\":null,\"origin_network_type\":\"SP\",\"origin_protocol\":\"SMPP\",\"origin_network_id\":3,\"dest_network_type\":\"GW\",\"dest_protocol\":\"SS7\",\"dest_network_id\":5,\"routing_id\":1,\"address_nature_msisdn\":null,\"numbering_plan_msisdn\":null,\"remote_dialog_id\":null,\"local_dialog_id\":null,\"sccp_called_party_address_pc\":null,\"sccp_called_party_address_ssn\":null,\"sccp_called_party_address\":null,\"sccp_calling_party_address_pc\":null,\"sccp_calling_party_address_ssn\":null,\"sccp_calling_party_address\":null,\"global_title\":null,\"global_title_indicator\":null,\"translation_type\":null,\"smsc_ssn\":null,\"hlr_ssn\":null,\"msc_ssn\":null,\"map_version\":null,\"is_retry\":false,\"retry_dest_network_id\":\"\",\"retry_number\":null,\"is_last_retry\":false,\"is_network_notify_error\":false,\"due_delay\":0,\"accumulated_time\":0,\"drop_map_sri\":false,\"network_id_to_map_sri\":-1,\"network_id_to_permanent_failure\":-1,\"drop_temp_failure\":false,\"network_id_temp_failure\":-1,\"imsi\":null,\"network_node_number\":null,\"network_node_number_nature_of_address\":null,\"network_node_number_numbering_plan\":null,\"mo_message\":false,\"is_sri_response\":false,\"check_sri_response\":false,\"msg_reference_number\":null,\"total_segment\":null,\"segment_sequence\":null,\"originator_sccp_address\":null,\"udhi\":\"0\",\"udh_json\":null,\"parent_id\":null,\"is_dlr\":false,\"message_parts\":null}",
                "{\"msisdn\":null,\"id\":\"1722446896082-12194920127675\",\"message_id\":\"1722446896081-12194920043917\",\"system_id\":\"smppsp\",\"deliver_sm_id\":null,\"deliver_sm_server_id\":null,\"command_status\":0,\"sequence_number\":5,\"source_addr_ton\":1,\"source_addr_npi\":1,\"source_addr\":\"6666\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"5555\",\"esm_class\":3,\"validity_period\":\"60\",\"registered_delivery\":1,\"data_coding\":0,\"sm_default_msg_id\":0,\"short_message\":\"Hello!\",\"delivery_receipt\":null,\"status\":null,\"error_code\":null,\"check_submit_sm_response\":null,\"optional_parameters\":null,\"origin_network_type\":\"SP\",\"origin_protocol\":\"SMPP\",\"origin_network_id\":3,\"dest_network_type\":\"GW\",\"dest_protocol\":\"SS7\",\"dest_network_id\":5,\"routing_id\":1,\"address_nature_msisdn\":null,\"numbering_plan_msisdn\":null,\"remote_dialog_id\":null,\"local_dialog_id\":null,\"sccp_called_party_address_pc\":null,\"sccp_called_party_address_ssn\":null,\"sccp_called_party_address\":null,\"sccp_calling_party_address_pc\":null,\"sccp_calling_party_address_ssn\":null,\"sccp_calling_party_address\":null,\"global_title\":null,\"global_title_indicator\":null,\"translation_type\":null,\"smsc_ssn\":null,\"hlr_ssn\":null,\"msc_ssn\":null,\"map_version\":null,\"is_retry\":false,\"retry_dest_network_id\":\"\",\"retry_number\":null,\"is_last_retry\":false,\"is_network_notify_error\":false,\"due_delay\":0,\"accumulated_time\":0,\"drop_map_sri\":false,\"network_id_to_map_sri\":-1,\"network_id_to_permanent_failure\":-1,\"drop_temp_failure\":false,\"network_id_temp_failure\":-1,\"imsi\":null,\"network_node_number\":null,\"network_node_number_nature_of_address\":null,\"network_node_number_numbering_plan\":null,\"mo_message\":false,\"is_sri_response\":false,\"check_sri_response\":false,\"msg_reference_number\":null,\"total_segment\":null,\"segment_sequence\":null,\"originator_sccp_address\":null,\"udhi\":\"0\",\"udh_json\":null,\"parent_id\":null,\"is_dlr\":true,\"message_parts\":null}"
        );
    }

    private List<String> getDlrs() {
        return List.of(
                "{\"msisdn\":null,\"id\":\"1722615370526-9949307382958\",\"message_id\":\"1722615339493-9918274592992\",\"system_id\":\"smppsp\",\"deliver_sm_id\":\"29bb38a6-8d0d-43a0-9586-7b97f8d2c479\",\"deliver_sm_server_id\":\"1722615339493-9918274592992\",\"command_status\":0,\"sequence_number\":12,\"source_addr_ton\":1,\"source_addr_npi\":1,\"source_addr\":\"5555\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"6666\",\"esm_class\":4,\"validity_period\":\"60\",\"registered_delivery\":1,\"data_coding\":0,\"sm_default_msg_id\":0,\"short_message\":\"id:1722615339493-9918274592992 sub:001 dlvrd:001 submit date:2408021016 done date:2408021016 stat:DELIVRD err:000 text:\",\"delivery_receipt\":\"id:1722615339493-9918274592992 sub:001 dlvrd:001 submit date:2408021016 done date:2408021016 stat:DELIVRD err:000 text:\",\"status\":\"DELIVRD\",\"error_code\":\"000\",\"check_submit_sm_response\":true,\"optional_parameters\":[{\"tag\":30,\"value\":\"29bb38a6-8d0d-43a0-9586-7b97f8d2c479\"},{\"tag\":1063,\"value\":\"1\"}],\"origin_network_type\":\"GW\",\"origin_protocol\":\"SMPP\",\"origin_network_id\":6,\"dest_network_type\":\"SP\",\"dest_protocol\":\"SMPP\",\"dest_network_id\":4,\"routing_id\":0,\"address_nature_msisdn\":null,\"numbering_plan_msisdn\":null,\"remote_dialog_id\":null,\"local_dialog_id\":null,\"sccp_called_party_address_pc\":null,\"sccp_called_party_address_ssn\":null,\"sccp_called_party_address\":null,\"sccp_calling_party_address_pc\":null,\"sccp_calling_party_address_ssn\":null,\"sccp_calling_party_address\":null,\"global_title\":null,\"global_title_indicator\":null,\"translation_type\":null,\"smsc_ssn\":null,\"hlr_ssn\":null,\"msc_ssn\":null,\"map_version\":null,\"is_retry\":false,\"retry_dest_network_id\":null,\"retry_number\":null,\"is_last_retry\":false,\"is_network_notify_error\":false,\"due_delay\":0,\"accumulated_time\":0,\"drop_map_sri\":false,\"network_id_to_map_sri\":0,\"network_id_to_permanent_failure\":0,\"drop_temp_failure\":false,\"network_id_temp_failure\":0,\"imsi\":null,\"network_node_number\":null,\"network_node_number_nature_of_address\":null,\"network_node_number_numbering_plan\":null,\"mo_message\":false,\"is_sri_response\":false,\"check_sri_response\":false,\"msg_reference_number\":null,\"total_segment\":null,\"segment_sequence\":null,\"originator_sccp_address\":null,\"udhi\":null,\"udh_json\":null,\"parent_id\":null,\"is_dlr\":true,\"message_parts\":null}",
                "{\"msisdn\":null,\"id\":\"1722615370524-9949305861760\",\"message_id\":\"1722615338515-9917296608286\",\"system_id\":\"smppsp\",\"deliver_sm_id\":\"b6b67189-867e-45bb-9548-8ef49a304d87\",\"deliver_sm_server_id\":\"1722615338515-9917296608286\",\"command_status\":0,\"sequence_number\":14,\"source_addr_ton\":1,\"source_addr_npi\":1,\"source_addr\":\"5555\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"6666\",\"esm_class\":4,\"validity_period\":\"60\",\"registered_delivery\":1,\"data_coding\":0,\"sm_default_msg_id\":0,\"short_message\":\"id:1722615338515-9917296608286 sub:001 dlvrd:001 submit date:2408021016 done date:2408021016 stat:DELIVRD err:000 text:\",\"delivery_receipt\":\"id:1722615338515-9917296608286 sub:001 dlvrd:001 submit date:2408021016 done date:2408021016 stat:DELIVRD err:000 text:\",\"status\":\"DELIVRD\",\"error_code\":\"000\",\"check_submit_sm_response\":true,\"optional_parameters\":[{\"tag\":30,\"value\":\"b6b67189-867e-45bb-9548-8ef49a304d87\"},{\"tag\":1063,\"value\":\"1\"}],\"origin_network_type\":\"GW\",\"origin_protocol\":\"SMPP\",\"origin_network_id\":6,\"dest_network_type\":\"SP\",\"dest_protocol\":\"HTTP\",\"dest_network_id\":4,\"routing_id\":0,\"address_nature_msisdn\":null,\"numbering_plan_msisdn\":null,\"remote_dialog_id\":null,\"local_dialog_id\":null,\"sccp_called_party_address_pc\":null,\"sccp_called_party_address_ssn\":null,\"sccp_called_party_address\":null,\"sccp_calling_party_address_pc\":null,\"sccp_calling_party_address_ssn\":null,\"sccp_calling_party_address\":null,\"global_title\":null,\"global_title_indicator\":null,\"translation_type\":null,\"smsc_ssn\":null,\"hlr_ssn\":null,\"msc_ssn\":null,\"map_version\":null,\"is_retry\":false,\"retry_dest_network_id\":null,\"retry_number\":null,\"is_last_retry\":false,\"is_network_notify_error\":false,\"due_delay\":0,\"accumulated_time\":0,\"drop_map_sri\":false,\"network_id_to_map_sri\":0,\"network_id_to_permanent_failure\":0,\"drop_temp_failure\":false,\"network_id_temp_failure\":0,\"imsi\":null,\"network_node_number\":null,\"network_node_number_nature_of_address\":null,\"network_node_number_numbering_plan\":null,\"mo_message\":false,\"is_sri_response\":false,\"check_sri_response\":false,\"msg_reference_number\":null,\"total_segment\":null,\"segment_sequence\":null,\"originator_sccp_address\":null,\"udhi\":null,\"udh_json\":null,\"parent_id\":null,\"is_dlr\":true,\"message_parts\":null}",
                "{\"msisdn\":null,\"id\":\"1722615370510-9949291899753\",\"message_id\":\"1722615339819-9918600612420\",\"system_id\":\"smppsp\",\"deliver_sm_id\":\"5ea289e1-8842-49d2-bca3-53ef98f42cae\",\"deliver_sm_server_id\":\"1722615339819-9918600612420\",\"command_status\":0,\"sequence_number\":11,\"source_addr_ton\":1,\"source_addr_npi\":1,\"source_addr\":\"5555\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"6666\",\"esm_class\":4,\"validity_period\":\"60\",\"registered_delivery\":1,\"data_coding\":0,\"sm_default_msg_id\":0,\"short_message\":\"id:1722615339819-9918600612420 sub:001 dlvrd:001 submit date:2408021016 done date:2408021016 stat:DELIVRD err:000 text:\",\"delivery_receipt\":\"id:1722615339819-9918600612420 sub:001 dlvrd:001 submit date:2408021016 done date:2408021016 stat:DELIVRD err:000 text:\",\"status\":\"DELIVRD\",\"error_code\":\"000\",\"check_submit_sm_response\":true,\"optional_parameters\":[{\"tag\":30,\"value\":\"5ea289e1-8842-49d2-bca3-53ef98f42cae\"},{\"tag\":1063,\"value\":\"1\"}],\"origin_network_type\":\"GW\",\"origin_protocol\":\"SMPP\",\"origin_network_id\":6,\"dest_network_type\":\"SP\",\"dest_protocol\":\"SS7\",\"dest_network_id\":4,\"routing_id\":0,\"address_nature_msisdn\":null,\"numbering_plan_msisdn\":null,\"remote_dialog_id\":null,\"local_dialog_id\":null,\"sccp_called_party_address_pc\":null,\"sccp_called_party_address_ssn\":null,\"sccp_called_party_address\":null,\"sccp_calling_party_address_pc\":null,\"sccp_calling_party_address_ssn\":null,\"sccp_calling_party_address\":null,\"global_title\":null,\"global_title_indicator\":null,\"translation_type\":null,\"smsc_ssn\":null,\"hlr_ssn\":null,\"msc_ssn\":null,\"map_version\":null,\"is_retry\":false,\"retry_dest_network_id\":null,\"retry_number\":null,\"is_last_retry\":false,\"is_network_notify_error\":false,\"due_delay\":0,\"accumulated_time\":0,\"drop_map_sri\":false,\"network_id_to_map_sri\":0,\"network_id_to_permanent_failure\":0,\"drop_temp_failure\":false,\"network_id_temp_failure\":0,\"imsi\":null,\"network_node_number\":null,\"network_node_number_nature_of_address\":null,\"network_node_number_numbering_plan\":null,\"mo_message\":false,\"is_sri_response\":false,\"check_sri_response\":false,\"msg_reference_number\":null,\"total_segment\":null,\"segment_sequence\":null,\"originator_sccp_address\":null,\"udhi\":null,\"udh_json\":null,\"parent_id\":null,\"is_dlr\":true,\"message_parts\":null}",
                "{\"msisdn\":null,\"id\":\"1722615370525-9949306734275\",\"message_id\":\"1722615338795-9917576554256\",\"system_id\":\"smppsp\",\"deliver_sm_id\":\"92f23f77-b1f1-4e4a-9365-1343232f2f89\",\"deliver_sm_server_id\":\"1722615338795-9917576554256\",\"command_status\":0,\"sequence_number\":15,\"source_addr_ton\":1,\"source_addr_npi\":1,\"source_addr\":\"5555\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"6666\",\"esm_class\":4,\"validity_period\":\"60\",\"registered_delivery\":1,\"data_coding\":0,\"sm_default_msg_id\":0,\"short_message\":\"id:1722615338795-9917576554256 sub:001 dlvrd:001 submit date:2408021016 done date:2408021016 stat:DELIVRD err:000 text:\",\"delivery_receipt\":\"id:1722615338795-9917576554256 sub:001 dlvrd:001 submit date:2408021016 done date:2408021016 stat:DELIVRD err:000 text:\",\"status\":\"DELIVRD\",\"error_code\":\"000\",\"check_submit_sm_response\":true,\"optional_parameters\":[{\"tag\":30,\"value\":\"92f23f77-b1f1-4e4a-9365-1343232f2f89\"},{\"tag\":1063,\"value\":\"1\"}],\"origin_network_type\":\"GW\",\"origin_protocol\":\"SMPP\",\"origin_network_id\":6,\"dest_network_type\":\"SP\",\"dest_protocol\":\"XHB\",\"dest_network_id\":4,\"routing_id\":0,\"address_nature_msisdn\":null,\"numbering_plan_msisdn\":null,\"remote_dialog_id\":null,\"local_dialog_id\":null,\"sccp_called_party_address_pc\":null,\"sccp_called_party_address_ssn\":null,\"sccp_called_party_address\":null,\"sccp_calling_party_address_pc\":null,\"sccp_calling_party_address_ssn\":null,\"sccp_calling_party_address\":null,\"global_title\":null,\"global_title_indicator\":null,\"translation_type\":null,\"smsc_ssn\":null,\"hlr_ssn\":null,\"msc_ssn\":null,\"map_version\":null,\"is_retry\":false,\"retry_dest_network_id\":null,\"retry_number\":null,\"is_last_retry\":false,\"is_network_notify_error\":false,\"due_delay\":0,\"accumulated_time\":0,\"drop_map_sri\":false,\"network_id_to_map_sri\":0,\"network_id_to_permanent_failure\":0,\"drop_temp_failure\":false,\"network_id_temp_failure\":0,\"imsi\":null,\"network_node_number\":null,\"network_node_number_nature_of_address\":null,\"network_node_number_numbering_plan\":null,\"mo_message\":false,\"is_sri_response\":false,\"check_sri_response\":false,\"msg_reference_number\":null,\"total_segment\":null,\"segment_sequence\":null,\"originator_sccp_address\":null,\"udhi\":null,\"udh_json\":null,\"parent_id\":null,\"is_dlr\":true,\"message_parts\":null}"
        );
    }
}
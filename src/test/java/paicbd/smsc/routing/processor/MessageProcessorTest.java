package paicbd.smsc.routing.processor;

import com.paicbd.smsc.dto.Gateway;
import com.paicbd.smsc.dto.MessageEvent;
import com.paicbd.smsc.dto.Ss7Settings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import paicbd.smsc.routing.util.AppProperties;
import paicbd.smsc.routing.util.RoutingHelper;
import paicbd.smsc.routing.util.StaticMethods;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import redis.clients.jedis.JedisCluster;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageProcessorTest {
    @Mock
    JedisCluster jedisCluster;
    @Mock
    AppProperties appProperties;
    @Mock
    RoutingHelper routingHelper;
    @Mock
    CommonProcessor commonProcessor;
    @Mock
    ConcurrentMap<Integer, Gateway> gateways;
    @Mock
    ConcurrentMap<Integer, Ss7Settings> ss7SettingsMap;

    @InjectMocks
    MessageProcessor messageProcessor;

    @Test
    void init() {
        assertDoesNotThrow(() -> messageProcessor.init());
    }

    @Test
    void fetchAllMessages() {
        when(appProperties.getPreMessageList()).thenReturn("preMessage");
        when(jedisCluster.llen("preMessage")).thenReturn(1L);
        when(appProperties.getPreMessageItemsToProcess()).thenReturn(1);
        when(appProperties.getPreMessageWorkers()).thenReturn(1);
        Assertions.assertDoesNotThrow(() -> messageProcessor.fetchAllMessages());

        when(jedisCluster.lpop(anyString(), anyInt())).thenReturn(getMessages());
        StepVerifier.create(messageProcessor.fetchAllMessages())
                .expectSubscription()
                .expectNextCount(1) // 1 flux in this case
                .verifyComplete();

        // test for null batch
        when(jedisCluster.lpop(anyString(), anyInt())).thenReturn(null);
        StepVerifier.create(messageProcessor.fetchAllMessages())
                .expectSubscription()
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void prepareMessage() {
        String stringEvent = "{\"msisdn\":null,\"id\":\"1722442489770-7788608933795\",\"message_id\":\"1722442489766-7788604799226\",\"system_id\":\"smppsp\",\"deliver_sm_id\":null,\"deliver_sm_server_id\":null,\"command_status\":0,\"sequence_number\":2,\"source_addr_ton\":1,\"source_addr_npi\":1,\"source_addr\":\"6666\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"5555\",\"esm_class\":3,\"validity_period\":\"60\",\"registered_delivery\":1,\"data_coding\":0,\"sm_default_msg_id\":0,\"short_message\":\"Hello!\",\"delivery_receipt\":null,\"status\":null,\"error_code\":null,\"check_submit_sm_response\":null,\"optional_parameters\":null,\"origin_network_type\":\"SP\",\"origin_protocol\":\"SMPP\",\"origin_network_id\":3,\"dest_network_type\":\"GW\",\"dest_protocol\":\"SMPP\",\"dest_network_id\":1,\"routing_id\":1,\"address_nature_msisdn\":null,\"numbering_plan_msisdn\":null,\"remote_dialog_id\":null,\"local_dialog_id\":null,\"sccp_called_party_address_pc\":null,\"sccp_called_party_address_ssn\":null,\"sccp_called_party_address\":null,\"sccp_calling_party_address_pc\":null,\"sccp_calling_party_address_ssn\":null,\"sccp_calling_party_address\":null,\"global_title\":null,\"global_title_indicator\":null,\"translation_type\":null,\"smsc_ssn\":null,\"hlr_ssn\":null,\"msc_ssn\":null,\"map_version\":null,\"is_retry\":false,\"retry_dest_network_id\":\"\",\"retry_number\":null,\"is_last_retry\":false,\"is_network_notify_error\":false,\"due_delay\":0,\"accumulated_time\":0,\"drop_map_sri\":false,\"network_id_to_map_sri\":-1,\"network_id_to_permanent_failure\":-1,\"drop_temp_failure\":false,\"network_id_temp_failure\":-1,\"imsi\":null,\"network_node_number\":null,\"network_node_number_nature_of_address\":null,\"network_node_number_numbering_plan\":null,\"mo_message\":false,\"is_sri_response\":false,\"check_sri_response\":false,\"msg_reference_number\":null,\"total_segment\":null,\"segment_sequence\":null,\"originator_sccp_address\":null,\"udhi\":\"0\",\"udh_json\":null,\"parent_id\":null,\"is_dlr\":false,\"message_parts\":null}";
        MessageEvent event = StaticMethods.stringAsEvent(stringEvent);
        assertDoesNotThrow(() -> messageProcessor.prepareMessage(event));

        assertNotNull(event);
        event.setCheckSubmitSmResponse(true);
        assertDoesNotThrow(() -> messageProcessor.prepareMessage(event));
    }

    @Test
    void preProcessBatchTest() throws NoSuchMethodException {
        when(jedisCluster.lpop(anyString(), anyInt())).thenReturn(Collections.emptyList());
        StepVerifier.create(preProcessBatch())
                .expectSubscription()
                .expectNextCount(0)
                .verifyComplete();

        when(jedisCluster.lpop(anyString(), anyInt())).thenReturn(getLongMessages());
        when(this.appProperties.getPreMessageList()).thenReturn("preMessage");
        when(jedisCluster.lpop(anyString(), anyInt())).thenReturn(getLongMessages());
        when(commonProcessor.getPartsOfMessage(any())).thenReturn(new CommonProcessor.MessagePart(
                2, 152, 1, true
        ));
        StepVerifier.create(preProcessBatch())
                .expectSubscription()
                .expectNextCount(1)
                .verifyComplete();

        when(commonProcessor.getPartsOfMessage(any())).thenReturn(new CommonProcessor.MessagePart(
                2, 152, 1, false
        ));
        StepVerifier.create(preProcessBatch())
                .expectSubscription()
                .expectNextCount(1)
                .verifyComplete();

        // Only 1 part
        when(commonProcessor.getPartsOfMessage(any())).thenReturn(new CommonProcessor.MessagePart(
                1, 152, 1, false
        ));
        StepVerifier.create(preProcessBatch())
                .expectSubscription()
                .expectNextCount(1)
                .verifyComplete();

    }

    private List<String> getMessages() {
        return List.of(
                "{\"msisdn\":null,\"id\":\"1722442489770-7788608933795\",\"message_id\":\"1722442489766-7788604799226\",\"system_id\":\"smppsp\",\"deliver_sm_id\":null,\"deliver_sm_server_id\":null,\"command_status\":0,\"sequence_number\":2,\"source_addr_ton\":1,\"source_addr_npi\":1,\"source_addr\":\"6666\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"5555\",\"esm_class\":3,\"validity_period\":\"60\",\"registered_delivery\":1,\"data_coding\":0,\"sm_default_msg_id\":0,\"short_message\":\"Hello!\",\"delivery_receipt\":null,\"status\":null,\"error_code\":null,\"check_submit_sm_response\":null,\"optional_parameters\":null,\"origin_network_type\":\"SP\",\"origin_protocol\":\"SMPP\",\"origin_network_id\":3,\"dest_network_type\":\"GW\",\"dest_protocol\":\"SMPP\",\"dest_network_id\":1,\"routing_id\":1,\"address_nature_msisdn\":null,\"numbering_plan_msisdn\":null,\"remote_dialog_id\":null,\"local_dialog_id\":null,\"sccp_called_party_address_pc\":null,\"sccp_called_party_address_ssn\":null,\"sccp_called_party_address\":null,\"sccp_calling_party_address_pc\":null,\"sccp_calling_party_address_ssn\":null,\"sccp_calling_party_address\":null,\"global_title\":null,\"global_title_indicator\":null,\"translation_type\":null,\"smsc_ssn\":null,\"hlr_ssn\":null,\"msc_ssn\":null,\"map_version\":null,\"is_retry\":false,\"retry_dest_network_id\":\"\",\"retry_number\":null,\"is_last_retry\":false,\"is_network_notify_error\":false,\"due_delay\":0,\"accumulated_time\":0,\"drop_map_sri\":false,\"network_id_to_map_sri\":-1,\"network_id_to_permanent_failure\":-1,\"drop_temp_failure\":false,\"network_id_temp_failure\":-1,\"imsi\":null,\"network_node_number\":null,\"network_node_number_nature_of_address\":null,\"network_node_number_numbering_plan\":null,\"mo_message\":false,\"is_sri_response\":false,\"check_sri_response\":false,\"msg_reference_number\":null,\"total_segment\":null,\"segment_sequence\":null,\"originator_sccp_address\":null,\"udhi\":\"0\",\"udh_json\":null,\"parent_id\":null,\"is_dlr\":false,\"message_parts\":null}",
                "{\"msisdn\":null,\"id\":\"1722442615535-7914373631079\",\"message_id\":\"1722442615535-7914373573310\",\"system_id\":\"smppsp\",\"deliver_sm_id\":null,\"deliver_sm_server_id\":null,\"command_status\":0,\"sequence_number\":4,\"source_addr_ton\":1,\"source_addr_npi\":1,\"source_addr\":\"6666\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"5555\",\"esm_class\":3,\"validity_period\":\"60\",\"registered_delivery\":1,\"data_coding\":0,\"sm_default_msg_id\":0,\"short_message\":\"Hello!\",\"delivery_receipt\":null,\"status\":null,\"error_code\":null,\"check_submit_sm_response\":null,\"optional_parameters\":null,\"origin_network_type\":\"SP\",\"origin_protocol\":\"SMPP\",\"origin_network_id\":3,\"dest_network_type\":\"GW\",\"dest_protocol\":\"HTTP\",\"dest_network_id\":3,\"routing_id\":1,\"address_nature_msisdn\":null,\"numbering_plan_msisdn\":null,\"remote_dialog_id\":null,\"local_dialog_id\":null,\"sccp_called_party_address_pc\":null,\"sccp_called_party_address_ssn\":null,\"sccp_called_party_address\":null,\"sccp_calling_party_address_pc\":null,\"sccp_calling_party_address_ssn\":null,\"sccp_calling_party_address\":null,\"global_title\":null,\"global_title_indicator\":null,\"translation_type\":null,\"smsc_ssn\":null,\"hlr_ssn\":null,\"msc_ssn\":null,\"map_version\":null,\"is_retry\":false,\"retry_dest_network_id\":\"\",\"retry_number\":null,\"is_last_retry\":false,\"is_network_notify_error\":false,\"due_delay\":0,\"accumulated_time\":0,\"drop_map_sri\":false,\"network_id_to_map_sri\":-1,\"network_id_to_permanent_failure\":-1,\"drop_temp_failure\":false,\"network_id_temp_failure\":-1,\"imsi\":null,\"network_node_number\":null,\"network_node_number_nature_of_address\":null,\"network_node_number_numbering_plan\":null,\"mo_message\":false,\"is_sri_response\":false,\"check_sri_response\":false,\"msg_reference_number\":null,\"total_segment\":null,\"segment_sequence\":null,\"originator_sccp_address\":null,\"udhi\":\"0\",\"udh_json\":null,\"parent_id\":null,\"is_dlr\":false,\"message_parts\":null}",
                "{\"msisdn\":null,\"id\":\"1722446896082-12194920127675\",\"message_id\":\"1722446896081-12194920043917\",\"system_id\":\"smppsp\",\"deliver_sm_id\":null,\"deliver_sm_server_id\":null,\"command_status\":0,\"sequence_number\":5,\"source_addr_ton\":1,\"source_addr_npi\":1,\"source_addr\":\"6666\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"5555\",\"esm_class\":3,\"validity_period\":\"60\",\"registered_delivery\":1,\"data_coding\":0,\"sm_default_msg_id\":0,\"short_message\":\"Hello!\",\"delivery_receipt\":null,\"status\":null,\"error_code\":null,\"check_submit_sm_response\":null,\"optional_parameters\":null,\"origin_network_type\":\"SP\",\"origin_protocol\":\"SMPP\",\"origin_network_id\":3,\"dest_network_type\":\"GW\",\"dest_protocol\":\"SS7\",\"dest_network_id\":5,\"routing_id\":1,\"address_nature_msisdn\":null,\"numbering_plan_msisdn\":null,\"remote_dialog_id\":null,\"local_dialog_id\":null,\"sccp_called_party_address_pc\":null,\"sccp_called_party_address_ssn\":null,\"sccp_called_party_address\":null,\"sccp_calling_party_address_pc\":null,\"sccp_calling_party_address_ssn\":null,\"sccp_calling_party_address\":null,\"global_title\":null,\"global_title_indicator\":null,\"translation_type\":null,\"smsc_ssn\":null,\"hlr_ssn\":null,\"msc_ssn\":null,\"map_version\":null,\"is_retry\":false,\"retry_dest_network_id\":\"\",\"retry_number\":null,\"is_last_retry\":false,\"is_network_notify_error\":false,\"due_delay\":0,\"accumulated_time\":0,\"drop_map_sri\":false,\"network_id_to_map_sri\":-1,\"network_id_to_permanent_failure\":-1,\"drop_temp_failure\":false,\"network_id_temp_failure\":-1,\"imsi\":null,\"network_node_number\":null,\"network_node_number_nature_of_address\":null,\"network_node_number_numbering_plan\":null,\"mo_message\":false,\"is_sri_response\":false,\"check_sri_response\":false,\"msg_reference_number\":null,\"total_segment\":null,\"segment_sequence\":null,\"originator_sccp_address\":null,\"udhi\":\"0\",\"udh_json\":null,\"parent_id\":null,\"is_dlr\":false,\"message_parts\":null}",
                "{\"msisdn\":null,\"id\":\"1722446896082-12194920127675\",\"message_id\":\"1722446896081-12194920043917\",\"system_id\":\"smppsp\",\"deliver_sm_id\":null,\"deliver_sm_server_id\":null,\"command_status\":0,\"sequence_number\":5,\"source_addr_ton\":1,\"source_addr_npi\":1,\"source_addr\":\"6666\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"5555\",\"esm_class\":3,\"validity_period\":\"60\",\"registered_delivery\":1,\"data_coding\":0,\"sm_default_msg_id\":0,\"short_message\":\"Hello!\",\"delivery_receipt\":null,\"status\":null,\"error_code\":null,\"check_submit_sm_response\":null,\"optional_parameters\":null,\"origin_network_type\":\"SP\",\"origin_protocol\":\"SMPP\",\"origin_network_id\":3,\"dest_network_type\":\"GW\",\"dest_protocol\":\"SS7\",\"dest_network_id\":5,\"routing_id\":1,\"address_nature_msisdn\":null,\"numbering_plan_msisdn\":null,\"remote_dialog_id\":null,\"local_dialog_id\":null,\"sccp_called_party_address_pc\":null,\"sccp_called_party_address_ssn\":null,\"sccp_called_party_address\":null,\"sccp_calling_party_address_pc\":null,\"sccp_calling_party_address_ssn\":null,\"sccp_calling_party_address\":null,\"global_title\":null,\"global_title_indicator\":null,\"translation_type\":null,\"smsc_ssn\":null,\"hlr_ssn\":null,\"msc_ssn\":null,\"map_version\":null,\"is_retry\":false,\"retry_dest_network_id\":\"\",\"retry_number\":null,\"is_last_retry\":false,\"is_network_notify_error\":false,\"due_delay\":0,\"accumulated_time\":0,\"drop_map_sri\":false,\"network_id_to_map_sri\":-1,\"network_id_to_permanent_failure\":-1,\"drop_temp_failure\":false,\"network_id_temp_failure\":-1,\"imsi\":null,\"network_node_number\":null,\"network_node_number_nature_of_address\":null,\"network_node_number_numbering_plan\":null,\"mo_message\":false,\"is_sri_response\":false,\"check_sri_response\":false,\"msg_reference_number\":null,\"total_segment\":null,\"segment_sequence\":null,\"originator_sccp_address\":null,\"udhi\":\"0\",\"udh_json\":null,\"parent_id\":null,\"is_dlr\":true,\"message_parts\":null}"
        );
    }

    private List<String> getLongMessages() {
        return List.of(
                "{\"msisdn\":null,\"id\":\"1722442489770-7788608933795\",\"message_id\":\"1722442489766-7788604799226\",\"system_id\":\"smppsp\",\"deliver_sm_id\":null,\"deliver_sm_server_id\":null,\"command_status\":0,\"sequence_number\":2,\"source_addr_ton\":1,\"source_addr_npi\":1,\"source_addr\":\"6666\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"5555\",\"esm_class\":3,\"validity_period\":\"60\",\"registered_delivery\":1,\"data_coding\":0,\"sm_default_msg_id\":0,\"short_message\":\"Use the SMSC simulator service from smpp.org for free to test SMS submission by your application. Your application can connect to the SMSC simulator, submit SMS, and receive message IDs for each message submitted. SMS delivery is simulated, with no messages delivered to mobiles, and delivery receipts indicating successful delivery are returned by the SMSC simulator.\",\"delivery_receipt\":null,\"status\":null,\"error_code\":null,\"check_submit_sm_response\":null,\"optional_parameters\":null,\"origin_network_type\":\"SP\",\"origin_protocol\":\"HTTP\",\"origin_network_id\":3,\"dest_network_type\":\"GW\",\"dest_protocol\":\"SMPP\",\"dest_network_id\":1,\"routing_id\":1,\"address_nature_msisdn\":null,\"numbering_plan_msisdn\":null,\"remote_dialog_id\":null,\"local_dialog_id\":null,\"sccp_called_party_address_pc\":null,\"sccp_called_party_address_ssn\":null,\"sccp_called_party_address\":null,\"sccp_calling_party_address_pc\":null,\"sccp_calling_party_address_ssn\":null,\"sccp_calling_party_address\":null,\"global_title\":null,\"global_title_indicator\":null,\"translation_type\":null,\"smsc_ssn\":null,\"hlr_ssn\":null,\"msc_ssn\":null,\"map_version\":null,\"is_retry\":false,\"retry_dest_network_id\":\"\",\"retry_number\":null,\"is_last_retry\":false,\"is_network_notify_error\":false,\"due_delay\":0,\"accumulated_time\":0,\"drop_map_sri\":false,\"network_id_to_map_sri\":-1,\"network_id_to_permanent_failure\":-1,\"drop_temp_failure\":false,\"network_id_temp_failure\":-1,\"imsi\":null,\"network_node_number\":null,\"network_node_number_nature_of_address\":null,\"network_node_number_numbering_plan\":null,\"mo_message\":false,\"is_sri_response\":false,\"check_sri_response\":false,\"msg_reference_number\":null,\"total_segment\":null,\"segment_sequence\":null,\"originator_sccp_address\":null,\"udhi\":\"0\",\"udh_json\":null,\"parent_id\":null,\"is_dlr\":false,\"message_parts\":null}",
                "{\"msisdn\":null,\"id\":\"1722442615535-7914373631079\",\"message_id\":\"1722442615535-7914373573310\",\"system_id\":\"smppsp\",\"deliver_sm_id\":null,\"deliver_sm_server_id\":null,\"command_status\":0,\"sequence_number\":4,\"source_addr_ton\":1,\"source_addr_npi\":1,\"source_addr\":\"6666\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"5555\",\"esm_class\":3,\"validity_period\":\"60\",\"registered_delivery\":1,\"data_coding\":0,\"sm_default_msg_id\":0,\"short_message\":\"Use the SMSC simulator service from smpp.org for free to test SMS submission by your application. Your application can connect to the SMSC simulator, submit SMS, and receive message IDs for each message submitted. SMS delivery is simulated, with no messages delivered to mobiles, and delivery receipts indicating successful delivery are returned by the SMSC simulator.\",\"delivery_receipt\":null,\"status\":null,\"error_code\":null,\"check_submit_sm_response\":null,\"optional_parameters\":null,\"origin_network_type\":\"SP\",\"origin_protocol\":\"HTTP\",\"origin_network_id\":3,\"dest_network_type\":\"GW\",\"dest_protocol\":\"HTTP\",\"dest_network_id\":3,\"routing_id\":1,\"address_nature_msisdn\":null,\"numbering_plan_msisdn\":null,\"remote_dialog_id\":null,\"local_dialog_id\":null,\"sccp_called_party_address_pc\":null,\"sccp_called_party_address_ssn\":null,\"sccp_called_party_address\":null,\"sccp_calling_party_address_pc\":null,\"sccp_calling_party_address_ssn\":null,\"sccp_calling_party_address\":null,\"global_title\":null,\"global_title_indicator\":null,\"translation_type\":null,\"smsc_ssn\":null,\"hlr_ssn\":null,\"msc_ssn\":null,\"map_version\":null,\"is_retry\":false,\"retry_dest_network_id\":\"\",\"retry_number\":null,\"is_last_retry\":false,\"is_network_notify_error\":false,\"due_delay\":0,\"accumulated_time\":0,\"drop_map_sri\":false,\"network_id_to_map_sri\":-1,\"network_id_to_permanent_failure\":-1,\"drop_temp_failure\":false,\"network_id_temp_failure\":-1,\"imsi\":null,\"network_node_number\":null,\"network_node_number_nature_of_address\":null,\"network_node_number_numbering_plan\":null,\"mo_message\":false,\"is_sri_response\":false,\"check_sri_response\":false,\"msg_reference_number\":null,\"total_segment\":null,\"segment_sequence\":null,\"originator_sccp_address\":null,\"udhi\":\"0\",\"udh_json\":null,\"parent_id\":null,\"is_dlr\":false,\"message_parts\":null}",
                "{\"msisdn\":null,\"id\":\"1722446896082-12194920127675\",\"message_id\":\"1722446896081-12194920043917\",\"system_id\":\"smppsp\",\"deliver_sm_id\":null,\"deliver_sm_server_id\":null,\"command_status\":0,\"sequence_number\":5,\"source_addr_ton\":1,\"source_addr_npi\":1,\"source_addr\":\"6666\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"5555\",\"esm_class\":3,\"validity_period\":\"60\",\"registered_delivery\":1,\"data_coding\":0,\"sm_default_msg_id\":0,\"short_message\":\"Use the SMSC simulator service from smpp.org for free to test SMS submission by your application. Your application can connect to the SMSC simulator, submit SMS, and receive message IDs for each message submitted. SMS delivery is simulated, with no messages delivered to mobiles, and delivery receipts indicating successful delivery are returned by the SMSC simulator.\",\"delivery_receipt\":null,\"status\":null,\"error_code\":null,\"check_submit_sm_response\":null,\"optional_parameters\":null,\"origin_network_type\":\"SP\",\"origin_protocol\":\"HTTP\",\"origin_network_id\":3,\"dest_network_type\":\"GW\",\"dest_protocol\":\"SS7\",\"dest_network_id\":5,\"routing_id\":1,\"address_nature_msisdn\":null,\"numbering_plan_msisdn\":null,\"remote_dialog_id\":null,\"local_dialog_id\":null,\"sccp_called_party_address_pc\":null,\"sccp_called_party_address_ssn\":null,\"sccp_called_party_address\":null,\"sccp_calling_party_address_pc\":null,\"sccp_calling_party_address_ssn\":null,\"sccp_calling_party_address\":null,\"global_title\":null,\"global_title_indicator\":null,\"translation_type\":null,\"smsc_ssn\":null,\"hlr_ssn\":null,\"msc_ssn\":null,\"map_version\":null,\"is_retry\":false,\"retry_dest_network_id\":\"\",\"retry_number\":null,\"is_last_retry\":false,\"is_network_notify_error\":false,\"due_delay\":0,\"accumulated_time\":0,\"drop_map_sri\":false,\"network_id_to_map_sri\":-1,\"network_id_to_permanent_failure\":-1,\"drop_temp_failure\":false,\"network_id_temp_failure\":-1,\"imsi\":null,\"network_node_number\":null,\"network_node_number_nature_of_address\":null,\"network_node_number_numbering_plan\":null,\"mo_message\":false,\"is_sri_response\":false,\"check_sri_response\":false,\"msg_reference_number\":null,\"total_segment\":null,\"segment_sequence\":null,\"originator_sccp_address\":null,\"udhi\":\"0\",\"udh_json\":null,\"parent_id\":null,\"is_dlr\":false,\"message_parts\":null}"
        );
    }

    @SuppressWarnings("unchecked")
    private Flux<List<MessageEvent>> preProcessBatch() throws NoSuchMethodException {
        Class<?> clazz = MessageProcessor.class;
        Method method = clazz.getDeclaredMethod("preProcessBatch", int.class);
        Objects.requireNonNull(method);
        method.setAccessible(true);
        try {
            var result = method.invoke(messageProcessor, 1);
            if (result instanceof Flux<?> response) {
                return (Flux<List<MessageEvent>>) response;
            }
            return Flux.empty();
        } catch (Exception e) {
            return Flux.empty();
        }
    }
}
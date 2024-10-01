package paicbd.smsc.routing.processor;

import com.paicbd.smsc.dto.MessageEvent;
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

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliverProcessorTest {
    @Mock
    JedisCluster jedisCluster;
    @Mock
    AppProperties appProperties;
    @Mock
    RoutingHelper routingHelper;
    @Mock
    CommonProcessor commonProcessor;

    @InjectMocks
    DeliverProcessor deliverProcessor;

    @Test
    void init() {
        deliverProcessor = new DeliverProcessor(jedisCluster, appProperties, routingHelper, commonProcessor);
        deliverProcessor.init();
        Flux<Long> flux = Flux.interval(Duration.ofSeconds(1))
                .doOnNext(val -> routingHelper.processEventsFlux(deliverProcessor.fetchAllMessages()));

        StepVerifier.withVirtualTime(() -> flux)
                .thenAwait(Duration.ofSeconds(1))
                .expectNextCount(1)
                .thenCancel()
                .verify();
    }

    @Test
    void fetchAllMessages() {
        when(appProperties.getPreDeliverList()).thenReturn("preDeliver");
        when(jedisCluster.llen("preDeliver")).thenReturn(1L);
        when(appProperties.getPreDeliverItemsToProcess()).thenReturn(2);
        when(appProperties.getPreDeliverWorkers()).thenReturn(1);
        assertDoesNotThrow(() -> deliverProcessor.fetchAllMessages());

        when(jedisCluster.lpop(anyString(), anyInt())).thenReturn(getDeliver());
        StepVerifier.create(deliverProcessor.fetchAllMessages())
                .expectSubscription()
                .expectNextCount(1) // 1 flux in this case
                .verifyComplete();

        // test for empty batch
        when(jedisCluster.llen("preDeliver")).thenReturn(0L);
        StepVerifier.create(deliverProcessor.fetchAllMessages())
                .expectSubscription()
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void fetchAllMessages_lpopNull() {
        when(appProperties.getPreDeliverList()).thenReturn("preDeliver");
        when(jedisCluster.llen("preDeliver")).thenReturn(1L);
        when(appProperties.getPreDeliverItemsToProcess()).thenReturn(2);
        when(appProperties.getPreDeliverWorkers()).thenReturn(1);
        assertDoesNotThrow(() -> deliverProcessor.fetchAllMessages());
        when(jedisCluster.lpop(anyString(), anyInt())).thenReturn(null);
        StepVerifier.create(deliverProcessor.fetchAllMessages())
                .expectSubscription()
                .expectNextCount(0)
                .verifyComplete();

        verify(jedisCluster, atLeastOnce()).lpop(anyString(), anyInt());
        when(jedisCluster.lpop(anyString(), anyInt())).thenReturn(List.of());
        StepVerifier.create(deliverProcessor.fetchAllMessages())
                .expectSubscription()
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void prepareMessage() {
        String deliverString = "{\"msisdn\":null,\"id\":\"1722615370525-9949306734275\",\"message_id\":\"1722615338795-9917576554256\",\"system_id\":\"smppsp\",\"deliver_sm_id\":\"92f23f77-b1f1-4e4a-9365-1343232f2f89\",\"deliver_sm_server_id\":\"1722615338795-9917576554256\",\"command_status\":0,\"sequence_number\":15,\"source_addr_ton\":1,\"source_addr_npi\":1,\"source_addr\":\"5555\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"6666\",\"esm_class\":4,\"validity_period\":\"60\",\"registered_delivery\":1,\"data_coding\":0,\"sm_default_msg_id\":0,\"short_message\":\"id:1722615338795-9917576554256 sub:001 dlvrd:001 submit date:2408021016 done date:2408021016 stat:DELIVRD err:000 text:\",\"delivery_receipt\":\"id:1722615338795-9917576554256 sub:001 dlvrd:001 submit date:2408021016 done date:2408021016 stat:DELIVRD err:000 text:\",\"status\":\"DELIVRD\",\"error_code\":\"000\",\"check_submit_sm_response\":true,\"optional_parameters\":[{\"tag\":30,\"value\":\"92f23f77-b1f1-4e4a-9365-1343232f2f89\"},{\"tag\":1063,\"value\":\"1\"}],\"origin_network_type\":\"GW\",\"origin_protocol\":\"SMPP\",\"origin_network_id\":6,\"dest_network_type\":\"SP\",\"dest_protocol\":\"SMPP\",\"dest_network_id\":4,\"routing_id\":0,\"address_nature_msisdn\":null,\"numbering_plan_msisdn\":null,\"remote_dialog_id\":null,\"local_dialog_id\":null,\"sccp_called_party_address_pc\":null,\"sccp_called_party_address_ssn\":null,\"sccp_called_party_address\":null,\"sccp_calling_party_address_pc\":null,\"sccp_calling_party_address_ssn\":null,\"sccp_calling_party_address\":null,\"global_title\":null,\"global_title_indicator\":null,\"translation_type\":null,\"smsc_ssn\":null,\"hlr_ssn\":null,\"msc_ssn\":null,\"map_version\":null,\"is_retry\":false,\"retry_dest_network_id\":null,\"retry_number\":null,\"is_last_retry\":false,\"is_network_notify_error\":false,\"due_delay\":0,\"accumulated_time\":0,\"drop_map_sri\":false,\"network_id_to_map_sri\":0,\"network_id_to_permanent_failure\":0,\"drop_temp_failure\":false,\"network_id_temp_failure\":0,\"imsi\":null,\"network_node_number\":null,\"network_node_number_nature_of_address\":null,\"network_node_number_numbering_plan\":null,\"mo_message\":false,\"is_sri_response\":false,\"check_sri_response\":false,\"msg_reference_number\":null,\"total_segment\":null,\"segment_sequence\":null,\"originator_sccp_address\":null,\"udhi\":null,\"udh_json\":null,\"parent_id\":null,\"is_dlr\":true,\"message_parts\":null}";
        MessageEvent event = StaticMethods.stringAsEvent(deliverString);
        assertDoesNotThrow(() -> deliverProcessor.prepareMessage(event));

        assertNotNull(event);
        event.setCheckSubmitSmResponse(false);
        assertDoesNotThrow(() -> deliverProcessor.prepareMessage(event));
    }

    private List<String> getDeliver() {
        return List.of(
                "{\"msisdn\":null,\"id\":\"1722615370524-9949305861760\",\"message_id\":\"1722615338515-9917296608286\",\"system_id\":\"smppsp\",\"deliver_sm_id\":\"b6b67189-867e-45bb-9548-8ef49a304d87\",\"deliver_sm_server_id\":\"1722615338515-9917296608286\",\"command_status\":0,\"sequence_number\":14,\"source_addr_ton\":1,\"source_addr_npi\":1,\"source_addr\":\"5555\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"6666\",\"esm_class\":4,\"validity_period\":\"60\",\"registered_delivery\":1,\"data_coding\":0,\"sm_default_msg_id\":0,\"short_message\":\"id:1722615338515-9917296608286 sub:001 dlvrd:001 submit date:2408021016 done date:2408021016 stat:DELIVRD err:000 text:\",\"delivery_receipt\":\"id:1722615338515-9917296608286 sub:001 dlvrd:001 submit date:2408021016 done date:2408021016 stat:DELIVRD err:000 text:\",\"status\":\"DELIVRD\",\"error_code\":\"000\",\"check_submit_sm_response\":true,\"optional_parameters\":[{\"tag\":30,\"value\":\"b6b67189-867e-45bb-9548-8ef49a304d87\"},{\"tag\":1063,\"value\":\"1\"}],\"origin_network_type\":\"GW\",\"origin_protocol\":\"SMPP\",\"origin_network_id\":6,\"dest_network_type\":\"SP\",\"dest_protocol\":\"SMPP\",\"dest_network_id\":4,\"routing_id\":0,\"address_nature_msisdn\":null,\"numbering_plan_msisdn\":null,\"remote_dialog_id\":null,\"local_dialog_id\":null,\"sccp_called_party_address_pc\":null,\"sccp_called_party_address_ssn\":null,\"sccp_called_party_address\":null,\"sccp_calling_party_address_pc\":null,\"sccp_calling_party_address_ssn\":null,\"sccp_calling_party_address\":null,\"global_title\":null,\"global_title_indicator\":null,\"translation_type\":null,\"smsc_ssn\":null,\"hlr_ssn\":null,\"msc_ssn\":null,\"map_version\":null,\"is_retry\":false,\"retry_dest_network_id\":null,\"retry_number\":null,\"is_last_retry\":false,\"is_network_notify_error\":false,\"due_delay\":0,\"accumulated_time\":0,\"drop_map_sri\":false,\"network_id_to_map_sri\":0,\"network_id_to_permanent_failure\":0,\"drop_temp_failure\":false,\"network_id_temp_failure\":0,\"imsi\":null,\"network_node_number\":null,\"network_node_number_nature_of_address\":null,\"network_node_number_numbering_plan\":null,\"mo_message\":false,\"is_sri_response\":false,\"check_sri_response\":false,\"msg_reference_number\":null,\"total_segment\":null,\"segment_sequence\":null,\"originator_sccp_address\":null,\"udhi\":null,\"udh_json\":null,\"parent_id\":null,\"is_dlr\":true,\"message_parts\":null}",
                "{\"msisdn\":null,\"id\":\"1722615370526-9949307382958\",\"message_id\":\"1722615339493-9918274592992\",\"system_id\":\"smppsp\",\"deliver_sm_id\":\"29bb38a6-8d0d-43a0-9586-7b97f8d2c479\",\"deliver_sm_server_id\":\"1722615339493-9918274592992\",\"command_status\":0,\"sequence_number\":12,\"source_addr_ton\":1,\"source_addr_npi\":1,\"source_addr\":\"5555\",\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"destination_addr\":\"6666\",\"esm_class\":4,\"validity_period\":\"60\",\"registered_delivery\":1,\"data_coding\":0,\"sm_default_msg_id\":0,\"short_message\":\"id:1722615339493-9918274592992 sub:001 dlvrd:001 submit date:2408021016 done date:2408021016 stat:DELIVRD err:000 text:\",\"delivery_receipt\":\"id:1722615339493-9918274592992 sub:001 dlvrd:001 submit date:2408021016 done date:2408021016 stat:DELIVRD err:000 text:\",\"status\":\"DELIVRD\",\"error_code\":\"000\",\"check_submit_sm_response\":true,\"optional_parameters\":[{\"tag\":30,\"value\":\"29bb38a6-8d0d-43a0-9586-7b97f8d2c479\"},{\"tag\":1063,\"value\":\"1\"}],\"origin_network_type\":\"GW\",\"origin_protocol\":\"SMPP\",\"origin_network_id\":6,\"dest_network_type\":\"SP\",\"dest_protocol\":\"SMPP\",\"dest_network_id\":4,\"routing_id\":0,\"address_nature_msisdn\":null,\"numbering_plan_msisdn\":null,\"remote_dialog_id\":null,\"local_dialog_id\":null,\"sccp_called_party_address_pc\":null,\"sccp_called_party_address_ssn\":null,\"sccp_called_party_address\":null,\"sccp_calling_party_address_pc\":null,\"sccp_calling_party_address_ssn\":null,\"sccp_calling_party_address\":null,\"global_title\":null,\"global_title_indicator\":null,\"translation_type\":null,\"smsc_ssn\":null,\"hlr_ssn\":null,\"msc_ssn\":null,\"map_version\":null,\"is_retry\":false,\"retry_dest_network_id\":null,\"retry_number\":null,\"is_last_retry\":false,\"is_network_notify_error\":false,\"due_delay\":0,\"accumulated_time\":0,\"drop_map_sri\":false,\"network_id_to_map_sri\":0,\"network_id_to_permanent_failure\":0,\"drop_temp_failure\":false,\"network_id_temp_failure\":0,\"imsi\":null,\"network_node_number\":null,\"network_node_number_nature_of_address\":null,\"network_node_number_numbering_plan\":null,\"mo_message\":false,\"is_sri_response\":false,\"check_sri_response\":false,\"msg_reference_number\":null,\"total_segment\":null,\"segment_sequence\":null,\"originator_sccp_address\":null,\"udhi\":null,\"udh_json\":null,\"parent_id\":null,\"is_dlr\":true,\"message_parts\":null}"
        );
    }
}
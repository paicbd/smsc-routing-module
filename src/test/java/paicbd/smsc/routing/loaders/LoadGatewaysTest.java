package paicbd.smsc.routing.loaders;

import com.paicbd.smsc.dto.Gateway;
import com.paicbd.smsc.exception.RTException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import paicbd.smsc.routing.util.AppProperties;
import redis.clients.jedis.JedisCluster;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoadGatewaysTest {
    @Mock
    JedisCluster jedisCluster;
    @Mock
    AppProperties appProperties;
    @Mock
    ConcurrentMap<Integer, Gateway> gateways;
    @Mock
    ConcurrentMap<String, Integer> gatewaysSystemId;

    @InjectMocks
    LoadGateways loadGateways;

    @Test
    void init() {
        assertDoesNotThrow(() -> loadGateways.init());
    }

    @Test
    void loadGateways() {
        when(this.appProperties.getGatewaysHash()).thenReturn("gateways");
        when(this.jedisCluster.hgetAll("gateways")).thenReturn(null);
        assertDoesNotThrow(() -> loadGateways.loadGateways());

        this.gateways = new ConcurrentHashMap<>();
        this.loadGateways = new LoadGateways(jedisCluster, appProperties, gateways, gatewaysSystemId);
        when(jedisCluster.hgetAll("gateways")).thenReturn(Map.of(
                "smppgw", "{\"network_id\":1,\"name\":\"smppgw\",\"system_id\":\"smppgw\",\"password\":\"1234\",\"ip\":\"192.168.100.20\",\"port\":7001,\"bind_type\":\"TRANSCEIVER\",\"system_type\":\"\",\"interface_version\":\"IF_50\",\"sessions_number\":10,\"address_ton\":0,\"address_npi\":0,\"address_range\":null,\"tps\":10,\"status\":\"STOPPED\",\"enabled\":0,\"enquire_link_period\":30000,\"enquire_link_timeout\":0,\"request_dlr\":true,\"no_retry_error_code\":\"\",\"retry_alternate_destination_error_code\":\"\",\"bind_timeout\":5000,\"bind_retry_period\":10000,\"pdu_timeout\":5000,\"pdu_degree\":1,\"thread_pool_size\":100,\"mno_id\":1,\"tlv_message_receipt_id\":false,\"message_id_decimal_format\":false,\"active_sessions_numbers\":0,\"protocol\":\"SMPP\",\"auto_retry_error_code\":\"\",\"encoding_iso88591\":3,\"encoding_gsm7\":0,\"encoding_ucs2\":2,\"split_message\":false,\"split_smpp_type\":\"TLV\"}"
        ));
        assertDoesNotThrow(() -> loadGateways.loadGateways());

        when(jedisCluster.hgetAll("gateways")).thenReturn(Map.of(
                "smppgw", "{mppgw\",\"system_id\":\"smppgw\",\"password\":\"1234\",\"ip\":\"192.168.100.20\",\"port\":7001,\"bind_type\":\"TRANSCEIVER\",\"system_type\":\"\",\"interface_version\":\"IF_50\",\"sessions_number\":10,\"address_ton\":0,\"address_npi\":0,\"address_range\":null,\"tps\":10,\"status\":\"STOPPED\",\"enabled\":0,\"enquire_link_period\":30000,\"enquire_link_timeout\":0,\"request_dlr\":true,\"no_retry_error_code\":\"\",\"retry_alternate_destination_error_code\":\"\",\"bind_timeout\":5000,\"bind_retry_period\":10000,\"pdu_timeout\":5000,\"pdu_degree\":1,\"thread_pool_size\":100,\"mno_id\":1,\"tlv_message_receipt_id\":false,\"message_id_decimal_format\":false,\"active_sessions_numbers\":0,\"protocol\":\"SMPP\",\"auto_retry_error_code\":\"\",\"encoding_iso88591\":3,\"encoding_gsm7\":0,\"encoding_ucs2\":2,\"split_message\":false,\"split_smpp_type\":\"TLV\"}"
        ));
        assertDoesNotThrow(() -> loadGateways.loadGateways());
    }

    @Test
    void updateGateway() {
        when(appProperties.getGatewaysHash()).thenReturn("gateways");
        when(jedisCluster.hget("gateways", "smppgw")).thenReturn(null);
        assertDoesNotThrow(() -> loadGateways.updateGateway("smppgw"));

        when(jedisCluster.hget("gateways", "smppgw")).thenReturn("{\"network_id\":1,\"name\":\"smppgw\",\"system_id\":\"smppgw\",\"password\":\"1234\",\"ip\":\"192.168.100.20\",\"port\":7001,\"bind_type\":\"TRANSCEIVER\",\"system_type\":\"\",\"interface_version\":\"IF_50\",\"sessions_number\":10,\"address_ton\":0,\"address_npi\":0,\"address_range\":null,\"tps\":10,\"status\":\"STOPPED\",\"enabled\":0,\"enquire_link_period\":30000,\"enquire_link_timeout\":0,\"request_dlr\":true,\"no_retry_error_code\":\"\",\"retry_alternate_destination_error_code\":\"\",\"bind_timeout\":5000,\"bind_retry_period\":10000,\"pdu_timeout\":5000,\"pdu_degree\":1,\"thread_pool_size\":100,\"mno_id\":1,\"tlv_message_receipt_id\":false,\"message_id_decimal_format\":false,\"active_sessions_numbers\":0,\"protocol\":\"SMPP\",\"auto_retry_error_code\":\"\",\"encoding_iso88591\":3,\"encoding_gsm7\":0,\"encoding_ucs2\":2,\"split_message\":false,\"split_smpp_type\":\"TLV\"}");
        assertDoesNotThrow(() -> loadGateways.updateGateway("smppgw"));

        when(jedisCluster.hget("gateways", "smppgw")).thenReturn(",,,\"name\":\"smppgw\",\"system_id\":\"smppgw\",\"password\":\"1234\",\"ip\":\"192.168.100.20\",\"port\":7001,\"bind_type\":\"TRANSCEIVER\",\"system_type\":\"\",\"interface_version\":\"IF_50\",\"sessions_number\":10,\"address_ton\":0,\"address_npi\":0,\"address_range\":null,\"tps\":10,\"status\":\"STOPPED\",\"enabled\":0,\"enquire_link_period\":30000,\"enquire_link_timeout\":0,\"request_dlr\":true,\"no_retry_error_code\":\"\",\"retry_alternate_destination_error_code\":\"\",\"bind_timeout\":5000,\"bind_retry_period\":10000,\"pdu_timeout\":5000,\"pdu_degree\":1,\"thread_pool_size\":100,\"mno_id\":1,\"tlv_message_receipt_id\":false,\"message_id_decimal_format\":false,\"active_sessions_numbers\":0,\"protocol\":\"SMPP\",\"auto_retry_error_code\":\"\",\"encoding_iso88591\":3,\"encoding_gsm7\":0,\"encoding_ucs2\":2,\"split_message\":false,\"split_smpp_type\":\"TLV\"}");
        assertThrows(RTException.class, () -> loadGateways.updateGateway("smppgw"));

        assertDoesNotThrow(() -> loadGateways.updateGateway(null));
    }

    @Test
    void deleteGateway() {
        assertDoesNotThrow(() -> loadGateways.deleteGateway(null));
        assertDoesNotThrow(() -> loadGateways.deleteGateway("smppgw"));
    }
}
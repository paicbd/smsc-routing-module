package paicbd.smsc.routing.loaders;

import com.paicbd.smsc.dto.ServiceProvider;
import com.paicbd.smsc.exception.RTException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import paicbd.smsc.routing.util.AppProperties;
import redis.clients.jedis.JedisCluster;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoadServiceProvidersTest {
    @Mock
    JedisCluster jedisCluster;
    @Mock
    AppProperties appProperties;
    @Mock
    ConcurrentMap<Integer, ServiceProvider> serviceProviders;
    @Mock
    ConcurrentMap<String, Integer> serviceProvidersSystemId;

    @InjectMocks
    LoadServiceProviders loadServiceProviders;

    @BeforeEach
    void setUp() {
        this.loadServiceProviders = new LoadServiceProviders(jedisCluster, appProperties, serviceProvidersSystemId, serviceProviders);
    }

    @Test
    void init() {
        assertDoesNotThrow(() -> loadServiceProviders.init());
    }

    @Test
    void getSystemIdByNetworkId() {
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setSystemId("httpsp");
        when(serviceProviders.get(4)).thenReturn(serviceProvider);
        assertEquals("httpsp", loadServiceProviders.getSystemIdByNetworkId(4));

        when(serviceProviders.get(4)).thenReturn(null);
        assertNull(loadServiceProviders.getSystemIdByNetworkId(4));
    }

    @Test
    void getNetworkIdBySystemId() {
        when(serviceProvidersSystemId.getOrDefault("httpsp", 0)).thenReturn(4);
        assertEquals(4, loadServiceProviders.getNetworkIdBySystemId("httpsp"));

        when(serviceProvidersSystemId.getOrDefault("httpsp", 0)).thenReturn(0);
        assertEquals(0, loadServiceProviders.getNetworkIdBySystemId("httpsp"));
    }

    @Test
    void loadServiceProviders() {
        when(appProperties.getServiceProvidersHash()).thenReturn("service_providers");
        when(jedisCluster.hgetAll(appProperties.getServiceProvidersHash())).thenReturn(null);
        assertDoesNotThrow(() -> loadServiceProviders.loadServiceProviders());

        when(jedisCluster.hgetAll("service_providers")).thenReturn(Map.of(
            "httpsp", "{\"name\":\"httpsp\",\"password\":\"1234\",\"tps\":1,\"validity\":0,\"network_id\":4,\"system_id\":\"httpsp\",\"system_type\":\"\",\"interface_version\":\"IF_50\",\"address_ton\":0,\"address_npi\":0,\"address_range\":\"^[0-9a-zA-Z]*$\",\"enabled\":0,\"enquire_link_period\":3000,\"pdu_timeout\":5000,\"request_dlr\":false,\"status\":\"STOPPED\",\"max_binds\":1,\"current_binds_count\":0,\"credit\":0,\"binds\":[],\"credit_used\":0,\"is_prepaid\":true,\"has_available_credit\":false,\"protocol\":\"HTTP\",\"contact_name\":\"Obed Navarrete\",\"email\":\"administrator@company.com\",\"phone_number\":\"85585858\",\"callback_url\":\"http://192.168.100.20:300/call\",\"authentication_types\":\"Undefined\",\"header_security_name\":\"\",\"token\":\"Undefined \",\"callback_headers_http\":[]}",
            "httpsp01", "{\"name\":\"HTTP SP\",\"password\":\"1234\",\"tps\":1,\"validity\":0,\"network_id\":1,\"system_id\":\"httpsp01\",\"system_type\":\"\",\"interface_version\":\"IF_50\",\"address_ton\":0,\"address_npi\":0,\"address_range\":\"^[0-9a-zA-Z]*$\",\"enabled\":1,\"enquire_link_period\":3000,\"pdu_timeout\":5000,\"request_dlr\":false,\"status\":\"STARTED\",\"max_binds\":1,\"current_binds_count\":0,\"credit\":9999970166,\"binds\":[],\"credit_used\":0,\"is_prepaid\":true,\"has_available_credit\":true,\"protocol\":\"HTTP\",\"contact_name\":\"Obed Navarrete\",\"email\":\"administrator@company.com\",\"phone_number\":\"85585858\",\"callback_url\":\"http://18.224.164.85:3000/api/callback\",\"authentication_types\":\"Undefined\",\"header_security_name\":\"\",\"token\":\"Undefined \",\"callback_headers_http\":[]}"
        ));
        assertDoesNotThrow(() -> loadServiceProviders.loadServiceProviders());

        when(jedisCluster.hgetAll("service_providers")).thenReturn(Map.of(
            "httpsp", "assword\":\"1234\",\"tps\":1,\"validity\":0,\"network_id\":4,\"system_id\":\"httpsp\",\"system_type\":\"\",\"interface_version\":\"IF_50\",\"address_ton\":0,\"address_npi\":0,\"address_range\":\"^[0-9a-zA-Z]*$\",\"enabled\":0,\"enquire_link_period\":3000,\"pdu_timeout\":5000,\"request_dlr\":false,\"status\":\"STOPPED\",\"max_binds\":1,\"current_binds_count\":0,\"credit\":0,\"binds\":[],\"credit_used\":0,\"is_prepaid\":true,\"has_available_credit\":false,\"protocol\":\"HTTP\",\"contact_name\":\"Obed Navarrete\",\"email\":\"asdf\",\"phone_number\":\"85585858\",\"callback_url\":\"http://asdf:300/call\",\"authentication_types\":\"Undefined\",\"header_security_name\":\"\",\"token\":\"Undefined \",\"callback_headers_http\":[]}"
        ));
        assertDoesNotThrow(() -> loadServiceProviders.loadServiceProviders());
    }

    @Test
    void updateServiceProvider() {
        assertDoesNotThrow(() -> loadServiceProviders.updateServiceProvider(null));

        when(this.appProperties.getServiceProvidersHash()).thenReturn("service_providers");

        when(jedisCluster.hget("service_providers", "bacSmpp")).thenReturn("{\"name\":\"BAC SMPP\",\"password\":\"1234\",\"tps\":100,\"validity\":0,\"network_id\":3,\"system_id\":\"bacSmpp\",\"system_type\":\"\",\"interface_version\":\"IF_50\",\"address_ton\":0,\"address_npi\":0,\"address_range\":\"^[0-9a-zA-Z]*$\",\"enabled\":1,\"enquire_link_period\":3000,\"pdu_timeout\":5000,\"request_dlr\":true,\"status\":\"BOUND\",\"max_binds\":50,\"current_binds_count\":2,\"credit\":999999999915,\"binds\":[],\"credit_used\":0,\"is_prepaid\":true,\"has_available_credit\":true,\"protocol\":\"SMPP\",\"contact_name\":\"Obed Navarrete\",\"email\":\"administrator@company.com\",\"phone_number\":\"85585858\",\"callback_url\":\"\",\"authentication_types\":\"Undefined\",\"header_security_name\":\"\",\"token\":\"Undefined \",\"callback_headers_http\":[]}");
        assertDoesNotThrow(() -> loadServiceProviders.updateServiceProvider("bacSmpp"));

        when(jedisCluster.hget("service_providers", "bacSmpp")).thenReturn(null);
        assertDoesNotThrow(() -> loadServiceProviders.updateServiceProvider("bacSmpp"));

        when(jedisCluster.hget("service_providers", "bacSmpp")).thenReturn("ord\":\"1234\",\"tps\":100,\"validity\":0,\"network_id\":3,\"system_id\":\"bacSmpp\",\"system_type\":\"\",\"interface_version\":\"IF_50\",\"address_ton\":0,\"address_npi\":0,\"address_range\":\"^[0-9a-zA-Z]*$\",\"enabled\":1,\"enquire_link_period\":3000,\"pdu_timeout\":5000,\"request_dlr\":true,\"status\":\"BOUND\",\"max_binds\":50,\"current_binds_count\":2,\"credit\":999999999915,\"binds\":[],\"credit_used\":0,\"is_prepaid\":true,\"has_available_credit\":true,\"protocol\":\"SMPP\",\"contact_name\":\"Obed Navarrete\",\"email\":\"administrator@company.com\",\"phone_number\":\"85585858\",\"callback_url\":\"\",\"authentication_types\":\"Undefined\",\"header_security_name\":\"\",\"token\":\"Undefined \",\"callback_headers_http\":[]}");
        assertThrows(RTException.class, () -> loadServiceProviders.updateServiceProvider("bacSmpp"));
    }

    @Test
    void deleteServiceProvider() {
        assertDoesNotThrow(() -> loadServiceProviders.deleteServiceProvider(null));

        when(serviceProvidersSystemId.getOrDefault("bacSmpp", 0)).thenReturn(3);
        assertDoesNotThrow(() -> loadServiceProviders.deleteServiceProvider("bacSmpp"));
    }
}
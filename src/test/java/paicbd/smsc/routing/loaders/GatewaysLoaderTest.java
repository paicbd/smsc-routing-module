package paicbd.smsc.routing.loaders;

import com.paicbd.smsc.dto.Gateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import paicbd.smsc.routing.util.AppProperties;
import redis.clients.jedis.JedisCluster;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GatewaysLoaderTest {
    @Mock
    JedisCluster jedisCluster;

    @Mock
    AppProperties appProperties;

    @Mock
    ConcurrentMap<Integer, Gateway> gateways;

    @Spy
    @InjectMocks
    GatewaysLoader gatewaysLoader;

    @BeforeEach
    void setUp() {
        gateways = spy(new ConcurrentHashMap<>());
        gatewaysLoader = new GatewaysLoader(jedisCluster, appProperties, gateways);
    }

    @Test
    void initWithData() {
        Gateway gateway = Gateway.builder()
                .networkId(1)
                .name("fakeGateway")
                .ip("127.0.0.1")
                .port(8080)
                .systemId("systemId")
                .password("password")
                .build();
        Map<String, String> gatewaysMap = Map.of("1", gateway.toString());

        when(appProperties.getGatewaysHash()).thenReturn("gateways");
        when(jedisCluster.hgetAll(appProperties.getGatewaysHash())).thenReturn(gatewaysMap);

        gatewaysLoader.init();

        ArgumentCaptor<Integer> networkIdCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Gateway> gatewayCaptor = ArgumentCaptor.forClass(Gateway.class);
        verify(gateways).put(networkIdCaptor.capture(), gatewayCaptor.capture());

        assertEquals(1, gateways.size());
        assertEquals(1, (int) networkIdCaptor.getValue());

        Gateway capturedGateway = gatewayCaptor.getValue();
        assertEquals(gateway.getNetworkId(), capturedGateway.getNetworkId());
        assertEquals(gateway.getName(), capturedGateway.getName());
        assertEquals(gateway.getIp(), capturedGateway.getIp());
        assertEquals(gateway.getPort(), capturedGateway.getPort());
        assertEquals(gateway.getSystemId(), capturedGateway.getSystemId());
        assertEquals(gateway.getPassword(), capturedGateway.getPassword());
    }

    @Test
    void initWithoutData() {
        when(appProperties.getGatewaysHash()).thenReturn("gateways");
        when(jedisCluster.hgetAll(appProperties.getGatewaysHash())).thenReturn(Collections.emptyMap());

        gatewaysLoader.init();
        assertTrue(gateways.isEmpty());
    }

    @Test
    void updateGatewayWithRedisData() {
        Gateway currentGateway = Gateway.builder()
                .networkId(1)
                .name("fakeGateway")
                .ip("127.0.0.1")
                .port(8080)
                .systemId("systemId")
                .password("password")
                .build();

        Gateway updatedGateway = Gateway.builder()
                .networkId(1)
                .name("updatedGateway")
                .ip("127.0.0.2")
                .port(8081)
                .systemId("systemId1")
                .password("password1")
                .build();

        gateways.put(currentGateway.getNetworkId(), currentGateway);

        when(appProperties.getGatewaysHash()).thenReturn("gateways");
        when(jedisCluster.hget(appProperties.getGatewaysHash(), "1")).thenReturn(updatedGateway.toString());

        gatewaysLoader.updateGateway("1");

        ArgumentCaptor<Integer> networkIdCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Gateway> gatewayCaptor = ArgumentCaptor.forClass(Gateway.class);

        verify(gateways, atLeast(1)).put(networkIdCaptor.capture(), gatewayCaptor.capture());

        assertEquals(1, gateways.size());
        assertEquals(1, (int) networkIdCaptor.getValue());

        Gateway capturedGateway = gatewayCaptor.getValue();
        assertEquals(updatedGateway.getNetworkId(), capturedGateway.getNetworkId());
        assertEquals(updatedGateway.getName(), capturedGateway.getName());
        assertEquals(updatedGateway.getIp(), capturedGateway.getIp());
        assertEquals(updatedGateway.getPort(), capturedGateway.getPort());
        assertEquals(updatedGateway.getSystemId(), capturedGateway.getSystemId());
        assertEquals(updatedGateway.getPassword(), capturedGateway.getPassword());
    }

    @Test
    void updateGatewayWithoutData() {
        when(appProperties.getGatewaysHash()).thenReturn("gateways");
        when(jedisCluster.hget(appProperties.getGatewaysHash(), "1")).thenReturn(null);

        gatewaysLoader.updateGateway("1");

        assertTrue(gateways.isEmpty());
    }

    @Test
    void deleteGateway() {
        Gateway gateway = Gateway.builder()
                .networkId(1)
                .name("fakeGateway")
                .ip("127.0.0.1")
                .port(8080)
                .systemId("systemId")
                .password("password")
                .build();

        gateways.put(gateway.getNetworkId(), gateway);
        gatewaysLoader.deleteGateway("1");
        verify(gateways).remove(1);
        assertTrue(gateways.isEmpty());
    }
}
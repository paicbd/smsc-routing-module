package paicbd.smsc.routing.config;

import com.paicbd.smsc.dto.Gateway;
import com.paicbd.smsc.dto.RoutingRule;
import com.paicbd.smsc.dto.ServiceProvider;
import com.paicbd.smsc.dto.Ss7Settings;
import com.paicbd.smsc.ws.SocketSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import paicbd.smsc.routing.util.AppProperties;
import redis.clients.jedis.JedisCluster;

import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BeansDefinitionTest {

    @Mock
    AppProperties appProperties;

    @Mock
    JedisCluster jedisCluster;

    @InjectMocks
    BeansDefinition beansDefinition;

    @Test
    void routingRules() {
        ConcurrentMap<Integer, List<RoutingRule>> connectionManagers = beansDefinition.routingRules();
        assertNotNull(connectionManagers);
        assertTrue(connectionManagers.isEmpty());
    }

    @Test
    void ss7SettingsMap() {
        ConcurrentMap<Integer, Ss7Settings> ss7SettingsMap = beansDefinition.ss7SettingsMap();
        assertNotNull(ss7SettingsMap);
        assertTrue(ss7SettingsMap.isEmpty());
    }

    @Test
    void gateways() {
        ConcurrentMap<Integer, Gateway> gateways = beansDefinition.gateways();
        assertNotNull(gateways);
        assertTrue(gateways.isEmpty());
    }

    @Test
    void systemIdHandler() {
        ConcurrentMap<String, Integer> systemIdHandler = beansDefinition.systemIdHandler();
        assertNotNull(systemIdHandler);
        assertTrue(systemIdHandler.isEmpty());
    }

    @Test
    void serviceProviders() {
        ConcurrentMap<Integer, ServiceProvider> serviceProviders = beansDefinition.serviceProviders();
        assertNotNull(serviceProviders);
        assertTrue(serviceProviders.isEmpty());
    }

    @Test
    void creditUsed() {
        ConcurrentMap<Integer, AtomicLong> creditUsed = beansDefinition.creditUsed();
        assertNotNull(creditUsed);
        assertTrue(creditUsed.isEmpty());
    }

    @Test
    void socketSessionTest() {
        SocketSession socketSession = beansDefinition.socketSession();
        assertNotNull(socketSession);
    }

    @Test
    void jedisCluster() {
        when(appProperties.getRedisNodes()).thenReturn(List.of("localhost:6379", "localhost:6380"));
        when(appProperties.getRedisMaxTotal()).thenReturn(10);
        when(appProperties.getRedisMinIdle()).thenReturn(1);
        when(appProperties.getRedisMaxIdle()).thenReturn(5);
        when(appProperties.isRedisBlockWhenExhausted()).thenReturn(true);
        assertNull(beansDefinition.jedisCluster());
    }

    @Test
    void testCdrProcessorConfigCreation() {
        assertNotNull(beansDefinition.cdrProcessor(jedisCluster));
    }
}
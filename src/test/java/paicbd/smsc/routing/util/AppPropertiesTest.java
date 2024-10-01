package paicbd.smsc.routing.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class AppPropertiesTest {
    @InjectMocks
    AppProperties appProperties;

    @BeforeEach
    void setUp() throws Exception {
        injectField("instanceName", "routing");
        injectField("redisNodes", Arrays.asList("192.168.100.1:6379", "192.168.100.2:6379", "192.168.100.3:6379"));
        injectField("redisMaxTotal", 20);
        injectField("redisMaxIdle", 20);
        injectField("redisMinIdle", 1);
        injectField("redisBlockWhenExhausted", true);
        injectField("preMessageList", "preMessageList");
        injectField("preMessageItemsToProcess", 1);
        injectField("preMessageWorkers", 1);
        injectField("preDeliverList", "preDeliverList");
        injectField("preDeliverItemsToProcess", 1);
        injectField("preDeliverWorkers", 1);
        injectField("smppMessageList", "smppMessageList");
        injectField("httpMessageList", "httpMessageList");
        injectField("ss7MessageList", "ss7MessageList");
        injectField("smppResult", "smppResult");
        injectField("httpResult", "httpResult");
        injectField("smppDlrList", "smppDlrList");
        injectField("httpDlrList", "httpDlrList");
        injectField("wsEnabled", true);
        injectField("wsHost", "192.168.100.0");
        injectField("wsPort", 8080);
        injectField("wsPath", "/ws");
        injectField("wsHeaderName", "Authorization");
        injectField("wsRetryInterval", 10);
        injectField("wsHeaderValue", "Bearer Token");
        injectField("routingRuleHash", "routingRuleHash");
        injectField("generalSettingsHash", "generalSettingsHash");
        injectField("ss7SettingsHash", "ss7SettingsHash");
        injectField("smppHttpGSKey", "smppHttpGSKey");
        injectField("smppDlrRetryList", "smppDlrRetryList");
        injectField("httpDlrRetryList", "httpDlrRetryList");
        injectField("gatewaysHash", "gatewaysHash");
        injectField("serviceProvidersHash", "serviceProvidersHash");
        injectField("backendUrl", "http://192.168.100.0:8080/api");
        injectField("backendApiKey", "randomApiKey");
    }

    @Test
    void testAppProperties_1() {
        assertEquals("routing", appProperties.getInstanceName());
        assertEquals(Arrays.asList("192.168.100.1:6379", "192.168.100.2:6379", "192.168.100.3:6379"), appProperties.getRedisNodes());
        assertEquals(20, appProperties.getRedisMaxTotal());
        assertEquals(20, appProperties.getRedisMaxIdle());
        assertEquals(1, appProperties.getRedisMinIdle());
        assertTrue(appProperties.isRedisBlockWhenExhausted());
        assertEquals("preMessageList", appProperties.getPreMessageList());
        assertEquals(1, appProperties.getPreMessageItemsToProcess());
        assertEquals(1, appProperties.getPreMessageWorkers());
        assertEquals("preDeliverList", appProperties.getPreDeliverList());
        assertEquals(1, appProperties.getPreDeliverItemsToProcess());
        assertEquals(1, appProperties.getPreDeliverWorkers());
        assertEquals("smppMessageList", appProperties.getSmppMessageList());
        assertEquals("httpMessageList", appProperties.getHttpMessageList());
        assertEquals("ss7MessageList", appProperties.getSs7MessageList());
        assertEquals("smppResult", appProperties.getSmppResult());
        assertEquals("httpResult", appProperties.getHttpResult());
    }

    @Test
    void testAppProperties_2() {
        assertEquals("smppDlrList", appProperties.getSmppDlrList());
        assertEquals("httpDlrList", appProperties.getHttpDlrList());
        assertTrue(appProperties.isWsEnabled());
        assertEquals("192.168.100.0", appProperties.getWsHost());
        assertEquals(8080, appProperties.getWsPort());
        assertEquals("/ws", appProperties.getWsPath());
        assertEquals("Authorization", appProperties.getWsHeaderName());
        assertEquals(10, appProperties.getWsRetryInterval());
        assertEquals("Bearer Token", appProperties.getWsHeaderValue());
        assertEquals("routingRuleHash", appProperties.getRoutingRuleHash());
        assertEquals("generalSettingsHash", appProperties.getGeneralSettingsHash());
        assertEquals("ss7SettingsHash", appProperties.getSs7SettingsHash());
        assertEquals("smppHttpGSKey", appProperties.getSmppHttpGSKey());
        assertEquals("smppDlrRetryList", appProperties.getSmppDlrRetryList());
        assertEquals("httpDlrRetryList", appProperties.getHttpDlrRetryList());
        assertEquals("gatewaysHash", appProperties.getGatewaysHash());
        assertEquals("serviceProvidersHash", appProperties.getServiceProvidersHash());
        assertEquals("http://192.168.100.0:8080/api", appProperties.getBackendUrl());
        assertEquals("randomApiKey", appProperties.getBackendApiKey());
    }

    private void injectField(String fieldName, Object value) throws Exception {
        Field field = AppProperties.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(appProperties, value);
    }
}
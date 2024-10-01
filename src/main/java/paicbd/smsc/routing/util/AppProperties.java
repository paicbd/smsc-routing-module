package paicbd.smsc.routing.util;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Component
public class AppProperties {
	@Value("${spring.application.name}")
    private String instanceName = "routing";
	
    @Value("#{'${redis.cluster.nodes}'.split(',')}")
    private List<String> redisNodes;

    @Value("${redis.threadPool.maxTotal}")
    private int redisMaxTotal = 20;

    @Value("${redis.threadPool.maxIdle}")
    private int redisMaxIdle = 20;

    @Value("${redis.threadPool.minIdle}")
    private int redisMinIdle = 1;

    @Value("${redis.threadPool.blockWhenExhausted}")
    private boolean redisBlockWhenExhausted = true;

    @Value("${redis.preMessageList}")
    private String preMessageList;

    @Value("${redis.preMessage.itemsToProcess}")
    private int preMessageItemsToProcess = 1;

    @Value("${redis.preMessage.workers}")
    private int preMessageWorkers = 1;

    @Value("${redis.preDeliverList}")
    private String preDeliverList;

    @Value("${redis.preDeliver.itemsToProcess}")
    private int preDeliverItemsToProcess = 1;

    @Value("${redis.preDeliver.workers}")
    private int preDeliverWorkers = 1;

    @Value("${redis.smpp.messageList}")
    private String smppMessageList;

    @Value("${redis.http.messageList}")
    private String httpMessageList;

    @Value("${redis.ss7.messageList}")
    private String ss7MessageList;

    @Value("${redis.smpp.result}")
    private String smppResult;

    @Value("${redis.http.result}")
    private String httpResult;

    @Value("${redis.smpp.dlrList}")
    private String smppDlrList;

    @Value("${redis.http.dlrList}")
    private String httpDlrList;

    @Value("${websocket.server.enabled}")
    private boolean wsEnabled = true;

    @Value("${websocket.server.host}")
    private String wsHost;

    @Value("${websocket.server.port}")
    private int wsPort;

    @Value("${websocket.server.path}")
    private String wsPath;

    @Value("${websocket.header.name}")
    private String wsHeaderName;

    @Value("${websocket.server.retryInterval}")
    private int wsRetryInterval;

    @Value("${websocket.header.value}")
    private String wsHeaderValue;

    @Value("${app.routingRuleHash}")
    private String routingRuleHash;

    @Value("${app.generalSettingsHash}")
    private String generalSettingsHash;

    @Value("${app.ss7SettingsHash}")
    private String ss7SettingsHash;

    @Value("${app.smppHttpGSKey}")
    private String smppHttpGSKey;

    @Value("${smpp.redis.deliverySm.retryList}")
    private String smppDlrRetryList;

    @Value("${http.redis.deliverySm.retryList}")
    private String httpDlrRetryList;
    
    @Value("${app.gateways}")
    private String gatewaysHash;
    
    @Value("${app.serviceProviders}")
    private String serviceProvidersHash;
    
    @Value("${backend.url}")
    private String backendUrl;
    
    @Value("${backend.apiKey}")
    private String backendApiKey;
}


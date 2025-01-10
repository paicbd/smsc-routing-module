package paicbd.smsc.routing.util;

import com.paicbd.smsc.utils.Generated;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Component
@Generated
public class AppProperties {
	@Value("${spring.application.name}")
    private String instanceName = "routing";
	
    @Value("#{'${redis.cluster.nodes}'.split(',')}")
    private List<String> redisNodes;

    @Value("${redis.threadPool.maxTotal}")
    private int redisMaxTotal;

    @Value("${redis.threadPool.maxIdle}")
    private int redisMaxIdle;

    @Value("${redis.threadPool.minIdle}")
    private int redisMinIdle;

    @Value("${redis.threadPool.blockWhenExhausted}")
    private boolean redisBlockWhenExhausted;

    @Value("${redis.connection.timeout:0}")
    private int redisConnectionTimeout;

    @Value("${redis.so.timeout:0}")
    private int redisSoTimeout;

    @Value("${redis.maxAttempts:0}")
    private int redisMaxAttempts;

    @Value("${redis.connection.password:}")
    private String redisPassword;

    @Value("${redis.connection.user:}")
    private String redisUser;

    @Value("${redis.preMessageList}")
    private String preMessageList;

    @Value("${redis.preMessage.itemsToProcess}")
    private int preMessageItemsToProcess;

    @Value("${redis.preMessage.workers}")
    private int preMessageWorkers;

    @Value("${redis.preDeliverList}")
    private String preDeliverList;

    @Value("${redis.preDeliver.itemsToProcess}")
    private int preDeliverItemsToProcess;

    @Value("${redis.preDeliver.workers}")
    private int preDeliverWorkers;

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
    private boolean wsEnabled;

    @Value("${websocket.server.host}")
    private String wsHost;

    @Value("${websocket.server.port}")
    private int wsPort;

    @Value("${websocket.server.path}")
    private String wsPath;

    @Value("${websocket.header.name}")
    private String wsHeaderName;

    @Value("${websocket.retry.intervalSeconds}")
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


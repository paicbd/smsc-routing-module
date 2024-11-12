package paicbd.smsc.routing.loaders;

import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.paicbd.smsc.utils.Converter;
import org.springframework.stereotype.Component;

import com.paicbd.smsc.dto.Gateway;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import paicbd.smsc.routing.util.AppProperties;
import redis.clients.jedis.JedisCluster;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoadGateways {
	private final JedisCluster jedisCluster;
    private final AppProperties appProperties;
    private final ConcurrentMap<Integer, Gateway> gateways;

    @PostConstruct
    public void init() {
    	this.loadGateways();
    }
    
    public void loadGateways() {
    	var gatewaysRedisMap = jedisCluster.hgetAll(this.appProperties.getGatewaysHash());
		if (Objects.isNull(gatewaysRedisMap)) {
			log.warn("No gateways found in cache.");
			return;
		}
    	gatewaysRedisMap.entrySet().parallelStream().forEach(entry -> {
    		try {
    			String gatewayInRaw = String.valueOf(entry.getValue());
    			gatewayInRaw = gatewayInRaw.replace("\\", "\\\\");
    			Gateway gateway = Converter.stringToObject(gatewayInRaw, new TypeReference<>() {
				});
    			
    			gateways.put(gateway.getNetworkId(), gateway);

    		} catch (Exception e) {
                log.error("Error loading gateways: {}", e.getMessage());
            }
    	});
    	log.info("Finished loading gateways for {} networkIds", gateways.size());
    }
    
    public void updateGateway(String networkId) {
    	if (networkId != null) {
    		String gatewayInRaw = jedisCluster.hget(this.appProperties.getGatewaysHash(), networkId);
    		if (gatewayInRaw == null) {
                log.warn("Gateways was not found on updateGateway");
                return;
            }
    		Gateway gateway = Converter.stringToObject(gatewayInRaw, Gateway.class);
			Assert.notNull(gateway, "An error occurred while casting Gateway in updateGateway");
			
			gateways.put(gateway.getNetworkId(), gateway);
			log.info("Updated gateways for system id {}: {}", networkId, gateway);
    	} else {
    		log.warn("SystemId to update Gateway is null");
        }
    }
    
    public void deleteGateway(String networkId) {
    	if (networkId != null) {
			gateways.remove(Integer.parseInt(networkId));
    		log.info("Deleted gateway for network id: {}", networkId);
    	} else {
            log.warn("SystemId to delete Gateway is null");
        }
    }
}

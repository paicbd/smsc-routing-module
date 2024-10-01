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
import paicbd.smsc.routing.util.AppProperties;
import redis.clients.jedis.JedisCluster;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoadGateways {
	private final JedisCluster jedisCluster;
    private final AppProperties appProperties;
    private final ConcurrentMap<Integer, Gateway> gateways;
    private final ConcurrentMap<String, Integer> gatewaysSystemId;
    
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
    			gatewaysSystemId.put(gateway.getSystemId(), gateway.getNetworkId());
    			
    		} catch (Exception e) {
                log.error("Error loading gateways: {}", e.getMessage());
            }
    	});
    	log.info("Finished loading gateways for {} networkIds", gateways.size());
    }
    
    public void updateGateway(String systemId) {
    	if (systemId != null) {
    		String gatewayInRaw = jedisCluster.hget(this.appProperties.getGatewaysHash(), systemId);
    		if (gatewayInRaw == null) {
                log.warn("Gateways was not found on updateGateway");
                return;
            }
    		Gateway gateway = Converter.stringToObject(gatewayInRaw, new TypeReference<>() {
			});
			
			gateways.put(gateway.getNetworkId(), gateway);
			gatewaysSystemId.put(gateway.getSystemId(), gateway.getNetworkId());
			log.info("Updated gateways for system id {}: {}", systemId, gateway.toString());
    	} else {
    		log.warn("SystemId to update Gateway is null");
        }
    }
    
    public void deleteGateway(String systemId) {
    	if (systemId != null) {
    		gateways.remove(gatewaysSystemId.getOrDefault(systemId, 0));
    		gatewaysSystemId.remove(systemId);
    		log.info("Deleted gateway for system id: {}", systemId);
    	} else {
            log.warn("SystemId to delete Gateway is null");
        }
    }
}

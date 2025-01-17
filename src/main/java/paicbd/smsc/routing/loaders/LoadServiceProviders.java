package paicbd.smsc.routing.loaders;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.paicbd.smsc.utils.Converter;
import org.springframework.stereotype.Component;

import com.paicbd.smsc.dto.ServiceProvider;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import paicbd.smsc.routing.util.AppProperties;
import redis.clients.jedis.JedisCluster;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoadServiceProviders {
	private final JedisCluster jedisCluster;
    private final AppProperties appProperties;
    private final ConcurrentMap<Integer, ServiceProvider> serviceProviders;
    
    @PostConstruct
    public void init() {
    	this.loadServiceProviders();
    }
    
    public void loadServiceProviders() {
    	Map<String, String> spRedisMap = jedisCluster.hgetAll(this.appProperties.getServiceProvidersHash());
		if (Objects.isNull(spRedisMap)) {
			log.warn("No service providers found in cache.");
			return;
		}

    	spRedisMap.entrySet().parallelStream().forEach(entry -> {
    		try {
	    		String spInRaw = String.valueOf(entry.getValue());
	    		spInRaw = spInRaw.replace("\\", "\\\\");
	    		ServiceProvider serviceProvider = Converter.stringToObject(spInRaw, new TypeReference<>() {
				});
	    		
	    		serviceProviders.put(serviceProvider.getNetworkId(), serviceProvider);
    		} catch (Exception e) {
                log.error("Error loading service providers: {}", e.getMessage());
            }
    	});
    	log.info("Finished loading service providers for {} networkIds", serviceProviders.size());
    }
    
    public void updateServiceProvider(String networkId) {
    	if (networkId != null) {
    		String spInRaw = jedisCluster.hget(this.appProperties.getServiceProvidersHash(), networkId);
    		if (Objects.isNull(spInRaw)) {
                log.warn("Service providers was not found on updateServiceProvider");
                return;
            }
    		ServiceProvider serviceProvider = Converter.stringToObject(spInRaw, ServiceProvider.class);
			Assert.notNull(serviceProvider, "An error occurred while casting ServiceProvider in updateServiceProvider");

    		serviceProviders.put(serviceProvider.getNetworkId(), serviceProvider);
    		log.info("Updated service provider for system id {}: {}", networkId, serviceProvider);
    	} else {
    		log.warn("SystemId to update service provider is null");
        }
    }
    
    public void deleteServiceProvider(String networkId) {
    	if (networkId != null) {
    		serviceProviders.remove(Integer.parseInt(networkId));
    		log.info("Deleted service provider for network id: {}", networkId);
    	} else {
            log.warn("networkId to delete service provider is null");
        }
    }
}

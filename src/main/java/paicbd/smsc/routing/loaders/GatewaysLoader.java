package paicbd.smsc.routing.loaders;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

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
public class GatewaysLoader {
    private final JedisCluster jedisCluster;
    private final AppProperties appProperties;
    private final ConcurrentMap<Integer, Gateway> gateways;

    @PostConstruct
    public void init() {
        this.loadGateways();
    }

    private void loadGateways() {
        Map<String, String> gatewaysRedisMap = jedisCluster.hgetAll(this.appProperties.getGatewaysHash());
        if (gatewaysRedisMap.isEmpty()) {
            log.warn("No gateways found in cache.");
            return;
        }

        gatewaysRedisMap.forEach((networkId, gatewayInRaw) -> {
            gatewayInRaw = gatewayInRaw.replace("\\", "\\\\");
            Gateway gateway = Converter.stringToObject(gatewayInRaw, Gateway.class);
            gateways.put(gateway.getNetworkId(), gateway);
        });

        log.info("Finished loading gateways for {} networkIds", gateways.size());
    }

    public void updateGateway(String networkId) {
        Assert.notNull(networkId, "NetworkId is null on updateGateway");
        String gatewayInRaw = jedisCluster.hget(this.appProperties.getGatewaysHash(), networkId);
        if (gatewayInRaw == null) {
            log.warn("Gateways was not found on updateGateway");
            return;
        }
        Gateway gateway = Converter.stringToObject(gatewayInRaw, Gateway.class);
        Assert.notNull(gateway, "An error occurred while casting Gateway in updateGateway");

        gateways.put(gateway.getNetworkId(), gateway);
        log.info("The gateway has been updated successfully. NetworkId {}: Gateway {}", networkId, gateway);
    }

    public void deleteGateway(String networkId) {
        Assert.notNull(networkId, "NetworkId is null on deleteGateway");
        gateways.remove(Integer.parseInt(networkId));
        log.info("The gateway has been deleted successfully. NetworkId {}", networkId);
    }
}

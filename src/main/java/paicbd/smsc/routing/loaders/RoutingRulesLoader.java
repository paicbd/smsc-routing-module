package paicbd.smsc.routing.loaders;

import com.fasterxml.jackson.core.type.TypeReference;
import com.paicbd.smsc.dto.RoutingRule;
import com.paicbd.smsc.utils.Converter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import paicbd.smsc.routing.util.AppProperties;
import redis.clients.jedis.JedisCluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoutingRulesLoader {
    private final JedisCluster jedisCluster;
    private final AppProperties appProperties;
    private final ConcurrentMap<Integer, List<RoutingRule>> routingRules;

    @PostConstruct
    public void init() {
        this.loadRoutingRules();
    }

    private void loadRoutingRules() {
        Map<String, String> redisRoutingRulesMap = jedisCluster.hgetAll(appProperties.getRoutingRuleHash());
        if (redisRoutingRulesMap.isEmpty()) {
            log.warn("No routing rules found in cache.");
            return;
        }

        redisRoutingRulesMap.forEach((networkId, routingRuleRaw) -> {
            setValuesInMap(networkId, routingRuleRaw);
            log.info("We has loaded {} routing rules for network id: {}", routingRules.get(Integer.parseInt(networkId)).size(), networkId);
        });

        log.info("Finished loading routing rules for {} networkIds", routingRules.size());
    }

    public void updateRoutingRule(String networkId) {
        String routingListRaw = jedisCluster.hget(appProperties.getRoutingRuleHash(), networkId);
        if (routingListRaw == null) {
            log.info("Error trying update routing rule for networkId {}", networkId);
            return;
        }

        setValuesInMap(networkId, routingListRaw);
        log.info("Routing rule for network id {} has been added or updated", networkId);
    }

    public void deleteRoutingRule(String networkId) {
        List<RoutingRule> routingRuleToDelete = routingRules.remove(Integer.parseInt(networkId));
        if (Objects.isNull(routingRuleToDelete)) {
            log.info("No routing rule found for network id: {}", networkId);
            return;
        }
        log.info("Deleted routing rule for network id: {}", networkId);
    }

    private void setValuesInMap(String networkId, String routingListRaw) {
        try {
            routingListRaw = routingListRaw.replace("\\", "\\\\");
            List<RoutingRule> routingList = Converter.stringToObject(routingListRaw, new TypeReference<>() {
            });

            routingRules.computeIfAbsent(Integer.parseInt(networkId), k -> new ArrayList<>())
                    .addAll(routingList);
        } catch (Exception e) {
            log.error("Error setting values Routing Rules Map: {}", e.getMessage());
        }
    }
}

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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoadRoutingRules {
    private final JedisCluster jedisCluster;
    private final AppProperties appProperties;
    private final ConcurrentMap<Integer, List<RoutingRule>> routingRules;

    @PostConstruct
    public void init() {
        this.loadRoutingRules();
    }

    public void loadRoutingRules() {
        var redisRoutingRules = jedisCluster.hgetAll(appProperties.getRoutingRuleHash());
        if (Objects.isNull(redisRoutingRules)) {
            log.warn("No routing rules found in cache.");
            return;
        }
        redisRoutingRules.entrySet().parallelStream().forEach(entry -> {
            try {
                String redisRoutingRuleRaw = String.valueOf(entry.getValue());
                redisRoutingRuleRaw = redisRoutingRuleRaw.replace("\\", "\\\\");
                List<RoutingRule> routingList = Converter.stringToObject(redisRoutingRuleRaw, new TypeReference<>() {
                });

                routingRules.put(Integer.parseInt(entry.getKey()), new ArrayList<>());
                routingList.forEach(r -> routingRules.get(r.getOriginNetworkId()).add(r));
                log.info("We has loaded {} routing rules for network id: {}", routingList.size(), entry.getKey());
            } catch (Exception e) {
                log.error("Error loading routing rule: {}", e.getMessage());
            }
        });
        log.info("Finished loading routing rules for {} networkIds", routingRules.size());
    }

    public void updateRoutingRule(String networkId) {
        try {
            var routingList = jedisCluster.hget(appProperties.getRoutingRuleHash(), networkId);
            if (routingList == null) {
                log.info("Error trying update routing rule for networkId {}", networkId);
                return;
            }

            var previousRoutingRules = routingRules.get(Integer.parseInt(networkId));
            if (Objects.nonNull(previousRoutingRules)) {
                List<String> strings = previousRoutingRules.stream()
                        .map(Converter::valueAsString)
                        .toList();
                log.info("Previous routing rules for network id {}: {}",
                        networkId, Arrays.toString(strings.toArray()));
            }

            List<RoutingRule> routingRulesToUpdate = Converter.stringToObject(routingList, new TypeReference<>() {
            });
            this.routingRules.put(Integer.parseInt(networkId), routingRulesToUpdate);
            log.info("Updated routing rules for network id {}", networkId);
        } catch (Exception e) {
            log.error("Error updating routing rule: {}", e.getMessage());
        }
    }

    public void deleteRoutingRule(String networkId) {
        try {
            var routingRuleToDelete = routingRules.remove(Integer.parseInt(networkId));
            if (Objects.isNull(routingRuleToDelete)) {
                log.info("No routing rule found for network id: {}", networkId);
                return;
            }
            log.info("Deleted routing rule for network id: {}", networkId);
        } catch (Exception e) {
            log.error("Error deleting routing rule: {}", e.getMessage());
        }
    }
}

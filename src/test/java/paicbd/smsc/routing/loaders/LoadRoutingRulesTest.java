package paicbd.smsc.routing.loaders;

import com.paicbd.smsc.dto.RoutingRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import paicbd.smsc.routing.util.AppProperties;
import redis.clients.jedis.JedisCluster;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoadRoutingRulesTest {
    @Mock
    JedisCluster jedisCluster;
    @Mock
    AppProperties appProperties;
    @Mock
    ConcurrentMap<Integer, List<RoutingRule>> routingRules;

    @InjectMocks
    LoadRoutingRules loadRoutingRules;

    @Test
    void init() {
        assertDoesNotThrow(() -> loadRoutingRules.init());
    }

    @Test
    void loadRoutingRules() {
        when(appProperties.getRoutingRuleHash()).thenReturn("routing_rules");
        when(jedisCluster.hgetAll("routing_rules")).thenReturn(null);
        assertDoesNotThrow(() -> loadRoutingRules.loadRoutingRules());

        this.routingRules = new ConcurrentHashMap<>();
        this.loadRoutingRules = new LoadRoutingRules(jedisCluster, appProperties, routingRules);
        when(jedisCluster.hgetAll("routing_rules")).thenReturn(Map.of(
                "3", "[{\"id\":1,\"origin_network_id\":3,\"regex_source_addr\":\"\",\"regex_source_addr_ton\":\"\",\"regex_source_addr_npi\":\"\",\"regex_destination_addr\":\"\",\"regex_dest_addr_ton\":\"\",\"regex_dest_addr_npi\":\"\",\"regex_imsi_digits_mask\":\"\",\"regex_network_node_number\":\"\",\"regex_calling_party_address\":\"\",\"is_sri_response\":false,\"destination\":[{\"priority\":1,\"network_id\":1,\"dest_protocol\":\"SMPP\",\"network_type\":\"GW\"},{\"priority\":2,\"network_id\":2,\"dest_protocol\":\"HTTP\",\"network_type\":\"GW\"}],\"new_source_addr\":\"\",\"new_source_addr_ton\":-1,\"new_source_addr_npi\":-1,\"new_destination_addr\":\"\",\"new_dest_addr_ton\":-1,\"new_dest_addr_npi\":-1,\"add_source_addr_prefix\":\"\",\"add_dest_addr_prefix\":\"\",\"remove_source_addr_prefix\":\"\",\"remove_dest_addr_prefix\":\"\",\"new_gt_sccp_addr_mt\":\"\",\"drop_map_sri\":false,\"network_id_to_map_sri\":-1,\"network_id_to_permanent_failure\":-1,\"drop_temp_failure\":false,\"network_id_temp_failure\":-1,\"check_sri_response\":false,\"origin_protocol\":\"SMPP\",\"origin_network_type\":\"SP\",\"has_filter_rules\":false,\"has_action_rules\":false}]"
        ));
        assertDoesNotThrow(() -> loadRoutingRules.loadRoutingRules());

        when(jedisCluster.hgetAll("routing_rules")).thenReturn(Map.of(
                "3", "[{\"id\":1,,urce_addr_ton\":\"\",\"regex_source_addr_npi\":\"\",\"regex_destination_addr\":\"\",\"regex_dest_addr_ton\":\"\",\"regex_dest_addr_npi\":\"\",\"regex_imsi_digits_mask\":\"\",\"regex_network_node_number\":\"\",\"regex_calling_party_address\":\"\",\"is_sri_response\":false,\"destination\":[{\"priority\":1,\"network_id\":1,\"dest_protocol\":\"SMPP\",\"network_type\":\"GW\"},{\"priority\":2,\"network_id\":2,\"dest_protocol\":\"HTTP\",\"network_type\":\"GW\"}],\"new_source_addr\":\"\",\"new_source_addr_ton\":-1,\"new_source_addr_npi\":-1,\"new_destination_addr\":\"\",\"new_dest_addr_ton\":-1,\"new_dest_addr_npi\":-1,\"add_source_addr_prefix\":\"\",\"add_dest_addr_prefix\":\"\",\"remove_source_addr_prefix\":\"\",\"remove_dest_addr_prefix\":\"\",\"new_gt_sccp_addr_mt\":\"\",\"drop_map_sri\":false,\"network_id_to_map_sri\":-1,\"network_id_to_permanent_failure\":-1,\"drop_temp_failure\":false,\"network_id_temp_failure\":-1,\"check_sri_response\":false,\"origin_protocol\":\"SMPP\",\"origin_network_type\":\"SP\",\"has_filter_rules\":false,\"has_action_rules\":false}]"
        ));
        assertDoesNotThrow(() -> loadRoutingRules.loadRoutingRules());
    }

    @Test
    void updateRoutingRule() {
        when(appProperties.getRoutingRuleHash()).thenReturn("routing_rules");
        when(jedisCluster.hget("routing_rules", "3")).thenReturn(null);
        assertDoesNotThrow(() -> loadRoutingRules.updateRoutingRule("3"));

        when(jedisCluster.hget("routing_rules", "3")).thenReturn("[{\"id\":1,\"origin_network_id\":3,\"regex_source_addr\":\"\",\"regex_source_addr_ton\":\"\",\"regex_source_addr_npi\":\"\",\"regex_destination_addr\":\"\",\"regex_dest_addr_ton\":\"\",\"regex_dest_addr_npi\":\"\",\"regex_imsi_digits_mask\":\"\",\"regex_network_node_number\":\"\",\"regex_calling_party_address\":\"\",\"is_sri_response\":false,\"destination\":[{\"priority\":1,\"network_id\":1,\"dest_protocol\":\"SMPP\",\"network_type\":\"GW\"},{\"priority\":2,\"network_id\":2,\"dest_protocol\":\"HTTP\",\"network_type\":\"GW\"}],\"new_source_addr\":\"\",\"new_source_addr_ton\":-1,\"new_source_addr_npi\":-1,\"new_destination_addr\":\"\",\"new_dest_addr_ton\":-1,\"new_dest_addr_npi\":-1,\"add_source_addr_prefix\":\"\",\"add_dest_addr_prefix\":\"\",\"remove_source_addr_prefix\":\"\",\"remove_dest_addr_prefix\":\"\",\"new_gt_sccp_addr_mt\":\"\",\"drop_map_sri\":false,\"network_id_to_map_sri\":-1,\"network_id_to_permanent_failure\":-1,\"drop_temp_failure\":false,\"network_id_temp_failure\":-1,\"check_sri_response\":false,\"origin_protocol\":\"SMPP\",\"origin_network_type\":\"SP\",\"has_filter_rules\":false,\"has_action_rules\":false}]");
        assertDoesNotThrow(() -> loadRoutingRules.updateRoutingRule("3"));

        when(jedisCluster.hget("routing_rules", "3")).thenReturn("[{\"id\":1,,urce_addr_ton\":\"\",\"regex_source_addr_npi\":\"\",\"regex_destination_addr\":\"\",\"regex_dest_addr_ton\":\"\",\"regex_dest_addr_npi\":\"\",\"regex_imsi_digits_mask\":\"\",\"regex_network_node_number\":\"\",\"regex_calling_party_address\":\"\",\"is_sri_response\":false,\"destination\":[{\"priority\":1,\"network_id\":1,\"dest_protocol\":\"SMPP\",\"network_type\":\"GW\"},{\"priority\":2,\"network_id\":2,\"dest_protocol\":\"HTTP\",\"network_type\":\"GW\"}],\"new_source_addr\":\"\",\"new_source_addr_ton\":-1,\"new_source_addr_npi\":-1,\"new_destination_addr\":\"\",\"new_dest_addr_ton\":-1,\"new_dest_addr_npi\":-1,\"add_source_addr_prefix\":\"\",\"add_dest_addr_prefix\":\"\",\"remove_source_addr_prefix\":\"\",\"remove_dest_addr_prefix\":\"\",\"new_gt_sccp_addr_mt\":\"\",\"drop_map_sri\":false,\"network_id_to_map_sri\":-1,\"network_id_to_permanent_failure\":-1,\"drop_temp_failure\":false,\"network_id_temp_failure\":-1,\"check_sri_response\":false,\"origin_protocol\":\"SMPP\",\"origin_network_type\":\"SP\",\"has_filter_rules\":false,\"has_action_rules\":false}]");
        assertDoesNotThrow(() -> loadRoutingRules.updateRoutingRule("3"));

        RoutingRule routingRule = new RoutingRule();
        routingRule.setOriginNetworkId(3);
        this.routingRules = new ConcurrentHashMap<>();
        this.routingRules.put(3, List.of(routingRule));
        this.loadRoutingRules = new LoadRoutingRules(jedisCluster, appProperties, routingRules);
        when(jedisCluster.hget("routing_rules", "3")).thenReturn("[{\"id\":1,\"origin_network_id\":3,\"regex_source_addr\":\"\",\"regex_source_addr_ton\":\"\",\"regex_source_addr_npi\":\"\",\"regex_destination_addr\":\"\",\"regex_dest_addr_ton\":\"\",\"regex_dest_addr_npi\":\"\",\"regex_imsi_digits_mask\":\"\",\"regex_network_node_number\":\"\",\"regex_calling_party_address\":\"\",\"is_sri_response\":false,\"destination\":[{\"priority\":1,\"network_id\":1,\"dest_protocol\":\"SMPP\",\"network_type\":\"GW\"},{\"priority\":2,\"network_id\":2,\"dest_protocol\":\"HTTP\",\"network_type\":\"GW\"}],\"new_source_addr\":\"\",\"new_source_addr_ton\":-1,\"new_source_addr_npi\":-1,\"new_destination_addr\":\"\",\"new_dest_addr_ton\":-1,\"new_dest_addr_npi\":-1,\"add_source_addr_prefix\":\"\",\"add_dest_addr_prefix\":\"\",\"remove_source_addr_prefix\":\"\",\"remove_dest_addr_prefix\":\"\",\"new_gt_sccp_addr_mt\":\"\",\"drop_map_sri\":false,\"network_id_to_map_sri\":-1,\"network_id_to_permanent_failure\":-1,\"drop_temp_failure\":false,\"network_id_temp_failure\":-1,\"check_sri_response\":false,\"origin_protocol\":\"SMPP\",\"origin_network_type\":\"SP\",\"has_filter_rules\":false,\"has_action_rules\":false}]");
        assertDoesNotThrow(() -> loadRoutingRules.updateRoutingRule("3"));

        assertDoesNotThrow(() -> loadRoutingRules.updateRoutingRule("a"));
    }

    @Test
    void deleteRoutingRule() {
        assertDoesNotThrow(() -> loadRoutingRules.deleteRoutingRule(null));
        assertDoesNotThrow(() -> loadRoutingRules.deleteRoutingRule("3"));

        RoutingRule routingRule = new RoutingRule();
        routingRule.setOriginNetworkId(3);
        this.routingRules = new ConcurrentHashMap<>();
        this.routingRules.put(3, List.of(routingRule));

        this.loadRoutingRules = new LoadRoutingRules(jedisCluster, appProperties, routingRules);
        assertDoesNotThrow(() -> loadRoutingRules.deleteRoutingRule("3"));

        assertDoesNotThrow(() -> loadRoutingRules.deleteRoutingRule("a"));
    }
}
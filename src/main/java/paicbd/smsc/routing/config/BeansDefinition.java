package paicbd.smsc.routing.config;

import com.paicbd.smsc.cdr.CdrProcessor;
import com.paicbd.smsc.dto.Gateway;
import com.paicbd.smsc.dto.RoutingRule;
import com.paicbd.smsc.dto.ServiceProvider;
import com.paicbd.smsc.dto.UtilsRecords;
import com.paicbd.smsc.utils.Converter;
import com.paicbd.smsc.ws.SocketSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.paicbd.smsc.dto.Ss7Settings;
import paicbd.smsc.routing.util.AppProperties;
import redis.clients.jedis.JedisCluster;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BeansDefinition {
    private final AppProperties appProperties;

    @Bean
    public ConcurrentMap<Integer, List<RoutingRule>> routingRules() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    public ConcurrentMap<Integer, Ss7Settings> ss7SettingsMap() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    public ConcurrentMap<Integer, Gateway> gateways() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    public ConcurrentMap<Integer, ServiceProvider> serviceProviders() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    public ConcurrentMap<Integer, AtomicLong> creditUsed() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    public SocketSession socketSession() {
        return new SocketSession("routing");
    }

    @Bean
    public JedisCluster jedisCluster() {
        UtilsRecords.JedisConfigParams params = getJedisConfigParams();
        return Converter.paramsToJedisCluster(params);
    }

    private UtilsRecords.JedisConfigParams getJedisConfigParams() {
        final int maxTotal = this.appProperties.getRedisMaxTotal();
        final int minIdle = this.appProperties.getRedisMinIdle();
        final int maxIdle = this.appProperties.getRedisMaxIdle();
        final boolean blockWhenExhausted = this.appProperties.isRedisBlockWhenExhausted();
        return new UtilsRecords.JedisConfigParams(this.appProperties.getRedisNodes(), maxTotal, minIdle, maxIdle, blockWhenExhausted);
    }

    @Bean
    public CdrProcessor cdrProcessor(JedisCluster jedisCluster) {
        return new CdrProcessor(jedisCluster);
    }
}

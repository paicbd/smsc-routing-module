package paicbd.smsc.routing.loaders;

import com.paicbd.smsc.dto.GeneralSettings;
import com.paicbd.smsc.utils.Converter;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.paicbd.smsc.dto.Ss7Settings;
import paicbd.smsc.routing.util.AppProperties;
import redis.clients.jedis.JedisCluster;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettingsLoader {
    private final JedisCluster jedisCluster;
    private final AppProperties appProperties;
    private final ConcurrentMap<Integer, Ss7Settings> ss7SettingsMap;

    @Getter
    private GeneralSettings smppHttpSettings;

    @PostConstruct
    public void init() {
        log.info("Loading Global Settings");
        this.loadOrUpdateSmppHttpSettings();
        this.loadAllSs7Settings();
    }

    public void loadOrUpdateSmppHttpSettings() {
        log.info("Start loading smpp-http settings");
        String gsRaw = this.jedisCluster.hget(this.appProperties.getGeneralSettingsHash(),
                this.appProperties.getSmppHttpGSKey());

        if (Objects.isNull(gsRaw)) {
            log.error("Error loading smpp-http settings for protocol key: {}", this.appProperties.getSmppHttpGSKey());
            return;
        }

        this.smppHttpSettings = Converter.stringToObject(gsRaw, GeneralSettings.class);
    }

    private void loadAllSs7Settings() {
        log.info("Start loading ss7 settings");
        Map<String, String> hashValues = this.jedisCluster.hgetAll(this.appProperties.getSs7SettingsHash());
        if (hashValues.isEmpty()) {
            log.warn("No ss7 settings found in cache.");
            return;
        }

        hashValues.forEach((ss7GatewayNetworkId, data) -> {
            Ss7Settings ss7Setting = Converter.stringToObject(data, Ss7Settings.class);
            this.addToSs7Map(Integer.parseInt(ss7GatewayNetworkId), ss7Setting);
        });

        log.info("Loaded ss7 settings, settings loaded: {}", ss7SettingsMap.size());
    }

    public void addToSs7Map(int networkId, Ss7Settings ss7Settings) {
        ss7SettingsMap.put(networkId, ss7Settings);
        log.info("Ss7 settings for network id {} was added to cache: {}", networkId, ss7Settings.toString());
    }

    public Ss7Settings getSs7Settings(Integer networkId) {
        return ss7SettingsMap.get(networkId);
    }

    public void updateSpecificSs7Setting(Integer networkId) {
        String ss7Setting = jedisCluster.hget(appProperties.getSs7SettingsHash(), networkId.toString());
        if (Objects.isNull(ss7Setting)) {
            log.info("Error trying update ss7 setting for network id {}", networkId);
            return;
        }
        Ss7Settings ss7SettingsToUpdate = Converter.stringToObject(ss7Setting, Ss7Settings.class);
        this.ss7SettingsMap.put(networkId, ss7SettingsToUpdate);
        log.info("Updated ss7 settings for network id {}: {}", networkId, ss7SettingsToUpdate);
    }

    public void removeFromSs7Map(int networkId) {
        ss7SettingsMap.remove(networkId);
        log.warn("Ss7 settings for network id {} was removed.", networkId);
    }
}

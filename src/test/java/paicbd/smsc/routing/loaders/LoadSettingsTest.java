package paicbd.smsc.routing.loaders;

import com.paicbd.smsc.dto.Ss7Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import paicbd.smsc.routing.util.AppProperties;
import redis.clients.jedis.JedisCluster;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoadSettingsTest {
    @Mock
    JedisCluster jedisCluster;
    @Mock
    AppProperties appProperties;
    @Mock
    ConcurrentMap<Integer, Ss7Settings> ss7SettingsMap;

    @InjectMocks
    LoadSettings loadSettings;

    @BeforeEach
    void setUp() {
        this.loadSettings = new LoadSettings(jedisCluster, appProperties, ss7SettingsMap);
    }

    @Test
    void init() {
        assertDoesNotThrow(() -> loadSettings.init());
    }

    @Test
    void loadOrUpdateSmppHttpSettings_NULL() {
        when(appProperties.getGeneralSettingsHash()).thenReturn("general_settings");
        when(appProperties.getSmppHttpGSKey()).thenReturn("smpp_http");
        when(jedisCluster.hget(appProperties.getGeneralSettingsHash(), appProperties.getSmppHttpGSKey())).thenReturn(null);
        assertDoesNotThrow(() -> loadSettings.loadOrUpdateSmppHttpSettings());
    }

    @Test
    void loadOrUpdateSmppHttpSettings_OK() {
        when(jedisCluster.hget(appProperties.getGeneralSettingsHash(), appProperties.getSmppHttpGSKey())).thenReturn("{\"id\":1,\"validity_period\":60,\"max_validity_period\":240,\"source_addr_ton\":1,\"source_addr_npi\":1,\"dest_addr_ton\":1,\"dest_addr_npi\":1,\"encoding_iso88591\":3,\"encoding_gsm7\":0,\"encoding_ucs2\":2}");
        assertDoesNotThrow(() -> loadSettings.loadOrUpdateSmppHttpSettings());
        assertNotNull(loadSettings.getSmppHttpSettings());
    }

    @Test
    void loadAllSs7Settings_NULL() {
        when(this.appProperties.getSs7SettingsHash()).thenReturn("ss7_settings");
        when(this.jedisCluster.hgetAll("ss7_settings")).thenReturn(null);
        assertDoesNotThrow(() -> loadSettings.loadAllSs7Settings());
    }

    @Test
    void loadAllSs7Settings_OK() {
        when(this.appProperties.getSs7SettingsHash()).thenReturn("ss7_settings");
        when(this.jedisCluster.hgetAll("ss7_settings")).thenReturn(Map.of(
                "2", "{\"name\":\"ss7GW07\",\"enabled\":0,\"status\":\"STARTED\",\"protocol\":\"SS7\",\"network_id\":2,\"mno_id\":1,\"global_title\":\"22220\",\"global_title_indicator\":\"GT0100\",\"translation_type\":0,\"smsc_ssn\":8,\"hlr_ssn\":6,\"msc_ssn\":8,\"map_version\":3,\"split_message\":true}"
        ));
        assertDoesNotThrow(() -> loadSettings.loadAllSs7Settings());
    }

    @Test
    void loadAllSs7Settings_NumberFormatError() {
        when(this.appProperties.getSs7SettingsHash()).thenReturn("ss7_settings");
        when(this.jedisCluster.hgetAll("ss7_settings")).thenReturn(Map.of(
                "asdf", "{\"name\":\"ss7GW07\",\"enabled\":0,\"status\":\"STARTED\",\"protocol\":\"SS7\",\"network_id\":2,\"mno_id\":1,\"global_title\":\"22220\",\"global_title_indicator\":\"GT0100\",\"translation_type\":0,\"smsc_ssn\":8,\"hlr_ssn\":6,\"msc_ssn\":8,\"map_version\":3,\"split_message\":true}"
        ));
        assertDoesNotThrow(() -> loadSettings.loadAllSs7Settings());
    }

    @Test
    void addToSs7Map() {
        Ss7Settings ss7Settings = new Ss7Settings();
        assertDoesNotThrow(() -> loadSettings.addToSs7Map(1, ss7Settings));
    }

    @Test
    void getSs7Settings() {
        when(ss7SettingsMap.get(1)).thenReturn(null);
        assertNull(loadSettings.getSs7Settings(1));

        Ss7Settings ss7Settings = new Ss7Settings();
        when(ss7SettingsMap.get(1)).thenReturn(ss7Settings);
        assertEquals(ss7Settings, loadSettings.getSs7Settings(1));
    }

    @Test
    void updateSpecificSs7Setting() {
        // SS7 setting is null
        when(jedisCluster.hget(appProperties.getSs7SettingsHash(), "1")).thenReturn(null);
        assertDoesNotThrow(() -> loadSettings.updateSpecificSs7Setting(1));

        // SS7 setting is not null
        when(jedisCluster.hget(appProperties.getSs7SettingsHash(), "1")).thenReturn("{\"name\":\"ss7GW07\",\"enabled\":0,\"status\":\"STARTED\",\"protocol\":\"SS7\",\"network_id\":2,\"mno_id\":1,\"global_title\":\"22220\",\"global_title_indicator\":\"GT0100\",\"translation_type\":0,\"smsc_ssn\":8,\"hlr_ssn\":6,\"msc_ssn\":8,\"map_version\":3,\"split_message\":true}");
        assertDoesNotThrow(() -> loadSettings.updateSpecificSs7Setting(1));
    }

    @Test
    void removeFromSs7Map() {
        when(ss7SettingsMap.remove(1)).thenReturn(null);
        assertDoesNotThrow(() -> loadSettings.removeFromSs7Map(1));

        Ss7Settings ss7Settings = new Ss7Settings();
        when(ss7SettingsMap.remove(1)).thenReturn(ss7Settings);
        assertDoesNotThrow(() -> loadSettings.removeFromSs7Map(1));
    }
}
package paicbd.smsc.routing.loaders;

import com.paicbd.smsc.dto.GeneralSettings;
import com.paicbd.smsc.dto.Ss7Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import paicbd.smsc.routing.util.AppProperties;
import redis.clients.jedis.JedisCluster;

import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettingsLoaderTest {
    @Mock
    JedisCluster jedisCluster;

    @Mock
    AppProperties appProperties;

    @Mock
    ConcurrentMap<Integer, Ss7Settings> ss7SettingsMap;

    @Spy
    @InjectMocks
    SettingsLoader settingsLoader;

    @BeforeEach
    void setUp() {
        ss7SettingsMap = spy(new ConcurrentHashMap<>());
        settingsLoader = new SettingsLoader(jedisCluster, appProperties, ss7SettingsMap);
    }

    @Test
    void initWhenStringGeneralSettingsFromRedisIsNullThenGeneralSettingsIsNull() {
        when(appProperties.getGeneralSettingsHash()).thenReturn("generalSettings");
        when(appProperties.getSmppHttpGSKey()).thenReturn("smppHttpGS");
        when(jedisCluster.hget("generalSettings", "smppHttpGS")).thenReturn(null);
        when(appProperties.getSs7SettingsHash()).thenReturn("ss7Settings");
        when(jedisCluster.hgetAll("ss7Settings")).thenReturn(Collections.emptyMap());

        settingsLoader.init();

        assertEquals(0, ss7SettingsMap.size());
        assertNull(settingsLoader.getSmppHttpSettings());
    }

    @Test
    void initWhenStringGeneralSettingsFromRedisIsNotNullThenGeneralSettingsIsNotNull() {
        GeneralSettings generalSettings = GeneralSettings.builder()
                .id(1)
                .validityPeriod(120)
                .maxValidityPeriod(600)
                .sourceAddrTon(1)
                .sourceAddrNpi(1)
                .destAddrTon(1)
                .destAddrNpi(1)
                .encodingIso88591(1)
                .encodingGsm7(1)
                .encodingUcs2(1)
                .build();

        Ss7Settings ss7Settings = Ss7Settings.builder()
                .networkId(1)
                .name("fakeSs7Settings")
                .mnoId(1)
                .splitMessage(true)
                .globalTitle("GT100")
                .build();

        when(appProperties.getGeneralSettingsHash()).thenReturn("generalSettings");
        when(appProperties.getSmppHttpGSKey()).thenReturn("smppHttpGS");
        when(jedisCluster.hget("generalSettings", "smppHttpGS")).thenReturn(generalSettings.toString());
        when(appProperties.getSs7SettingsHash()).thenReturn("ss7Settings");
        when(jedisCluster.hgetAll("ss7Settings")).thenReturn(Collections.singletonMap("1", ss7Settings.toString()));

        settingsLoader.init();

        assertEquals(1, ss7SettingsMap.size());
        assertEquals(generalSettings.toString(), settingsLoader.getSmppHttpSettings().toString());
    }

    @Test
    void updateSpecificSs7SettingWhenSs7SettingIsNullThenDoNothing() {
        when(appProperties.getSs7SettingsHash()).thenReturn("ss7Settings");
        when(jedisCluster.hget(appProperties.getSs7SettingsHash(), "1")).thenReturn(null);
        settingsLoader.updateSpecificSs7Setting(1);
        verify(ss7SettingsMap, never()).put(1, null);
        assertNull(ss7SettingsMap.get(1));
    }

    @Test
    void updateSpecificSs7SettingWhenSs7SettingIsNotNullThenUpdateSs7Setting() {
        Ss7Settings ss7Settings = Ss7Settings.builder()
                .networkId(1)
                .name("fakeSs7Settings")
                .mnoId(1)
                .splitMessage(true)
                .globalTitle("GT100")
                .build();

        when(appProperties.getSs7SettingsHash()).thenReturn("ss7Settings");
        when(jedisCluster.hget(appProperties.getSs7SettingsHash(), "1")).thenReturn(ss7Settings.toString());
        settingsLoader.updateSpecificSs7Setting(1);

        ArgumentCaptor<Integer> networkIdCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Ss7Settings> ss7SettingsCaptor = ArgumentCaptor.forClass(Ss7Settings.class);

        verify(ss7SettingsMap).put(networkIdCaptor.capture(), ss7SettingsCaptor.capture());
        assertEquals(1, ss7SettingsMap.size());
        assertEquals(1, (int) networkIdCaptor.getValue());
        assertEquals(ss7Settings.toString(), ss7SettingsCaptor.getValue().toString());

        assertEquals(settingsLoader.getSs7Settings(1).toString(), ss7Settings.toString());
    }

    @Test
    void removeFromSs7MapWhenNetworkIdIsNotNullThenRemoveSs7Setting() {
        Ss7Settings ss7Settings = Ss7Settings.builder()
                .networkId(1)
                .name("fakeSs7Settings")
                .mnoId(1)
                .splitMessage(true)
                .globalTitle("GT100")
                .build();
        ss7SettingsMap.put(1, ss7Settings);

        settingsLoader.removeFromSs7Map(1);
        verify(ss7SettingsMap).remove(1);
        assertEquals(0, ss7SettingsMap.size());
    }
}
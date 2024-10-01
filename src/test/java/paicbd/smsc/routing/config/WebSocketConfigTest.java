package paicbd.smsc.routing.config;

import com.paicbd.smsc.ws.SocketClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import paicbd.smsc.routing.util.AppProperties;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebSocketConfigTest {
    @Mock
    AppProperties appProperties;
    @Mock
    CustomFrameHandler customFrameHandler;
    @InjectMocks
    WebSocketConfig webSocketConfig;

    @Test
    void socketClient() {
        when(appProperties.isWsEnabled()).thenReturn(true);
        when(appProperties.getWsHost()).thenReturn("localhost");
        when(appProperties.getWsPort()).thenReturn(8080);
        when(appProperties.getWsPath()).thenReturn("/ws");
        when(appProperties.getWsHeaderName()).thenReturn("Authorization");
        when(appProperties.getWsHeaderValue()).thenReturn("Token");
        when(appProperties.getWsRetryInterval()).thenReturn(10);

        SocketClient socketClient = webSocketConfig.socketClient();
        assertNotNull(socketClient, "SocketClient should not be null");
    }
}
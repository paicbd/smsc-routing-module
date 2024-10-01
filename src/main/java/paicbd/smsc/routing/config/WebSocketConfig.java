package paicbd.smsc.routing.config;

import com.paicbd.smsc.dto.UtilsRecords;
import com.paicbd.smsc.ws.SocketClient;
import com.paicbd.smsc.ws.SocketSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import paicbd.smsc.routing.util.AppProperties;

import java.util.List;

import static paicbd.smsc.routing.util.AppConstants.CONNECT_HTTP_GATEWAY;
import static paicbd.smsc.routing.util.AppConstants.CONNECT_SMPP_GATEWAY;
import static paicbd.smsc.routing.util.AppConstants.DELETE_HTTP_GATEWAY;
import static paicbd.smsc.routing.util.AppConstants.DELETE_ROUTING_RULE;
import static paicbd.smsc.routing.util.AppConstants.DELETE_SERVICE_HTTP_PROVIDER_ENDPOINT;
import static paicbd.smsc.routing.util.AppConstants.DELETE_SERVICE_SMPP_PROVIDER_ENDPOINT;
import static paicbd.smsc.routing.util.AppConstants.DELETE_SMPP_GATEWAY;
import static paicbd.smsc.routing.util.AppConstants.DELETE_SS7_CONFIG;
import static paicbd.smsc.routing.util.AppConstants.STOP_HTTP_GATEWAY;
import static paicbd.smsc.routing.util.AppConstants.STOP_SMPP_GATEWAY;
import static paicbd.smsc.routing.util.AppConstants.UPDATE_GS_SMPP_HTTP;
import static paicbd.smsc.routing.util.AppConstants.UPDATE_HTTP_GATEWAY;
import static paicbd.smsc.routing.util.AppConstants.UPDATE_ROUTING_RULE;
import static paicbd.smsc.routing.util.AppConstants.UPDATE_SERVICE_HTTP_PROVIDER_ENDPOINT;
import static paicbd.smsc.routing.util.AppConstants.UPDATE_SERVICE_SMPP_PROVIDER_ENDPOINT;
import static paicbd.smsc.routing.util.AppConstants.UPDATE_SMPP_GATEWAY;
import static paicbd.smsc.routing.util.AppConstants.UPDATE_SS7_CONFIG;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebSocketConfig {
    private final AppProperties appProperties;
    private final SocketSession socketSession;
    private final CustomFrameHandler customFrameHandler;

    @Bean
    public SocketClient socketClient() {
        List<String> topicsToSubscribe = List.of(
                UPDATE_ROUTING_RULE,
                DELETE_ROUTING_RULE,
                UPDATE_GS_SMPP_HTTP,
                UPDATE_SS7_CONFIG,
                DELETE_SS7_CONFIG,
                UPDATE_SMPP_GATEWAY,
                CONNECT_SMPP_GATEWAY,
                STOP_SMPP_GATEWAY,
                DELETE_SMPP_GATEWAY,
                UPDATE_SERVICE_SMPP_PROVIDER_ENDPOINT,
                DELETE_SERVICE_SMPP_PROVIDER_ENDPOINT,
                UPDATE_HTTP_GATEWAY,
                CONNECT_HTTP_GATEWAY,
                STOP_HTTP_GATEWAY,
                DELETE_HTTP_GATEWAY,
                UPDATE_SERVICE_HTTP_PROVIDER_ENDPOINT,
                DELETE_SERVICE_HTTP_PROVIDER_ENDPOINT
        );
        UtilsRecords.WebSocketConnectionParams wsp = new UtilsRecords.WebSocketConnectionParams(
                appProperties.isWsEnabled(),
                appProperties.getWsHost(),
                appProperties.getWsPort(),
                appProperties.getWsPath(),
                topicsToSubscribe,
                appProperties.getWsHeaderName(),
                appProperties.getWsHeaderValue(),
                appProperties.getWsRetryInterval(),
                "ROUTING-MODULE"
        );
        return new SocketClient(customFrameHandler, wsp, socketSession);
    }
}

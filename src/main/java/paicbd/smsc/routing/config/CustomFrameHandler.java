package paicbd.smsc.routing.config;

import com.paicbd.smsc.ws.FrameHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.stereotype.Component;
import paicbd.smsc.routing.util.CreditHandler;
import paicbd.smsc.routing.loaders.LoadGateways;
import paicbd.smsc.routing.loaders.LoadRoutingRules;
import paicbd.smsc.routing.loaders.LoadServiceProviders;
import paicbd.smsc.routing.loaders.LoadSettings;

import java.util.Objects;

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
@Component
@RequiredArgsConstructor
public class CustomFrameHandler implements FrameHandler {
    private final LoadSettings loadSettings;
    private final LoadGateways loadGateways;
    private final CreditHandler creditHandler;
    private final LoadRoutingRules loadRoutingRules;
    private final LoadServiceProviders loadServiceProviders;

    @Override
    public void handleFrameLogic(StompHeaders headers, Object payload) {
        String identifier = payload.toString();
        String destination = headers.getDestination();
        Objects.requireNonNull(identifier, "System ID cannot be null");
        Objects.requireNonNull(destination, "Destination cannot be null");

        switch (destination) {
            case UPDATE_ROUTING_RULE -> loadRoutingRules.updateRoutingRule(identifier);
            case DELETE_ROUTING_RULE -> loadRoutingRules.deleteRoutingRule(identifier);
            case UPDATE_GS_SMPP_HTTP -> loadSettings.loadOrUpdateSmppHttpSettings();
            case UPDATE_SS7_CONFIG -> loadSettings.updateSpecificSs7Setting(Integer.parseInt(identifier));
            case DELETE_SS7_CONFIG -> loadSettings.removeFromSs7Map(Integer.parseInt(identifier));
            case UPDATE_SMPP_GATEWAY, CONNECT_SMPP_GATEWAY, STOP_SMPP_GATEWAY, UPDATE_HTTP_GATEWAY,
                 CONNECT_HTTP_GATEWAY, STOP_HTTP_GATEWAY -> loadGateways.updateGateway(identifier);
            case DELETE_SMPP_GATEWAY, DELETE_HTTP_GATEWAY -> loadGateways.deleteGateway(identifier);
            case UPDATE_SERVICE_SMPP_PROVIDER_ENDPOINT, UPDATE_SERVICE_HTTP_PROVIDER_ENDPOINT ->
                    loadServiceProviders.updateServiceProvider(identifier);
            case DELETE_SERVICE_SMPP_PROVIDER_ENDPOINT, DELETE_SERVICE_HTTP_PROVIDER_ENDPOINT ->
                    deleteAction(identifier);
            default -> log.warn("Unknown destination: {}", headers.getDestination());
        }
    }

    private void deleteAction(String identifier) {
        loadServiceProviders.deleteServiceProvider(identifier);
        creditHandler.removeCreditUsed(identifier);
    }
}

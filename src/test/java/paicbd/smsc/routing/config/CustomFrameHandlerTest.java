package paicbd.smsc.routing.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.stomp.StompHeaders;
import paicbd.smsc.routing.util.CreditHandler;
import paicbd.smsc.routing.loaders.LoadGateways;
import paicbd.smsc.routing.loaders.LoadRoutingRules;
import paicbd.smsc.routing.loaders.LoadServiceProviders;
import paicbd.smsc.routing.loaders.LoadSettings;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static paicbd.smsc.routing.util.AppConstants.DELETE_ROUTING_RULE;
import static paicbd.smsc.routing.util.AppConstants.DELETE_SERVICE_SMPP_PROVIDER_ENDPOINT;
import static paicbd.smsc.routing.util.AppConstants.DELETE_SMPP_GATEWAY;
import static paicbd.smsc.routing.util.AppConstants.DELETE_SS7_CONFIG;
import static paicbd.smsc.routing.util.AppConstants.UPDATE_GS_SMPP_HTTP;
import static paicbd.smsc.routing.util.AppConstants.UPDATE_ROUTING_RULE;
import static paicbd.smsc.routing.util.AppConstants.UPDATE_SERVICE_SMPP_PROVIDER_ENDPOINT;
import static paicbd.smsc.routing.util.AppConstants.UPDATE_SMPP_GATEWAY;
import static paicbd.smsc.routing.util.AppConstants.UPDATE_SS7_CONFIG;

@ExtendWith(MockitoExtension.class)
class CustomFrameHandlerTest {
    @Mock
    LoadSettings loadSettings;
    @Mock
    LoadRoutingRules loadRoutingRules;
    @Mock
    LoadGateways loadGateways;
    @Mock
    LoadServiceProviders loadServiceProviders;
    @Mock
    CreditHandler creditHandler;

    @InjectMocks
    CustomFrameHandler customFrameHandler;

    @Test
    void handleFrameLogic_routingRules() {
        String payload = "systemId123";
        StompHeaders headers = new StompHeaders();

        headers.setDestination(UPDATE_ROUTING_RULE);
        assertDoesNotThrow(() -> customFrameHandler.handleFrameLogic(headers, payload));

        headers.setDestination(DELETE_ROUTING_RULE);
        assertDoesNotThrow(() -> customFrameHandler.handleFrameLogic(headers, payload));
    }

    @Test
    void handleFrameLogic_smppHttpSettings() {
        String payload = "systemId123";
        StompHeaders headers = new StompHeaders();

        headers.setDestination(UPDATE_GS_SMPP_HTTP);
        assertDoesNotThrow(() -> customFrameHandler.handleFrameLogic(headers, payload));
    }

    @Test
    void handleFrameLogic_ss7Settings() {
        String payload = "1";
        StompHeaders headers = new StompHeaders();

        headers.setDestination(UPDATE_SS7_CONFIG);
        assertDoesNotThrow(() -> customFrameHandler.handleFrameLogic(headers, payload));

        headers.setDestination(DELETE_SS7_CONFIG);
        assertDoesNotThrow(() -> customFrameHandler.handleFrameLogic(headers, payload));
    }

    @Test
    void handleFrameLogic_gateways() {
        String payload = "systemId123";
        StompHeaders headers = new StompHeaders();

        headers.setDestination(UPDATE_SMPP_GATEWAY);
        assertDoesNotThrow(() -> customFrameHandler.handleFrameLogic(headers, payload));

        headers.setDestination(DELETE_SMPP_GATEWAY);
        assertDoesNotThrow(() -> customFrameHandler.handleFrameLogic(headers, payload));
    }

    @Test
    void handleFrameLogic_serviceProviders() {
        String payload = "systemId123";
        StompHeaders headers = new StompHeaders();

        headers.setDestination(UPDATE_SERVICE_SMPP_PROVIDER_ENDPOINT);
        assertDoesNotThrow(() -> customFrameHandler.handleFrameLogic(headers, payload));

        headers.setDestination(DELETE_SERVICE_SMPP_PROVIDER_ENDPOINT);
        assertDoesNotThrow(() -> customFrameHandler.handleFrameLogic(headers, payload));
    }

    @Test
    void handleFrameLogic_unknownDestination() {
        StompHeaders headers = new StompHeaders();
        headers.setDestination("unknown");
        assertDoesNotThrow(() -> customFrameHandler.handleFrameLogic(headers, "payload"));
    }
}
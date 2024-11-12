package paicbd.smsc.routing.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreditHandlerTest {
    @Mock
    private AppProperties appProperties;

    @Mock
    private ConcurrentMap<Integer, AtomicLong> creditUsed;

    @InjectMocks
    private CreditHandler creditHandler;


    @Test
    void incrementCreditUsed() {
        this.creditUsed = new ConcurrentHashMap<>();
        this.creditHandler = new CreditHandler(appProperties, creditUsed);
        assertDoesNotThrow(() -> creditHandler.incrementCreditUsed(1, 1));
    }

    @Test
    void removeCreditUsed() {
        this.creditUsed = new ConcurrentHashMap<>();
        this.creditHandler = new CreditHandler(appProperties, creditUsed);
        assertDoesNotThrow(() -> creditHandler.removeCreditUsed("1"));
    }

    @Test
    void processAndSendData() {
        this.creditUsed = new ConcurrentHashMap<>();
        this.creditHandler = new CreditHandler(appProperties, creditUsed);
        assertDoesNotThrow(() -> creditHandler.processAndSendData());

        this.creditUsed.put(1, new AtomicLong(0));
        this.creditUsed.put(2, new AtomicLong(0));
        this.creditUsed.put(3, new AtomicLong(0));
        this.creditHandler = new CreditHandler(appProperties, creditUsed);
        assertDoesNotThrow(() -> creditHandler.processAndSendData());

        this.creditUsed.put(1, new AtomicLong(1));
        this.creditUsed.put(2, new AtomicLong(2));
        this.creditUsed.put(3, new AtomicLong(3));
        this.creditHandler = new CreditHandler(appProperties, creditUsed);
        assertDoesNotThrow(() -> creditHandler.processAndSendData());
    }

    @Test
    void processAndSendData_URL() {
        this.creditUsed = new ConcurrentHashMap<>();
        this.creditUsed.put(1, new AtomicLong(12));
        this.creditUsed.put(2, new AtomicLong(0));
        this.creditUsed.put(3, new AtomicLong(0));

        this.creditHandler = new CreditHandler(appProperties, creditUsed);

        when(this.appProperties.getBackendUrl()).thenReturn("http://localhost:8080/");
        when(this.appProperties.getInstanceName()).thenReturn("instanceName");
        when(this.appProperties.getBackendApiKey()).thenReturn("backendApiKey");

        assertDoesNotThrow(() -> creditHandler.processAndSendData());
        assertDoesNotThrow(() -> creditHandler.processAndSendData());
    }

    @Test
    void getCreditUsedByServiceProvider_SidNull() {
        this.creditUsed = new ConcurrentHashMap<>();
        this.creditHandler = new CreditHandler(appProperties, creditUsed);
        ConcurrentMap<Integer, AtomicInteger> creditUsedNull = new ConcurrentHashMap<>();
        creditUsedNull.put(1, new AtomicInteger(1));
        StepVerifier.create(creditHandler.getCreditUsedByServiceProvider(creditUsedNull))
                .expectNextCount(1)
                .verifyComplete();
    }
}
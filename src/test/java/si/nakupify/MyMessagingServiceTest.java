package si.nakupify;

import io.quarkus.test.junit.QuarkusTest;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import si.nakupify.service.MyMessagingService;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class MyMessagingServiceTest {

    @Inject
    MyMessagingService application;

    @Test
    void test() {
        assertEquals("HELLO", application.toUpperCase(Message.of("Hello")).getPayload());
        assertEquals("BONJOUR", application.toUpperCase(Message.of("bonjour")).getPayload());
    }
}

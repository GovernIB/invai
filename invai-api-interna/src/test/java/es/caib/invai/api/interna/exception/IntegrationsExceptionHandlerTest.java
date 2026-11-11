package es.caib.invai.api.interna.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link IntegrationsExceptionHandler}: each exception type is translated into the
 * expected HTTP status with the message in an {@code error} field.
 */
class IntegrationsExceptionHandlerTest {

    private final IntegrationsExceptionHandler handler = new IntegrationsExceptionHandler();

    @Test
    void handleTimeout_returnsGatewayTimeout() {
        ResponseEntity<Map<String, String>> response =
                handler.handleTimeout(new IntegrationTimeoutException("too slow", new RuntimeException()));

        assertEquals(HttpStatus.GATEWAY_TIMEOUT, response.getStatusCode());
        assertEquals("too slow", response.getBody().get("error"));
    }

    @Test
    void handleUnavailable_returnsBadGateway() {
        ResponseEntity<Map<String, String>> response =
                handler.handleUnavailable(new IntegrationUnavailableException("down", new RuntimeException()));

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertEquals("down", response.getBody().get("error"));
    }

    @Test
    void handleGeneric_returnsInternalServerError() {
        ResponseEntity<Map<String, String>> response = handler.handleGeneric(new IllegalStateException("boom"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("boom", response.getBody().get("error"));
    }
}

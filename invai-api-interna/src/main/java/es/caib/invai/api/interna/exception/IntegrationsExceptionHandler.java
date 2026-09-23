package es.caib.invai.api.interna.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Translates this module's integration failures into plain JSON error bodies, without the
 * i18n/{@code MessageSource} machinery {@code invai-back}'s own {@code GlobalExceptionHandler}
 * uses - this module is a technical proxy, not a user-facing application.
 *
 * @since 1.0.4
 */
@RestControllerAdvice
@Slf4j
public class IntegrationsExceptionHandler {

    /**
     * Maps a timed-out integration call to HTTP 504 Gateway Timeout.
     *
     * @param ex the timeout exception
     * @return a minimal JSON error body with the 504 statuses
     */
    @ExceptionHandler(IntegrationTimeoutException.class)
    public ResponseEntity<Map<String, String>> handleTimeout(IntegrationTimeoutException ex) {
        log.error("Integration timed out", ex);
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(Map.of("error", ex.getMessage()));
    }

    /**
     * Maps an unreachable/failing integration call to HTTP 502 Bad Gateway.
     *
     * @param ex the unavailability exception
     * @return a minimal JSON error body with the 502 statuses
     */
    @ExceptionHandler(IntegrationUnavailableException.class)
    public ResponseEntity<Map<String, String>> handleUnavailable(IntegrationUnavailableException ex) {
        log.error("Integration unavailable", ex);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("error", ex.getMessage()));
    }

    /**
     * Maps any other unexpected failure to HTTP 500 Internal Server Error.
     *
     * @param ex the unexpected exception
     * @return a minimal JSON error body with the 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
    }
}

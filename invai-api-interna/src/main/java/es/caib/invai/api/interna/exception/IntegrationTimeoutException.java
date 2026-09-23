package es.caib.invai.api.interna.exception;

/**
 * Thrown when an external integration (Soffid, DIR3CAIB, an application's own OpenAPI endpoint)
 * does not respond within the configured timeout. Mapped to HTTP 504 by
 * {@link IntegrationsExceptionHandler}.
 *
 * @since 1.0.4
 */
public class IntegrationTimeoutException extends RuntimeException {

    /**
     * Creates the exception with a plain diagnostic message and the underlying cause.
     *
     * @param message description of which integration timed out
     * @param cause   the underlying error
     */
    public IntegrationTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}

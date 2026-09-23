package es.caib.invai.api.interna.exception;

/**
 * Thrown when an external integration (Soffid, DIR3CAIB, an application's own OpenAPI endpoint)
 * cannot be reached or returns an unexpected error. Mapped to HTTP 502 by
 * {@link IntegrationsExceptionHandler}.
 *
 * @since 1.0.4
 */
public class IntegrationUnavailableException extends RuntimeException {

    /**
     * Creates the exception with a plain diagnostic message and the underlying cause.
     *
     * @param message description of which integration failed
     * @param cause   the underlying error
     */
    public IntegrationUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

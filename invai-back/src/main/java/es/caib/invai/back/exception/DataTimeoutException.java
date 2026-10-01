package es.caib.invai.back.exception;

import java.io.Serial;

/**
 * Thrown when fetching an application's Open Data/Reutilització OpenAPI document exceeds its
 * configured timeout. Kept distinct from {@link BusinessRuleException} in general so callers can
 * tell a timeout apart from any other fetch failure - e.g. {@link GlobalExceptionHandler} maps
 * this one to HTTP 504 (Gateway Timeout) instead of the generic 400 used for other
 * business/business-adjacent failures.
 *
 * @since 1.0.5
 */
public class DataTimeoutException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;
}

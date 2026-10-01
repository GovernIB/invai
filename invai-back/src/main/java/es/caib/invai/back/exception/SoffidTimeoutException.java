package es.caib.invai.back.exception;

import java.io.Serial;

/**
 * Thrown when a call to the Soffid SCIM 2.0 API exceeds its configured timeout. Kept distinct from
 * {@link SoffidClientException} (and from {@link BusinessRuleException} in general) so callers can
 * tell a timeout apart from any other Soffid failure - e.g. {@link GlobalExceptionHandler} maps
 * this one to HTTP 504 (Gateway Timeout) instead of the generic 400 used for other
 * business/business-adjacent failures.
 *
 * @since 1.0.5
 */
public class SoffidTimeoutException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;
}

package es.caib.invai.back.exception;

import java.io.Serial;

/**
 * Raised when the token of the user who launched a background job can no longer be used: their
 * login session is gone (logged out, expired at the identity provider) or its token can't be
 * renewed. Deliberately not an {@link IllegalStateException}: {@code SoffidClient} reads that type
 * as a request timeout.
 *
 * @since 1.0.6
 */
public class UserSessionTokenException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * @param message description of what went wrong
     */
    public UserSessionTokenException(String message) {
        super(message);
    }

    /**
     * @param message description of what went wrong
     * @param cause   the underlying failure
     */
    public UserSessionTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}

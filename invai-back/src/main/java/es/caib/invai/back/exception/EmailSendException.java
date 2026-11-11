package es.caib.invai.back.exception;

import java.io.Serial;

/**
 * Thrown when an e-mail can't be sent: the message can't be built or handed to the mail server
 * (unreachable, authentication refused, message refused). Kept distinct from
 * {@link BusinessRuleException} so {@link GlobalExceptionHandler} reports it as HTTP 502 (Bad
 * Gateway), as the failure is not in the request but in the mail server behind the application.
 *
 * @since 1.0.7
 */
public class EmailSendException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * @param cause what made the sending fail
     */
    public EmailSendException(Throwable cause) {
        super(cause);
    }
}

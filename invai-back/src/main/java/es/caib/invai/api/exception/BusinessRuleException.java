package es.caib.invai.api.exception;

/**
 * Custom runtime exception thrown when a corporate or business domain validation rule is violated.
 * <p>
 * This exception generally stores an i18n translation key instead of a raw message string,
 * allowing the global exception handler to localize the error response downstream.
 * </p>
 *
 * @since 1.0.0
 */
public class BusinessRuleException extends RuntimeException {

    /**
     * Constructs a new exception with the specified localization message key or raw message text.
     *
     * @param messageKey the resource bundle key or clear text explaining the reason for the exception
     */
    public BusinessRuleException(String messageKey) {
        super(messageKey);
    }
}
package es.caib.invai.back.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import es.caib.invai.back.utils.Constants;

import java.util.Locale;

/**
 * Global controller advice responsible for intercepting, translating, and standardizing
 * application errors across all REST controllers.
 * <p>
 * It automatically extracts localization keys from validation constraints or business failures
 * and maps them into a standard, localized error payload structure: HTTP 400 (Bad Request) for
 * validation/business-rule/persistence failures, and HTTP 500 (Internal Server Error) as the
 * final catch-all for anything else otherwise unhandled.
 * </p>
 *
 * @since 1.0.1
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Message source used to resolve localized validation and error messages. */
    @Autowired
    private MessageSource messageSource;

    /**
     * Handles payload bean validation exceptions raised by Spring's {@code @Valid} processing.
     * Extracts the first encountered structural error constraint and processes its localization.
     *
     * @param ex the intercepted validation exception context
     * @return a localized HTTP response entity containing the validation error details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        ObjectError firstError = ex.getBindingResult().getAllErrors().stream()
                .findFirst()
                .orElse(new ObjectError("application", Constants.VALIDATION_APPLICATION_DEFAULT));

        String rawMessage = firstError.getDefaultMessage();

        if (rawMessage != null && rawMessage.startsWith("{") && rawMessage.endsWith("}")) {
            String cleanKey = rawMessage.replace("{", "").replace("}", "");
            return buildLocalizedErrorResponse(cleanKey, firstError.getArguments(), true);
        } else {
            return buildLocalizedErrorResponse(rawMessage, null, false);
        }
    }

    /**
     * Intercepts explicit functional anomalies wrapped into a {@link BusinessRuleException}.
     * Parses key placeholders or standard text descriptors to issue localized API responses.
     *
     * @param ex the intercepted domain business exception details
     * @return a serialized localized HTTP response payload indicating the client error rule
     */
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ValidationErrorResponse> handleBusinessExceptions(BusinessRuleException ex) {
        String rawMessage = ex.getMessage();

        if (rawMessage == null) {
            return buildLocalizedErrorResponse(Constants.VALIDATION_APPLICATION_DEFAULT, null, true);
        }

        if (rawMessage.startsWith("{") && rawMessage.endsWith("}")) {
            rawMessage = rawMessage.replace("{", "").replace("}", "");
        }

        return buildLocalizedErrorResponse(rawMessage, null, true);
    }

    /**
     * Intercepts transaction flush/commit database exceptions. The raw database engine error
     * (which can include schema/query internals, e.g. Oracle {@code ORA-} messages) is logged
     * server-side only; the client always receives a generic, localized message.
     *
     * @param ex the intercepted persistence exception
     * @return a generic localized error response, never containing the raw database error text
     */
    @ExceptionHandler(jakarta.persistence.PersistenceException.class)
    public ResponseEntity<ValidationErrorResponse> handlePersistenceExceptions(jakarta.persistence.PersistenceException ex) {
        String databaseErrorMessage = null;

        Throwable cause = ex.getCause();
        while (cause != null) {
            if (cause instanceof java.sql.SQLException || (cause.getMessage() != null && cause.getMessage().contains("ORA-"))) {
                databaseErrorMessage = cause.getMessage();
                break;
            }
            cause = cause.getCause();
        }

        if (databaseErrorMessage == null) {
            databaseErrorMessage = ex.getMessage() != null ? ex.getMessage() : "Unknown persistence error";
        }
        log.error("Unhandled persistence exception: {}", databaseErrorMessage, ex);

        return buildLocalizedErrorResponse(Constants.ERR_PERSISTENCE_GENERIC, null, true);
    }

    /**
     * Catch-all for any exception not handled by a more specific {@code @ExceptionHandler} above
     * (a genuine programming error, an unexpected runtime failure, etc). Without this, such an
     * exception would fall through to the servlet container/Spring Boot's default error handling
     * instead of this API's standard, localized, i18n'd error payload shape. Logs the full
     * exception server-side; the client only ever receives a generic message, never any exception
     * detail.
     *
     * @param ex the intercepted exception
     * @return a generic localized error response with HTTP 500 (Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ValidationErrorResponse> handleUnexpectedExceptions(Exception ex) {
        log.error("Unhandled exception reached the global exception handler", ex);

        Locale currentLocale = LocaleContextHolder.getLocale();
        ValidationErrorResponse response = new ValidationErrorResponse(
                messageSource.getMessage(Constants.VALIDATION_APPLICATION_TITLE, null, currentLocale),
                messageSource.getMessage(Constants.ERR_UNEXPECTED_GENERIC, null, currentLocale)
        );

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Helper utility method that acts upon the resolved context locale to construct
     * a fully translated {@link ValidationErrorResponse}.
     *
     * @param messageOrKey the raw clear text message or resource bundle property entry key
     * @param args         optional substitution parameters passed to the message formatter
     * @param isKey        flag indicating if the string should be treated as a resource bundle translation key
     * @return the assembled response entity wrapper containing localized titles and text payloads
     */
    private ResponseEntity<ValidationErrorResponse> buildLocalizedErrorResponse(String messageOrKey, Object[] args, boolean isKey) {
        Locale currentLocale = LocaleContextHolder.getLocale();

        String translatedTitle = messageSource.getMessage(Constants.VALIDATION_APPLICATION_TITLE, null, currentLocale);

        String translatedMessage;
        if (isKey) {
            translatedMessage = messageSource.getMessage(messageOrKey, args, currentLocale);
        } else {
            translatedMessage = messageOrKey;
        }

        ValidationErrorResponse response = new ValidationErrorResponse(
                translatedTitle,
                translatedMessage
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

}
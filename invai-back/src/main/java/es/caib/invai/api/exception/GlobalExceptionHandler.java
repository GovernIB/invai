package es.caib.invai.api.exception;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Locale;

/**
 * Global controller advice responsible for intercepting, translating, and standardizing
 * application errors across all REST controllers.
 * <p>
 * It automatically extracts localization keys from validation constraints or business failures
 * and maps them into an explicit HTTP 400 (Bad Request) payload structure.
 * </p>
 *
 * @since 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

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
                .orElse(new ObjectError("application", "validation.application.default"));

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
            return buildLocalizedErrorResponse("validation.application.default", null, true);
        }

        if (rawMessage.startsWith("{") && rawMessage.endsWith("}")) {
            rawMessage = rawMessage.replace("{", "").replace("}", "");
        }

        return buildLocalizedErrorResponse(rawMessage, null, true);
    }

    /**
     * Intercepts transaction flush/commit database exceptions and exposes
     * the real raw database engine error message to the client response.
     *
     * @param ex the intercepted persistence exception
     * @return structured response containing the actual raw database error string
     */
    @ExceptionHandler(javax.persistence.PersistenceException.class)
    public ResponseEntity<ValidationErrorResponse> handlePersistenceExceptions(javax.persistence.PersistenceException ex) {
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
        return buildLocalizedErrorResponse(databaseErrorMessage, null, false);
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

        String translatedTitle = messageSource.getMessage("validation.application.title", null, currentLocale);

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
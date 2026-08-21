package es.caib.invai.back.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import jakarta.persistence.PersistenceException;
import java.sql.SQLException;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link GlobalExceptionHandler}, covering every {@code @ExceptionHandler}
 * method and its branching logic around raw text vs. translation-key style messages.
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private MessageSource messageSource;

    @Mock
    private MethodParameter methodParameter;

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        ReflectionTestUtils.setField(handler, "messageSource", messageSource);
        when(messageSource.getMessage(eq("validation.application.title"), any(), any(Locale.class)))
                .thenReturn("Validation Error");
    }

    private MethodArgumentNotValidException validationException(BindingResult bindingResult) {
        return new MethodArgumentNotValidException(methodParameter, bindingResult);
    }

    // --- handleValidationExceptions ---

    @Test
    void handleValidationExceptions_keyStyleMessage_resolvesTranslationKeyWithArguments() {
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        Object[] args = new Object[]{"foo"};
        bindingResult.reject("code", args, "{validation.application.name}");

        when(messageSource.getMessage(eq("validation.application.name"), eq(args), any(Locale.class)))
                .thenReturn("Name is required: foo");

        ResponseEntity<ValidationErrorResponse> response =
                handler.handleValidationExceptions(validationException(bindingResult));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation Error", response.getBody().getError());
        assertEquals("Name is required: foo", response.getBody().getMessage());
    }

    @Test
    void handleValidationExceptions_plainMessage_usesRawMessageDirectlyWithoutLookup() {
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.reject("code", null, "Plain text error");

        ResponseEntity<ValidationErrorResponse> response =
                handler.handleValidationExceptions(validationException(bindingResult));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Plain text error", response.getBody().getMessage());
        verify(messageSource, never()).getMessage(eq("Plain text error"), any(), any(Locale.class));
    }

    @Test
    void handleValidationExceptions_noErrors_fallsBackToUnresolvedDefaultKeyLiteral() {
        // No errors registered -> getAllErrors() is empty -> orElse default ObjectError is used.
        // NOTE: the default ObjectError's message ("validation.application.default") has no
        // surrounding braces, so the code's brace-detection branch treats it as a *raw message*
        // rather than a translation key, and it is never passed through messageSource. The
        // literal key string therefore leaks to the client untranslated (likely unintended).
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");

        ResponseEntity<ValidationErrorResponse> response =
                handler.handleValidationExceptions(validationException(bindingResult));

        assertEquals("validation.application.default", response.getBody().getMessage());
        verify(messageSource, never()).getMessage(eq("validation.application.default"), any(), any(Locale.class));
    }

    // --- handleBusinessExceptions ---

    @Test
    void handleBusinessExceptions_nullMessage_resolvesDefaultKey() {
        when(messageSource.getMessage(eq("validation.application.default"), eq(null), any(Locale.class)))
                .thenReturn("Default business error");

        ResponseEntity<ValidationErrorResponse> response =
                handler.handleBusinessExceptions(new BusinessRuleException(null));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Default business error", response.getBody().getMessage());
    }

    @Test
    void handleBusinessExceptions_keyStyleMessage_stripsBracesAndResolvesKey() {
        when(messageSource.getMessage(eq("exception.application.notfound"), eq(null), any(Locale.class)))
                .thenReturn("Application not found");

        ResponseEntity<ValidationErrorResponse> response =
                handler.handleBusinessExceptions(new BusinessRuleException("{exception.application.notfound}"));

        assertEquals("Application not found", response.getBody().getMessage());
    }

    @Test
    void handleBusinessExceptions_plainMessage_isStillTreatedAsTranslationKey() {
        // Unlike handleValidationExceptions, handleBusinessExceptions hardcodes isKey=true
        // regardless of braces, so a plain (non-braced) message is still resolved as an i18n
        // key via messageSource - inconsistent with the validation handler's behavior.
        when(messageSource.getMessage(eq("plain.text.without.braces"), eq(null), any(Locale.class)))
                .thenReturn("Resolved message");

        ResponseEntity<ValidationErrorResponse> response =
                handler.handleBusinessExceptions(new BusinessRuleException("plain.text.without.braces"));

        assertEquals("Resolved message", response.getBody().getMessage());
        verify(messageSource).getMessage(eq("plain.text.without.braces"), eq(null), any(Locale.class));
    }

    // --- handlePersistenceExceptions ---

    @Test
    void handlePersistenceExceptions_sqlExceptionCause_extractsRawDatabaseMessage() {
        SQLException sqlException = new SQLException("ORA-00001: unique constraint violated");
        PersistenceException ex = new PersistenceException("wrapper", sqlException);

        ResponseEntity<ValidationErrorResponse> response = handler.handlePersistenceExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("ORA-00001: unique constraint violated", response.getBody().getMessage());
    }

    @Test
    void handlePersistenceExceptions_oraMessageWithoutSqlExceptionType_isDetectedByContent() {
        RuntimeException wrapped = new RuntimeException("Something ORA-12345: failure occurred");
        PersistenceException ex = new PersistenceException("wrapper", wrapped);

        ResponseEntity<ValidationErrorResponse> response = handler.handlePersistenceExceptions(ex);

        assertEquals("Something ORA-12345: failure occurred", response.getBody().getMessage());
    }

    @Test
    void handlePersistenceExceptions_noMatchingCause_usesExceptionMessage() {
        PersistenceException ex = new PersistenceException("top level persistence failure");

        ResponseEntity<ValidationErrorResponse> response = handler.handlePersistenceExceptions(ex);

        assertEquals("top level persistence failure", response.getBody().getMessage());
    }

    @Test
    void handlePersistenceExceptions_noMessageAndNoCause_usesUnknownFallback() {
        PersistenceException ex = new PersistenceException((String) null);

        ResponseEntity<ValidationErrorResponse> response = handler.handlePersistenceExceptions(ex);

        assertEquals("Unknown persistence error", response.getBody().getMessage());
    }
}

package es.caib.invai.back.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link BusinessRuleException}, verifying that the message key or raw text
 * passed to the constructor is preserved and retrievable, and that it behaves as a standard
 * unchecked {@link RuntimeException}.
 */
class BusinessRuleExceptionTest {

    @Test
    void constructor_storesMessageKey() {
        BusinessRuleException ex = new BusinessRuleException("exception.application.notfound");

        assertEquals("exception.application.notfound", ex.getMessage());
    }

    @Test
    void constructor_withNullMessage_storesNull() {
        BusinessRuleException ex = new BusinessRuleException(null);

        assertNull(ex.getMessage());
    }

    @Test
    void isRuntimeException() {
        BusinessRuleException ex = new BusinessRuleException("{some.key}");

        assertInstanceOf(RuntimeException.class, ex);
        assertEquals("{some.key}", ex.getMessage());
    }
}

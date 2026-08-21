package es.caib.invai.back.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link ValidationErrorResponse}, verifying the Lombok-generated
 * all-args constructor and accessor behavior for both populated and null values.
 */
class ValidationErrorResponseTest {

    @Test
    void constructor_populatesErrorAndMessage() {
        ValidationErrorResponse response = new ValidationErrorResponse("Bad Request", "Field is required");

        assertEquals("Bad Request", response.getError());
        assertEquals("Field is required", response.getMessage());
    }

    @Test
    void constructor_allowsNullValues() {
        ValidationErrorResponse response = new ValidationErrorResponse(null, null);

        assertNull(response.getError());
        assertNull(response.getMessage());
    }
}

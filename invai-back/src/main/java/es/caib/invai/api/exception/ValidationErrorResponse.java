package es.caib.invai.api.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.Map;

/**
 * Standard data transfer object representing structured error responses returned by the API.
 * Contains localized informational metrics intended to be parsed by Frontend user interfaces.
 *
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public class ValidationErrorResponse {

    /** The error classification header or localized summary title string. */
    private String error;

    /** The verbose, descriptive error instruction message detailing what exactly went wrong. */
    private String message;
}
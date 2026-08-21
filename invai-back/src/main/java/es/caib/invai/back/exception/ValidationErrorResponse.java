package es.caib.invai.back.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Standard data transfer object representing structured error responses returned by the API.
 * Contains localized informational metrics intended to be parsed by Frontend user interfaces.
 *
 * @since 1.0.1
 */
@Getter
@AllArgsConstructor
public class ValidationErrorResponse {

    /** The error classification header or localized summary title string. */
    private String error;

    /** The verbose, descriptive error instruction message detailing what exactly went wrong. */
    private String message;
}
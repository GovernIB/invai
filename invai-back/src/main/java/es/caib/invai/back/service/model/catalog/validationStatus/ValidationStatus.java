package es.caib.invai.back.service.model.catalog.validationStatus;

import lombok.Getter;
import lombok.Setter;

/**
 * Rich domain dictionary object carrying the translated display labels for a validation
 * state, mirroring {@code Status}.
 *
 * @since 1.0.5
 */
@Getter
@Setter
public class ValidationStatus {

    /** Unique identifier of the validation status lookup entry. */
    private Long id;

    /** Fixed technical code for the state (e.g. {@code "VALIDATED"}). */
    private String code;

    /** Display label of the state (typically Catalan). */
    private String name;

    /** Display label of the state in Spanish. */
    private String nameEs;
}

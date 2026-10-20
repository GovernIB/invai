package es.caib.invai.back.service.model.catalog.dir3Status;

import lombok.Getter;
import lombok.Setter;

/**
 * Rich domain dictionary object carrying the translated display labels for a DIR3 validation
 * state, mirroring {@code Status}.
 *
 * @since 1.0.5
 */
@Getter
@Setter
public class Dir3Status {

    /** Unique identifier of the DIR3 status lookup entry. */
    private Long id;

    /** Fixed technical code for the state (e.g. {@code "VALIDATED"}). */
    private String code;

    /** Display label of the state (typically Catalan). */
    private String name;

    /** Display label of the state in Spanish. */
    private String nameEs;
}

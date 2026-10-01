package es.caib.invai.back.interna.catalog.dir3Status.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) modeling the standard outbound response payload for a DIR3
 * validation status lookup entry.
 *
 * @since 1.0.5
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Dir3StatusOutputDTO {

    /** Unique identifier of the DIR3 status lookup entry. */
    private Long id;

    /** Fixed technical code for the state (e.g. {@code "VALIDATED"}). */
    private String code;

    /** Display label of the state (typically Catalan). */
    private String name;

    /** Display label of the state in Spanish. */
    private String nameEs;
}

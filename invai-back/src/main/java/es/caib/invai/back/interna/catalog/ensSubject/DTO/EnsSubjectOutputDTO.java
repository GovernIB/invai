package es.caib.invai.back.interna.catalog.ensSubject.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) modeling the standard outbound response payload for an
 * ENS subjection lookup entry.
 *
 * @since 1.0.4
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EnsSubjectOutputDTO {

    /** Unique identifier of the ENS subjection lookup entry. */
    private Long id;

    /** Display label of the ENS subjection (typically Catalan). */
    private String name;

    /** Display label of the ENS subjection in Spanish. */
    private String nameEs;

}

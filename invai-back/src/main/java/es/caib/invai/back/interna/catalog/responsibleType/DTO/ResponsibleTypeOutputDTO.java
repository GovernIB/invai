package es.caib.invai.back.interna.catalog.responsibleType.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) modeling the standard outbound response payload for a Responsible
 * Type lookup entry.
 *
 * @since 1.0.3
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResponsibleTypeOutputDTO {

    /** Unique identifier of the responsible type lookup entry. */
    private Long id;

    /** Display label of the responsible type (typically Catalan). */
    private String name;

    /** Display label of the responsible type in Spanish. */
    private String nameEs;

    /** Whether this responsible type may only be held by Personal CAIB persons. */
    private boolean requiresPersonalCaib;
}

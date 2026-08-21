package es.caib.invai.back.interna.catalog.modality.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) modeling the standard outbound response payload for a Modality
 * lookup entry.
 *
 * @since 1.0.3
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ModalityOutputDTO {

    /** Unique identifier of the modality lookup entry. */
    private Long id;

    /** Display label of the modality (typically Catalan). */
    private String name;

    /** Display label of the modality in Spanish. */
    private String nameEs;
}

package es.caib.invai.back.interna.catalog.status.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) modeling the standard outbound response payload for a Status
 * lookup entry.
 *
 * @since 1.0.3
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StatusOutputDTO {

    /** Unique identifier of the status lookup entry. */
    private Long id;

    /** Display label of the status (typically Catalan). */
    private String name;

    /** Display label of the status in Spanish. */
    private String nameEs;
}

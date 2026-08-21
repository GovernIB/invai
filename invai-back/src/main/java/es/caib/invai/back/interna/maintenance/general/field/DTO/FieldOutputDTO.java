package es.caib.invai.back.interna.maintenance.general.field.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) modeling the standard outbound response payload
 * for an operational Field resource.
 * <p>
 * Shields internal database entities by exposing only the structural properties
 * required by frontend consumers.
 * </p>
 *
 * @since 1.0.1
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FieldOutputDTO {

    /** The unique database persistent primary key identifier. */
    private Long id;

    /** The official localized descriptor name of the field (Catalan). */
    private String name;

    /** The translated Spanish localized descriptive name of the field. */
    private String nameEs;

    /** Timestamp when this record was soft-deleted, or {@code null} if still active. */
    private LocalDateTime deletedAt;
}
package es.caib.invai.api.interna.field.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) modeling the standard outbound response payload
 * for an operational Field resource.
 * <p>
 * Shields internal database entities by exposing only the structural properties
 * required by frontend consumers.
 * </p>
 *
 * @since 1.0.0
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
}
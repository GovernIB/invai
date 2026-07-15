package es.caib.invai.api.interna.commission.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) modeling the standard outbound response payload
 * for a Commission resource.
 * <p>
 * Projects the foundational identity and localized naming metrics back to the client application,
 * shielding the core JPA implementation structures.
 * </p>
 *
 * @since 1.0.0
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommissionOutputDTO {

    /** The unique data storage database persistent primary key. */
    private Long id;

    /** The official localized descriptive name of the commission (Catalan). */
    private String name;

    /** The translated Spanish localized descriptive name of the commission. */
    private String nameEs;
}
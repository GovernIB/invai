package es.caib.invai.api.interna.category.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) modeling the standard outbound response payload
 * for a Category resource.
 * <p>
 * Decouples internal persistence schemas from client contracts by presenting
 * only public-facing structural properties.
 * </p>
 *
 * @since 1.0.0
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryOutputDTO {

    /** The unique data storage database auto-incremented primary key. */
    private Long id;

    /** The official localized descriptor name of the category (Catalan). */
    private String name;

    /** The translated Spanish localized descriptive name of the category. */
    private String nameEs;
}
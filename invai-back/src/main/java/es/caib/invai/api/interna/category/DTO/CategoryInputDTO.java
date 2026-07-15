package es.caib.invai.api.interna.category.DTO;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) defining the inbound API request payload structure
 * for registering or modifying taxonomic classification Category resource data entries.
 * <p>
 * Enforces string field constraint parameters coupled with localized internationalization error bundles.
 * </p>
 *
 * @since 1.0.0
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryInputDTO {

    /**
     * Localized descriptive classification title label string (typically in Catalan).
     * Constraint demands content presence capped at a maximum sequence of 50 characters.
     */
    @NotBlank(message = "{validation.category.name.required}")
    @Size(max = 50, message = "{validation.category.name.size}")
    private String name;

    /**
     * Secondary translated description text variant explicitly matching Spanish locale records.
     * Constraint demands content presence capped at a maximum sequence of 100 characters.
     */
    @NotBlank(message = "{validation.category.nameEs.required}")
    @Size(max = 100, message = "{validation.category.nameEs.size}")
    private String nameEs;
}
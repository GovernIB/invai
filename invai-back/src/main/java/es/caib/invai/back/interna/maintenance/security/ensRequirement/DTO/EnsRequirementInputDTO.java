package es.caib.invai.back.interna.maintenance.security.ensRequirement.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) defining the inbound API request payload structure
 * for registering or modifying EnsRequirement resource data entries.
 *
 * @since 1.0.4
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EnsRequirementInputDTO {

    /** ENS requirement descriptive label string. */
    @NotBlank(message = "{validation.ensrequirement.name.required}")
    @Size(max = 150, message = "{validation.ensrequirement.name.size}")
    private String name;

    /**
     * Secondary translated description text variant explicitly matching Spanish locale records.
     * Constraint demands content presence capped at a maximum sequence of 100 characters.
     */
    @NotBlank(message = "{validation.ensrequirement.nameEs.required}")
    @Size(max = 100, message = "{validation.ensrequirement.nameEs.size}")
    private String nameEs;
}

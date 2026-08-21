package es.caib.invai.back.interna.maintenance.development.role.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) defining the inbound API request payload structure
 * for registering or modifying provider Role resource data entries.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoleInputDTO {

    /** Localized descriptive role title label string (typically in Catalan). */
    @NotBlank(message = "{validation.role.name.required}")
    @Size(max = 100, message = "{validation.role.name.size}")
    private String name;

    /** Secondary translated description text variant explicitly matching Spanish locale records. */
    @Size(max = 100, message = "{validation.role.nameEs.size}")
    private String nameEs;
}

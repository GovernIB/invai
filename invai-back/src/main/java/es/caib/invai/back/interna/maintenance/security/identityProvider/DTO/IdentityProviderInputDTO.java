package es.caib.invai.back.interna.maintenance.security.identityProvider.DTO;

import es.caib.invai.back.utils.Constants;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) defining the inbound API request payload structure
 * for registering or modifying IdentityProvider resource data entries.
 *
 * @since 1.0.4
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IdentityProviderInputDTO {

    /** Identity provider descriptive label string (e.g. "Cl@ve"). */
    @NotBlank(message = "{" + Constants.VALIDATION_IDENTITYPROVIDER_NAME_REQUIRED + "}")
    @Size(max = 150, message = "{" + Constants.VALIDATION_IDENTITYPROVIDER_NAME_SIZE + "}")
    private String name;
}

package es.caib.invai.back.interna.maintenance.responsible.authorizationType.DTO;

import es.caib.invai.back.utils.Constants;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) defining the inbound API request payload structure
 * for registering or modifying AuthorizationType resource data entries.
 *
 * @since 1.0.3
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthorizationTypeInputDTO {

    /** Authorization type descriptive label string (e.g. "Firmar peticiones"). */
    @NotBlank(message = "{" + Constants.VALIDATION_AUTHORIZATIONTYPE_NAME_REQUIRED + "}")
    @Size(max = 150, message = "{" + Constants.VALIDATION_AUTHORIZATIONTYPE_NAME_SIZE + "}")
    private String name;

    /**
     * Secondary translated description text variant explicitly matching Spanish locale records.
     * Constraint demands content presence capped at a maximum sequence of 100 characters.
     */
    @NotBlank(message = "{" + Constants.VALIDATION_AUTHORIZATIONTYPE_NAME_ES_REQUIRED + "}")
    @Size(max = 100, message = "{" + Constants.VALIDATION_AUTHORIZATIONTYPE_NAME_ES_SIZE + "}")
    private String nameEs;
}

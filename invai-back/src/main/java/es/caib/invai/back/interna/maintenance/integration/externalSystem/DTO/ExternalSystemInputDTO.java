package es.caib.invai.back.interna.maintenance.integration.externalSystem.DTO;

import es.caib.invai.back.utils.Constants;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) defining the inbound API request payload structure
 * for registering or modifying External System resource data entries.
 *
 * @since 1.0.5
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExternalSystemInputDTO {

    /** External system name. */
    @NotBlank(message = "{" + Constants.VALIDATION_EXTERNALSYSTEM_NAME_REQUIRED + "}")
    @Size(max = 150, message = "{" + Constants.VALIDATION_EXTERNALSYSTEM_NAME_SIZE + "}")
    private String name;

    /** Identifier of the company responsible for the external system. */
    @NotNull(message = "{" + Constants.VALIDATION_EXTERNALSYSTEM_COMPANY_ID_REQUIRED + "}")
    private Long companyId;
}

package es.caib.invai.back.interna.application.development.technology.DTO;

import es.caib.invai.back.utils.Constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Inbound payload for creating or updating a technology stack entry linked to a development module.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppTechnologyInputDTO {

    /** Identifier of the owning development record. */
    @NotNull(message = "{" + Constants.VALIDATION_TECHNOLOGY_APP_DEVELOPMENT_ID + "}")
    private Long appDevelopmentId;

    /** Identifier of the architecture layer catalog entry. */
    @NotNull(message = "{" + Constants.VALIDATION_TECHNOLOGY_LAYER_ID + "}")
    private Long layerId;

    /** Identifier of the technology catalog entry. */
    @NotNull(message = "{" + Constants.VALIDATION_TECHNOLOGY_TECHNOLOGY_ID + "}")
    private Long technologyId;

    /** Version release details. */
    @NotBlank(message = "{" + Constants.VALIDATION_TECHNOLOGY_VERSION_REQUIRED + "}")
    @Size(max = 255, message = "{" + Constants.VALIDATION_TECHNOLOGY_VERSION_OVERFLOW + "}")
    private String version;

    /** Architectural model description. */
    @NotBlank(message = "{" + Constants.VALIDATION_TECHNOLOGY_ARCHITECTURE_REQUIRED + "}")
    @Size(max = 255, message = "{" + Constants.VALIDATION_TECHNOLOGY_ARCHITECTURE_OVERFLOW + "}")
    private String architecture;
}

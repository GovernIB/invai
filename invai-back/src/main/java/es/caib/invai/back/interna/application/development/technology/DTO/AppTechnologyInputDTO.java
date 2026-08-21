package es.caib.invai.back.interna.application.development.technology.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Inbound validation data transport contract containing properties required for
 * instantiation and mutation of a technology stack entry linked to a development module.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppTechnologyInputDTO {

    /** Foreign key unique identification pointer referencing the parent development module entry. */
    @NotNull(message = "{validation.technology.appDevelopmentId}")
    private Long appDevelopmentId;

    /** Foreign key unique identification pointer referencing the architecture layer catalog entry. */
    @NotNull(message = "{validation.technology.layerId}")
    private Long layerId;

    /** Foreign key unique identification pointer referencing the technology catalog entry. */
    @NotNull(message = "{validation.technology.technologyId}")
    private Long technologyId;

    /** Version release details. */
    @NotBlank(message = "{validation.technology.version.required}")
    @Size(max = 255, message = "{validation.technology.version.overflow}")
    private String version;

    /** Architectural model description. */
    @NotBlank(message = "{validation.technology.architecture.required}")
    @Size(max = 255, message = "{validation.technology.architecture.overflow}")
    private String architecture;
}

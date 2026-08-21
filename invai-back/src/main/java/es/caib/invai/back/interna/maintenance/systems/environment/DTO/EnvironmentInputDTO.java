package es.caib.invai.back.interna.maintenance.systems.environment.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) capturing incoming payload attributes required to register or update
 * an Environment deployment profile record within the enterprise framework.
 *
 * @since 1.0.1
 */
@Getter
@Setter
public class EnvironmentInputDTO {

    /** Mnemonic alphanumeric code identifying the execution environment. */
    @NotBlank(message = "{validation.environment.code}")
    @Size(max = 50, message = "{validation.environment.code.overflow}")
    private String code;

    /** Descriptive descriptive text name in Catalan language. */
    @NotBlank(message = "{validation.environment.name}")
    @Size(max = 100, message = "{validation.environment.name.overflow}")
    private String name;

    /** Descriptive descriptive text name in Spanish language. */
    @NotBlank(message = "{validation.environment.name_es}")
    @Size(max = 100, message = "{validation.environment.name_es.overflow}")
    private String nameEs;
}
package es.caib.invai.back.interna.maintenance.systems.environment.DTO;

import es.caib.invai.back.utils.Constants;

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
    @NotBlank(message = "{" + Constants.VALIDATION_ENVIRONMENT_CODE + "}")
    @Size(max = 50, message = "{" + Constants.VALIDATION_ENVIRONMENT_CODE_OVERFLOW + "}")
    private String code;

    /** Descriptive descriptive text name in Catalan language. */
    @NotBlank(message = "{" + Constants.VALIDATION_ENVIRONMENT_NAME + "}")
    @Size(max = 100, message = "{" + Constants.VALIDATION_ENVIRONMENT_NAME_OVERFLOW + "}")
    private String name;

    /** Descriptive descriptive text name in Spanish language. */
    @NotBlank(message = "{" + Constants.VALIDATION_ENVIRONMENT_NAME_ES + "}")
    @Size(max = 100, message = "{" + Constants.VALIDATION_ENVIRONMENT_NAME_ES_OVERFLOW + "}")
    private String nameEs;
}
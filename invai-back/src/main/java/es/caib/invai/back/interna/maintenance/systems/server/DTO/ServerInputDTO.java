package es.caib.invai.back.interna.maintenance.systems.server.DTO;

import es.caib.invai.back.utils.Constants;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) capturing incoming payload attributes required to register or update
 * a Server infrastructure asset record within the enterprise inventory system framework.
 *
 * @since 1.0.2
 */
@Getter
@Setter
public class ServerInputDTO {

    /** Host name or server identification label. */
    @NotBlank(message = "{" + Constants.VALIDATION_SERVER_NAME + "}")
    @Size(max = 255, message = "{" + Constants.VALIDATION_SERVER_NAME_OVERFLOW + "}")
    private String name;

    /** Foreign key unique identification pointer referencing the target deployment Environment zone. */
    @NotNull(message = "{" + Constants.VALIDATION_SERVER_ENVIRONMENT_ID + "}")
    private Long environmentId;

    /** Foreign key reference pointing to the server type lookup entry. */
    @NotNull(message = "{" + Constants.VALIDATION_SERVER_SERVER_TYPE_ID + "}")
    private Long serverTypeId;
}

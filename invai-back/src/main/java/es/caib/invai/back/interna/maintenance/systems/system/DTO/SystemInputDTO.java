package es.caib.invai.back.interna.maintenance.systems.system.DTO;

import es.caib.invai.back.utils.Constants;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) capturing incoming payload attributes required to register or update
 * a System software or infrastructure asset record within the enterprise inventory system framework.
 *
 * @since 1.0.2
 */
@Getter
@Setter
public class SystemInputDTO {

    /** Foreign key unique identification pointer referencing the host Server this system runs on. */
    @NotNull(message = "{" + Constants.VALIDATION_SYSTEM_SERVER_ID + "}")
    private Long serverId;

    /** System instance name, connection identifier or node routing string. */
    @NotBlank(message = "{" + Constants.VALIDATION_SYSTEM_INSTANCE + "}")
    @Size(max = 50, message = "{" + Constants.VALIDATION_SYSTEM_INSTANCE_OVERFLOW + "}")
    private String instance;

    /** Operational connection network port number. */
    @NotNull(message = "{" + Constants.VALIDATION_SYSTEM_PORT + "}")
    @Min(value = 1, message = "{" + Constants.VALIDATION_SYSTEM_PORT_OUT_OF_BOUNDS + "}")
    @Max(value = 65535, message = "{" + Constants.VALIDATION_SYSTEM_PORT_OUT_OF_BOUNDS + "}")
    private Integer port;

    /** Release version tag representing current deployment state. */
    @NotBlank(message = "{" + Constants.VALIDATION_SYSTEM_VERSION + "}")
    @Size(max = 20, message = "{" + Constants.VALIDATION_SYSTEM_VERSION_OVERFLOW + "}")
    private String version;

    /** Short definition narrative detailing system context targets. */
    private String description;
}
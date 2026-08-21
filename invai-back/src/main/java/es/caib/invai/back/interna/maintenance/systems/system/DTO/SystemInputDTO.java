package es.caib.invai.back.interna.maintenance.systems.system.DTO;

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
    @NotNull(message = "{validation.system.serverId}")
    private Long serverId;

    /** System instance name, connection identifier or node routing string. */
    @NotBlank(message = "{validation.system.instance}")
    @Size(max = 50, message = "{validation.system.instance.overflow}")
    private String instance;

    /** Operational connection network port number. */
    @NotNull(message = "{validation.system.port}")
    private Integer port;

    /** Release version tag representing current deployment state. */
    @NotBlank(message = "{validation.system.version}")
    @Size(max = 20, message = "{validation.system.version.overflow}")
    private String version;

    /** Short definition narrative detailing system context targets. */
    private String description;
}
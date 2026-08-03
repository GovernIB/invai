package es.caib.invai.api.interna.maintenance.server.DTO;

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
    @NotBlank(message = "{validation.server.name}")
    @Size(max = 255, message = "{validation.server.name.overflow}")
    private String name;

    /** Foreign key unique identification pointer referencing the target deployment Environment zone. */
    @NotNull(message = "{validation.server.environmentId}")
    private Long environmentId;

    /** Foreign key reference pointing to the server type lookup entry. */
    @NotNull(message = "{validation.server.serverTypeId}")
    private Long serverTypeId;
}

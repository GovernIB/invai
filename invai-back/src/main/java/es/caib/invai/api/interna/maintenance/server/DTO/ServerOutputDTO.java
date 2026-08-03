package es.caib.invai.api.interna.maintenance.server.DTO;

import es.caib.invai.api.interna.maintenance.environment.DTO.EnvironmentOutputDTO;
import es.caib.invai.api.service.model.ServerType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) wrapping outgoing host server state metadata payload models.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServerOutputDTO {

    /** Primary persistent storage unique sequence row reference. */
    private Long id;

    /** Host name or server identification label. */
    private String name;

    /** Resolved deployment environment zone snapshot. */
    private EnvironmentOutputDTO environment;

    /** Resolved server type lookup snapshot. */
    private ServerType serverType;

    private LocalDateTime deletedAt;
}

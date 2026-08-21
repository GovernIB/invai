package es.caib.invai.back.interna.maintenance.systems.server.DTO;

import es.caib.invai.back.interna.maintenance.systems.environment.DTO.EnvironmentOutputDTO;
import es.caib.invai.back.service.model.maintenance.systems.serverType.ServerType;
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

    /** Timestamp at which the server was logically deleted, or {@code null} if still active. */
    private LocalDateTime deletedAt;
}

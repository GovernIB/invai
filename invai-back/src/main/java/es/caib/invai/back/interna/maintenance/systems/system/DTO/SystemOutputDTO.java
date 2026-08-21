package es.caib.invai.back.interna.maintenance.systems.system.DTO;

import es.caib.invai.back.interna.maintenance.systems.server.DTO.ServerOutputDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


/**
 * Data Transfer Object (DTO) wrapping outgoing infrastructure system state metadata payload models.
 * <p>
 * Aggregates complete domain entity snapshots along with automated timeline audit log metadata.
 * </p>
 *
 * @since 1.0.2
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SystemOutputDTO {

    /** Primary persistent storage unique sequence row reference. */
    private Long id;

    /** Resolved host server snapshot whose name matches this system's name. */
    private ServerOutputDTO server;

    /** System instance name, connection identifier, or node routing string. */
    private String instance;

    /** Operational connection network port number. */
    private Integer port;

    /** Release version tag representing current deployment state. */
    private String version;

    /** Detailed administrative description text. */
    private String description;

    /** Timestamp at which the system was logically deleted, or {@code null} if still active. */
    private LocalDateTime deletedAt;
}
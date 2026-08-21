package es.caib.invai.back.service.model.maintenance.systems.system;

import lombok.*;

import java.time.LocalDateTime;
import es.caib.invai.back.service.model.maintenance.systems.server.Server;

/**
 * Domain Model object representing pure system asset infrastructure logic schemas, linked to a host server.
 *
 * @since 1.0.2
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class System {
    /** Unique system asset entry sequence key. */
    private Long id;

    /** Resolved host server this system runs on. */
    private Server server;

    /** System instance name, connection identifier or node routing string. */
    private String instance;

    /** Operational connection network port number. */
    private Integer port;

    /** Release version tag representing current deployment state. */
    private String version;

    /** Short definition narrative detailing system context targets. */
    private String description;

    /** Timestamp when this record was created. */
    private LocalDateTime createdAt;
    /** User who created this record. */
    private String createdBy;
    /** Timestamp of the last update to this record. */
    private LocalDateTime updatedAt;
    /** User who last updated this record. */
    private String updatedBy;
    /** Timestamp when this record was soft-deleted, or {@code null} if still active. */
    private LocalDateTime deletedAt;
    /** User who soft-deleted this record. */
    private String deletedBy;
}
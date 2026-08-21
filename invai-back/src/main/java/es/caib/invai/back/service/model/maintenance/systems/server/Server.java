package es.caib.invai.back.service.model.maintenance.systems.server;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import es.caib.invai.back.service.model.maintenance.systems.environment.Environment;
import es.caib.invai.back.service.model.maintenance.systems.serverType.ServerType;

/**
 * Domain Model object representing pure host server infrastructure logic schemas.
 *
 * @since 1.0.2
 */
@Getter
@Setter
public class Server {

    /** Unique identifier. */
    private Long id;
    /** Host server name. */
    private String name;
    /** Execution environment this server belongs to. */
    private Environment environment;
    /** Classification of this server's role or platform. */
    private ServerType serverType;

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

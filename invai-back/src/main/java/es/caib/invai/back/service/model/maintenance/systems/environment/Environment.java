package es.caib.invai.back.service.model.maintenance.systems.environment;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Domain Model object representing pure system execution environment logic schemas.
 *
 * @since 1.0.1
 */
@Getter
@Setter
public class Environment {

    /** Unique identifier. */
    private Long id;
    /** Short unique environment code. */
    private String code;
    /** Environment name, typically in Catalan. */
    private String name;
    /** Environment name in Spanish. */
    private String nameEs;

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
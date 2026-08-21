package es.caib.invai.back.service.model.maintenance.development.role;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Domain entity model configuring provider roles assignable within application development modules.
 *
 * @since 1.0.2
 */
@Getter
@Setter
public class Role {

    /** Unique database role index identifier key. */
    private Long id;

    /** Primary descriptive role title label string (typically Catalan). */
    private String name;

    /** Secondary descriptive role title label string matching Spanish locales. */
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

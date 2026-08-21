package es.caib.invai.back.service.model.maintenance.development.layer;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Domain entity model configuring architecture layers assignable to technologies.
 *
 * @since 1.0.2
 */
@Getter
@Setter
public class Layer {

    /** Unique identifier. */
    private Long id;

    /** Layer name. */
    private String name;

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

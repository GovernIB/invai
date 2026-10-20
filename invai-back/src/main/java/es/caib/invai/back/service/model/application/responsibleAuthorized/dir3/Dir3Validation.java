package es.caib.invai.back.service.model.application.responsibleAuthorized.dir3;

import lombok.*;
import java.time.LocalDateTime;
import es.caib.invai.back.service.model.catalog.dir3Status.Dir3ValidationStatus;

/**
 * Domain model representing the DIR3 validation state of a single {@code AppResponsible}/
 * {@code AppAuthorized} assignment.
 *
 * @since 1.0.5
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Dir3Validation {
    /** Unique identifier. */
    private Long id;
    /** Current DIR3 validation state. */
    private Dir3ValidationStatus dir3Status;
    /** Timestamp of the manual DIR3 validation, populated only when {@link #dir3Status} is MANUAL. */
    private LocalDateTime manualValidatedAt;
    /** User who performed the manual DIR3 validation. */
    private String manualValidatedBy;
    /** Free-text reason given when manually validating, populated only when {@link #dir3Status} is MANUAL. */
    private String reason;
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

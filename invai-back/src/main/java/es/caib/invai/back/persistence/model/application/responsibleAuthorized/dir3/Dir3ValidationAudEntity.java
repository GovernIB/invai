package es.caib.invai.back.persistence.model.application.responsibleAuthorized.dir3;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Flat audit trail entity capturing historical row-level changes for {@link Dir3ValidationEntity},
 * mapped onto the {@code INV_DIR3_VALIDATION_AUD} table.
 *
 * @since 1.0.5
 */
@Entity
@Table(name = "INV_DIR3_VALIDATION_AUD")
@Getter
@Setter
public class Dir3ValidationAudEntity {

    /** Primary key unique identifier of this audit row. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_DIR3_VALIDATION_AUD_SEQ")
    @SequenceGenerator(name = "INV_DIR3_VALIDATION_AUD_SEQ", sequenceName = "INV_DIR3_VALIDATION_AUD_SEQ", allocationSize = 1)
    @Column(name = "AUDIT_ID", nullable = false, updatable = false)
    private Long auditId;

    /** Identifier of the {@code Dir3ValidationEntity} row this audit entry mirrors. */
    @Column(name = "DIR3_VALIDATION_ID", nullable = false)
    private Long dir3ValidationId;

    /** Identifier of the DIR3 validation state at the time of the change. */
    @Column(name = "DIR3_STATUS_ID", nullable = false)
    private Long dir3StatusId;

    /** Timestamp of the manual DIR3 validation at the time of the change, if any. */
    @Column(name = "MANUAL_VALIDATED_AT")
    private LocalDateTime manualValidatedAt;

    /** User who performed the manual DIR3 validation, if any. */
    @Column(name = "MANUAL_VALIDATED_BY")
    private String manualValidatedBy;

    /** Free-text reason given when manually validating, if any. */
    @Column(name = "REASON", length = 4000)
    private String reason;

    /** Timestamp when the mirrored row was created. */
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;
    /** User identifier who created the mirrored row. */
    @Column(name = "CREATED_BY")
    private String createdBy;
    /** Timestamp of the last update to the mirrored row. */
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;
    /** User identifier who last updated the mirrored row. */
    @Column(name = "UPDATED_BY")
    private String updatedBy;
    /** Timestamp when the mirrored row was soft-deleted, or {@code null} if still active. */
    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;
    /** User identifier who soft-deleted the mirrored row. */
    @Column(name = "DELETED_BY")
    private String deletedBy;
    /** Type of change recorded by this audit entry (e.g. insert, update, delete). */
    @Column(name = "AUD_ACTION", nullable = false, length = 10)
    private String audAction;
    /** Timestamp when this audit entry itself was recorded. */
    @Column(name = "AUDIT_DATE", nullable = false)
    private LocalDateTime auditDate;
    /** User identifier who triggered the change captured by this audit entry. */
    @Column(name = "AUDIT_USER", length = 64)
    private String auditUser;
}

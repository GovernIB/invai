package es.caib.invai.back.persistence.model.maintenance.classificationSegment;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Historical snapshot entity capturing data mutations and transactional state changes
 * targeting the {@code INV_CLASSIFICATION_SEGMENT} database table.
 *
 * @since 1.0.4
 */
@Getter
@Setter
@Entity
@Table(name = "INV_CLASSIFICATION_SEGMENT_AUD")
public class ClassificationSegmentAudEntity {

    /** Unique auto-generated primary key of the audit record. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_classification_segment_aud_seq")
    @SequenceGenerator(name = "inv_classification_segment_aud_seq", sequenceName = "INV_CLASSIFICATION_SEGMENT_AUD_SEQ", allocationSize = 1)
    @Column(name = "AUDIT_ID", nullable = false, updatable = false)
    private Long auditId;

    /** Identifier of the {@code ClassificationSegmentEntity} this audit snapshot refers to. */
    @Column(name = "CLASSIFICATION_SEGMENT_ID", nullable = false)
    private Long classificationSegmentId;

    /** Classification segment descriptive label captured at the time of the audited change. */
    @Column(name = "NAME", length = 150, nullable = false)
    private String name;

    /** Spanish localized descriptive name captured at the time of the audited change. */
    @Column(name = "NAME_ES", length = 100)
    private String nameEs;

    /** Creation timestamp of the source record at the time of the audited change. */
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    /** Username that created the source record at the time of the audited change. */
    @Column(name = "CREATED_BY", length = 64)
    private String createdBy;

    /** Last update timestamp of the source record at the time of the audited change. */
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    /** Username that last updated the source record at the time of the audited change. */
    @Column(name = "UPDATED_BY", length = 64)
    private String updatedBy;

    /** Logical deletion timestamp of the source record at the time of the audited change. */
    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    /** Username that logically deleted the source record at the time of the audited change. */
    @Column(name = "DELETED_BY", length = 64)
    private String deletedBy;

    /** Type of operation that triggered this audit snapshot (e.g. {@code INSERT}, {@code UPDATE}, {@code DELETE}). */
    @Column(name = "AUD_ACTION", length = 10, nullable = false)
    private String audAction;

    /** Timestamp at which this audit snapshot was recorded. */
    @Column(name = "AUDIT_DATE", nullable = false)
    private LocalDateTime auditDate;

    /** Username responsible for the operation that generated this audit snapshot. */
    @Column(name = "AUDIT_USER", length = 64)
    private String auditUser;
}

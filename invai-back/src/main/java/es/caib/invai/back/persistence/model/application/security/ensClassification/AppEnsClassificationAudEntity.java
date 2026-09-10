package es.caib.invai.back.persistence.model.application.security.ensClassification;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Historical audit trail entity recording every lifecycle mutation applied
 * to {@link AppEnsClassificationEntity} records.
 *
 * @since 1.0.4
 */
@Entity
@Table(name = "INV_APP_ENS_CLASSIFICATION_AUD")
@Getter
@Setter
public class AppEnsClassificationAudEntity {

    /** Unique identification pointer of this audit trail row. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_ENS_CLASSIFICATION_AUD_SEQ")
    @SequenceGenerator(name = "INV_APP_ENS_CLASSIFICATION_AUD_SEQ", sequenceName = "INV_APP_ENS_CLASSIFICATION_AUD_SEQ", allocationSize = 1)
    @Column(name = "AUDIT_ID", nullable = false, updatable = false)
    private Long auditId;

    /** Identifier of the audited {@code AppEnsClassificationEntity} record. */
    @Column(name = "APP_ENS_CLASSIFICATION_ID", nullable = false)
    private Long appEnsClassificationId;

    /** Identifier of the parent security anchor at the time of the audited change. */
    @Column(name = "APP_SECURITY_ID", nullable = false)
    private Long appSecurityId;

    /** Identifier of the identity provider at the time of the audited change, or {@code null} if unset. */
    @Column(name = "IDENTITY_PROVIDER_ID")
    private Long identityProviderId;

    /** Identifier of the ENS subjection lookup entry at the time of the audited change, or {@code null} if unset. */
    @Column(name = "ENS_SUBJECT_ID")
    private Long ensSubjectId;

    /** Identifier of the personal data processing entry at the time of the audited change, or {@code null} if unset. */
    @Column(name = "PERSONAL_DATA_PROCESSING_ID")
    private Long personalDataProcessingId;

    /** Approval date at the time of the audited change, or {@code null} if unset. */
    @Column(name = "APPROVAL_DATE")
    private LocalDateTime approvalDate;

    /** Identifier of the confidentiality security level at the time of the audited change, or {@code null} if unset. */
    @Column(name = "CONFIDENTIALITY_LEVEL_ID")
    private Long confidentialityId;

    /** Identifier of the integrity security level at the time of the audited change, or {@code null} if unset. */
    @Column(name = "INTEGRITY_LEVEL_ID")
    private Long integrityId;

    /** Identifier of the traceability security level at the time of the audited change, or {@code null} if unset. */
    @Column(name = "TRACEABILITY_LEVEL_ID")
    private Long traceabilityId;

    /** Identifier of the availability security level at the time of the audited change, or {@code null} if unset. */
    @Column(name = "AVAILABILITY_LEVEL_ID")
    private Long availabilityId;

    /** Identifier of the authenticity security level at the time of the audited change, or {@code null} if unset. */
    @Column(name = "AUTHENTICITY_LEVEL_ID")
    private Long authenticityId;

    /** Identifier of the overall ENS adequacy grade security level at the time of the audited change, or {@code null} if unset. */
    @Column(name = "OVERALL_GRADE_LEVEL_ID")
    private Long overallGradeId;

    /** Timestamp at which the original record was created. */
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    /** Identifier of the user who created the original record. */
    @Column(name = "CREATED_BY")
    private String createdBy;

    /** Timestamp at which the original record was last updated. */
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    /** Identifier of the user who last updated the original record. */
    @Column(name = "UPDATED_BY")
    private String updatedBy;

    /** Timestamp at which the original record was logically deleted, or {@code null} if still active. */
    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    /** Identifier of the user who logically deleted the original record, or {@code null} if still active. */
    @Column(name = "DELETED_BY")
    private String deletedBy;

    /** Type of audited action (e.g. insert, update, delete). */
    @Column(name = "AUD_ACTION", nullable = false, length = 10)
    private String audAction;

    /** Timestamp at which this audit row was recorded. */
    @Column(name = "AUDIT_DATE", nullable = false)
    private LocalDateTime auditDate;

    /** Identifier of the user who triggered the audited action. */
    @Column(name = "AUDIT_USER", length = 64)
    private String auditUser;
}

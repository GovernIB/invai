package es.caib.invai.back.persistence.model.application.development.core;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Historical audit trail entity recording every lifecycle mutation applied
 * to {@link AppDevelopmentEntity} records.
 *
 * @since 1.0.2
 */
@Entity
@Table(name = "INV_APP_DEVELOPMENT_AUD")
@Getter
@Setter
public class AppDevelopmentAudEntity {

    /** Primary key of this audit trail row. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_DEVELOPMENT_AUD_SEQ")
    @SequenceGenerator(name = "INV_APP_DEVELOPMENT_AUD_SEQ", sequenceName = "INV_APP_DEVELOPMENT_AUD_SEQ", allocationSize = 1)
    @Column(name = "AUDIT_ID", nullable = false, updatable = false)
    private Long auditId;

    /** Identifier of the original development record this audit snapshot pertains to. */
    @Column(name = "APP_DEVELOPMENT_ID", nullable = false)
    private Long developmentId;

    /** Foreign key snapshot of the associated application at the time of the mutation. */
    @Column(name = "APPLICATION_ID", nullable = false)
    private Long applicationId;

    /** Foreign key snapshot of the associated deployment environment at the time of the mutation. */
    @Column(name = "ENVIRONMENT_ID", nullable = false)
    private Long environmentId;

    /** Foreign key snapshot of the associated development modality lookup entry. */
    @Column(name = "MODALITY_ID")
    private Long modalityId;

    /** Snapshot of the source code repository URL. */
    @Column(name = "CODE", length = 1000)
    private String code;

    /** Foreign key snapshot of the associated GOIB standards compliance lookup entry. */
    @Column(name = "STANDARD_ADAPTION")
    private Long standardAdaptionId;

    /** Snapshot of the latest standards revision date. */
    @Column(name = "REVISION_DATE")
    private LocalDateTime revisionDate;

    /** Snapshot of the general observation text. */
    @Lob
    @Column(name = "OBSERVATION")
    private String observation;

    /** Timestamp when the original record was created. */
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    /** Username of the user who created the original record. */
    @Column(name = "CREATED_BY")
    private String createdBy;

    /** Timestamp of the last update to the original record. */
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    /** Username of the user who last updated the original record. */
    @Column(name = "UPDATED_BY")
    private String updatedBy;

    /** Timestamp when the original record was logically deleted, or null if still active. */
    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    /** Username of the user who logically deleted the original record. */
    @Column(name = "DELETED_BY")
    private String deletedBy;

    /** Type of lifecycle mutation that triggered this audit row (INSERT, UPDATE, or DELETE). */
    @Column(name = "AUD_ACTION", nullable = false, length = 10)
    private String audAction;

    /** Timestamp when this audit row was recorded. */
    @Column(name = "AUDIT_DATE", nullable = false)
    private LocalDateTime auditDate;

    /** Username of the user who triggered the audited mutation. */
    @Column(name = "AUDIT_USER", length = 64)
    private String auditUser;
}

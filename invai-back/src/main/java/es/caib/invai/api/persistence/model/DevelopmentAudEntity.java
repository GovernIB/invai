package es.caib.invai.api.persistence.model;

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
 * to {@link DevelopmentEntity} records.
 *
 * @since 1.0.2
 */
@Entity
@Table(name = "INV_APP_DEVELOPMENT_AUD")
@Getter
@Setter
public class DevelopmentAudEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_DEVELOPMENT_AUD_SEQ")
    @SequenceGenerator(name = "INV_APP_DEVELOPMENT_AUD_SEQ", sequenceName = "INV_APP_DEVELOPMENT_AUD_SEQ", allocationSize = 1)
    @Column(name = "AUDIT_ID", nullable = false, updatable = false)
    private Long auditId;

    @Column(name = "APP_DEVELOPMENT_ID", nullable = false)
    private Long developmentId;

    @Column(name = "APPLICATION_ID", nullable = false)
    private Long applicationId;

    @Column(name = "ENVIRONMENT_ID", nullable = false)
    private Long environmentId;

    @Column(name = "MODALITY_ID")
    private Long modalityId;

    @Column(name = "CODE", length = 1000)
    private String code;

    @Column(name = "STANDARD_ADAPTION")
    private Long standardAdaptionId;

    @Column(name = "REVISION_DATE")
    private LocalDateTime revisionDate;

    @Lob
    @Column(name = "OBSERVATION")
    private String observation;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "UPDATED_BY")
    private String updatedBy;

    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    @Column(name = "DELETED_BY")
    private String deletedBy;

    @Column(name = "AUD_ACTION", nullable = false, length = 10)
    private String audAction;

    @Column(name = "AUDIT_DATE", nullable = false)
    private LocalDateTime auditDate;

    @Column(name = "AUDIT_USER", length = 64)
    private String auditUser;
}

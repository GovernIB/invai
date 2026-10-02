package es.caib.invai.back.persistence.model.application.security.webContext;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Historical audit trail entity recording every lifecycle mutation applied
 * to {@link AppWebContextEntity} records.
 *
 * @since 1.0.4
 */
@Entity
@Table(name = "INV_APP_WEB_CONTEXT_AUD")
@Getter
@Setter
public class AppWebContextAudEntity {

    /** Unique identification pointer of this audit trail row. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_WEB_CONTEXT_AUD_SEQ")
    @SequenceGenerator(name = "INV_APP_WEB_CONTEXT_AUD_SEQ", sequenceName = "INV_APP_WEB_CONTEXT_AUD_SEQ", allocationSize = 1)
    @Column(name = "AUDIT_ID", nullable = false, updatable = false)
    private Long auditId;

    /** Identifier of the audited {@code AppWebContextEntity} record. */
    @Column(name = "APP_WEB_CONTEXT_ID", nullable = false)
    private Long appWebContextId;

    /** Identifier of the security anchor at the time of the audited change. */
    @Column(name = "APP_SECURITY_ID", nullable = false)
    private Long appSecurityId;

    /** Identifier of the development anchor at the time of the audited change. */
    @Column(name = "APP_DEVELOPMENT_ID")
    private Long appDevelopmentId;

    /** Identifier of the associated web context at the time of the audited change. */
    @Column(name = "WEB_CONTEXT_ID", nullable = false)
    private Long webContextId;

    /** Identifier of the associated functional field at the time of the audited change. */
    @Column(name = "FIELD_ID", nullable = false)
    private Long fieldId;

    /** URL of the web context at the time of the audited change. */
    @Column(name = "URL")
    private String url;

    /** Snapshot of the {@code validated} flag at the time of the audited change. */
    @Column(name = "VALIDATED")
    private Boolean validated;

    /** Snapshot of the validation timestamp at the time of the audited change. */
    @Column(name = "VALIDATED_AT")
    private LocalDateTime validatedAt;

    /** Snapshot of the validating user at the time of the audited change. */
    @Column(name = "VALIDATED_BY")
    private String validatedBy;

    /** Snapshot of the validation justification at the time of the audited change. */
    @Column(name = "VALIDATED_REASON", length = 4000)
    private String validatedReason;

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

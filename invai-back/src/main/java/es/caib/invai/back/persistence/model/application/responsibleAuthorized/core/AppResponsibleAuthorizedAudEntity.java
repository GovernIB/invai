package es.caib.invai.back.persistence.model.application.responsibleAuthorized.core;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Historical snapshot entity capturing data mutations and transactional state changes
 * targeting the {@code INV_APP_RESPONSIBLE_AUTHORIZED} database table.
 *
 * @since 1.0.3
 */
@Getter
@Setter
@Entity
@Table(name = "INV_APP_RESPONSIBLE_AUTHORIZED_AUD")
public class AppResponsibleAuthorizedAudEntity {

    /** Primary key unique identifier of this audit row. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_RESPONSIBLE_AUTHORIZED_AUD_SEQ")
    @SequenceGenerator(name = "INV_APP_RESPONSIBLE_AUTHORIZED_AUD_SEQ", sequenceName = "INV_APP_RESPONSIBLE_AUTHORIZED_AUD_SEQ", allocationSize = 1)
    @Column(name = "AUDIT_ID", nullable = false, updatable = false)
    private Long auditId;

    /** Identifier of the {@code AppResponsibleAuthorized} anchor row this audit entry mirrors. */
    @Column(name = "APP_RESPONSIBLE_AUTHORIZED_ID", nullable = false)
    private Long appResponsibleAuthorizedId;

    /** Identifier of the corporate application the anchor belongs to. */
    @Column(name = "APPLICATION_ID", nullable = false)
    private Long applicationId;

    /** Timestamp when the mirrored row was created. */
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    /** User identifier who created the mirrored row. */
    @Column(name = "CREATED_BY", length = 64)
    private String createdBy;

    /** Timestamp of the last update to the mirrored row. */
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    /** User identifier who last updated the mirrored row. */
    @Column(name = "UPDATED_BY", length = 64)
    private String updatedBy;

    /** Timestamp when the mirrored row was soft-deleted, or {@code null} if still active. */
    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    /** User identifier who soft-deleted the mirrored row. */
    @Column(name = "DELETED_BY", length = 64)
    private String deletedBy;

    /** Type of change recorded by this audit entry (e.g. insert, update, delete). */
    @Column(name = "AUD_ACTION", length = 10, nullable = false)
    private String audAction;

    /** Timestamp when this audit entry itself was recorded. */
    @Column(name = "AUDIT_DATE", nullable = false)
    private LocalDateTime auditDate;

    /** User identifier who triggered the change captured by this audit entry. */
    @Column(name = "AUDIT_USER", length = 64)
    private String auditUser;
}

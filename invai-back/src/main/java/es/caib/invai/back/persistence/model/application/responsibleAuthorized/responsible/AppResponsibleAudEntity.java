package es.caib.invai.back.persistence.model.application.responsibleAuthorized.responsible;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Flat audit trail entity capturing historical row-level changes for
 * {@link AppResponsibleEntity}, mapped onto the {@code INV_APP_RESPONSIBLE_AUD} table.
 *
 * @since 1.0.3
 */
@Entity
@Table(name = "INV_APP_RESPONSIBLE_AUD")
@Getter
@Setter
public class AppResponsibleAudEntity {

    /** Primary key unique identifier of this audit row. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_RESPONSIBLE_AUD_SEQ")
    @SequenceGenerator(name = "INV_APP_RESPONSIBLE_AUD_SEQ", sequenceName = "INV_APP_RESPONSIBLE_AUD_SEQ", allocationSize = 1)
    @Column(name = "AUDIT_ID", nullable = false, updatable = false)
    private Long auditId;

    /** Identifier of the {@code AppResponsibleEntity} row this audit entry mirrors. */
    @Column(name = "APP_RESPONSIBLE_ID", nullable = false)
    private Long appResponsibleId;

    /** Identifier of the parent "Responsables i Autoritzats" anchor. */
    @Column(name = "APP_RESPONSIBLE_AUTHORIZED_ID", nullable = false)
    private Long appResponsibleAuthorizedId;

    /** Identifier of the responsible person. */
    @Column(name = "PERSON_ID", nullable = false)
    private Long personId;

    /** Identifier of the assigned responsibility type catalog value. */
    @Column(name = "RESPONSIBLE_TYPE_ID", nullable = false)
    private Long responsibleTypeId;

    /** Free-text job title/position held by the responsible person at the time of the change. */
    @Column(name = "JOB_TITLE")
    private String jobTitle;

    /** Free-text remarks about the responsible assignment at the time of the change. */
    @Lob
    @Column(name = "OBSERVATION")
    private String observation;

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

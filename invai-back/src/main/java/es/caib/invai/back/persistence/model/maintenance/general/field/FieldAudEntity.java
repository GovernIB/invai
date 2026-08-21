package es.caib.invai.back.persistence.model.maintenance.general.field;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Historical snapshot entity capturing data mutations and transactional state changes
 * targeting the {@code INV_FIELD} database table.
 * Retains mirrors of all base entity properties alongside flat execution metadata profiles.
 *
 * @since 1.0.1
 */
@Getter
@Setter
@Entity
@Table(name = "INV_FIELD_AUD")
public class FieldAudEntity {

    /** Primary key unique identifier of this audit row. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_field_aud_seq")
    @SequenceGenerator(name = "inv_field_aud_seq", sequenceName = "INV_FIELD_AUD_SEQ", allocationSize = 1)
    @Column(name = "AUDIT_ID")
    private Long audId;

    /** Identifier of the {@code FieldEntity} row this audit entry mirrors. */
    @Column(name = "FIELD_ID")
    private Long fieldId;

    /** Field name (Catalan) at the time of the change. */
    @Column(name = "NAME")
    private String name;

    /** Field name in Spanish at the time of the change. */
    @Column(name = "NAME_ES", length = 100)
    private String nameEs;

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
    @Column(name = "AUD_ACTION")
    private String audAction;

    /** Timestamp when this audit entry itself was recorded. */
    @Column(name = "AUDIT_DATE")
    private LocalDateTime auditDate;

    /** User identifier who triggered the change captured by this audit entry. */
    @Column(name = "AUDIT_USER")
    private String auditUser;
}
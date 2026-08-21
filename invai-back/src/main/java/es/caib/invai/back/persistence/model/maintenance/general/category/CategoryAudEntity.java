package es.caib.invai.back.persistence.model.maintenance.general.category;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Historical snapshot entity capturing data mutations and transactional state changes
 * targeting the {@code INV_CATEGORY} database table.
 * Tracks structural modifications made to taxonomy assets for corporate regulatory verification.
 *
 * @since 1.0.1
 */
@Getter
@Setter
@Entity
@Table(name = "INV_CATEGORY_AUD")
public class CategoryAudEntity {

    /** The unique auto-generated primary key of this audit snapshot row. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_category_aud_seq")
    @SequenceGenerator(name = "inv_category_aud_seq", sequenceName = "INV_CATEGORY_AUD_SEQ", allocationSize = 1)
    @Column(name = "AUDIT_ID")
    private Long audId;

    /** The identifier of the {@code CategoryEntity} record this snapshot was captured from. */
    @Column(name = "CATEGORY_ID")
    private Long categoryId;

    /** Snapshot of the category's Catalan name at the time of the audited action. */
    @Column(name = "NAME")
    private String name;

    /** Snapshot of the category's Spanish name at the time of the audited action. */
    @Column(name = "NAME_ES", length = 100)
    private String nameEs;

    /** Snapshot of the timestamp at which the original category record was created. */
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    /** Snapshot of the username of the user who created the original category record. */
    @Column(name = "CREATED_BY")
    private String createdBy;

    /** Snapshot of the timestamp of the last modification made to the original category record. */
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    /** Snapshot of the username of the user who last modified the original category record. */
    @Column(name = "UPDATED_BY")
    private String updatedBy;

    /** Snapshot of the timestamp at which the original category record was soft-deleted, or {@code null} if it was active. */
    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    /** Snapshot of the username of the user who soft-deleted the original category record. */
    @Column(name = "DELETED_BY")
    private String deletedBy;

    /** The type of mutation that triggered this audit snapshot (e.g. INSERT, UPDATE, DELETE). */
    @Column(name = "AUD_ACTION")
    private String audAction;

    /** Timestamp at which this audit snapshot was recorded. */
    @Column(name = "AUDIT_DATE")
    private LocalDateTime auditDate;

    /** Username of the user who performed the action that triggered this audit snapshot. */
    @Column(name = "AUDIT_USER")
    private String auditUser;
}
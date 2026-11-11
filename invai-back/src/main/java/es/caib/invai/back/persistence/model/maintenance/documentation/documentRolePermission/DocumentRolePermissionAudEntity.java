package es.caib.invai.back.persistence.model.maintenance.documentation.documentRolePermission;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Historical snapshot entity capturing data mutations targeting the {@code INV_DOC_ROLE_PERM}
 * database table.
 *
 * @since 1.0.7
 */
@Getter
@Setter
@Entity
@Table(name = "INV_DOC_ROLE_PERM_AUD")
public class DocumentRolePermissionAudEntity {

    /** Unique auto-generated identifier of this audit snapshot row. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inv_doc_role_perm_aud_seq")
    @SequenceGenerator(name = "inv_doc_role_perm_aud_seq", sequenceName = "INV_DOC_ROLE_PERM_AUD_SEQ", allocationSize = 1)
    @Column(name = "AUDIT_ID", nullable = false, updatable = false)
    private Long auditId;

    /** Identifier of the {@code DocumentRolePermissionEntity} this audit snapshot refers to. */
    @Column(name = "DOC_ROLE_PERM_ID", nullable = false)
    private Long docRolePermId;

    /** Identifier of the role the permissions belonged to. */
    @Column(name = "INV_ROLE_ID", nullable = false)
    private Long roleId;

    /** Identifier of the category the permissions were set on. */
    @Column(name = "DOCUMENT_CATEGORY_ID", nullable = false)
    private Long categoryId;

    /** Whether the role could read documents at the time of the audited change. */
    @Column(name = "CAN_READ", nullable = false)
    private boolean canRead;

    /** Whether the role could upload documents at the time of the audited change. */
    @Column(name = "CAN_UPLOAD", nullable = false)
    private boolean canUpload;

    /** Whether the role could edit documents at the time of the audited change. */
    @Column(name = "CAN_EDIT", nullable = false)
    private boolean canEdit;

    /** Whether the role could delete documents at the time of the audited change. */
    @Column(name = "CAN_DELETE", nullable = false)
    private boolean canDelete;

    /** Timestamp at which the record was originally created. */
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    /** Username of the user who originally created the record. */
    @Column(name = "CREATED_BY", length = 64)
    private String createdBy;

    /** Timestamp at which the record was last updated. */
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    /** Username of the user who last updated the record. */
    @Column(name = "UPDATED_BY", length = 64)
    private String updatedBy;

    /** Timestamp at which the record was deleted, only filled in by the {@code DELETE} snapshot. */
    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    /** Username of the user who deleted the record, only filled in by the {@code DELETE} snapshot. */
    @Column(name = "DELETED_BY", length = 64)
    private String deletedBy;

    /** Type of mutation that produced this audit snapshot ({@code INSERT}, {@code UPDATE} or {@code DELETE}). */
    @Column(name = "AUD_ACTION", length = 10, nullable = false)
    private String audAction;

    /** Timestamp at which this audit snapshot was recorded. */
    @Column(name = "AUDIT_DATE", nullable = false)
    private LocalDateTime auditDate;

    /** Username of the user who triggered the audited mutation. */
    @Column(name = "AUDIT_USER", length = 64)
    private String auditUser;
}

package es.caib.invai.back.persistence.model.application.documentation.attachment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Historical snapshot entity capturing every mutation of the {@code INV_APP_DOC_ATTACHMENT} table.
 *
 * @since 1.0.6
 */
@Getter
@Setter
@Entity
@Table(name = "INV_APP_DOC_ATT_AUD")
public class AppDocumentationAttachmentAudEntity {

    /** Unique auto-generated identifier of this audit snapshot row. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INV_APP_DOC_ATT_AUD_SEQ")
    @SequenceGenerator(name = "INV_APP_DOC_ATT_AUD_SEQ", sequenceName = "INV_APP_DOC_ATT_AUD_SEQ", allocationSize = 1)
    @Column(name = "AUDIT_ID", nullable = false, updatable = false)
    private Long auditId;

    /** Identifier of the {@code AppDocumentationAttachmentEntity} this snapshot refers to. */
    @Column(name = "APP_DOC_ATTACHMENT_ID", nullable = false)
    private Long appDocumentationAttachmentId;

    /** Identifier of the owning application captured at the time of the change. */
    @Column(name = "APPLICATION_ID", nullable = false)
    private Long applicationId;

    /** Title captured at the time of the change. */
    @Column(name = "TITLE", nullable = false, length = 250)
    private String title;

    /** Identifier of the section captured at the time of the change. */
    @Column(name = "SECTION_ID", nullable = false)
    private Long sectionId;

    /** Identifier of the category captured at the time of the change. */
    @Column(name = "DOCUMENT_CATEGORY_ID", nullable = false)
    private Long documentCategoryId;

    /** Identifier of the status captured at the time of the change. */
    @Column(name = "STATUS_ID", nullable = false)
    private Long statusId;

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

    /** Timestamp at which the record was logically deleted, or {@code null} if active. */
    @Column(name = "DELETED_AT")
    private LocalDateTime deletedAt;

    /** Username of the user who logically deleted the record. */
    @Column(name = "DELETED_BY", length = 64)
    private String deletedBy;

    /** Type of mutation that produced this snapshot ({@code INSERT}, {@code UPDATE} or {@code DELETE}). */
    @Column(name = "AUD_ACTION", nullable = false, length = 10)
    private String audAction;

    /** Timestamp at which this snapshot was recorded. */
    @Column(name = "AUDIT_DATE", nullable = false)
    private LocalDateTime auditDate;

    /** Username of the user who triggered the audited mutation. */
    @Column(name = "AUDIT_USER", length = 64)
    private String auditUser;
}

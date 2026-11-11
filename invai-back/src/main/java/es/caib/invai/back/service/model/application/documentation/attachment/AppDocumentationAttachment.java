package es.caib.invai.back.service.model.application.documentation.attachment;

import es.caib.invai.back.service.model.catalog.documentStatus.DocumentStatus;
import es.caib.invai.back.service.model.maintenance.documentation.documentCategory.DocumentCategory;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain model representing a document attached to an application, filed under a category and,
 * optionally, one of its subcategories.
 *
 * @since 1.0.6
 */
@Getter
@Setter
public class AppDocumentationAttachment {

    /** Unique database identifier key. */
    private Long id;

    /** Identifier of the application the document belongs to. */
    private Long applicationId;

    /** Title of the document. */
    private String title;

    /** Category the document is filed under. */
    private DocumentCategory category;

    /** Subcategory of the document, or {@code null} if it only has a category. */
    private DocumentCategory subcategory;

    /** Current state of the document. */
    private DocumentStatus status;

    /** Current version of the document; only filled in by the listing. */
    private AppDocumentationAttachmentVersion currentAttachment;

    /** Every active version of the document, newest first; only filled in by the detail. */
    private List<AppDocumentationAttachmentVersion> attachments = new ArrayList<>();

    /** Timestamp at which the record was created. */
    private LocalDateTime createdAt;
    /** Username of the user who created the record. */
    private String createdBy;
    /** Timestamp at which the record was last updated. */
    private LocalDateTime updatedAt;
    /** Username of the user who last updated the record. */
    private String updatedBy;
    /** Timestamp at which the record was logically deleted, or {@code null} if it is active. */
    private LocalDateTime deletedAt;
    /** Username of the user who logically deleted the record. */
    private String deletedBy;
}

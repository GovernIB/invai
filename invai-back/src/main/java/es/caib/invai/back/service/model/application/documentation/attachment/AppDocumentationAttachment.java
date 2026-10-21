package es.caib.invai.back.service.model.application.documentation.attachment;

import es.caib.invai.back.service.model.catalog.appSection.AppSection;
import es.caib.invai.back.service.model.catalog.documentStatus.DocumentStatus;
import es.caib.invai.back.service.model.maintenance.documentation.documentCategory.DocumentCategory;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Domain model representing a document attached to an application. It carries the stored
 * {@link #documentCategory} (the most specific one) and, when that is a subcategory, its
 * {@link #parentCategory}, from which the category and the subcategory shown to the user derive.
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

    /** Section of the application the document is filed under. */
    private AppSection section;

    /** Most specific category of the document, as stored: a subcategory if it has one, else a category. */
    private DocumentCategory documentCategory;

    /** Category the stored {@link #documentCategory} belongs to, or {@code null} if that is itself a category. */
    private DocumentCategory parentCategory;

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

    /**
     * Gets the category shown to the user: the parent of the stored category when that is a
     * subcategory, or the stored category itself.
     *
     * @return the category
     */
    public DocumentCategory getCategory() {
        return Objects.nonNull(parentCategory) ? parentCategory : documentCategory;
    }

    /**
     * Gets the subcategory shown to the user: the stored category when it has a parent.
     *
     * @return the subcategory, or {@code null} if the document only has a category
     */
    public DocumentCategory getSubcategory() {
        return Objects.nonNull(parentCategory) ? documentCategory : null;
    }
}

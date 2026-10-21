package es.caib.invai.back.persistence.repository.application.documentation.attachment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Search filters used to build the dynamic query that lists the documents of an application, deleted
 * ones included unless the {@code deleted} filter says otherwise. Every filter is optional except {@code applicationId}, which scopes the listing.
 *
 * @since 1.0.6
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppDocumentationAttachmentCriteria {

    /** Identifier of the application whose documents are listed. */
    private Long applicationId;

    /** Global text lookup, partial and case-insensitive, matching the title of the document. */
    private String search;

    /** Identifier of the section the documents are filed under. */
    private Long sectionId;

    /**
     * Identifier of a category or subcategory: a category matches the documents of that category
     * and of any of its subcategories, a subcategory matches only its own documents.
     */
    private Long categoryId;

    /** Identifier of the state of the documents (the "Estat" column: active, in review, obsolete). */
    private Long statusId;

    /**
     * Logical deletion of the documents: {@code true} lists only the deleted ones, {@code false}
     * only the ones that are not deleted, and {@code null} (the default) lists all of them.
     */
    private Boolean deleted;

    /** Identifier of the file format of the current version of the documents. */
    private Long mimeTypeId;
}

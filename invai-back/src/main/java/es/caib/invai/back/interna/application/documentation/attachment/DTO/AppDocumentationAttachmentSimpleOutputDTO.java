package es.caib.invai.back.interna.application.documentation.attachment.DTO;

import es.caib.invai.back.interna.catalog.appSection.DTO.AppSectionOutputDTO;
import es.caib.invai.back.interna.catalog.documentStatus.DTO.DocumentStatusOutputDTO;
import es.caib.invai.back.interna.catalog.mimeType.DTO.MimeTypeOutputDTO;
import es.caib.invai.back.interna.maintenance.documentation.category.DTO.DocumentCategoryOutputDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) modeling one row of the document listing: the document with the data
 * of its current version, and no file content.
 *
 * @since 1.0.6
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppDocumentationAttachmentSimpleOutputDTO {

    /** Unique identifier of the document. */
    private Long id;

    /** Title of the document. */
    private String title;

    /** Section of the application the document is filed under. */
    private AppSectionOutputDTO section;

    /** File format of the current version, shown as the document type. */
    private MimeTypeOutputDTO mimeType;

    /** Category of the document. */
    private DocumentCategoryOutputDTO category;

    /** Subcategory of the document, or {@code null} if it only has a category. */
    private DocumentCategoryOutputDTO subcategory;

    /** Version label of the current version. */
    private String versionLabel;

    /** Identifier of the current version, to download it straight from the listing. */
    private Long currentAttachmentId;

    /** Current state of the document. */
    private DocumentStatusOutputDTO status;

    /** Timestamp at which the document was logically deleted, or {@code null} if it is not deleted. */
    private LocalDateTime deletedAt;
}

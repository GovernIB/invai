package es.caib.invai.back.interna.application.documentation.attachment.DTO;

import es.caib.invai.back.interna.catalog.appSection.DTO.AppSectionOutputDTO;
import es.caib.invai.back.interna.catalog.documentStatus.DTO.DocumentStatusOutputDTO;
import es.caib.invai.back.interna.maintenance.documentation.category.DTO.DocumentCategoryOutputDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object (DTO) modeling the detail of a document: its main record and every one of
 * its attachments, without any file content.
 *
 * @since 1.0.6
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppDocumentationAttachmentOutputDTO {

    /** Unique identifier of the document. */
    private Long id;

    /** Identifier of the application the document belongs to. */
    private Long applicationId;

    /** Title of the document. */
    private String title;

    /** Section of the application the document is filed under. */
    private AppSectionOutputDTO section;

    /** Category of the document. */
    private DocumentCategoryOutputDTO category;

    /** Subcategory of the document, or {@code null} if it only has a category. */
    private DocumentCategoryOutputDTO subcategory;

    /** Current state of the document. */
    private DocumentStatusOutputDTO status;

    /** Timestamp at which the document was logically deleted, or {@code null} if it is not deleted. */
    private LocalDateTime deletedAt;

    /** Every version of the document, the newest first. */
    private List<AppDocumentationAttachmentVersionOutputDTO> attachments;
}

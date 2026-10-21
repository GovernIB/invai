package es.caib.invai.back.interna.application.documentation.attachment.DTO;

import es.caib.invai.back.utils.Constants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Data Transfer Object (DTO) modeling the inbound payload to create a new document along with its
 * first version. The file itself travels in its own multipart part.
 *
 * @since 1.0.6
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppDocumentationAttachmentInputDTO {

    /** Identifier of the application the document belongs to. */
    @NotNull(message = "{" + Constants.VALIDATION_DOCUMENTATION_APPLICATION_REQUIRED + "}")
    private Long applicationId;

    /** Title of the document. */
    @NotBlank(message = "{" + Constants.VALIDATION_DOCUMENTATION_TITLE_REQUIRED + "}")
    @Size(max = 250, message = "{" + Constants.VALIDATION_DOCUMENTATION_TITLE_SIZE + "}")
    private String title;

    /** Identifier of the section of the application the document is filed under. */
    @NotNull(message = "{" + Constants.VALIDATION_DOCUMENTATION_SECTION_REQUIRED + "}")
    private Long sectionId;

    /** Identifier of the most specific category: the subcategory if there is one, otherwise the category. */
    @NotNull(message = "{" + Constants.VALIDATION_DOCUMENTATION_CATEGORY_REQUIRED + "}")
    private Long categoryId;

    /** Identifier of the state of the document. */
    @NotNull(message = "{" + Constants.VALIDATION_DOCUMENTATION_STATUS_REQUIRED + "}")
    private Long statusId;

    /** Version of the first file, typed in by hand. */
    @NotBlank(message = "{" + Constants.VALIDATION_DOCUMENTATION_ATTACHMENT_VERSION_LABEL_REQUIRED + "}")
    @Size(max = 50, message = "{" + Constants.VALIDATION_DOCUMENTATION_ATTACHMENT_VERSION_LABEL_SIZE + "}")
    private String versionLabel;

    /** Optional note describing the first version. */
    @Size(max = 500, message = "{" + Constants.VALIDATION_DOCUMENTATION_ATTACHMENT_CHANGE_NOTE_SIZE + "}")
    private String changeNote;

    /** Metadata of the first attachment, optional; the keys must not repeat. */
    @Valid
    private List<AppDocumentationMetadataInputDTO> metadata;
}

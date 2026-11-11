package es.caib.invai.back.interna.application.documentation.attachment.DTO;

import es.caib.invai.back.utils.Constants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) modeling the inbound payload to update the main record of a
 * document. Its attachments and files are never modified.
 *
 * @since 1.0.6
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppDocumentationAttachmentUpdateInputDTO {

    /** Title of the document. */
    @NotBlank(message = "{" + Constants.VALIDATION_DOCUMENTATION_TITLE_REQUIRED + "}")
    @Size(max = 250, message = "{" + Constants.VALIDATION_DOCUMENTATION_TITLE_SIZE + "}")
    private String title;

    /** Identifier of the category the document is filed under. */
    @NotNull(message = "{" + Constants.VALIDATION_DOCUMENTATION_CATEGORY_REQUIRED + "}")
    private Long categoryId;

    /** Identifier of the subcategory of that category, optional. */
    private Long subcategoryId;

    /** Identifier of the state of the document. */
    @NotNull(message = "{" + Constants.VALIDATION_DOCUMENTATION_STATUS_REQUIRED + "}")
    private Long statusId;
}

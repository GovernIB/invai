package es.caib.invai.back.interna.application.documentation.email.DTO;

import es.caib.invai.back.utils.Constants;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) modeling a file chosen to be sent by e-mail: a specific version of a
 * document.
 *
 * @since 1.0.7
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppDocumentationEmailAttachmentInputDTO {

    /** Identifier of the document the file belongs to. */
    @NotNull(message = "{" + Constants.VALIDATION_EMAIL_DOCUMENT_REQUIRED + "}")
    private Long documentId;

    /** Identifier of the version of the document to attach. */
    @NotNull(message = "{" + Constants.VALIDATION_EMAIL_VERSION_REQUIRED + "}")
    private Long versionId;
}

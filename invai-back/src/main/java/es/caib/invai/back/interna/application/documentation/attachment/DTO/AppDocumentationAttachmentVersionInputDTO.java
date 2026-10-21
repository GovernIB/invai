package es.caib.invai.back.interna.application.documentation.attachment.DTO;

import es.caib.invai.back.utils.Constants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Data Transfer Object (DTO) modeling the inbound payload to add a new version to an existing
 * document. The file itself travels in its own multipart part.
 *
 * @since 1.0.6
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppDocumentationAttachmentVersionInputDTO {

    /** Version of the file, typed in by hand. */
    @NotBlank(message = "{" + Constants.VALIDATION_DOCUMENTATION_ATTACHMENT_VERSION_LABEL_REQUIRED + "}")
    @Size(max = 50, message = "{" + Constants.VALIDATION_DOCUMENTATION_ATTACHMENT_VERSION_LABEL_SIZE + "}")
    private String versionLabel;

    /** Optional note describing what changed in this version. */
    @Size(max = 500, message = "{" + Constants.VALIDATION_DOCUMENTATION_ATTACHMENT_CHANGE_NOTE_SIZE + "}")
    private String changeNote;

    /** Metadata of the new attachment, optional; the keys must not repeat. */
    @Valid
    private List<AppDocumentationMetadataInputDTO> metadata;
}

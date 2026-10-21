package es.caib.invai.back.interna.application.documentation.attachment.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data holder carrying a stored file and what the controller needs to send it back: it is the
 * result of a download, not a JSON payload.
 *
 * @since 1.0.6
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppDocumentationAttachmentVersionDownloadDTO {

    /** Original name of the file. */
    private String fileName;

    /** Media type of the file. */
    private String mimeType;

    /** Bytes of the file. */
    private byte[] content;
}

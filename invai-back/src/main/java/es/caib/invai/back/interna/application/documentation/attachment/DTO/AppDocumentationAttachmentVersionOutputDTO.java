package es.caib.invai.back.interna.application.documentation.attachment.DTO;

import es.caib.invai.back.interna.catalog.mimeType.DTO.MimeTypeOutputDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) modeling one version of a document, without its file content.
 *
 * @since 1.0.6
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppDocumentationAttachmentVersionOutputDTO {

    /** Unique identifier of the version. */
    private Long id;

    /** Version typed in by hand by whoever uploaded the file. */
    private String versionLabel;

    /** Original name of the uploaded file. */
    private String fileName;

    /** File format of the uploaded file. */
    private MimeTypeOutputDTO mimeType;

    /** Size of the uploaded file in bytes. */
    private Long fileSize;

    /** Timestamp at which the file was uploaded. */
    private LocalDateTime uploadedAt;

    /** Username of the user who uploaded the file. */
    private String uploadedBy;

    /** Whether this is the current version of the document. */
    private boolean current;

    /** Optional note describing what changed in this version. */
    private String changeNote;

    /** Metadata of this attachment, sorted by key. */
    private List<AppDocumentationMetadataOutputDTO> metadata;
}

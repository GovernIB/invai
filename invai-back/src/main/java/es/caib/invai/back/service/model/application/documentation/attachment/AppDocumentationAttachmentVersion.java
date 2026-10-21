package es.caib.invai.back.service.model.application.documentation.attachment;

import es.caib.invai.back.service.model.catalog.mimeType.MimeType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain model representing each file uploaded for a document. It never carries the file bytes:
 * the repository reads and writes them apart.
 *
 * @since 1.0.6
 */
@Getter
@Setter
public class AppDocumentationAttachmentVersion {

    /** Unique database identifier key. */
    private Long id;

    /** Identifier of the document this file is a version of. */
    private Long appDocumentationAttachmentId;

    /** Version typed in by hand by whoever uploaded the file (e.g. {@code "2.1"}). */
    private String versionLabel;

    /** Original name of the uploaded file. */
    private String fileName;

    /** File format of the uploaded file. */
    private MimeType mimeType;

    /** Size of the uploaded file in bytes. */
    private Long fileSize;

    /** Hexadecimal SHA-256 of the uploaded content. */
    private String checksumSha256;

    /** Whether this is the current version of the document. */
    private boolean current;

    /** Optional note describing what changed in this version. */
    private String changeNote;

    /** Metadata of this attachment, sorted by key; only filled in by the detail of its document. */
    private List<AppDocumentationMetadata> metadata = new ArrayList<>();

    /** Timestamp at which the record was created, i.e. the upload date. */
    private LocalDateTime createdAt;
    /** Username of the user who created the record, i.e. who uploaded the file. */
    private String createdBy;
    /** Timestamp at which the record was last updated. */
    private LocalDateTime updatedAt;
    /** Username of the user who last updated the record. */
    private String updatedBy;
    /** Timestamp at which the record was logically deleted, or {@code null} if it is active. */
    private LocalDateTime deletedAt;
    /** Username of the user who logically deleted the record. */
    private String deletedBy;
}

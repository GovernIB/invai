package es.caib.invai.back.service.model.application.documentation.attachment;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Domain model representing one extensible key-value metadata of a document, or of one of its
 * attachments.
 *
 * @since 1.0.6
 */
@Getter
@Setter
public class AppDocumentationMetadata {

    /** Unique database identifier key. */
    private Long id;

    /** Identifier of the attachment the metadata belongs to. */
    private Long appDocumentationAttachmentVersionId;

    /** Key of the metadata, unique among the active metadata of the same attachment. */
    private String metaKey;

    /** Value of the metadata, free text. */
    private String metaValue;

    /** Timestamp at which the record was created. */
    private LocalDateTime createdAt;
    /** Username of the user who created the record. */
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

package es.caib.invai.back.persistence.repository.application.documentation.attachment;

import es.caib.invai.back.service.model.application.documentation.attachment.AppDocumentationMetadata;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Outbound port declaring the persistence mechanisms for the metadata of the attachments of the
 * documents.
 *
 * @since 1.0.6
 */
public interface AppDocumentationMetadataRepository {

    /**
     * Finds the active metadata of the given attachments, sorted by key.
     *
     * @param attachmentIds identifiers of the attachments
     * @return the active metadata of those attachments
     */
    List<AppDocumentationMetadata> findActiveByAttachmentIds(Collection<Long> attachmentIds);

    /**
     * Finds the metadata of the given attachments that was deleted at the very moment their
     * document was, sorted by key. It is the metadata the document had when it was deleted.
     *
     * @param attachmentIds identifiers of the attachments
     * @param deletedAt     the deletion timestamp of the document
     * @return the metadata deleted along with the document
     */
    List<AppDocumentationMetadata> findDeletedWithDocumentationAttachment(Collection<Long> attachmentIds, LocalDateTime deletedAt);

    /**
     * Persists a new metadata.
     *
     * @param metadata the metadata to create
     * @return the persisted metadata
     */
    AppDocumentationMetadata create(AppDocumentationMetadata metadata);

    /**
     * Logically deletes every active metadata of the given attachments.
     *
     * @param attachmentIds identifiers of the attachments
     * @param deletedAt     timestamp of the deletion
     * @param deletedBy     username of the user who deletes them
     */
    void deleteAllByAttachmentIds(Collection<Long> attachmentIds, LocalDateTime deletedAt, String deletedBy);
}

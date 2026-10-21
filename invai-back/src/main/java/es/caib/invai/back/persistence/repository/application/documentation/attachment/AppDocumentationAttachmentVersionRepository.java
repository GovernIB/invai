package es.caib.invai.back.persistence.repository.application.documentation.attachment;

import es.caib.invai.back.service.model.application.documentation.attachment.AppDocumentationAttachmentVersion;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Outbound port declaring the persistence mechanisms for the attachments of a document. The file
 * bytes only travel through {@link #create} and {@link #findContentById}; every other method
 * leaves them in the database.
 *
 * @since 1.0.6
 */
public interface AppDocumentationAttachmentVersionRepository {

    /**
     * Finds a version by its identifier, active or logically deleted.
     *
     * @param id the identifier
     * @return the matching version without its content, or {@code null} if none is found
     */
    AppDocumentationAttachmentVersion findById(Long id);

    /**
     * Finds every active version of a document, the newest first.
     *
     * @param documentationAttachmentId identifier of the document
     * @return the active attachments, without their content
     */
    List<AppDocumentationAttachmentVersion> findActiveByDocumentationAttachmentId(Long documentationAttachmentId);

    /**
     * Finds the current attachment of each of the given documents, also of the deleted ones.
     *
     * @param documentationAttachmentIds identifiers of the documents
     * @return the current attachments, without their content, by the identifier of their document
     */
    Map<Long, AppDocumentationAttachmentVersion> findCurrentByDocumentationAttachmentIds(Collection<Long> documentationAttachmentIds);

    /**
     * Checks whether an active version of the document already has the given version label.
     *
     * @param documentationAttachmentId identifier of the document
     * @param versionLabel the version label to check
     * @return {@code true} if such an active version exists
     */
    boolean existsActiveWithLabel(Long documentationAttachmentId, String versionLabel);

    /**
     * Persists a new version along with its file.
     *
     * @param version the version to create
     * @param content the bytes of the file
     * @return the persisted version, without its content
     */
    AppDocumentationAttachmentVersion create(AppDocumentationAttachmentVersion version, byte[] content);

    /**
     * Makes the current version of the document stop being so, immediately, so that a new current
     * version can be stored afterwards. Does nothing if the document has no current version.
     *
     * @param documentationAttachmentId identifier of the document
     */
    void unmarkCurrent(Long documentationAttachmentId);

    /**
     * Logically deletes every active version of the document.
     *
     * @param documentationAttachmentId identifier of the document
     * @param deletedAt    timestamp of the deletion
     * @param deletedBy    username of the user who deletes them
     */
    void deleteAllByDocumentationAttachmentId(Long documentationAttachmentId, LocalDateTime deletedAt, String deletedBy);

    /**
     * Reads the file bytes of a version.
     *
     * @param id the identifier of the version
     * @return the bytes of the file, or {@code null} if the version doesn't exist
     */
    byte[] findContentById(Long id);

    /**
     * Registers in the audit history of the attachment that its file was downloaded, or viewed in
     * the browser, by the current user. The attachment itself is not modified.
     *
     * @param id     the identifier of the attachment
     * @param inline {@code true} if the file was viewed in the browser, {@code false} if it was downloaded
     */
    void registerDownload(Long id, boolean inline);

    /**
     * Logically deletes the given attachment. A deleted attachment is never the current one.
     *
     * @param attachment the attachment to delete, already carrying its deletion timestamp and author
     */
    void delete(AppDocumentationAttachmentVersion attachment);

    /**
     * Makes the given attachment the current one of its document. The document must not have
     * another current attachment.
     *
     * @param id the identifier of the attachment
     */
    void markCurrent(Long id);

    /**
     * Finds the attachments that were deleted at the very moment their document was, the newest
     * first. They are the ones the document had when it was deleted.
     *
     * @param documentationAttachmentId identifier of the deleted document
     * @param deletedAt       the deletion timestamp of the document
     * @return the attachments deleted along with the document, without their content
     */
    List<AppDocumentationAttachmentVersion> findDeletedWithDocumentationAttachment(Long documentationAttachmentId, LocalDateTime deletedAt);
}

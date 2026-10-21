package es.caib.invai.back.persistence.repository.application.documentation.attachment;

import es.caib.invai.back.persistence.model.application.documentation.attachment.AppDocumentationAttachmentVersionEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Native Spring Data JPA repository for {@link AppDocumentationAttachmentVersionEntity} records. The
 * queries that return attachments load their file format along, so mapping them never fires one
 * more query per row.
 *
 * @since 1.0.6
 */
@Repository
public interface AppDocumentationAttachmentVersionJPARepository extends JpaRepository<AppDocumentationAttachmentVersionEntity, Long> {

    /**
     * Finds every active attachment of a document, the newest first, with its file format.
     *
     * @param documentationAttachmentId identifier of the document
     * @return the active attachments of the document
     */
    @EntityGraph(attributePaths = "mimeType")
    List<AppDocumentationAttachmentVersionEntity> findByAppDocumentationAttachmentIdAndDeletedAtIsNullOrderByCreatedAtDescIdDesc(Long documentationAttachmentId);

    /**
     * Finds the attachments that were deleted at the very moment their document was, the newest
     * first, with their file format. They are the ones the document had when it was deleted.
     *
     * @param documentationAttachmentId identifier of the deleted document
     * @param deletedAt       the deletion timestamp of the document
     * @return the attachments deleted along with the document
     */
    @EntityGraph(attributePaths = "mimeType")
    List<AppDocumentationAttachmentVersionEntity> findByAppDocumentationAttachmentIdAndDeletedAtOrderByCreatedAtDescIdDesc(Long documentationAttachmentId, LocalDateTime deletedAt);

    /**
     * Finds the current attachment of each of the given documents, with its file format. A deleted
     * document keeps the current attachment it had when it was deleted, so it is found too.
     *
     * @param documentationAttachmentIds identifiers of the documents
     * @return the current attachments of those documents
     */
    @EntityGraph(attributePaths = "mimeType")
    List<AppDocumentationAttachmentVersionEntity> findByAppDocumentationAttachmentIdInAndCurrentTrue(Collection<Long> documentationAttachmentIds);

    /**
     * Finds the current, active attachment of a document to stop it from being the current one.
     *
     * @param documentationAttachmentId identifier of the document
     * @return the current attachments of the document, at most one
     */
    List<AppDocumentationAttachmentVersionEntity> findByAppDocumentationAttachmentIdAndCurrentTrueAndDeletedAtIsNull(Long documentationAttachmentId);

    /**
     * Checks whether an active attachment of the document already has the given version label.
     *
     * @param documentationAttachmentId identifier of the document
     * @param versionLabel    the version label to check
     * @return {@code true} if such an active attachment exists
     */
    boolean existsByAppDocumentationAttachmentIdAndVersionLabelAndDeletedAtIsNull(Long documentationAttachmentId, String versionLabel);
}

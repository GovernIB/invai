package es.caib.invai.back.persistence.repository.application.documentation.attachment;

import es.caib.invai.back.persistence.model.application.documentation.attachment.AppDocumentationMetadataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Native Spring Data JPA repository for {@link AppDocumentationMetadataEntity} records.
 *
 * @since 1.0.6
 */
@Repository
public interface AppDocumentationMetadataJPARepository extends JpaRepository<AppDocumentationMetadataEntity, Long> {

    /**
     * Finds the active metadata of the given attachments, sorted by key.
     *
     * @param attachmentIds identifiers of the attachments
     * @return the active metadata of those attachments
     */
    List<AppDocumentationMetadataEntity> findByAppDocumentationAttachmentVersionIdInAndDeletedAtIsNullOrderByMetaKeyAscIdAsc(Collection<Long> attachmentIds);

    /**
     * Finds the metadata of the given attachments that was deleted at the very moment their
     * document was, sorted by key. It is the metadata the document had when it was deleted.
     *
     * @param attachmentIds identifiers of the attachments
     * @param deletedAt     the deletion timestamp of the document
     * @return the metadata deleted along with the document
     */
    List<AppDocumentationMetadataEntity> findByAppDocumentationAttachmentVersionIdInAndDeletedAtOrderByMetaKeyAscIdAsc(Collection<Long> attachmentIds, LocalDateTime deletedAt);
}

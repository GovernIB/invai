package es.caib.invai.back.persistence.repository.application.documentation.attachment;

import es.caib.invai.back.persistence.model.application.documentation.attachment.AppDocumentationAttachmentVersionEntity;
import es.caib.invai.back.persistence.model.application.documentation.attachment.AppDocumentationMetadataAudEntity;
import es.caib.invai.back.persistence.model.application.documentation.attachment.AppDocumentationMetadataEntity;
import es.caib.invai.back.service.mapper.application.documentation.attachment.AppDocumentationMetadataMapper;
import es.caib.invai.back.service.model.application.documentation.attachment.AppDocumentationMetadata;
import es.caib.invai.back.utils.Utils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Infrastructure repository adapter implementing the outbound port
 * {@link AppDocumentationMetadataRepository}. The document and the attachment a metadata points to
 * are linked through references built from their identifiers.
 *
 * @since 1.0.6
 */
@Repository
@Slf4j
public class AppDocumentationMetadataRepositoryAdapter implements AppDocumentationMetadataRepository {

    /** Entity manager used to build the references to the document and the attachment. */
    @PersistenceContext
    private EntityManager entityManager;

    /** Spring Data JPA repository used to access live {@link AppDocumentationMetadataEntity} records. */
    @Autowired
    private AppDocumentationMetadataJPARepository appDocumentationMetadataJPARepository;

    /** Spring Data JPA repository used to persist {@link AppDocumentationMetadataAudEntity} audit snapshots. */
    @Autowired
    private AppDocumentationMetadataAudJPARepository appDocumentationMetadataAudJPARepository;

    /** Mapper used to convert between metadata domain models and JPA entities. */
    @Autowired
    private AppDocumentationMetadataMapper appDocumentationMetadataMapper;

    @Override
    public List<AppDocumentationMetadata> findActiveByAttachmentIds(Collection<Long> attachmentIds) {
        if (attachmentIds.isEmpty()) {
            return List.of();
        }
        return appDocumentationMetadataMapper.toModelList(
                appDocumentationMetadataJPARepository.findByAppDocumentationAttachmentVersionIdInAndDeletedAtIsNullOrderByMetaKeyAscIdAsc(attachmentIds));
    }

    @Override
    public List<AppDocumentationMetadata> findDeletedWithDocumentationAttachment(Collection<Long> attachmentIds, LocalDateTime deletedAt) {
        if (attachmentIds.isEmpty()) {
            return List.of();
        }
        return appDocumentationMetadataMapper.toModelList(
                appDocumentationMetadataJPARepository.findByAppDocumentationAttachmentVersionIdInAndDeletedAtOrderByMetaKeyAscIdAsc(attachmentIds, deletedAt));
    }

    @Override
    public AppDocumentationMetadata create(AppDocumentationMetadata metadata) {
        log.debug("Repository: Persisting new application document metadata for attachment ID: {}", metadata.getAppDocumentationAttachmentVersionId());
        AppDocumentationMetadataEntity entity = appDocumentationMetadataMapper.toEntity(metadata);
        entity.setAppDocumentationAttachmentVersion(entityManager.getReference(AppDocumentationAttachmentVersionEntity.class, metadata.getAppDocumentationAttachmentVersionId()));
        entity = appDocumentationMetadataJPARepository.saveAndFlush(entity);
        saveAuditRecord(entity, "INSERT");
        return appDocumentationMetadataMapper.toModel(entity);
    }

    @Override
    public void deleteAllByAttachmentIds(Collection<Long> attachmentIds, LocalDateTime deletedAt, String deletedBy) {
        for (AppDocumentationMetadata metadata : findActiveByAttachmentIds(attachmentIds)) {
            log.debug("Repository: Soft deleting metadata ID: {} of attachment ID: {}", metadata.getId(), metadata.getAppDocumentationAttachmentVersionId());
            softDelete(metadata.getId(), deletedAt, deletedBy);
        }
    }

    /**
     * Logically deletes a metadata through its managed entity, so its document and attachment
     * references are left as they are. It is flushed at once, so a new metadata with the same key
     * can be stored right after.
     *
     * @param id        identifier of the metadata
     * @param deletedAt timestamp of the deletion
     * @param deletedBy username of the user who deletes it
     */
    private void softDelete(Long id, LocalDateTime deletedAt, String deletedBy) {
        AppDocumentationMetadataEntity entity = appDocumentationMetadataJPARepository.getReferenceById(id);
        entity.setDeletedAt(deletedAt);
        entity.setDeletedBy(deletedBy);
        entity = appDocumentationMetadataJPARepository.saveAndFlush(entity);
        saveAuditRecord(entity, "DELETE");
    }

    /**
     * Builds and persists an audit snapshot capturing the current state of the given entity.
     *
     * @param entity the metadata entity state to snapshot
     * @param action the type of mutation being audited ({@code INSERT}, {@code UPDATE} or {@code DELETE})
     */
    private void saveAuditRecord(AppDocumentationMetadataEntity entity, String action) {
        AppDocumentationMetadataAudEntity aud = new AppDocumentationMetadataAudEntity();
        aud.setAppDocumentationMetadataId(entity.getId());
        aud.setAppDocumentationAttachmentVersionId(entity.getAppDocumentationAttachmentVersion().getId());
        aud.setMetaKey(entity.getMetaKey());
        aud.setMetaValue(entity.getMetaValue());
        aud.setCreatedAt(Objects.nonNull(entity.getCreatedAt()) ? entity.getCreatedAt() : LocalDateTime.now());
        aud.setCreatedBy(Objects.nonNull(entity.getCreatedBy()) ? entity.getCreatedBy() : Utils.resolveCurrentUsername());
        aud.setUpdatedAt(entity.getUpdatedAt());
        aud.setUpdatedBy(entity.getUpdatedBy());
        aud.setDeletedAt(entity.getDeletedAt());
        aud.setDeletedBy(entity.getDeletedBy());
        aud.setAudAction(action);
        aud.setAuditDate(LocalDateTime.now());
        aud.setAuditUser(Utils.resolveCurrentUsername());
        appDocumentationMetadataAudJPARepository.save(aud);
    }
}

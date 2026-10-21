package es.caib.invai.back.persistence.repository.application.documentation.attachment;

import es.caib.invai.back.persistence.model.application.documentation.attachment.AppDocumentationAttachmentEntity;
import es.caib.invai.back.persistence.model.application.documentation.attachment.AppDocumentationAttachmentVersionAudEntity;
import es.caib.invai.back.persistence.model.application.documentation.attachment.AppDocumentationAttachmentVersionEntity;
import es.caib.invai.back.persistence.model.catalog.mimeType.LkupMimeTypeEntity;
import es.caib.invai.back.service.mapper.application.documentation.attachment.AppDocumentationAttachmentVersionMapper;
import es.caib.invai.back.service.model.application.documentation.attachment.AppDocumentationAttachmentVersion;
import es.caib.invai.back.utils.Utils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.jdbc.BlobProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Blob;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Infrastructure repository adapter implementing the outbound port
 * {@link AppDocumentationAttachmentVersionRepository}. The attachments that change after being stored are updated
 * through their managed entities, so their file content is never rewritten.
 *
 * @since 1.0.6
 */
@Repository
@Slf4j
public class AppDocumentationAttachmentVersionRepositoryAdapter implements AppDocumentationAttachmentVersionRepository {

    /** Entity manager used to build the references to the document and file format of a version. */
    @PersistenceContext
    private EntityManager entityManager;

    /** Spring Data JPA repository used to access live {@link AppDocumentationAttachmentVersionEntity} records. */
    @Autowired
    private AppDocumentationAttachmentVersionJPARepository appDocumentationAttachmentVersionJPARepository;

    /** Spring Data JPA repository used to persist {@link AppDocumentationAttachmentVersionAudEntity} audit snapshots. */
    @Autowired
    private AppDocumentationAttachmentVersionAudJPARepository appDocumentationAttachmentVersionAudJPARepository;

    /** Mapper used to convert between version domain models and JPA entities. */
    @Autowired
    private AppDocumentationAttachmentVersionMapper appDocumentationAttachmentVersionMapper;

    @Override
    public AppDocumentationAttachmentVersion findById(Long id) {
        return appDocumentationAttachmentVersionJPARepository.findById(id).map(appDocumentationAttachmentVersionMapper::toModel).orElse(null);
    }

    @Override
    public List<AppDocumentationAttachmentVersion> findActiveByDocumentationAttachmentId(Long documentationAttachmentId) {
        return appDocumentationAttachmentVersionMapper.toModelList(appDocumentationAttachmentVersionJPARepository.findByAppDocumentationAttachmentIdAndDeletedAtIsNullOrderByCreatedAtDescIdDesc(documentationAttachmentId));
    }

    @Override
    public List<AppDocumentationAttachmentVersion> findDeletedWithDocumentationAttachment(Long documentationAttachmentId, LocalDateTime deletedAt) {
        return appDocumentationAttachmentVersionMapper.toModelList(
                appDocumentationAttachmentVersionJPARepository.findByAppDocumentationAttachmentIdAndDeletedAtOrderByCreatedAtDescIdDesc(documentationAttachmentId, deletedAt));
    }

    @Override
    public Map<Long, AppDocumentationAttachmentVersion> findCurrentByDocumentationAttachmentIds(Collection<Long> documentationAttachmentIds) {
        if (documentationAttachmentIds.isEmpty()) {
            return Map.of();
        }
        return appDocumentationAttachmentVersionJPARepository.findByAppDocumentationAttachmentIdInAndCurrentTrue(documentationAttachmentIds).stream()
                .map(appDocumentationAttachmentVersionMapper::toModel)
                .collect(Collectors.toMap(AppDocumentationAttachmentVersion::getAppDocumentationAttachmentId, Function.identity()));
    }

    @Override
    public boolean existsActiveWithLabel(Long documentationAttachmentId, String versionLabel) {
        return appDocumentationAttachmentVersionJPARepository.existsByAppDocumentationAttachmentIdAndVersionLabelAndDeletedAtIsNull(documentationAttachmentId, versionLabel);
    }

    @Override
    public AppDocumentationAttachmentVersion create(AppDocumentationAttachmentVersion version, byte[] content) {
        log.debug("Repository: Persisting new application document version for document ID: {}", version.getAppDocumentationAttachmentId());
        AppDocumentationAttachmentVersionEntity entity = appDocumentationAttachmentVersionMapper.toEntity(version);
        entity.setAppDocumentationAttachment(entityManager.getReference(AppDocumentationAttachmentEntity.class, version.getAppDocumentationAttachmentId()));
        entity.setMimeType(entityManager.getReference(LkupMimeTypeEntity.class, version.getMimeType().getId()));
        entity.setContent(BlobProxy.generateProxy(content));
        entity = appDocumentationAttachmentVersionJPARepository.saveAndFlush(entity);
        saveAuditRecord(entity, "INSERT");
        return appDocumentationAttachmentVersionMapper.toModel(entity);
    }

    @Override
    public void unmarkCurrent(Long documentationAttachmentId) {
        for (AppDocumentationAttachmentVersionEntity entity : appDocumentationAttachmentVersionJPARepository.findByAppDocumentationAttachmentIdAndCurrentTrueAndDeletedAtIsNull(documentationAttachmentId)) {
            log.debug("Repository: Unmarking the current version ID: {} of document ID: {}", entity.getId(), documentationAttachmentId);
            entity.setCurrent(false);
            entity = appDocumentationAttachmentVersionJPARepository.saveAndFlush(entity);
            saveAuditRecord(entity, "UPDATE");
        }
    }

    @Override
    public void deleteAllByDocumentationAttachmentId(Long documentationAttachmentId, LocalDateTime deletedAt, String deletedBy) {
        for (AppDocumentationAttachmentVersionEntity entity : appDocumentationAttachmentVersionJPARepository.findByAppDocumentationAttachmentIdAndDeletedAtIsNullOrderByCreatedAtDescIdDesc(documentationAttachmentId)) {
            log.debug("Repository: Soft deleting version ID: {} of document ID: {}", entity.getId(), documentationAttachmentId);
            entity.setDeletedAt(deletedAt);
            entity.setDeletedBy(deletedBy);
            entity = appDocumentationAttachmentVersionJPARepository.save(entity);
            saveAuditRecord(entity, "DELETE");
        }
    }

    @Override
    public byte[] findContentById(Long id) {
        Blob blob = appDocumentationAttachmentVersionJPARepository.findById(id).map(AppDocumentationAttachmentVersionEntity::getContent).orElse(null);
        if (Objects.isNull(blob)) {
            return null;
        }
        try {
            return blob.getBytes(1, (int) blob.length());
        } catch (SQLException e) {
            log.error("Repository error: Failure reading the content of version ID: {}", id, e);
            throw new IllegalStateException("The content of the version could not be read", e);
        }
    }

    @Override
    public void delete(AppDocumentationAttachmentVersion attachment) {
        log.debug("Repository: Soft deleting application document attachment with ID: {}", attachment.getId());
        AppDocumentationAttachmentVersionEntity entity = appDocumentationAttachmentVersionJPARepository.getReferenceById(attachment.getId());
        entity.setCurrent(false);
        entity.setDeletedAt(attachment.getDeletedAt());
        entity.setDeletedBy(attachment.getDeletedBy());
        entity = appDocumentationAttachmentVersionJPARepository.saveAndFlush(entity);
        saveAuditRecord(entity, "DELETE");
    }

    @Override
    public void markCurrent(Long id) {
        log.debug("Repository: Making attachment ID: {} the current one of its document", id);
        AppDocumentationAttachmentVersionEntity entity = appDocumentationAttachmentVersionJPARepository.getReferenceById(id);
        entity.setCurrent(true);
        entity = appDocumentationAttachmentVersionJPARepository.saveAndFlush(entity);
        saveAuditRecord(entity, "UPDATE");
    }

    @Override
    public void registerDownload(Long id, boolean inline) {
        saveAuditRecord(appDocumentationAttachmentVersionJPARepository.getReferenceById(id), inline ? "VIEW" : "DOWNLOAD");
    }

    /**
     * Builds and persists an audit snapshot capturing the current state of the given entity,
     * leaving the file content out.
     *
     * @param entity the version entity state to snapshot
     * @param action the type of event being audited ({@code INSERT}, {@code UPDATE}, {@code DELETE}, {@code DOWNLOAD} or {@code VIEW})
     */
    private void saveAuditRecord(AppDocumentationAttachmentVersionEntity entity, String action) {
        AppDocumentationAttachmentVersionAudEntity aud = new AppDocumentationAttachmentVersionAudEntity();
        aud.setAppDocumentationAttachmentVersionId(entity.getId());
        aud.setAppDocumentationAttachmentId(entity.getAppDocumentationAttachment().getId());
        aud.setVersionLabel(entity.getVersionLabel());
        aud.setFileName(entity.getFileName());
        aud.setMimeTypeId(entity.getMimeType().getId());
        aud.setFileSize(entity.getFileSize());
        aud.setChecksumSha256(entity.getChecksumSha256());
        aud.setCurrent(entity.isCurrent());
        aud.setChangeNote(entity.getChangeNote());
        aud.setCreatedAt(Objects.nonNull(entity.getCreatedAt()) ? entity.getCreatedAt() : LocalDateTime.now());
        aud.setCreatedBy(Objects.nonNull(entity.getCreatedBy()) ? entity.getCreatedBy() : Utils.resolveCurrentUsername());
        aud.setUpdatedAt(entity.getUpdatedAt());
        aud.setUpdatedBy(entity.getUpdatedBy());
        aud.setDeletedAt(entity.getDeletedAt());
        aud.setDeletedBy(entity.getDeletedBy());
        aud.setAudAction(action);
        aud.setAuditDate(LocalDateTime.now());
        aud.setAuditUser(Utils.resolveCurrentUsername());
        appDocumentationAttachmentVersionAudJPARepository.save(aud);
    }
}

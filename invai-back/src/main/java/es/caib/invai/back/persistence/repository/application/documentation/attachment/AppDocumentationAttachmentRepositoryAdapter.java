package es.caib.invai.back.persistence.repository.application.documentation.attachment;

import es.caib.invai.back.persistence.model.application.core.ApplicationEntity;
import es.caib.invai.back.persistence.model.application.documentation.attachment.AppDocumentationAttachmentAudEntity;
import es.caib.invai.back.persistence.model.application.documentation.attachment.AppDocumentationAttachmentEntity;
import es.caib.invai.back.persistence.model.catalog.appSection.LkupAppSectionEntity;
import es.caib.invai.back.persistence.model.catalog.documentStatus.LkupDocumentStatusEntity;
import es.caib.invai.back.persistence.model.maintenance.documentation.documentCategory.DocumentCategoryEntity;
import es.caib.invai.back.service.mapper.application.documentation.attachment.AppDocumentationAttachmentMapper;
import es.caib.invai.back.service.model.application.documentation.attachment.AppDocumentationAttachment;
import es.caib.invai.back.utils.Utils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Infrastructure repository adapter implementing the outbound port {@link AppDocumentationAttachmentRepository}.
 * The related records (application, section, category and state) are linked through references
 * built from their identifiers, so everything that is read back afterwards is the stored data.
 *
 * @since 1.0.6
 */
@Repository
@Slf4j
public class AppDocumentationAttachmentRepositoryAdapter implements AppDocumentationAttachmentRepository {

    /** Entity manager used to build the references to the records a document points to. */
    @PersistenceContext
    private EntityManager entityManager;

    /** Spring Data JPA repository used to access live {@link AppDocumentationAttachmentEntity} records. */
    @Autowired
    private AppDocumentationAttachmentJPARepository appDocumentationAttachmentJPARepository;

    /** Spring Data JPA repository used to persist {@link AppDocumentationAttachmentAudEntity} audit snapshots. */
    @Autowired
    private AppDocumentationAttachmentAudJPARepository appDocumentationAttachmentAudJPARepository;

    /** Mapper used to convert between document domain models and JPA entities. */
    @Autowired
    private AppDocumentationAttachmentMapper appDocumentationAttachmentMapper;

    @Override
    public AppDocumentationAttachment findById(Long id) {
        return appDocumentationAttachmentJPARepository.findById(id).map(appDocumentationAttachmentMapper::toModel).orElse(null);
    }

    @Override
    public Page<AppDocumentationAttachment> findAll(AppDocumentationAttachmentCriteria filter, Pageable pageable) {
        Specification<AppDocumentationAttachmentEntity> spec = AppDocumentationAttachmentSpecification.filterByCriteria(filter, pageable.getSort());
        Pageable unsorted = pageable.isPaged() ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()) : Pageable.unpaged();
        return appDocumentationAttachmentJPARepository.findAll(spec, unsorted).map(appDocumentationAttachmentMapper::toModel);
    }

    @Override
    public AppDocumentationAttachment create(AppDocumentationAttachment appDocumentationAttachment) {
        log.debug("Repository: Persisting new application document entity into database");
        AppDocumentationAttachmentEntity entity = appDocumentationAttachmentJPARepository.saveAndFlush(toEntityWithReferences(appDocumentationAttachment));
        saveAuditRecord(entity, "INSERT");
        return appDocumentationAttachmentMapper.toModel(entity);
    }

    @Override
    public AppDocumentationAttachment update(AppDocumentationAttachment appDocumentationAttachment, Long id) {
        log.debug("Repository: Merging changes into the application document with ID: {}", id);
        AppDocumentationAttachmentEntity entity = toEntityWithReferences(appDocumentationAttachment);
        entity.setId(id);
        entity = appDocumentationAttachmentJPARepository.save(entity);
        saveAuditRecord(entity, "UPDATE");
        return appDocumentationAttachmentMapper.toModel(entity);
    }

    @Override
    public void delete(AppDocumentationAttachment appDocumentationAttachment) {
        log.debug("Repository: Soft deleting application document entity with ID: {}", appDocumentationAttachment.getId());
        AppDocumentationAttachmentEntity entity = toEntityWithReferences(appDocumentationAttachment);
        entity.setId(appDocumentationAttachment.getId());
        entity = appDocumentationAttachmentJPARepository.save(entity);
        saveAuditRecord(entity, "DELETE");
    }

    /**
     * Converts the document into its entity and links it to the application, section, category
     * and state it points to, through references that hold only their identifiers.
     *
     * @param appDocumentationAttachment the document to convert
     * @return the entity ready to be saved
     */
    private AppDocumentationAttachmentEntity toEntityWithReferences(AppDocumentationAttachment appDocumentationAttachment) {
        AppDocumentationAttachmentEntity entity = appDocumentationAttachmentMapper.toEntity(appDocumentationAttachment);
        entity.setApplication(entityManager.getReference(ApplicationEntity.class, appDocumentationAttachment.getApplicationId()));
        entity.setSection(entityManager.getReference(LkupAppSectionEntity.class, appDocumentationAttachment.getSection().getId()));
        entity.setDocumentCategory(entityManager.getReference(DocumentCategoryEntity.class, appDocumentationAttachment.getDocumentCategory().getId()));
        entity.setStatus(entityManager.getReference(LkupDocumentStatusEntity.class, appDocumentationAttachment.getStatus().getId()));
        return entity;
    }

    /**
     * Builds and persists an audit snapshot capturing the current state of the given entity.
     *
     * @param entity the document entity state to snapshot
     * @param action the type of mutation being audited ({@code INSERT}, {@code UPDATE} or {@code DELETE})
     */
    private void saveAuditRecord(AppDocumentationAttachmentEntity entity, String action) {
        AppDocumentationAttachmentAudEntity aud = new AppDocumentationAttachmentAudEntity();
        aud.setAppDocumentationAttachmentId(entity.getId());
        aud.setApplicationId(entity.getApplication().getId());
        aud.setTitle(entity.getTitle());
        aud.setSectionId(entity.getSection().getId());
        aud.setDocumentCategoryId(entity.getDocumentCategory().getId());
        aud.setStatusId(entity.getStatus().getId());
        aud.setCreatedAt(Objects.nonNull(entity.getCreatedAt()) ? entity.getCreatedAt() : LocalDateTime.now());
        aud.setCreatedBy(Objects.nonNull(entity.getCreatedBy()) ? entity.getCreatedBy() : Utils.resolveCurrentUsername());
        aud.setUpdatedAt(entity.getUpdatedAt());
        aud.setUpdatedBy(entity.getUpdatedBy());
        aud.setDeletedAt(entity.getDeletedAt());
        aud.setDeletedBy(entity.getDeletedBy());
        aud.setAudAction(action);
        aud.setAuditDate(LocalDateTime.now());
        aud.setAuditUser(Utils.resolveCurrentUsername());
        appDocumentationAttachmentAudJPARepository.save(aud);
    }
}

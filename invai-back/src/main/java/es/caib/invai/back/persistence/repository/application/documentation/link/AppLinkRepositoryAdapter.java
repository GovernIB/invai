package es.caib.invai.back.persistence.repository.application.documentation.link;

import es.caib.invai.back.persistence.model.application.core.ApplicationEntity;
import es.caib.invai.back.persistence.model.application.documentation.link.AppLinkAudEntity;
import es.caib.invai.back.persistence.model.application.documentation.link.AppLinkEntity;
import es.caib.invai.back.persistence.model.catalog.appSection.LkupAppSectionEntity;
import es.caib.invai.back.service.mapper.application.documentation.link.AppLinkMapper;
import es.caib.invai.back.service.model.application.documentation.link.AppLink;
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
 * Infrastructure repository adapter implementing the outbound port {@link AppLinkRepository}. The
 * application and the section of a link are linked through references built from their
 * identifiers, so everything that is read back afterwards is the stored data.
 *
 * @since 1.0.6
 */
@Repository
@Slf4j
public class AppLinkRepositoryAdapter implements AppLinkRepository {

    /** Entity manager used to build the references to the records a link points to. */
    @PersistenceContext
    private EntityManager entityManager;

    /** Spring Data JPA repository used to access live {@link AppLinkEntity} records. */
    @Autowired
    private AppLinkJPARepository appLinkJPARepository;

    /** Spring Data JPA repository used to persist {@link AppLinkAudEntity} audit snapshots. */
    @Autowired
    private AppLinkAudJPARepository appLinkAudJPARepository;

    /** Mapper used to convert between link domain models and JPA entities. */
    @Autowired
    private AppLinkMapper appLinkMapper;

    @Override
    public AppLink findById(Long id) {
        return appLinkJPARepository.findById(id).map(appLinkMapper::toModel).orElse(null);
    }

    @Override
    public Page<AppLink> findAll(AppDocumentationLinkCriteria filter, Pageable pageable) {
        Specification<AppLinkEntity> spec = AppLinkSpecification.filterByCriteria(filter, pageable.getSort());
        Pageable unsorted = pageable.isPaged() ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()) : Pageable.unpaged();
        return appLinkJPARepository.findAll(spec, unsorted).map(appLinkMapper::toModel);
    }

    @Override
    public boolean existsActiveDuplicate(Long applicationId, String title, Long sectionId, String url) {
        return appLinkJPARepository.existsByApplicationIdAndTitleAndSectionIdAndUrlAndDeletedAtIsNull(applicationId, title, sectionId, url);
    }

    @Override
    public boolean existsActiveDuplicateExcluding(Long applicationId, String title, Long sectionId, String url, Long id) {
        return appLinkJPARepository.existsByApplicationIdAndTitleAndSectionIdAndUrlAndIdNotAndDeletedAtIsNull(applicationId, title, sectionId, url, id);
    }

    @Override
    public AppLink create(AppLink appLink) {
        log.debug("Repository: Persisting new application link entity into database");
        AppLinkEntity entity = appLinkJPARepository.saveAndFlush(toEntityWithReferences(appLink));
        saveAuditRecord(entity, "INSERT");
        return appLinkMapper.toModel(entity);
    }

    @Override
    public AppLink update(AppLink appLink, Long id) {
        log.debug("Repository: Merging changes into the application link with ID: {}", id);
        AppLinkEntity entity = toEntityWithReferences(appLink);
        entity.setId(id);
        entity = appLinkJPARepository.save(entity);
        saveAuditRecord(entity, "UPDATE");
        return appLinkMapper.toModel(entity);
    }

    @Override
    public void delete(AppLink appLink) {
        log.debug("Repository: Soft deleting application link entity with ID: {}", appLink.getId());
        AppLinkEntity entity = toEntityWithReferences(appLink);
        entity.setId(appLink.getId());
        entity = appLinkJPARepository.save(entity);
        saveAuditRecord(entity, "DELETE");
    }

    /**
     * Converts the link into its entity and links it to the application and the section it points
     * to, through references that hold only their identifiers.
     *
     * @param appLink the link to convert
     * @return the entity ready to be saved
     */
    private AppLinkEntity toEntityWithReferences(AppLink appLink) {
        AppLinkEntity entity = appLinkMapper.toEntity(appLink);
        entity.setApplication(entityManager.getReference(ApplicationEntity.class, appLink.getApplicationId()));
        entity.setSection(entityManager.getReference(LkupAppSectionEntity.class, appLink.getSection().getId()));
        return entity;
    }

    /**
     * Builds and persists an audit snapshot capturing the current state of the given entity.
     *
     * @param entity the link entity state to snapshot
     * @param action the type of mutation being audited ({@code INSERT}, {@code UPDATE} or {@code DELETE})
     */
    private void saveAuditRecord(AppLinkEntity entity, String action) {
        AppLinkAudEntity aud = new AppLinkAudEntity();
        aud.setAppLinkId(entity.getId());
        aud.setApplicationId(entity.getApplication().getId());
        aud.setTitle(entity.getTitle());
        aud.setDescription(entity.getDescription());
        aud.setSectionId(entity.getSection().getId());
        aud.setUrl(entity.getUrl());
        aud.setCreatedAt(Objects.nonNull(entity.getCreatedAt()) ? entity.getCreatedAt() : LocalDateTime.now());
        aud.setCreatedBy(Objects.nonNull(entity.getCreatedBy()) ? entity.getCreatedBy() : Utils.resolveCurrentUsername());
        aud.setUpdatedAt(entity.getUpdatedAt());
        aud.setUpdatedBy(entity.getUpdatedBy());
        aud.setDeletedAt(entity.getDeletedAt());
        aud.setDeletedBy(entity.getDeletedBy());
        aud.setAudAction(action);
        aud.setAuditDate(LocalDateTime.now());
        aud.setAuditUser(Utils.resolveCurrentUsername());
        appLinkAudJPARepository.save(aud);
    }
}

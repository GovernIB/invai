package es.caib.invai.back.persistence.repository.application.development.technology;

import es.caib.invai.back.persistence.model.application.development.technology.AppTechnologyEntity;
import es.caib.invai.back.persistence.model.application.development.technology.AppTechnologyAudEntity;
import es.caib.invai.back.service.mapper.application.development.technology.AppTechnologyMapper;
import es.caib.invai.back.service.model.application.development.technology.AppTechnology;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Infrastructure repository Adapter implementing the outbound port boundary {@link AppTechnologyRepository}.
 *
 * @since 1.0.2
 */
@Repository
@Slf4j
public class AppTechnologyRepositoryAdapter implements AppTechnologyRepository {

    /** Spring Data repository handling CRUD operations for the technology entity. */
    @Autowired
    private AppTechnologyJPARepository appTechnologyJPARepository;

    /** Spring Data repository handling persistence of technology audit trail rows. */
    @Autowired
    private AppTechnologyAudJPARepository appTechnologyAudJPARepository;

    /** MapStruct mapper handling transformations between entities and domain models. */
    @Autowired
    private AppTechnologyMapper appTechnologyMapper;

    /**
     * Persists a new technology entity and records the corresponding audit trail row.
     *
     * @param technology the domain model to persist
     * @return the persisted domain model, including its generated identifier
     * @throws DataAccessException if the underlying relational write fails
     */
    @Override
    public AppTechnology create(AppTechnology technology) {
        log.info("Repository: Persisting new technology entity ledger row");
        try {
            AppTechnologyEntity entity = appTechnologyMapper.toEntity(technology);
            entity = appTechnologyJPARepository.save(entity);

            saveAuditRecord(entity, "INSERT");
            return appTechnologyMapper.toModel(entity);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure executing relational write across technology settings", e);
            throw e;
        }
    }

    /**
     * Persists changes to an existing technology entity and records the corresponding audit trail row.
     *
     * @param technology the domain model carrying the updated values
     * @param id         the identifier of the record being updated
     * @return the persisted domain model reflecting the applied changes
     * @throws DataAccessException if the underlying relational write fails
     */
    @Override
    public AppTechnology update(AppTechnology technology, Long id) {
        log.info("Repository: Merging operational records variables for technology ID: {}", id);
        try {
            AppTechnologyEntity entity = appTechnologyMapper.toEntity(technology);
            entity.setId(id);
            entity = appTechnologyJPARepository.save(entity);

            saveAuditRecord(entity, "UPDATE");
            return appTechnologyMapper.toModel(entity);
        } catch (DataAccessException e) {
            log.error("Repository error: Corrupted state runtime merging delta mappings onto target key index: {}", id, e);
            throw e;
        }
    }

    /**
     * Persists the logical (soft) deletion of a technology entity and records the corresponding
     * audit trail row.
     *
     * @param technology the domain model, with deletion tracking fields already set
     * @throws DataAccessException if the underlying relational write fails
     */
    @Override
    public void delete(AppTechnology technology) {
        log.info("Repository: Merging deactivation tracking signatures for technology identifier ID: {}", technology.getId());
        try {
            AppTechnologyEntity entity = appTechnologyMapper.toEntity(technology);
            entity.setId(technology.getId());
            entity = appTechnologyJPARepository.save(entity);

            saveAuditRecord(entity, "DELETE");
        } catch (DataAccessException e) {
            log.error("Repository error: Unhandled transaction rollback execution during deactivation for ID: {}", technology.getId(), e);
            throw e;
        }
    }

    /**
     * Resolves a single technology entity by its primary key.
     *
     * @param id the record identifier
     * @return the matching domain model, or {@code null} if not found
     * @throws DataAccessException if the underlying relational read fails
     */
    @Override
    public AppTechnology findById(Long id) {
        try {
            return appTechnologyJPARepository.findById(id)
                    .map(appTechnologyMapper::toModel)
                    .orElse(null);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure tracking contextual matching reference rows for index ID: {}", id, e);
            throw e;
        }
    }

    /**
     * Resolves a paginated, criteria-filtered sequence of technology entities scoped to a single
     * parent development module.
     *
     * @param appDevelopmentId mandatory parent development identifier scoping the result set
     * @param criteria         the multi-parameter business query filter boundaries
     * @param pageable         pagination structural constraints
     * @return a paginated matrix of matching domain models
     * @throws DataAccessException if the underlying relational read fails
     */
    @Override
    public Page<AppTechnology> findAll(Long appDevelopmentId, AppTechnologyCriteria criteria, Pageable pageable) {
        log.info("Repository: Dynamic search pattern stream across technology relations for Development ID: {}", appDevelopmentId);
        try {
            Specification<AppTechnologyEntity> spec = AppTechnologySpecification.filterByCriteria(appDevelopmentId, criteria);
            Page<AppTechnologyEntity> entityPage = appTechnologyJPARepository.findAll(spec, pageable);
            return entityPage.map(appTechnologyMapper::toModel);
        } catch (DataAccessException e) {
            log.error("Repository error: Broken chunk streaming query handling for technology layouts", e);
            throw e;
        }
    }

    /**
     * Checks whether any technology record references the given architecture layer catalog entry.
     *
     * @param layerId the layer catalog identifier to check
     * @return {@code true} if at least one technology entry references the layer, {@code false} otherwise
     */
    @Override
    public boolean existsByLayerId(Long layerId) {
        return appTechnologyJPARepository.existsByLayerId(layerId);
    }

    /**
     * Checks whether any technology record references the given technology catalog entry.
     *
     * @param technologyId the technology catalog identifier to check
     * @return {@code true} if at least one technology entry references the catalog entry, {@code false} otherwise
     */
    @Override
    public boolean existsByTechnologyId(Long technologyId) {
        return appTechnologyJPARepository.existsByTechnologyId(technologyId);
    }

    /**
     * Builds and persists an audit trail row capturing a snapshot of the given entity state.
     *
     * @param entity the entity state to snapshot
     * @param action the lifecycle action being recorded (INSERT, UPDATE, or DELETE)
     */
    private void saveAuditRecord(AppTechnologyEntity entity, String action) {
        try {
            AppTechnologyAudEntity aud = new AppTechnologyAudEntity();

            aud.setTechnologyId(entity.getId());
            aud.setAppDevelopmentId(entity.getAppDevelopment().getId());
            aud.setLayerId(entity.getLayer() != null ? entity.getLayer().getId() : null);
            aud.setTechnologyCatalogId(entity.getTechnology() != null ? entity.getTechnology().getId() : null);
            aud.setVersion(entity.getVersion());
            aud.setArchitecture(entity.getArchitecture());

            aud.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now());
            aud.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy() : Utils.resolveCurrentUsername());
           
                        aud.setUpdatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt() : LocalDateTime.now());
            aud.setUpdatedBy(entity.getUpdatedBy() != null ? entity.getUpdatedBy() : Utils.resolveCurrentUsername());
            aud.setDeletedAt(entity.getDeletedAt());
            aud.setDeletedBy(entity.getDeletedBy());

            aud.setAudAction(action);
            aud.setAuditDate(LocalDateTime.now());
            aud.setAuditUser(Utils.resolveCurrentUsername());

            appTechnologyAudJPARepository.save(aud);
        } catch (DataAccessException e) {
            log.error("Repository trace error: Critical trace audit ledger commit crash mapping technology event tracer", e);
            throw e;
        }
    }
}

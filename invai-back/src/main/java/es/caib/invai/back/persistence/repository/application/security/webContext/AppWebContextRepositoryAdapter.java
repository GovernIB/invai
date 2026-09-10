package es.caib.invai.back.persistence.repository.application.security.webContext;

import es.caib.invai.back.persistence.model.application.security.webContext.AppWebContextAudEntity;
import es.caib.invai.back.persistence.model.application.security.webContext.AppWebContextEntity;
import es.caib.invai.back.service.mapper.application.security.webContext.AppWebContextMapper;
import es.caib.invai.back.service.model.application.security.webContext.AppWebContext;
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
 * Repository adapter implementing {@link AppWebContextRepository} on top of Spring Data
 * JPA, translating between domain models and JPA entities and keeping the associated
 * audit trail up to date.
 *
 * @since 1.0.4
 */
@Repository
@Slf4j
public class AppWebContextRepositoryAdapter implements AppWebContextRepository {

    /** JPA repository providing CRUD and specification-based search access to the root entity. */
    @Autowired
    private AppWebContextJPARepository appWebContextJPARepository;

    /** JPA repository providing persistence for the audit trail entity. */
    @Autowired
    private AppWebContextAudJPARepository appWebContextAudJPARepository;

    /** Mapper converting between domain models and JPA entities. */
    @Autowired
    private AppWebContextMapper appWebContextMapper;

    /**
     * Persists a new application-web context link and records an INSERT audit row.
     *
     * @param appWebContext the model to persist
     * @return the persisted model, including its generated identifier
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public AppWebContext create(AppWebContext appWebContext) {
        log.info("Repository: Persisting new application web context entity ledger row");
        try {
            AppWebContextEntity entity = appWebContextMapper.toEntity(appWebContext);
            entity = appWebContextJPARepository.save(entity);

            saveAuditRecord(entity, "INSERT");
            return appWebContextMapper.toModel(entity);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure executing relational write across application settings", e);
            throw e;
        }
    }

    /**
     * Updates the application-web context link identified by {@code id} and records an
     * UPDATE audit row.
     *
     * @param appWebContext the model carrying the updated values
     * @param id            identifier of the record to update
     * @return the updated model
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public AppWebContext update(AppWebContext appWebContext, Long id) {
        log.info("Repository: Merging operational records variables for application-web context link ID: {}", id);
        try {
            AppWebContextEntity entity = appWebContextMapper.toEntity(appWebContext);
            entity.setId(id);
            entity = appWebContextJPARepository.save(entity);

            saveAuditRecord(entity, "UPDATE");
            return appWebContextMapper.toModel(entity);
        } catch (DataAccessException e) {
            log.error("Repository error: Corrupted state runtime merging delta mappings onto link target key index: {}", id, e);
            throw e;
        }
    }

    /**
     * Deletes (logically) the given application-web context link and records a DELETE
     * audit row.
     *
     * @param appWebContext the model to delete
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public void delete(AppWebContext appWebContext) {
        log.info("Repository: Merging deactivation tracking signatures for link assignment mapping identifier ID: {}", appWebContext.getId());
        try {
            AppWebContextEntity entity = appWebContextMapper.toEntity(appWebContext);
            entity.setId(appWebContext.getId());
            entity = appWebContextJPARepository.save(entity);

            saveAuditRecord(entity, "DELETE");
        } catch (DataAccessException e) {
            log.error("Repository error: Unhandled transaction rollback execution during link deactivation for ID: {}", appWebContext.getId(), e);
            throw e;
        }
    }

    /**
     * Resolves an application-web context link by its identifier.
     *
     * @param id identifier of the record to resolve
     * @return the matching model, or {@code null} if not found
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public AppWebContext findById(Long id) {
        try {
            return appWebContextJPARepository.findById(id)
                    .map(appWebContextMapper::toModel)
                    .orElse(null);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure tracking contextual matching reference rows for link index ID: {}", id, e);
            throw e;
        }
    }

    /**
     * Resolves a paginated, filtered list of application-web context links scoped to a
     * single parent security anchor.
     *
     * @param appSecurityId mandatory parent security anchor identifier scoping the result set
     * @param criteria      additional filter criteria
     * @param pageable      pagination and sorting instructions
     * @return the paginated page of matching models
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public Page<AppWebContext> findAll(Long appSecurityId, AppWebContextCriteria criteria, Pageable pageable) {
        log.debug("Repository: Dynamic search pattern stream across application web context relations for appSecurityId ID: {}", appSecurityId);
        try {
            Specification<AppWebContextEntity> spec = AppWebContextSpecification.filterByCriteria(appSecurityId, criteria);
            Page<AppWebContextEntity> entityPage = appWebContextJPARepository.findAll(spec, pageable);
            return entityPage.map(appWebContextMapper::toModel);
        } catch (DataAccessException e) {
            log.error("Repository error: Broken chunk streaming query handling for application web context allocation layouts", e);
            throw e;
        }
    }

    /**
     * Builds and persists an audit trail row snapshotting the current state of the
     * given entity for the given action.
     *
     * @param entity the entity whose current state is captured in the audit row
     * @param action the action that triggered the audit (e.g. "INSERT", "UPDATE", "DELETE")
     * @throws DataAccessException if the underlying persistence operation fails
     */
    private void saveAuditRecord(AppWebContextEntity entity, String action) {
        try {
            AppWebContextAudEntity aud = new AppWebContextAudEntity();

            aud.setAppWebContextId(entity.getId());
            aud.setAppSecurityId(entity.getAppSecurity().getId());
            aud.setWebContextId(entity.getWebContext().getId());
            aud.setFieldId(entity.getField().getId());

            aud.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now());
            aud.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy() : Utils.resolveCurrentUsername());

            aud.setUpdatedAt(entity.getUpdatedAt());
            aud.setUpdatedBy(entity.getUpdatedBy());
            aud.setDeletedAt(entity.getDeletedAt());
            aud.setDeletedBy(entity.getDeletedBy());

            aud.setAudAction(action);
            aud.setAuditDate(LocalDateTime.now());
            aud.setAuditUser(Utils.resolveCurrentUsername());

            appWebContextAudJPARepository.save(aud);
        } catch (DataAccessException e) {
            log.error("Repository trace error: Critical trace audit ledger commit crash mapping application-web context event link tracer", e);
            throw e;
        }
    }
}

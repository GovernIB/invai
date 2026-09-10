package es.caib.invai.back.persistence.repository.application.security.measure;

import es.caib.invai.back.persistence.model.application.security.measure.AppSecurityMeasureAudEntity;
import es.caib.invai.back.persistence.model.application.security.measure.AppSecurityMeasureEntity;
import es.caib.invai.back.service.mapper.application.security.measure.AppSecurityMeasureMapper;
import es.caib.invai.back.service.model.application.security.measure.AppSecurityMeasure;
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
 * Repository adapter implementing {@link AppSecurityMeasureRepository} on top of Spring Data
 * JPA, translating between domain models and JPA entities and keeping the associated
 * audit trail up to date.
 *
 * @since 1.0.4
 */
@Repository
@Slf4j
public class AppSecurityMeasureRepositoryAdapter implements AppSecurityMeasureRepository {

    /** JPA repository providing CRUD and specification-based search access to the root entity. */
    @Autowired
    private AppSecurityMeasureJPARepository appSecurityMeasureJPARepository;

    /** JPA repository providing persistence for the audit trail entity. */
    @Autowired
    private AppSecurityMeasureAudJPARepository appSecurityMeasureAudJPARepository;

    /** Mapper converting between domain models and JPA entities. */
    @Autowired
    private AppSecurityMeasureMapper appSecurityMeasureMapper;

    /**
     * Persists a new security measure and records an INSERT audit row.
     *
     * @param appSecurityMeasure the model to persist
     * @return the persisted model, including its generated identifier
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public AppSecurityMeasure create(AppSecurityMeasure appSecurityMeasure) {
        log.info("Repository: Persisting new application security measure entity ledger row");
        try {
            AppSecurityMeasureEntity entity = appSecurityMeasureMapper.toEntity(appSecurityMeasure);
            entity = appSecurityMeasureJPARepository.save(entity);

            saveAuditRecord(entity, "INSERT");
            return appSecurityMeasureMapper.toModel(entity);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure executing relational write across application security measure settings", e);
            throw e;
        }
    }

    /**
     * Updates the security measure identified by {@code id} and records an UPDATE audit row.
     *
     * @param appSecurityMeasure the model carrying the updated values
     * @param id                 identifier of the record to update
     * @return the updated model
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public AppSecurityMeasure update(AppSecurityMeasure appSecurityMeasure, Long id) {
        log.info("Repository: Merging operational records variables for application security measure ID: {}", id);
        try {
            AppSecurityMeasureEntity entity = appSecurityMeasureMapper.toEntity(appSecurityMeasure);
            entity.setId(id);
            entity = appSecurityMeasureJPARepository.save(entity);

            saveAuditRecord(entity, "UPDATE");
            return appSecurityMeasureMapper.toModel(entity);
        } catch (DataAccessException e) {
            log.error("Repository error: Corrupted state runtime merging delta mappings onto security measure target key index: {}", id, e);
            throw e;
        }
    }

    /**
     * Deletes (logically) the given security measure and records a DELETE audit row.
     *
     * @param appSecurityMeasure the model to delete
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public void delete(AppSecurityMeasure appSecurityMeasure) {
        log.info("Repository: Merging deactivation tracking signatures for security measure identifier ID: {}", appSecurityMeasure.getId());
        try {
            AppSecurityMeasureEntity entity = appSecurityMeasureMapper.toEntity(appSecurityMeasure);
            entity.setId(appSecurityMeasure.getId());
            entity = appSecurityMeasureJPARepository.save(entity);

            saveAuditRecord(entity, "DELETE");
        } catch (DataAccessException e) {
            log.error("Repository error: Unhandled transaction rollback execution during security measure deactivation for ID: {}", appSecurityMeasure.getId(), e);
            throw e;
        }
    }

    /**
     * Resolves a security measure by its identifier.
     *
     * @param id identifier of the record to resolve
     * @return the matching model, or {@code null} if not found
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public AppSecurityMeasure findById(Long id) {
        try {
            return appSecurityMeasureJPARepository.findById(id)
                    .map(appSecurityMeasureMapper::toModel)
                    .orElse(null);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure tracking contextual matching reference rows for security measure index ID: {}", id, e);
            throw e;
        }
    }

    /**
     * Resolves a paginated, filtered list of security measures scoped to a single parent
     * security anchor.
     *
     * @param appSecurityId mandatory parent security anchor identifier scoping the result set
     * @param criteria      additional filter criteria
     * @param pageable      pagination and sorting instructions
     * @return the paginated page of matching models
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public Page<AppSecurityMeasure> findAll(Long appSecurityId, AppSecurityMeasureCriteria criteria, Pageable pageable) {
        log.debug("Repository: Dynamic search pattern stream across application security measure relations for appSecurityId ID: {}", appSecurityId);
        try {
            Specification<AppSecurityMeasureEntity> spec = AppSecurityMeasureSpecification.filterByCriteria(appSecurityId, criteria);
            Page<AppSecurityMeasureEntity> entityPage = appSecurityMeasureJPARepository.findAll(spec, pageable);
            return entityPage.map(appSecurityMeasureMapper::toModel);
        } catch (DataAccessException e) {
            log.error("Repository error: Broken chunk streaming query handling for application security measure allocation layouts", e);
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
    private void saveAuditRecord(AppSecurityMeasureEntity entity, String action) {
        try {
            AppSecurityMeasureAudEntity aud = new AppSecurityMeasureAudEntity();

            aud.setAppSecurityMeasureId(entity.getId());
            aud.setAppSecurityId(entity.getAppSecurity().getId());
            aud.setTypeId(entity.getType() != null ? entity.getType().getId() : null);
            aud.setEnsRequirementId(entity.getEnsRequirement() != null ? entity.getEnsRequirement().getId() : null);

            aud.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now());
            aud.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy() : Utils.resolveCurrentUsername());

            aud.setUpdatedAt(entity.getUpdatedAt());
            aud.setUpdatedBy(entity.getUpdatedBy());
            aud.setDeletedAt(entity.getDeletedAt());
            aud.setDeletedBy(entity.getDeletedBy());

            aud.setAudAction(action);
            aud.setAuditDate(LocalDateTime.now());
            aud.setAuditUser(Utils.resolveCurrentUsername());

            appSecurityMeasureAudJPARepository.save(aud);
        } catch (DataAccessException e) {
            log.error("Repository trace error: Critical trace audit ledger commit crash mapping application security measure event tracer", e);
            throw e;
        }
    }
}

package es.caib.invai.back.persistence.repository.application.system_database.system;

import es.caib.invai.back.persistence.model.application.system_database.system.AppSystemAudEntity;
import es.caib.invai.back.persistence.model.application.system_database.system.AppSystemEntity;
import es.caib.invai.back.service.mapper.application.system_database.system.AppSystemMapper;
import es.caib.invai.back.service.model.application.system_database.system.AppSystem;
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
 * Repository adapter implementing {@link AppSystemRepository} on top of Spring Data
 * JPA, translating between domain models and JPA entities and keeping the associated
 * audit trail up to date.
 *
 * @since 1.0.2
 */
@Repository
@Slf4j
public class AppSystemRepositoryAdapter implements AppSystemRepository {

    /** JPA repository providing CRUD and specification-based search access to the root entity. */
    @Autowired
    private AppSystemJPARepository appSystemJPARepository;

    /** JPA repository providing persistence for the audit trail entity. */
    @Autowired
    private AppSystemAudJPARepository appSystemAudJPARepository;

    /** Mapper converting between domain models and JPA entities. */
    @Autowired
    private AppSystemMapper appSystemMapper;

    /**
     * Persists a new application-system link and records an INSERT audit row.
     *
     * @param appSystem the model to persist
     * @return the persisted model, including its generated identifier
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public AppSystem create(AppSystem appSystem) {
        log.info("Repository: Persisting new application system connection entity ledger row");
        try {
            AppSystemEntity entity = appSystemMapper.toEntity(appSystem);
            entity = appSystemJPARepository.save(entity);

            saveAuditRecord(entity, "INSERT");
            return appSystemMapper.toModel(entity);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure executing relational write across application settings", e);
            throw e;
        }
    }

    /**
     * Updates the application-system link identified by {@code id} and records an
     * UPDATE audit row.
     *
     * @param appSystem the model carrying the updated values
     * @param id        identifier of the record to update
     * @return the updated model
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public AppSystem update(AppSystem appSystem, Long id) {
        log.info("Repository: Merging operational records variables for application-system link ID: {}", id);
        try {
            AppSystemEntity entity = appSystemMapper.toEntity(appSystem);
            entity.setId(id);
            entity = appSystemJPARepository.save(entity);

            saveAuditRecord(entity, "UPDATE");
            return appSystemMapper.toModel(entity);
        } catch (DataAccessException e) {
            log.error("Repository error: Corrupted state runtime merging delta mappings onto link target key index: {}", id, e);
            throw e;
        }
    }

    /**
     * Deletes (logically) the given application-system link and records a DELETE
     * audit row.
     *
     * @param appSystem the model to delete
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public void delete(AppSystem appSystem) {
        log.info("Repository: Merging deactivation tracking signatures for link assignment mapping identifier ID: {}", appSystem.getId());
        try {
            AppSystemEntity entity = appSystemMapper.toEntity(appSystem);
            entity.setId(appSystem.getId());
            entity = appSystemJPARepository.save(entity);

            saveAuditRecord(entity, "DELETE");
        } catch (DataAccessException e) {
            log.error("Repository error: Unhandled transaction rollback execution during link deactivation for ID: {}", appSystem.getId(), e);
            throw e;
        }
    }

    /**
     * Resolves an application-system link by its identifier.
     *
     * @param id identifier of the record to resolve
     * @return the matching model, or {@code null} if not found
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public AppSystem findById(Long id) {
        try {
            return appSystemJPARepository.findById(id)
                    .map(appSystemMapper::toModel)
                    .orElse(null);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure tracking contextual matching reference rows for link index ID: {}", id, e);
            throw e;
        }
    }

    /**
     * Resolves a paginated, filtered list of application-system links scoped to a
     * single parent information system database grouping.
     *
     * @param informationSystemDbId mandatory parent grouping identifier scoping the result set
     * @param criteria              additional filter criteria
     * @param pageable              pagination and sorting instructions
     * @return the paginated page of matching models
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public Page<AppSystem> findAll(Long informationSystemDbId, AppSystemCriteria criteria, Pageable pageable) {
        log.debug("Repository: Streaming partitioned mapping allocations frames for informationSystemDbId ID: {}", informationSystemDbId);
        try {
            Specification<AppSystemEntity> spec = AppSystemSpecification.filterByCriteria(informationSystemDbId, criteria);
            Page<AppSystemEntity> entityPage = appSystemJPARepository.findAll(spec, pageable);
            return entityPage.map(appSystemMapper::toModel);
        } catch (DataAccessException e) {
            log.error("Repository error: Broken chunk streaming query handling for links maps allocation layouts", e);
            throw e;
        }
    }

    /**
     * Checks whether an application-system link already exists for the given
     * information system database grouping and system.
     *
     * @param informationSystemDbId identifier of the information system database grouping
     * @param systemId               identifier of the system
     * @return {@code true} if a matching link exists, {@code false} otherwise
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public boolean existsByInformationSystemDbAndSystem(Long informationSystemDbId, Long systemId) {
        try {
            return appSystemJPARepository.existsByInformationSystemDbIdAndSystemId(informationSystemDbId, systemId);
        } catch (DataAccessException e) {
            log.error("Repository error: Failed mapping constraint check verifying link matching tracking state unicity definitions", e);
            throw e;
        }
    }

    /**
     * Checks whether an application-system link already exists for the given
     * information system database grouping and system, excluding a specific record.
     *
     * @param informationSystemDbId identifier of the information system database grouping
     * @param systemId               identifier of the system
     * @param id                     identifier of the record to exclude from the check
     * @return {@code true} if a matching link exists, {@code false} otherwise
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public boolean existsByInformationSystemDbAndSystemAndIdNot(Long informationSystemDbId, Long systemId, Long id) {
        try {
            return appSystemJPARepository.existsByInformationSystemDbIdAndSystemIdAndIdNot(informationSystemDbId, systemId, id);
        } catch (DataAccessException e) {
            log.error("Repository error: Multi-conditional anomaly tracking unique constraints indices parameters conflicts updates context", e);
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
    private void saveAuditRecord(AppSystemEntity entity, String action) {
        try {
            AppSystemAudEntity aud = new AppSystemAudEntity();

            aud.setAppSystemId(entity.getId());
            aud.setInformationSystemDbId(entity.getInformationSystemDb().getId());
            aud.setSystemId(entity.getSystem().getId());

            aud.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now());
            aud.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy() : Utils.resolveCurrentUsername());
           
                        aud.setUpdatedAt(entity.getUpdatedAt());
            aud.setUpdatedBy(entity.getUpdatedBy());
            aud.setDeletedAt(entity.getDeletedAt());
            aud.setDeletedBy(entity.getDeletedBy());

            aud.setAudAction(action);
            aud.setAuditDate(LocalDateTime.now());
            aud.setAuditUser(Utils.resolveCurrentUsername());

            appSystemAudJPARepository.save(aud);
        } catch (DataAccessException e) {
            log.error("Repository trace error: Critical trace audit ledger commit crash mapping application-system event link tracer", e);
            throw e;
        }
    }
}

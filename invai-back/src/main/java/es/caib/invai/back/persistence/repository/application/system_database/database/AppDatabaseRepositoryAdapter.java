package es.caib.invai.back.persistence.repository.application.system_database.database;

import es.caib.invai.back.persistence.model.application.system_database.database.AppDatabaseAudEntity;
import es.caib.invai.back.persistence.model.application.system_database.database.AppDatabaseEntity;
import es.caib.invai.back.service.mapper.application.system_database.database.AppDatabaseMapper;
import es.caib.invai.back.service.model.application.system_database.database.AppDatabase;
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
 * Repository adapter implementing {@link AppDatabaseRepository} on top of Spring Data
 * JPA, translating between domain models and JPA entities and keeping the associated
 * audit trail up to date.
 *
 * @since 1.0.2
 */
@Repository
@Slf4j
public class AppDatabaseRepositoryAdapter implements AppDatabaseRepository {

    /** JPA repository providing CRUD and specification-based search access to the root entity. */
    @Autowired
    private AppDatabaseJPARepository appDatabaseJPARepository;

    /** JPA repository providing persistence for the audit trail entity. */
    @Autowired
    private AppDatabaseAudJPARepository appDatabaseAudJPARepository;

    /** Mapper converting between domain models and JPA entities. */
    @Autowired
    private AppDatabaseMapper appDatabaseMapper;

    /**
     * Persists a new application-database link and records an INSERT audit row.
     *
     * @param appDatabase the model to persist
     * @return the persisted model, including its generated identifier
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public AppDatabase create(AppDatabase appDatabase) {
        log.info("Repository: Persisting new application database connection entity ledger row");
        try {
            AppDatabaseEntity entity = appDatabaseMapper.toEntity(appDatabase);
            entity = appDatabaseJPARepository.save(entity);

            saveAuditRecord(entity, "INSERT");
            return appDatabaseMapper.toModel(entity);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure executing relational write across application settings", e);
            throw e;
        }
    }

    /**
     * Updates the application-database link identified by {@code id} and records an
     * UPDATE audit row.
     *
     * @param appDatabase the model carrying the updated values
     * @param id          identifier of the record to update
     * @return the updated model
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public AppDatabase update(AppDatabase appDatabase, Long id) {
        log.info("Repository: Merging operational records variables for application-database link ID: {}", id);
        try {
            AppDatabaseEntity entity = appDatabaseMapper.toEntity(appDatabase);
            entity.setId(id);
            entity = appDatabaseJPARepository.save(entity);

            saveAuditRecord(entity, "UPDATE");
            return appDatabaseMapper.toModel(entity);
        } catch (DataAccessException e) {
            log.error("Repository error: Corrupted state runtime merging delta mappings onto link target key index: {}", id, e);
            throw e;
        }
    }

    /**
     * Deletes (logically) the given application-database link and records a DELETE
     * audit row.
     *
     * @param appDatabase the model to delete
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public void delete(AppDatabase appDatabase) {
        log.info("Repository: Merging deactivation tracking signatures for link assignment mapping identifier ID: {}", appDatabase.getId());
        try {
            AppDatabaseEntity entity = appDatabaseMapper.toEntity(appDatabase);
            entity.setId(appDatabase.getId());
            entity = appDatabaseJPARepository.save(entity);

            saveAuditRecord(entity, "DELETE");
        } catch (DataAccessException e) {
            log.error("Repository error: Unhandled transaction rollback execution during link deactivation for ID: {}", appDatabase.getId(), e);
            throw e;
        }
    }

    /**
     * Resolves an application-database link by its identifier.
     *
     * @param id identifier of the record to resolve
     * @return the matching model, or {@code null} if not found
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public AppDatabase findById(Long id) {
        try {
            return appDatabaseJPARepository.findById(id)
                    .map(appDatabaseMapper::toModel)
                    .orElse(null);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure tracking contextual matching reference rows for link index ID: {}", id, e);
            throw e;
        }
    }

    /**
     * Resolves a paginated, filtered list of application-database links scoped to a
     * single parent information system database grouping.
     *
     * @param informationSystemDbId mandatory parent grouping identifier scoping the result set
     * @param criteria              additional filter criteria
     * @param pageable              pagination and sorting instructions
     * @return the paginated page of matching models
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public Page<AppDatabase> findAll(Long informationSystemDbId, AppDatabaseCriteria criteria, Pageable pageable) {
        log.info("Repository: Dynamic search pattern stream across application database relations for informationSystemDbId ID: {}", informationSystemDbId);
        try {
            Specification<AppDatabaseEntity> spec = AppDatabaseSpecification.filterByCriteria(informationSystemDbId, criteria);
            Page<AppDatabaseEntity> entityPage = appDatabaseJPARepository.findAll(spec, pageable);
            return entityPage.map(appDatabaseMapper::toModel);
        } catch (DataAccessException e) {
            log.error("Repository error: Broken chunk streaming query handling for application database allocation layouts", e);
            throw e;
        }
    }

    /**
     * Checks whether an application-database link already exists for the given
     * information system database grouping and database.
     *
     * @param informationSystemDbId identifier of the information system database grouping
     * @param databaseId            identifier of the database
     * @return {@code true} if a matching link exists, {@code false} otherwise
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public boolean existsByInformationSystemDbIdAndDatabaseId(Long informationSystemDbId, Long databaseId) {
        try {
            return appDatabaseJPARepository.existsByInformationSystemDbIdAndDatabaseId(informationSystemDbId, databaseId);
        } catch (DataAccessException e) {
            log.error("Repository error: Failed mapping constraint check verifying link matching tracking state unicity definitions", e);
            throw e;
        }
    }

    /**
     * Checks whether an application-database link already exists for the given
     * information system database grouping and database, excluding a specific record.
     *
     * @param informationSystemDbId identifier of the information system database grouping
     * @param databaseId            identifier of the database
     * @param id                    identifier of the record to exclude from the check
     * @return {@code true} if a matching link exists, {@code false} otherwise
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public boolean existsByUniqueCombinationExcludingId(Long informationSystemDbId, Long databaseId, Long id) {
        try {
            return appDatabaseJPARepository.existsByInformationSystemDbIdAndDatabaseIdAndIdNot(informationSystemDbId, databaseId, id);
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
    private void saveAuditRecord(AppDatabaseEntity entity, String action) {
        try {
            AppDatabaseAudEntity aud = new AppDatabaseAudEntity();

            aud.setAppDatabaseId(entity.getId());
            aud.setInformationSystemDbId(entity.getInformationSystemDb().getId());
            aud.setDatabaseId(entity.getDatabase().getId());

            aud.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now());
            aud.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy() : Utils.resolveCurrentUsername());
           
                        aud.setUpdatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt() : LocalDateTime.now());
            aud.setUpdatedBy(entity.getUpdatedBy() != null ? entity.getUpdatedBy() : Utils.resolveCurrentUsername());
            aud.setDeletedAt(entity.getDeletedAt());
            aud.setDeletedBy(entity.getDeletedBy());

            aud.setAudAction(action);
            aud.setAuditDate(LocalDateTime.now());
            aud.setAuditUser(Utils.resolveCurrentUsername());

            appDatabaseAudJPARepository.save(aud);
        } catch (DataAccessException e) {
            log.error("Repository trace error: Critical trace audit ledger commit crash mapping application-database event link tracer", e);
            throw e;
        }
    }
}

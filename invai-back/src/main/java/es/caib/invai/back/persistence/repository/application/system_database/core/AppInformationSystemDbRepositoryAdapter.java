package es.caib.invai.back.persistence.repository.application.system_database.core;

import es.caib.invai.back.persistence.model.application.system_database.core.AppInformationSystemDbAudEntity;
import es.caib.invai.back.persistence.model.application.system_database.core.AppInformationSystemDbEntity;
import es.caib.invai.back.service.mapper.application.system_database.core.AppInformationSystemDbMapper;
import es.caib.invai.back.service.model.application.system_database.core.AppInformationSystemDb;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Repository adapter implementing {@link AppInformationSystemDbRepository} on top of
 * Spring Data JPA, translating between domain models and JPA entities and keeping the
 * associated audit trail up to date.
 *
 * @since 1.0.2
 */
@Repository
@Slf4j
public class AppInformationSystemDbRepositoryAdapter implements AppInformationSystemDbRepository {

    /** JPA repository providing CRUD access to the root entity. */
    @Autowired
    private AppInformationSystemDbJPARepository appInformationSystemDbJPARepository;

    /** JPA repository providing persistence for the audit trail entity. */
    @Autowired
    private AppInformationSystemDbAudJPARepository appInformationSystemDbAudJPARepository;

    /** Mapper converting between domain models and JPA entities. */
    @Autowired
    private AppInformationSystemDbMapper appInformationSystemDbMapper;

    /**
     * Persists a new information system database grouping and records an INSERT audit row.
     *
     * @param appInformationSystemDb the model to persist
     * @return the persisted model, including its generated identifier
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public AppInformationSystemDb create(AppInformationSystemDb appInformationSystemDb) {
        log.info("Repository: Persisting new application information system database entity ledger row");
        try {
            AppInformationSystemDbEntity entity = appInformationSystemDbMapper.toEntity(appInformationSystemDb);
            entity = appInformationSystemDbJPARepository.save(entity);

            saveAuditRecord(entity, "INSERT");
            return appInformationSystemDbMapper.toModel(entity);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure executing relational write across application settings", e);
            throw e;
        }
    }

    /**
     * Updates the information system database grouping identified by {@code id} and
     * records an UPDATE audit row.
     *
     * @param appInformationSystemDb the model carrying the updated values
     * @param id                     identifier of the record to update
     * @return the updated model
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public AppInformationSystemDb update(AppInformationSystemDb appInformationSystemDb, Long id) {
        log.info("Repository: Merging operational records variables for application information system database ID: {}", id);
        try {
            AppInformationSystemDbEntity entity = appInformationSystemDbMapper.toEntity(appInformationSystemDb);
            entity.setId(id);
            entity = appInformationSystemDbJPARepository.save(entity);

            saveAuditRecord(entity, "UPDATE");
            return appInformationSystemDbMapper.toModel(entity);
        } catch (DataAccessException e) {
            log.error("Repository error: Corrupted state runtime merging delta mappings onto target key index: {}", id, e);
            throw e;
        }
    }

    /**
     * Deletes (logically) the given information system database grouping and records
     * a DELETE audit row.
     *
     * @param appInformationSystemDb the model to delete
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public void delete(AppInformationSystemDb appInformationSystemDb) {
        log.info("Repository: Merging deactivation tracking signatures for assignment mapping identifier ID: {}", appInformationSystemDb.getId());
        try {
            AppInformationSystemDbEntity entity = appInformationSystemDbMapper.toEntity(appInformationSystemDb);
            entity.setId(appInformationSystemDb.getId());
            entity = appInformationSystemDbJPARepository.save(entity);

            saveAuditRecord(entity, "DELETE");
        } catch (DataAccessException e) {
            log.error("Repository error: Unhandled transaction rollback execution during deactivation for ID: {}", appInformationSystemDb.getId(), e);
            throw e;
        }
    }

    /**
     * Resolves an information system database grouping by its identifier.
     *
     * @param id identifier of the record to resolve
     * @return the matching model, or {@code null} if not found
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public AppInformationSystemDb findById(Long id) {
        try {
            return appInformationSystemDbJPARepository.findById(id)
                    .map(appInformationSystemDbMapper::toModel)
                    .orElse(null);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure tracking contextual matching reference rows for index ID: {}", id, e);
            throw e;
        }
    }

    /**
     * Resolves the information system database grouping owned by the given application.
     *
     * @param applicationId identifier of the parent application
     * @return the matching model, or {@code null} if not found
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public AppInformationSystemDb findByApplicationId(Long applicationId) {
        log.info("Repository: Dynamic search pattern stream across application information system database relations for Application ID: {}", applicationId);
        try {
            return appInformationSystemDbJPARepository.findByApplicationId(applicationId)
                    .map(appInformationSystemDbMapper::toModel)
                    .orElse(null);
        } catch (DataAccessException e) {
            log.error("Repository error: Broken chunk streaming query handling for application information system database layouts", e);
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
    private void saveAuditRecord(AppInformationSystemDbEntity entity, String action) {
        try {
            AppInformationSystemDbAudEntity aud = new AppInformationSystemDbAudEntity();

            aud.setId(entity.getId());
            aud.setApplicationId(entity.getApplication().getId());
            aud.setObservation(entity.getObservation());

            aud.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now());
            aud.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy() : Utils.resolveCurrentUsername());
           
                        aud.setUpdatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt() : LocalDateTime.now());
            aud.setUpdatedBy(entity.getUpdatedBy() != null ? entity.getUpdatedBy() : Utils.resolveCurrentUsername());
            aud.setDeletedAt(entity.getDeletedAt());
            aud.setDeletedBy(entity.getDeletedBy());

            aud.setAudAction(action);
            aud.setAuditDate(LocalDateTime.now());
            aud.setAuditUser(Utils.resolveCurrentUsername());

            appInformationSystemDbAudJPARepository.save(aud);
        } catch (DataAccessException e) {
            log.error("Repository trace error: Critical trace audit ledger commit crash mapping application information system database event tracer", e);
            throw e;
        }
    }
}

package es.caib.invai.back.persistence.repository.application.security.core;

import es.caib.invai.back.persistence.model.application.security.core.AppSecurityAudEntity;
import es.caib.invai.back.persistence.model.application.security.core.AppSecurityEntity;
import es.caib.invai.back.service.mapper.application.security.core.AppSecurityMapper;
import es.caib.invai.back.service.model.application.security.core.AppSecurity;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Repository adapter implementing {@link AppSecurityRepository} on top of
 * Spring Data JPA, translating between domain models and JPA entities and keeping the
 * associated audit trail up to date.
 *
 * @since 1.0.4
 */
@Repository
@Slf4j
public class AppSecurityRepositoryAdapter implements AppSecurityRepository {

    /** JPA repository providing CRUD access to the root entity. */
    @Autowired
    private AppSecurityJPARepository appSecurityJPARepository;

    /** JPA repository providing persistence for the audit trail entity. */
    @Autowired
    private AppSecurityAudJPARepository appSecurityAudJPARepository;

    /** Mapper converting between domain models and JPA entities. */
    @Autowired
    private AppSecurityMapper appSecurityMapper;

    /**
     * Persists a new security anchor and records an INSERT audit row.
     *
     * @param appSecurity the model to persist
     * @return the persisted model, including its generated identifier
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public AppSecurity create(AppSecurity appSecurity) {
        log.info("Repository: Persisting new application security entity ledger row");
        try {
            AppSecurityEntity entity = appSecurityMapper.toEntity(appSecurity);
            entity = appSecurityJPARepository.save(entity);

            saveAuditRecord(entity, "INSERT");
            return appSecurityMapper.toModel(entity);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure executing relational write across application settings", e);
            throw e;
        }
    }

    /**
     * Updates the security anchor identified by {@code id} and
     * records an UPDATE audit row.
     *
     * @param appSecurity the model carrying the updated values
     * @param id          identifier of the record to update
     * @return the updated model
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public AppSecurity update(AppSecurity appSecurity, Long id) {
        log.info("Repository: Merging operational records variables for application security ID: {}", id);
        try {
            AppSecurityEntity entity = appSecurityMapper.toEntity(appSecurity);
            entity.setId(id);
            entity = appSecurityJPARepository.save(entity);

            saveAuditRecord(entity, "UPDATE");
            return appSecurityMapper.toModel(entity);
        } catch (DataAccessException e) {
            log.error("Repository error: Corrupted state runtime merging delta mappings onto target key index: {}", id, e);
            throw e;
        }
    }

    /**
     * Deletes (logically) the given security anchor and records
     * a DELETE audit row.
     *
     * @param appSecurity the model to delete
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public void delete(AppSecurity appSecurity) {
        log.info("Repository: Merging deactivation tracking signatures for assignment mapping identifier ID: {}", appSecurity.getId());
        try {
            AppSecurityEntity entity = appSecurityMapper.toEntity(appSecurity);
            entity.setId(appSecurity.getId());
            entity = appSecurityJPARepository.save(entity);

            saveAuditRecord(entity, "DELETE");
        } catch (DataAccessException e) {
            log.error("Repository error: Unhandled transaction rollback execution during deactivation for ID: {}", appSecurity.getId(), e);
            throw e;
        }
    }

    /**
     * Resolves a security anchor by its identifier.
     *
     * @param id identifier of the record to resolve
     * @return the matching model, or {@code null} if not found
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public AppSecurity findById(Long id) {
        try {
            return appSecurityJPARepository.findById(id)
                    .map(appSecurityMapper::toModel)
                    .orElse(null);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure tracking contextual matching reference rows for index ID: {}", id, e);
            throw e;
        }
    }

    /**
     * Resolves the security anchor owned by the given application.
     *
     * @param applicationId identifier of the parent application
     * @return the matching model, or {@code null} if not found
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public AppSecurity findByApplicationId(Long applicationId) {
        log.debug("Repository: Dynamic search pattern stream across application security relations for Application ID: {}", applicationId);
        try {
            return appSecurityJPARepository.findByApplicationId(applicationId)
                    .map(appSecurityMapper::toModel)
                    .orElse(null);
        } catch (DataAccessException e) {
            log.error("Repository error: Broken chunk streaming query handling for application security layouts", e);
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
    private void saveAuditRecord(AppSecurityEntity entity, String action) {
        try {
            AppSecurityAudEntity aud = new AppSecurityAudEntity();

            aud.setId(entity.getId());
            aud.setApplicationId(entity.getApplication().getId());
            aud.setObservation(entity.getObservation());

            aud.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now());
            aud.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy() : Utils.resolveCurrentUsername());

            aud.setUpdatedAt(entity.getUpdatedAt());
            aud.setUpdatedBy(entity.getUpdatedBy());
            aud.setDeletedAt(entity.getDeletedAt());
            aud.setDeletedBy(entity.getDeletedBy());

            aud.setAudAction(action);
            aud.setAuditDate(LocalDateTime.now());
            aud.setAuditUser(Utils.resolveCurrentUsername());

            appSecurityAudJPARepository.save(aud);
        } catch (DataAccessException e) {
            log.error("Repository trace error: Critical trace audit ledger commit crash mapping application security event tracer", e);
            throw e;
        }
    }
}

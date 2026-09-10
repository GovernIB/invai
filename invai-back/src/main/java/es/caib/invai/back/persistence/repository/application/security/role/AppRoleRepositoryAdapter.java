package es.caib.invai.back.persistence.repository.application.security.role;

import es.caib.invai.back.persistence.model.application.security.role.AppRoleAudEntity;
import es.caib.invai.back.persistence.model.application.security.role.AppRoleEntity;
import es.caib.invai.back.service.mapper.application.security.role.AppRoleMapper;
import es.caib.invai.back.service.model.application.security.role.AppRole;
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
 * Repository adapter implementing {@link AppRoleRepository} on top of Spring Data
 * JPA, translating between domain models and JPA entities and keeping the associated
 * audit trail up to date.
 *
 * @since 1.0.4
 */
@Repository
@Slf4j
public class AppRoleRepositoryAdapter implements AppRoleRepository {

    /** JPA repository providing CRUD and specification-based search access to the root entity. */
    @Autowired
    private AppRoleJPARepository appRoleJPARepository;

    /** JPA repository providing persistence for the audit trail entity. */
    @Autowired
    private AppRoleAudJPARepository appRoleAudJPARepository;

    /** Mapper converting between domain models and JPA entities. */
    @Autowired
    private AppRoleMapper appRoleMapper;

    /**
     * Persists a new application-role assignment and records an INSERT audit row.
     *
     * @param appRole the model to persist
     * @return the persisted model, including its generated identifier
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public AppRole create(AppRole appRole) {
        log.info("Repository: Persisting new application role assignment entity ledger row");
        try {
            AppRoleEntity entity = appRoleMapper.toEntity(appRole);
            entity = appRoleJPARepository.save(entity);

            saveAuditRecord(entity, "INSERT");
            return appRoleMapper.toModel(entity);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure executing relational write across application settings", e);
            throw e;
        }
    }

    /**
     * Updates the application-role assignment identified by {@code id} and records an
     * UPDATE audit row.
     *
     * @param appRole the model carrying the updated values
     * @param id      identifier of the record to update
     * @return the updated model
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public AppRole update(AppRole appRole, Long id) {
        log.info("Repository: Merging operational records variables for application-role assignment ID: {}", id);
        try {
            AppRoleEntity entity = appRoleMapper.toEntity(appRole);
            entity.setId(id);
            entity = appRoleJPARepository.save(entity);

            saveAuditRecord(entity, "UPDATE");
            return appRoleMapper.toModel(entity);
        } catch (DataAccessException e) {
            log.error("Repository error: Corrupted state runtime merging delta mappings onto assignment target key index: {}", id, e);
            throw e;
        }
    }

    /**
     * Deletes (logically) the given application-role assignment and records a DELETE
     * audit row.
     *
     * @param appRole the model to delete
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public void delete(AppRole appRole) {
        log.info("Repository: Merging deactivation tracking signatures for assignment mapping identifier ID: {}", appRole.getId());
        try {
            AppRoleEntity entity = appRoleMapper.toEntity(appRole);
            entity.setId(appRole.getId());
            entity = appRoleJPARepository.save(entity);

            saveAuditRecord(entity, "DELETE");
        } catch (DataAccessException e) {
            log.error("Repository error: Unhandled transaction rollback execution during assignment deactivation for ID: {}", appRole.getId(), e);
            throw e;
        }
    }

    /**
     * Resolves an application-role assignment by its identifier.
     *
     * @param id identifier of the record to resolve
     * @return the matching model, or {@code null} if not found
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public AppRole findById(Long id) {
        try {
            return appRoleJPARepository.findById(id)
                    .map(appRoleMapper::toModel)
                    .orElse(null);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure tracking contextual matching reference rows for assignment index ID: {}", id, e);
            throw e;
        }
    }

    /**
     * Resolves a paginated, filtered list of application-role assignments scoped to a
     * single parent security anchor.
     *
     * @param appSecurityId mandatory parent security anchor identifier scoping the result set
     * @param criteria      additional filter criteria
     * @param pageable      pagination and sorting instructions
     * @return the paginated page of matching models
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public Page<AppRole> findAll(Long appSecurityId, AppRoleCriteria criteria, Pageable pageable) {
        log.debug("Repository: Dynamic search pattern stream across application role relations for appSecurityId ID: {}", appSecurityId);
        try {
            Specification<AppRoleEntity> spec = AppRoleSpecification.filterByCriteria(appSecurityId, criteria);
            Page<AppRoleEntity> entityPage = appRoleJPARepository.findAll(spec, pageable);
            return entityPage.map(appRoleMapper::toModel);
        } catch (DataAccessException e) {
            log.error("Repository error: Broken chunk streaming query handling for application role allocation layouts", e);
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
    private void saveAuditRecord(AppRoleEntity entity, String action) {
        try {
            AppRoleAudEntity aud = new AppRoleAudEntity();

            aud.setAppRoleId(entity.getId());
            aud.setAppSecurityId(entity.getAppSecurity().getId());
            aud.setSecurityRoleId(entity.getSecurityRole().getId());

            aud.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now());
            aud.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy() : Utils.resolveCurrentUsername());

            aud.setUpdatedAt(entity.getUpdatedAt());
            aud.setUpdatedBy(entity.getUpdatedBy());
            aud.setDeletedAt(entity.getDeletedAt());
            aud.setDeletedBy(entity.getDeletedBy());

            aud.setAudAction(action);
            aud.setAuditDate(LocalDateTime.now());
            aud.setAuditUser(Utils.resolveCurrentUsername());

            appRoleAudJPARepository.save(aud);
        } catch (DataAccessException e) {
            log.error("Repository trace error: Critical trace audit ledger commit crash mapping application-role event link tracer", e);
            throw e;
        }
    }
}

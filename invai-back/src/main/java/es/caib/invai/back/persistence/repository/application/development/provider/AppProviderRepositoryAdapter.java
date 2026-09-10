package es.caib.invai.back.persistence.repository.application.development.provider;

import es.caib.invai.back.persistence.model.application.development.provider.AppProviderAudEntity;
import es.caib.invai.back.persistence.model.application.development.provider.AppProviderEntity;
import es.caib.invai.back.service.mapper.application.development.provider.AppProviderMapper;
import es.caib.invai.back.service.model.application.development.provider.AppProvider;
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
 * JPA-backed implementation of {@link AppProviderRepository}: delegates CRUD and filtered
 * lookups to {@link AppProviderJPARepository}, and records an {@link AppProviderAudEntity}
 * snapshot on every create, update, and (soft) delete.
 *
 * @since 1.0.2
 */
@Repository
@Slf4j
public class AppProviderRepositoryAdapter implements AppProviderRepository {

    /** Spring Data repository handling CRUD operations for the provider entity. */
    @Autowired
    private AppProviderJPARepository appProviderJPARepository;

    /** Spring Data repository handling persistence of provider audit trail rows. */
    @Autowired
    private AppProviderAudJPARepository appProviderAudJPARepository;

    /** MapStruct mapper handling transformations between entities and domain models. */
    @Autowired
    private AppProviderMapper appProviderMapper;

    /**
     * Persists a new provider entity and records the corresponding audit trail row.
     *
     * @param appProvider the domain model to persist
     * @return the persisted domain model, including its generated identifier
     * @throws DataAccessException if the underlying relational write fails
     */
    @Override
    public AppProvider create(AppProvider appProvider) {
        log.info("Repository: Persisting new provider entity ledger row");
        try {
            AppProviderEntity entity = appProviderMapper.toEntity(appProvider);
            entity = appProviderJPARepository.save(entity);

            saveAuditRecord(entity, "INSERT");
            return appProviderMapper.toModel(entity);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure executing relational write across provider settings", e);
            throw e;
        }
    }

    /**
     * Persists changes to an existing provider entity and records the corresponding audit trail row.
     *
     * @param appProvider the domain model carrying the updated values
     * @param id          the identifier of the record being updated
     * @return the persisted domain model reflecting the applied changes
     * @throws DataAccessException if the underlying relational write fails
     */
    @Override
    public AppProvider update(AppProvider appProvider, Long id) {
        log.info("Repository: Merging operational records variables for provider ID: {}", id);
        try {
            AppProviderEntity entity = appProviderMapper.toEntity(appProvider);
            entity.setId(id);
            entity = appProviderJPARepository.save(entity);

            saveAuditRecord(entity, "UPDATE");
            return appProviderMapper.toModel(entity);
        } catch (DataAccessException e) {
            log.error("Repository error: Corrupted state runtime merging delta mappings onto target key index: {}", id, e);
            throw e;
        }
    }

    /**
     * Persists the logical (soft) deletion of a provider entity and records the corresponding
     * audit trail row.
     *
     * @param appProvider the domain model, with deletion tracking fields already set
     * @throws DataAccessException if the underlying relational write fails
     */
    @Override
    public void delete(AppProvider appProvider) {
        log.info("Repository: Merging deactivation tracking signatures for provider identifier ID: {}", appProvider.getId());
        try {
            AppProviderEntity entity = appProviderMapper.toEntity(appProvider);
            entity.setId(appProvider.getId());
            entity = appProviderJPARepository.save(entity);

            saveAuditRecord(entity, "DELETE");
        } catch (DataAccessException e) {
            log.error("Repository error: Unhandled transaction rollback execution during deactivation for ID: {}", appProvider.getId(), e);
            throw e;
        }
    }

    /**
     * Resolves a single provider entity by its primary key.
     *
     * @param id the record identifier
     * @return the matching domain model, or {@code null} if not found
     * @throws DataAccessException if the underlying relational read fails
     */
    @Override
    public AppProvider findById(Long id) {
        try {
            return appProviderJPARepository.findById(id)
                    .map(appProviderMapper::toModel)
                    .orElse(null);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure tracking contextual matching reference rows for index ID: {}", id, e);
            throw e;
        }
    }

    /**
     * Fetches the providers assigned to a single development, filtered by {@code criteria} and
     * paged, via the {@link AppProviderSpecification} built from both parameters.
     *
     * @param appDevelopmentId mandatory parent development identifier scoping the result set
     * @param criteria         optional additional filters (status, search); {@code null} applies
     *                         only the development scope
     * @param pageable         pagination and sorting parameters
     * @return the matching page of domain models
     * @throws DataAccessException if the underlying relational read fails
     */
    @Override
    public Page<AppProvider> findAll(Long appDevelopmentId, AppProviderCriteria criteria, Pageable pageable) {
        log.debug("Repository: Dynamic search pattern stream across provider relations for Development ID: {}", appDevelopmentId);
        try {
            Specification<AppProviderEntity> spec = AppProviderSpecification.filterByCriteria(appDevelopmentId, criteria);
            Page<AppProviderEntity> entityPage = appProviderJPARepository.findAll(spec, pageable);
            return entityPage.map(appProviderMapper::toModel);
        } catch (DataAccessException e) {
            log.error("Repository error: Broken chunk streaming query handling for provider layouts", e);
            throw e;
        }
    }

    /**
     * Checks whether any provider record references the given role catalog entry.
     *
     * @param roleId the role catalog identifier to check
     * @return {@code true} if at least one provider references the role, {@code false} otherwise
     */
    @Override
    public boolean existsByRoleId(Long roleId) {
        return appProviderJPARepository.existsByRoleId(roleId);
    }

    /**
     * Builds and persists an audit trail row capturing a snapshot of the given entity state.
     *
     * @param entity the entity state to snapshot
     * @param action the lifecycle action being recorded (INSERT, UPDATE, or DELETE)
     */
    private void saveAuditRecord(AppProviderEntity entity, String action) {
        try {
            AppProviderAudEntity aud = new AppProviderAudEntity();

            aud.setProviderId(entity.getId());
            aud.setAppDevelopmentId(entity.getAppDevelopment().getId());
            aud.setCompanyName(entity.getCompanyName());
            aud.setRoleId(entity.getRole() != null ? entity.getRole().getId() : null);
            aud.setStartDate(entity.getStartDate());
            aud.setExpireDate(entity.getExpireDate());

            aud.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now());
            aud.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy() : Utils.resolveCurrentUsername());
           
                        aud.setUpdatedAt(entity.getUpdatedAt());
            aud.setUpdatedBy(entity.getUpdatedBy());
            aud.setDeletedAt(entity.getDeletedAt());
            aud.setDeletedBy(entity.getDeletedBy());

            aud.setAudAction(action);
            aud.setAuditDate(LocalDateTime.now());
            aud.setAuditUser(Utils.resolveCurrentUsername());

            appProviderAudJPARepository.save(aud);
        } catch (DataAccessException e) {
            log.error("Repository trace error: Critical trace audit ledger commit crash mapping provider event tracer", e);
            throw e;
        }
    }
}

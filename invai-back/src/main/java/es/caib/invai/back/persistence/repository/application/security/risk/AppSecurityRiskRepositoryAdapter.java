package es.caib.invai.back.persistence.repository.application.security.risk;

import es.caib.invai.back.persistence.model.application.security.risk.AppSecurityRiskAudEntity;
import es.caib.invai.back.persistence.model.application.security.risk.AppSecurityRiskEntity;
import es.caib.invai.back.service.mapper.application.security.risk.AppSecurityRiskMapper;
import es.caib.invai.back.service.model.application.security.risk.AppSecurityRisk;
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
 * Repository adapter implementing {@link AppSecurityRiskRepository} on top of Spring Data
 * JPA, translating between domain models and JPA entities and keeping the associated
 * audit trail up to date.
 *
 * @since 1.0.4
 */
@Repository
@Slf4j
public class AppSecurityRiskRepositoryAdapter implements AppSecurityRiskRepository {

    /** JPA repository providing CRUD and specification-based search access to the root entity. */
    @Autowired
    private AppSecurityRiskJPARepository appSecurityRiskJPARepository;

    /** JPA repository providing persistence for the audit trail entity. */
    @Autowired
    private AppSecurityRiskAudJPARepository appSecurityRiskAudJPARepository;

    /** Mapper converting between domain models and JPA entities. */
    @Autowired
    private AppSecurityRiskMapper appSecurityRiskMapper;

    /**
     * Persists a new security risk and records an INSERT audit row.
     *
     * @param appSecurityRisk the model to persist
     * @return the persisted model, including its generated identifier
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public AppSecurityRisk create(AppSecurityRisk appSecurityRisk) {
        log.info("Repository: Persisting new application security risk entity ledger row");
        try {
            AppSecurityRiskEntity entity = appSecurityRiskMapper.toEntity(appSecurityRisk);
            entity = appSecurityRiskJPARepository.save(entity);

            saveAuditRecord(entity, "INSERT");
            return appSecurityRiskMapper.toModel(entity);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure executing relational write across application settings", e);
            throw e;
        }
    }

    /**
     * Updates the security risk identified by {@code id} and records an
     * UPDATE audit row.
     *
     * @param appSecurityRisk the model carrying the updated values
     * @param id              identifier of the record to update
     * @return the updated model
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public AppSecurityRisk update(AppSecurityRisk appSecurityRisk, Long id) {
        log.info("Repository: Merging operational records variables for application security risk ID: {}", id);
        try {
            AppSecurityRiskEntity entity = appSecurityRiskMapper.toEntity(appSecurityRisk);
            entity.setId(id);
            entity = appSecurityRiskJPARepository.save(entity);

            saveAuditRecord(entity, "UPDATE");
            return appSecurityRiskMapper.toModel(entity);
        } catch (DataAccessException e) {
            log.error("Repository error: Corrupted state runtime merging delta mappings onto risk target key index: {}", id, e);
            throw e;
        }
    }

    /**
     * Deletes (logically) the given security risk and records a DELETE
     * audit row.
     *
     * @param appSecurityRisk the model to delete
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public void delete(AppSecurityRisk appSecurityRisk) {
        log.info("Repository: Merging deactivation tracking signatures for risk assignment mapping identifier ID: {}", appSecurityRisk.getId());
        try {
            AppSecurityRiskEntity entity = appSecurityRiskMapper.toEntity(appSecurityRisk);
            entity.setId(appSecurityRisk.getId());
            entity = appSecurityRiskJPARepository.save(entity);

            saveAuditRecord(entity, "DELETE");
        } catch (DataAccessException e) {
            log.error("Repository error: Unhandled transaction rollback execution during risk deactivation for ID: {}", appSecurityRisk.getId(), e);
            throw e;
        }
    }

    /**
     * Resolves a security risk by its identifier.
     *
     * @param id identifier of the record to resolve
     * @return the matching model, or {@code null} if not found
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public AppSecurityRisk findById(Long id) {
        try {
            return appSecurityRiskJPARepository.findById(id)
                    .map(appSecurityRiskMapper::toModel)
                    .orElse(null);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure tracking contextual matching reference rows for risk index ID: {}", id, e);
            throw e;
        }
    }

    /**
     * Resolves a paginated, filtered list of security risks scoped to a
     * single parent security anchor.
     *
     * @param appSecurityId mandatory parent security anchor identifier scoping the result set
     * @param criteria      additional filter criteria
     * @param pageable      pagination and sorting instructions
     * @return the paginated page of matching models
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public Page<AppSecurityRisk> findAll(Long appSecurityId, AppSecurityRiskCriteria criteria, Pageable pageable) {
        log.debug("Repository: Dynamic search pattern stream across application security risk relations for appSecurityId ID: {}", appSecurityId);
        try {
            Specification<AppSecurityRiskEntity> spec = AppSecurityRiskSpecification.filterByCriteria(appSecurityId, criteria);
            Page<AppSecurityRiskEntity> entityPage = appSecurityRiskJPARepository.findAll(spec, pageable);
            return entityPage.map(appSecurityRiskMapper::toModel);
        } catch (DataAccessException e) {
            log.error("Repository error: Broken chunk streaming query handling for application security risk allocation layouts", e);
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
    private void saveAuditRecord(AppSecurityRiskEntity entity, String action) {
        try {
            AppSecurityRiskAudEntity aud = new AppSecurityRiskAudEntity();

            aud.setAppSecurityRiskId(entity.getId());
            aud.setAppSecurityId(entity.getAppSecurity() != null ? entity.getAppSecurity().getId() : null);
            aud.setLevelId(entity.getLevel() != null ? entity.getLevel().getId() : null);
            aud.setDescription(entity.getDescription());
            aud.setFieldId(entity.getField() != null ? entity.getField().getId() : null);

            aud.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now());
            aud.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy() : Utils.resolveCurrentUsername());

            aud.setUpdatedAt(entity.getUpdatedAt());
            aud.setUpdatedBy(entity.getUpdatedBy());
            aud.setDeletedAt(entity.getDeletedAt());
            aud.setDeletedBy(entity.getDeletedBy());

            aud.setAudAction(action);
            aud.setAuditDate(LocalDateTime.now());
            aud.setAuditUser(Utils.resolveCurrentUsername());

            appSecurityRiskAudJPARepository.save(aud);
        } catch (DataAccessException e) {
            log.error("Repository trace error: Critical trace audit ledger commit crash mapping application security risk event link tracer", e);
            throw e;
        }
    }
}

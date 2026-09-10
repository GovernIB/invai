package es.caib.invai.back.persistence.repository.application.accessibility;

import es.caib.invai.back.persistence.model.application.accessibility.AppAccessibilityAudEntity;
import es.caib.invai.back.persistence.model.application.accessibility.AppAccessibilityEntity;
import es.caib.invai.back.service.mapper.application.accessibility.AppAccessibilityMapper;
import es.caib.invai.back.service.model.application.accessibility.AppAccessibility;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Repository adapter implementing {@link AppAccessibilityRepository} on top of
 * Spring Data JPA, translating between domain models and JPA entities and keeping the
 * associated audit trail up to date.
 *
 * @since 1.0.4
 */
@Repository
@Slf4j
public class AppAccessibilityRepositoryAdapter implements AppAccessibilityRepository {

    /** JPA repository providing CRUD access to the root entity. */
    @Autowired
    private AppAccessibilityJPARepository appAccessibilityJPARepository;

    /** JPA repository providing persistence for the audit trail entity. */
    @Autowired
    private AppAccessibilityAudJPARepository appAccessibilityAudJPARepository;

    /** Mapper converting between domain models and JPA entities. */
    @Autowired
    private AppAccessibilityMapper appAccessibilityMapper;

    /**
     * Persists a new accessibility anchor and records an INSERT audit row.
     *
     * @param appAccessibility the model to persist
     * @return the persisted model, including its generated identifier
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public AppAccessibility create(AppAccessibility appAccessibility) {
        log.info("Repository: Persisting new application accessibility entity ledger row");
        try {
            AppAccessibilityEntity entity = appAccessibilityMapper.toEntity(appAccessibility);
            entity = appAccessibilityJPARepository.save(entity);

            saveAuditRecord(entity, "INSERT");
            return appAccessibilityMapper.toModel(entity);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure executing relational write across application settings", e);
            throw e;
        }
    }

    /**
     * Updates the accessibility anchor identified by {@code id} and
     * records an UPDATE audit row.
     *
     * @param appAccessibility the model carrying the updated values
     * @param id                identifier of the record to update
     * @return the updated model
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public AppAccessibility update(AppAccessibility appAccessibility, Long id) {
        log.info("Repository: Merging operational records variables for application accessibility ID: {}", id);
        try {
            AppAccessibilityEntity entity = appAccessibilityMapper.toEntity(appAccessibility);
            entity.setId(id);
            entity = appAccessibilityJPARepository.save(entity);

            saveAuditRecord(entity, "UPDATE");
            return appAccessibilityMapper.toModel(entity);
        } catch (DataAccessException e) {
            log.error("Repository error: Corrupted state runtime merging delta mappings onto target key index: {}", id, e);
            throw e;
        }
    }

    /**
     * Deletes (logically) the given accessibility anchor and records
     * a DELETE audit row.
     *
     * @param appAccessibility the model to delete
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public void delete(AppAccessibility appAccessibility) {
        log.info("Repository: Merging deactivation tracking signatures for accessibility mapping identifier ID: {}", appAccessibility.getId());
        try {
            AppAccessibilityEntity entity = appAccessibilityMapper.toEntity(appAccessibility);
            entity.setId(appAccessibility.getId());
            entity = appAccessibilityJPARepository.save(entity);

            saveAuditRecord(entity, "DELETE");
        } catch (DataAccessException e) {
            log.error("Repository error: Unhandled transaction rollback execution during deactivation for ID: {}", appAccessibility.getId(), e);
            throw e;
        }
    }

    /**
     * Resolves an accessibility anchor by its identifier.
     *
     * @param id identifier of the record to resolve
     * @return the matching model, or {@code null} if not found
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public AppAccessibility findById(Long id) {
        try {
            return appAccessibilityJPARepository.findById(id)
                    .map(appAccessibilityMapper::toModel)
                    .orElse(null);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure tracking contextual matching reference rows for index ID: {}", id, e);
            throw e;
        }
    }

    /**
     * Resolves the accessibility anchor owned by the given application, active or soft-deleted.
     *
     * @param applicationId identifier of the parent application
     * @return the matching model, or {@code null} if none exists (soft-deleted rows still match)
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public AppAccessibility findByApplicationId(Long applicationId) {
        log.debug("Repository: Dynamic search pattern stream across application accessibility relations for Application ID: {}", applicationId);
        try {
            return appAccessibilityJPARepository.findByApplicationId(applicationId)
                    .map(appAccessibilityMapper::toModel)
                    .orElse(null);
        } catch (DataAccessException e) {
            log.error("Repository error: Broken chunk streaming query handling for application accessibility layouts", e);
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
    private void saveAuditRecord(AppAccessibilityEntity entity, String action) {
        try {
            AppAccessibilityAudEntity aud = new AppAccessibilityAudEntity();

            aud.setAppAccessibilityId(entity.getId());
            aud.setApplicationId(entity.getApplication().getId());
            aud.setComplianceId(entity.getCompliance() != null ? entity.getCompliance().getId() : null);
            aud.setClassificationSegmentId(entity.getClassificationSegment() != null ? entity.getClassificationSegment().getId() : null);
            aud.setPublicUrl(entity.getPublicUrl());
            aud.setMobileApplication(entity.getMobileApplication());
            aud.setMobileApplicationName(entity.getMobileApplicationName());
            aud.setNonAccessibleContent(entity.getNonAccessibleContent());
            aud.setObservations(entity.getObservations());
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

            appAccessibilityAudJPARepository.save(aud);
        } catch (DataAccessException e) {
            log.error("Repository trace error: Critical trace audit ledger commit crash mapping application accessibility event tracer", e);
            throw e;
        }
    }
}

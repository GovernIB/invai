package es.caib.invai.back.persistence.repository.application.security.ensClassification;

import es.caib.invai.back.persistence.model.application.security.ensClassification.AppEnsClassificationAudEntity;
import es.caib.invai.back.persistence.model.application.security.ensClassification.AppEnsClassificationEntity;
import es.caib.invai.back.service.mapper.application.security.ensClassification.AppEnsClassificationMapper;
import es.caib.invai.back.service.model.application.security.ensClassification.AppEnsClassification;
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
 * Repository adapter implementing {@link AppEnsClassificationRepository} on top of Spring Data
 * JPA, translating between domain models and JPA entities and keeping the associated
 * audit trail up to date.
 *
 * @since 1.0.4
 */
@Repository
@Slf4j
public class AppEnsClassificationRepositoryAdapter implements AppEnsClassificationRepository {

    /** JPA repository providing CRUD and specification-based search access to the root entity. */
    @Autowired
    private AppEnsClassificationJPARepository appEnsClassificationJPARepository;

    /** JPA repository providing persistence for the audit trail entity. */
    @Autowired
    private AppEnsClassificationAudJPARepository appEnsClassificationAudJPARepository;

    /** Mapper converting between domain models and JPA entities. */
    @Autowired
    private AppEnsClassificationMapper appEnsClassificationMapper;

    /**
     * Persists a new ENS classification record and records an INSERT audit row.
     *
     * @param appEnsClassification the model to persist
     * @return the persisted model, including its generated identifier
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public AppEnsClassification create(AppEnsClassification appEnsClassification) {
        log.info("Repository: Persisting new ENS classification entity ledger row");
        try {
            AppEnsClassificationEntity entity = appEnsClassificationMapper.toEntity(appEnsClassification);
            entity = appEnsClassificationJPARepository.save(entity);

            saveAuditRecord(entity, "INSERT");
            return appEnsClassificationMapper.toModel(entity);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure executing relational write across application settings", e);
            throw e;
        }
    }

    /**
     * Updates the ENS classification record identified by {@code id} and records an
     * UPDATE audit row.
     *
     * @param appEnsClassification the model carrying the updated values
     * @param id                   identifier of the record to update
     * @return the updated model
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public AppEnsClassification update(AppEnsClassification appEnsClassification, Long id) {
        log.info("Repository: Merging operational records variables for ENS classification ID: {}", id);
        try {
            AppEnsClassificationEntity entity = appEnsClassificationMapper.toEntity(appEnsClassification);
            entity.setId(id);
            entity = appEnsClassificationJPARepository.save(entity);

            saveAuditRecord(entity, "UPDATE");
            return appEnsClassificationMapper.toModel(entity);
        } catch (DataAccessException e) {
            log.error("Repository error: Corrupted state runtime merging delta mappings onto link target key index: {}", id, e);
            throw e;
        }
    }

    /**
     * Deletes (logically) the given ENS classification record and records a DELETE
     * audit row.
     *
     * @param appEnsClassification the model to delete
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public void delete(AppEnsClassification appEnsClassification) {
        log.info("Repository: Merging deactivation tracking signatures for ENS classification identifier ID: {}", appEnsClassification.getId());
        try {
            AppEnsClassificationEntity entity = appEnsClassificationMapper.toEntity(appEnsClassification);
            entity.setId(appEnsClassification.getId());
            entity = appEnsClassificationJPARepository.save(entity);

            saveAuditRecord(entity, "DELETE");
        } catch (DataAccessException e) {
            log.error("Repository error: Unhandled transaction rollback execution during deactivation for ID: {}", appEnsClassification.getId(), e);
            throw e;
        }
    }

    /**
     * Resolves an ENS classification record by its identifier.
     *
     * @param id identifier of the record to resolve
     * @return the matching model, or {@code null} if not found
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public AppEnsClassification findById(Long id) {
        try {
            return appEnsClassificationJPARepository.findById(id)
                    .map(appEnsClassificationMapper::toModel)
                    .orElse(null);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure tracking contextual matching reference rows for ENS classification index ID: {}", id, e);
            throw e;
        }
    }

    /**
     * Resolves a paginated, filtered list of ENS classification records scoped to a
     * single parent security anchor.
     *
     * @param appSecurityId mandatory parent security anchor identifier scoping the result set
     * @param criteria      additional filter criteria
     * @param pageable      pagination and sorting instructions
     * @return the paginated page of matching models
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public Page<AppEnsClassification> findAll(Long appSecurityId, AppEnsClassificationCriteria criteria, Pageable pageable) {
        log.debug("Repository: Dynamic search pattern stream across ENS classification relations for appSecurityId ID: {}", appSecurityId);
        try {
            Specification<AppEnsClassificationEntity> spec = AppEnsClassificationSpecification.filterByCriteria(appSecurityId, criteria);
            Page<AppEnsClassificationEntity> entityPage = appEnsClassificationJPARepository.findAll(spec, pageable);
            return entityPage.map(appEnsClassificationMapper::toModel);
        } catch (DataAccessException e) {
            log.error("Repository error: Broken chunk streaming query handling for ENS classification allocation layouts", e);
            throw e;
        }
    }

    /**
     * Builds and persists an audit trail row snapshotting the current state of the
     * given entity for the given action. Optional foreign key fields are read
     * null-safely since most fields on this entity are meant to be filled in gradually.
     *
     * @param entity the entity whose current state is captured in the audit row
     * @param action the action that triggered the audit (e.g. "INSERT", "UPDATE", "DELETE")
     * @throws DataAccessException if the underlying persistence operation fails
     */
    private void saveAuditRecord(AppEnsClassificationEntity entity, String action) {
        try {
            AppEnsClassificationAudEntity aud = new AppEnsClassificationAudEntity();

            aud.setAppEnsClassificationId(entity.getId());
            aud.setAppSecurityId(entity.getAppSecurity().getId());
            aud.setIdentityProviderId(entity.getIdentityProvider() != null ? entity.getIdentityProvider().getId() : null);
            aud.setEnsSubjectId(entity.getEnsSubject() != null ? entity.getEnsSubject().getId() : null);
            aud.setPersonalDataProcessingId(entity.getPersonalDataProcessing() != null ? entity.getPersonalDataProcessing().getId() : null);
            aud.setApprovalDate(entity.getApprovalDate());
            aud.setConfidentialityId(entity.getConfidentiality() != null ? entity.getConfidentiality().getId() : null);
            aud.setIntegrityId(entity.getIntegrity() != null ? entity.getIntegrity().getId() : null);
            aud.setTraceabilityId(entity.getTraceability() != null ? entity.getTraceability().getId() : null);
            aud.setAvailabilityId(entity.getAvailability() != null ? entity.getAvailability().getId() : null);
            aud.setAuthenticityId(entity.getAuthenticity() != null ? entity.getAuthenticity().getId() : null);
            aud.setOverallGradeId(entity.getOverallGrade() != null ? entity.getOverallGrade().getId() : null);

            aud.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now());
            aud.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy() : Utils.resolveCurrentUsername());

            aud.setUpdatedAt(entity.getUpdatedAt());
            aud.setUpdatedBy(entity.getUpdatedBy());
            aud.setDeletedAt(entity.getDeletedAt());
            aud.setDeletedBy(entity.getDeletedBy());

            aud.setAudAction(action);
            aud.setAuditDate(LocalDateTime.now());
            aud.setAuditUser(Utils.resolveCurrentUsername());

            appEnsClassificationAudJPARepository.save(aud);
        } catch (DataAccessException e) {
            log.error("Repository trace error: Critical trace audit ledger commit crash mapping ENS classification event link tracer", e);
            throw e;
        }
    }
}

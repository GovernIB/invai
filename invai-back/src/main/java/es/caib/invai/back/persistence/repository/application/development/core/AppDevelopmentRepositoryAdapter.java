package es.caib.invai.back.persistence.repository.application.development.core;

import es.caib.invai.back.persistence.model.application.development.core.AppDevelopmentAudEntity;
import es.caib.invai.back.persistence.model.application.development.core.AppDevelopmentEntity;
import es.caib.invai.back.service.mapper.application.development.core.AppDevelopmentMapper;
import es.caib.invai.back.service.model.application.development.core.AppDevelopment;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Infrastructure repository Adapter implementing the outbound port boundary
 * {@link AppDevelopmentRepository}.
 *
 * @since 1.0.2
 */
@Repository
@Slf4j
public class AppDevelopmentRepositoryAdapter implements AppDevelopmentRepository {

    /** Spring Data repository handling CRUD operations for the main development entity. */
    @Autowired
    private AppDevelopmentJPARepository appDevelopmentJPARepository;

    /** Spring Data repository handling persistence of development audit trail rows. */
    @Autowired
    private AppDevelopmentAudJPARepository appDevelopmentAudJPARepository;

    /** MapStruct mapper handling transformations between entities and domain models. */
    @Autowired
    private AppDevelopmentMapper appDevelopmentMapper;

    /**
     * Persists a new development entity and records the corresponding audit trail row.
     *
     * @param appDevelopment the domain model to persist
     * @return the persisted domain model, including its generated identifier
     * @throws DataAccessException if the underlying relational write fails
     */
    @Override
    public AppDevelopment create(AppDevelopment appDevelopment) {
        log.info("Repository: Persisting new application development entity ledger row");
        try {
            AppDevelopmentEntity entity = appDevelopmentMapper.toEntity(appDevelopment);
            entity = appDevelopmentJPARepository.save(entity);

            saveAuditRecord(entity, "INSERT");
            return appDevelopmentMapper.toModel(entity);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure executing relational write across development settings", e);
            throw e;
        }
    }

    /**
     * Persists changes to an existing development entity and records the corresponding audit trail row.
     *
     * @param appDevelopment the domain model carrying the updated values
     * @param id             the identifier of the record being updated
     * @return the persisted domain model reflecting the applied changes
     * @throws DataAccessException if the underlying relational write fails
     */
    @Override
    public AppDevelopment update(AppDevelopment appDevelopment, Long id) {
        log.info("Repository: Merging operational records variables for development ID: {}", id);
        try {
            AppDevelopmentEntity entity = appDevelopmentMapper.toEntity(appDevelopment);
            entity.setId(id);
            entity = appDevelopmentJPARepository.save(entity);

            saveAuditRecord(entity, "UPDATE");
            return appDevelopmentMapper.toModel(entity);
        } catch (DataAccessException e) {
            log.error("Repository error: Corrupted state runtime merging delta mappings onto target key index: {}", id, e);
            throw e;
        }
    }

    /**
     * Persists the logical (soft) deletion of a development entity and records the corresponding
     * audit trail row.
     *
     * @param appDevelopment the domain model, with deletion tracking fields already set
     * @throws DataAccessException if the underlying relational write fails
     */
    @Override
    public void delete(AppDevelopment appDevelopment) {
        log.info("Repository: Merging deactivation tracking signatures for development identifier ID: {}", appDevelopment.getId());
        try {
            AppDevelopmentEntity entity = appDevelopmentMapper.toEntity(appDevelopment);
            entity.setId(appDevelopment.getId());
            entity = appDevelopmentJPARepository.save(entity);

            saveAuditRecord(entity, "DELETE");
        } catch (DataAccessException e) {
            log.error("Repository error: Unhandled transaction rollback execution during deactivation for ID: {}", appDevelopment.getId(), e);
            throw e;
        }
    }

    /**
     * Resolves a single development entity by its primary key.
     *
     * @param id the record identifier
     * @return the matching domain model, or {@code null} if not found
     * @throws DataAccessException if the underlying relational read fails
     */
    @Override
    public AppDevelopment findById(Long id) {
        try {
            return appDevelopmentJPARepository.findById(id)
                    .map(appDevelopmentMapper::toModel)
                    .orElse(null);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure tracking contextual matching reference rows for index ID: {}", id, e);
            throw e;
        }
    }

    /**
     * Resolves the development entity associated with a given parent application.
     *
     * @param applicationId the parent application identifier
     * @return the matching domain model, or {@code null} if not found
     * @throws DataAccessException if the underlying relational read fails
     */
    @Override
    public AppDevelopment findByApplicationId(Long applicationId) {
        log.info("Repository: Dynamic search pattern stream across application development relations for Application ID: {}", applicationId);
        try {
            return appDevelopmentJPARepository.findByApplicationId(applicationId).map(appDevelopmentMapper::toModel).orElse(null);
        } catch (DataAccessException e) {
            log.error("Repository error: Broken chunk streaming query handling for application development layouts", e);
            throw e;
        }
    }

    /**
     * Builds and persists an audit trail row capturing a snapshot of the given entity state.
     *
     * @param entity the entity state to snapshot
     * @param action the lifecycle action being recorded (INSERT, UPDATE, or DELETE)
     */
    private void saveAuditRecord(AppDevelopmentEntity entity, String action) {
        try {
            AppDevelopmentAudEntity aud = new AppDevelopmentAudEntity();

            aud.setDevelopmentId(entity.getId());
            aud.setApplicationId(entity.getApplication().getId());
            aud.setEnvironmentId(entity.getEnvironment() != null ? entity.getEnvironment().getId() : null);
            aud.setModalityId(entity.getModality() != null ? entity.getModality().getId() : null);
            aud.setCode(entity.getCode());
            aud.setStandardAdaptionId(entity.getStandardAdaption() != null ? entity.getStandardAdaption().getId() : null);
            aud.setRevisionDate(entity.getRevisionDate());
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

            appDevelopmentAudJPARepository.save(aud);
        } catch (DataAccessException e) {
            log.error("Repository trace error: Critical trace audit ledger commit crash mapping application development event tracer", e);
            throw e;
        }
    }
}

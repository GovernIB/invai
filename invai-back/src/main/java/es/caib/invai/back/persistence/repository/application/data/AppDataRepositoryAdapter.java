package es.caib.invai.back.persistence.repository.application.data;

import es.caib.invai.back.persistence.model.application.data.AppDataAudEntity;
import es.caib.invai.back.persistence.model.application.data.AppDataEntity;
import es.caib.invai.back.service.mapper.application.data.AppDataMapper;
import es.caib.invai.back.service.model.application.data.AppData;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Repository adapter implementing {@link AppDataRepository} on top of Spring Data JPA,
 * translating between domain models and JPA entities and keeping the associated audit trail up
 * to date.
 *
 * @since 1.0.5
 */
@Repository
@Slf4j
public class AppDataRepositoryAdapter implements AppDataRepository {

    /** JPA repository providing CRUD access to the root entity. */
    @Autowired
    private AppDataJPARepository appDataJPARepository;

    /** JPA repository providing persistence for the audit trail entity. */
    @Autowired
    private AppDataAudJPARepository appDataAudJPARepository;

    /** Mapper converting between domain models and JPA entities. */
    @Autowired
    private AppDataMapper appDataMapper;

    @Override
    public AppData create(AppData appData) {
        log.info("Repository: Persisting new application open data anchor");
        try {
            AppDataEntity entity = appDataMapper.toEntity(appData);
            entity = appDataJPARepository.save(entity);

            saveAuditRecord(entity, "INSERT");
            return appDataMapper.toModel(entity);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure persisting application open data anchor", e);
            throw e;
        }
    }

    @Override
    public AppData update(AppData appData, Long id) {
        log.info("Repository: Updating application open data anchor ID: {}", id);
        try {
            AppDataEntity entity = appDataMapper.toEntity(appData);
            entity.setId(id);
            entity = appDataJPARepository.save(entity);

            saveAuditRecord(entity, "UPDATE");
            return appDataMapper.toModel(entity);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure updating application open data anchor ID: {}", id, e);
            throw e;
        }
    }

    @Override
    public void delete(AppData appData) {
        log.info("Repository: Soft deleting application open data anchor ID: {}", appData.getId());
        try {
            AppDataEntity entity = appDataMapper.toEntity(appData);
            entity.setId(appData.getId());
            entity = appDataJPARepository.save(entity);

            saveAuditRecord(entity, "DELETE");
        } catch (DataAccessException e) {
            log.error("Repository error: Failure soft-deleting application open data anchor ID: {}", appData.getId(), e);
            throw e;
        }
    }

    @Override
    public AppData findById(Long id) {
        try {
            return appDataJPARepository.findById(id)
                    .map(appDataMapper::toModel)
                    .orElse(null);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure resolving application open data anchor ID: {}", id, e);
            throw e;
        }
    }

    @Override
    public AppData findByApplicationId(Long applicationId) {
        try {
            return appDataJPARepository.findByApplicationId(applicationId)
                    .map(appDataMapper::toModel)
                    .orElse(null);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure resolving application open data anchor for application ID: {}", applicationId, e);
            throw e;
        }
    }

    /**
     * Builds and persists an audit trail row snapshotting the current state of the given entity
     * for the given action.
     *
     * @param entity the entity whose current state is captured in the audit row
     * @param action the action that triggered the audit (e.g. "INSERT", "UPDATE", "DELETE")
     */
    private void saveAuditRecord(AppDataEntity entity, String action) {
        try {
            AppDataAudEntity aud = new AppDataAudEntity();

            aud.setAppDataId(entity.getId());
            aud.setApplicationId(entity.getApplication().getId());
            aud.setObservation(entity.getObservation());
            aud.setOpenDataUrl(entity.getOpenDataUrl());
            aud.setUseOpenDataUrl(entity.isUseOpenDataUrl());
            aud.setReuseUrl(entity.getReuseUrl());
            aud.setUseReuseUrl(entity.isUseReuseUrl());

            aud.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now());
            aud.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy() : Utils.resolveCurrentUsername());
            aud.setUpdatedAt(entity.getUpdatedAt());
            aud.setUpdatedBy(entity.getUpdatedBy());
            aud.setDeletedAt(entity.getDeletedAt());
            aud.setDeletedBy(entity.getDeletedBy());

            aud.setAudAction(action);
            aud.setAuditDate(LocalDateTime.now());
            aud.setAuditUser(Utils.resolveCurrentUsername());

            appDataAudJPARepository.save(aud);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure persisting audit record for application open data anchor", e);
            throw e;
        }
    }
}

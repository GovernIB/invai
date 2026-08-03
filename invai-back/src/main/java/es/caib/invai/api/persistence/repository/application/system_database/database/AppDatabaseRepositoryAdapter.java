package es.caib.invai.api.persistence.repository.application.system_database.database;

import es.caib.invai.api.persistence.model.AppDatabaseAudEntity;
import es.caib.invai.api.persistence.model.AppDatabaseEntity;
import es.caib.invai.api.service.mapper.AppDatabaseMapper;
import es.caib.invai.api.service.model.AppDatabase;
import es.caib.invai.api.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
@Slf4j
public class AppDatabaseRepositoryAdapter implements AppDatabaseRepository {

    @Autowired
    private AppDatabaseJPARepository appDatabaseJPARepository;

    @Autowired
    private AppDatabaseAudJPARepository appDatabaseAudJPARepository;

    @Autowired
    private AppDatabaseMapper appDatabaseMapper;

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

    @Override
    public boolean existsByInformationSystemDbIdAndDatabaseId(Long informationSystemDbId, Long databaseId) {
        try {
            return appDatabaseJPARepository.existsByInformationSystemDbIdAndDatabaseId(informationSystemDbId, databaseId);
        } catch (DataAccessException e) {
            log.error("Repository error: Failed mapping constraint check verifying link matching tracking state unicity definitions", e);
            throw e;
        }
    }

    @Override
    public boolean existsByUniqueCombinationExcludingId(Long informationSystemDbId, Long databaseId, Long id) {
        try {
            return appDatabaseJPARepository.existsByInformationSystemDbIdAndDatabaseIdAndIdNot(informationSystemDbId, databaseId, id);
        } catch (DataAccessException e) {
            log.error("Repository error: Multi-conditional anomaly tracking unique constraints indices parameters conflicts updates context", e);
            throw e;
        }
    }

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

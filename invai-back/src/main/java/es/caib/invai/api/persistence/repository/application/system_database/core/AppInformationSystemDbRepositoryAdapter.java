package es.caib.invai.api.persistence.repository.application.system_database.core;

import es.caib.invai.api.persistence.model.AppInformationSystemDbAudEntity;
import es.caib.invai.api.persistence.model.AppInformationSystemDbEntity;
import es.caib.invai.api.service.mapper.AppInformationSystemDbMapper;
import es.caib.invai.api.service.model.AppInformationSystemDb;
import es.caib.invai.api.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class AppInformationSystemDbRepositoryAdapter implements AppInformationSystemDbRepository {

    @Autowired
    private AppInformationSystemDbJPARepository appInformationSystemDbJPARepository;

    @Autowired
    private AppInformationSystemDbAudJPARepository appInformationSystemDbAudJPARepository;

    @Autowired
    private AppInformationSystemDbMapper appInformationSystemDbMapper;

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

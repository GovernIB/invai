package es.caib.invai.api.persistence.repository.application.system_database.system;

import es.caib.invai.api.persistence.model.AppSystemAudEntity;
import es.caib.invai.api.persistence.model.AppSystemEntity;
import es.caib.invai.api.service.mapper.AppSystemMapper;
import es.caib.invai.api.service.model.AppSystem;
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
public class AppSystemRepositoryAdapter implements AppSystemRepository {

    @Autowired
    private AppSystemJPARepository appSystemJPARepository;

    @Autowired
    private AppSystemAudJPARepository appSystemAudJPARepository;

    @Autowired
    private AppSystemMapper appSystemMapper;

    @Override
    public AppSystem create(AppSystem appSystem) {
        log.info("Repository: Persisting new application system connection entity ledger row");
        try {
            AppSystemEntity entity = appSystemMapper.toEntity(appSystem);
            entity = appSystemJPARepository.save(entity);

            saveAuditRecord(entity, "INSERT");
            return appSystemMapper.toModel(entity);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure executing relational write across application settings", e);
            throw e;
        }
    }

    @Override
    public AppSystem update(AppSystem appSystem, Long id) {
        log.info("Repository: Merging operational records variables for application-system link ID: {}", id);
        try {
            AppSystemEntity entity = appSystemMapper.toEntity(appSystem);
            entity.setId(id);
            entity = appSystemJPARepository.save(entity);

            saveAuditRecord(entity, "UPDATE");
            return appSystemMapper.toModel(entity);
        } catch (DataAccessException e) {
            log.error("Repository error: Corrupted state runtime merging delta mappings onto link target key index: {}", id, e);
            throw e;
        }
    }

    @Override
    public void delete(AppSystem appSystem) {
        log.info("Repository: Merging deactivation tracking signatures for link assignment mapping identifier ID: {}", appSystem.getId());
        try {
            AppSystemEntity entity = appSystemMapper.toEntity(appSystem);
            entity.setId(appSystem.getId());
            entity = appSystemJPARepository.save(entity);

            saveAuditRecord(entity, "DELETE");
        } catch (DataAccessException e) {
            log.error("Repository error: Unhandled transaction rollback execution during link deactivation for ID: {}", appSystem.getId(), e);
            throw e;
        }
    }

    @Override
    public AppSystem findById(Long id) {
        try {
            return appSystemJPARepository.findById(id)
                    .map(appSystemMapper::toModel)
                    .orElse(null);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure tracking contextual matching reference rows for link index ID: {}", id, e);
            throw e;
        }
    }

    @Override
    public Page<AppSystem> findAll(Long informationSystemDbId, AppSystemCriteria criteria, Pageable pageable) {
        log.info("Repository: Streaming partitioned mapping allocations frames for informationSystemDbId ID: {}", informationSystemDbId);
        try {
            Specification<AppSystemEntity> spec = AppSystemSpecification.filterByCriteria(informationSystemDbId, criteria);
            Page<AppSystemEntity> entityPage = appSystemJPARepository.findAll(spec, pageable);
            return entityPage.map(appSystemMapper::toModel);
        } catch (DataAccessException e) {
            log.error("Repository error: Broken chunk streaming query handling for links maps allocation layouts", e);
            throw e;
        }
    }

    @Override
    public boolean existsByInformationSystemDbAndSystem(Long informationSystemDbId, Long systemId) {
        try {
            return appSystemJPARepository.existsByInformationSystemDbIdAndSystemId(informationSystemDbId, systemId);
        } catch (DataAccessException e) {
            log.error("Repository error: Failed mapping constraint check verifying link matching tracking state unicity definitions", e);
            throw e;
        }
    }

    @Override
    public boolean existsByInformationSystemDbAndSystemAndIdNot(Long informationSystemDbId, Long systemId, Long id) {
        try {
            return appSystemJPARepository.existsByInformationSystemDbIdAndSystemIdAndIdNot(informationSystemDbId, systemId, id);
        } catch (DataAccessException e) {
            log.error("Repository error: Multi-conditional anomaly tracking unique constraints indices parameters conflicts updates context", e);
            throw e;
        }
    }

    private void saveAuditRecord(AppSystemEntity entity, String action) {
        try {
            AppSystemAudEntity aud = new AppSystemAudEntity();

            aud.setAppSystemId(entity.getId());
            aud.setInformationSystemDbId(entity.getInformationSystemDb().getId());
            aud.setSystemId(entity.getSystem().getId());

            aud.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now());
            aud.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy() : Utils.resolveCurrentUsername());
           
                        aud.setUpdatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt() : LocalDateTime.now());
            aud.setUpdatedBy(entity.getUpdatedBy() != null ? entity.getUpdatedBy() : Utils.resolveCurrentUsername());
            aud.setDeletedAt(entity.getDeletedAt());
            aud.setDeletedBy(entity.getDeletedBy());

            aud.setAudAction(action);
            aud.setAuditDate(LocalDateTime.now());
            aud.setAuditUser(Utils.resolveCurrentUsername());

            appSystemAudJPARepository.save(aud);
        } catch (DataAccessException e) {
            log.error("Repository trace error: Critical trace audit ledger commit crash mapping application-system event link tracer", e);
            throw e;
        }
    }
}

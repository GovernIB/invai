package es.caib.invai.api.persistence.repository.application.development.technology;

import es.caib.invai.api.persistence.model.AppTechnologyEntity;
import es.caib.invai.api.persistence.model.AppTechnologyAudEntity;
import es.caib.invai.api.service.mapper.AppTechnologyMapper;
import es.caib.invai.api.service.model.AppTechnology;
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
public class AppTechnologyRepositoryAdapter implements AppTechnologyRepository {

    @Autowired
    private AppTechnologyJPARepository appTechnologyJPARepository;

    @Autowired
    private AppTechnologyAudJPARepository appTechnologyAudJPARepository;

    @Autowired
    private AppTechnologyMapper appTechnologyMapper;

    @Override
    public AppTechnology create(AppTechnology technology) {
        log.info("Repository: Persisting new technology entity ledger row");
        try {
            AppTechnologyEntity entity = appTechnologyMapper.toEntity(technology);
            entity = appTechnologyJPARepository.save(entity);

            saveAuditRecord(entity, "INSERT");
            return appTechnologyMapper.toModel(entity);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure executing relational write across technology settings", e);
            throw e;
        }
    }

    @Override
    public AppTechnology update(AppTechnology technology, Long id) {
        log.info("Repository: Merging operational records variables for technology ID: {}", id);
        try {
            AppTechnologyEntity entity = appTechnologyMapper.toEntity(technology);
            entity.setId(id);
            entity = appTechnologyJPARepository.save(entity);

            saveAuditRecord(entity, "UPDATE");
            return appTechnologyMapper.toModel(entity);
        } catch (DataAccessException e) {
            log.error("Repository error: Corrupted state runtime merging delta mappings onto target key index: {}", id, e);
            throw e;
        }
    }

    @Override
    public void delete(AppTechnology technology) {
        log.info("Repository: Merging deactivation tracking signatures for technology identifier ID: {}", technology.getId());
        try {
            AppTechnologyEntity entity = appTechnologyMapper.toEntity(technology);
            entity.setId(technology.getId());
            entity = appTechnologyJPARepository.save(entity);

            saveAuditRecord(entity, "DELETE");
        } catch (DataAccessException e) {
            log.error("Repository error: Unhandled transaction rollback execution during deactivation for ID: {}", technology.getId(), e);
            throw e;
        }
    }

    @Override
    public AppTechnology findById(Long id) {
        try {
            return appTechnologyJPARepository.findById(id)
                    .map(appTechnologyMapper::toModel)
                    .orElse(null);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure tracking contextual matching reference rows for index ID: {}", id, e);
            throw e;
        }
    }

    @Override
    public Page<AppTechnology> findAll(Long appDevelopmentId, AppTechnologyCriteria criteria, Pageable pageable) {
        log.info("Repository: Dynamic search pattern stream across technology relations for Development ID: {}", appDevelopmentId);
        try {
            Specification<AppTechnologyEntity> spec = AppTechnologySpecification.filterByCriteria(appDevelopmentId, criteria);
            Page<AppTechnologyEntity> entityPage = appTechnologyJPARepository.findAll(spec, pageable);
            return entityPage.map(appTechnologyMapper::toModel);
        } catch (DataAccessException e) {
            log.error("Repository error: Broken chunk streaming query handling for technology layouts", e);
            throw e;
        }
    }

    @Override
    public boolean existsByLayerId(Long layerId) {
        return appTechnologyJPARepository.existsByLayerId(layerId);
    }

    @Override
    public boolean existsByTechnologyId(Long technologyId) {
        return appTechnologyJPARepository.existsByTechnologyId(technologyId);
    }

    private void saveAuditRecord(AppTechnologyEntity entity, String action) {
        try {
            AppTechnologyAudEntity aud = new AppTechnologyAudEntity();

            aud.setTechnologyId(entity.getId());
            aud.setAppDevelopmentId(entity.getAppDevelopment().getId());
            aud.setLayerId(entity.getLayer() != null ? entity.getLayer().getId() : null);
            aud.setTechnologyCatalogId(entity.getTechnology() != null ? entity.getTechnology().getId() : null);
            aud.setVersion(entity.getVersion());
            aud.setArchitecture(entity.getArchitecture());

            aud.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now());
            aud.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy() : Utils.resolveCurrentUsername());
           
                        aud.setUpdatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt() : LocalDateTime.now());
            aud.setUpdatedBy(entity.getUpdatedBy() != null ? entity.getUpdatedBy() : Utils.resolveCurrentUsername());
            aud.setDeletedAt(entity.getDeletedAt());
            aud.setDeletedBy(entity.getDeletedBy());

            aud.setAudAction(action);
            aud.setAuditDate(LocalDateTime.now());
            aud.setAuditUser(Utils.resolveCurrentUsername());

            appTechnologyAudJPARepository.save(aud);
        } catch (DataAccessException e) {
            log.error("Repository trace error: Critical trace audit ledger commit crash mapping technology event tracer", e);
            throw e;
        }
    }
}

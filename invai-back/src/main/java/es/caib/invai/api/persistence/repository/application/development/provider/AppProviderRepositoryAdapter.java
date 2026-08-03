package es.caib.invai.api.persistence.repository.application.development.provider;

import es.caib.invai.api.persistence.model.AppProviderAudEntity;
import es.caib.invai.api.persistence.model.AppProviderEntity;
import es.caib.invai.api.service.mapper.AppProviderMapper;
import es.caib.invai.api.service.model.AppProvider;
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
public class AppProviderRepositoryAdapter implements AppProviderRepository {

    @Autowired
    private AppProviderJPARepository appProviderJPARepository;

    @Autowired
    private AppProviderAudJPARepository appProviderAudJPARepository;

    @Autowired
    private AppProviderMapper appProviderMapper;

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

    @Override
    public Page<AppProvider> findAll(Long appDevelopmentId, AppProviderCriteria criteria, Pageable pageable) {
        log.info("Repository: Dynamic search pattern stream across provider relations for Development ID: {}", appDevelopmentId);
        try {
            Specification<AppProviderEntity> spec = AppProviderSpecification.filterByCriteria(appDevelopmentId, criteria);
            Page<AppProviderEntity> entityPage = appProviderJPARepository.findAll(spec, pageable);
            return entityPage.map(appProviderMapper::toModel);
        } catch (DataAccessException e) {
            log.error("Repository error: Broken chunk streaming query handling for provider layouts", e);
            throw e;
        }
    }

    @Override
    public boolean existsByRoleId(Long roleId) {
        return appProviderJPARepository.existsByRoleId(roleId);
    }

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
           
                        aud.setUpdatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt() : LocalDateTime.now());
            aud.setUpdatedBy(entity.getUpdatedBy() != null ? entity.getUpdatedBy() : Utils.resolveCurrentUsername());
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

package es.caib.invai.api.persistence.repository.application.development.core;

import es.caib.invai.api.persistence.model.DevelopmentAudEntity;
import es.caib.invai.api.persistence.model.DevelopmentEntity;
import es.caib.invai.api.service.mapper.DevelopmentMapper;
import es.caib.invai.api.service.model.AppDevelopment;
import es.caib.invai.api.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
@Slf4j
public class DevelopmentRepositoryAdapter implements DevelopmentRepository {

    @Autowired
    private DevelopmentJPARepository developmentJPARepository;

    @Autowired
    private DevelopmentAudJPARepository developmentAudJPARepository;

    @Autowired
    private DevelopmentMapper developmentMapper;

    @Override
    public AppDevelopment create(AppDevelopment appDevelopment) {
        log.info("Repository: Persisting new application development entity ledger row");
        try {
            DevelopmentEntity entity = developmentMapper.toEntity(appDevelopment);
            entity = developmentJPARepository.save(entity);

            saveAuditRecord(entity, "INSERT");
            return developmentMapper.toModel(entity);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure executing relational write across development settings", e);
            throw e;
        }
    }

    @Override
    public AppDevelopment update(AppDevelopment appDevelopment, Long id) {
        log.info("Repository: Merging operational records variables for development ID: {}", id);
        try {
            DevelopmentEntity entity = developmentMapper.toEntity(appDevelopment);
            entity.setId(id);
            entity = developmentJPARepository.save(entity);

            saveAuditRecord(entity, "UPDATE");
            return developmentMapper.toModel(entity);
        } catch (DataAccessException e) {
            log.error("Repository error: Corrupted state runtime merging delta mappings onto target key index: {}", id, e);
            throw e;
        }
    }

    @Override
    public void delete(AppDevelopment appDevelopment) {
        log.info("Repository: Merging deactivation tracking signatures for development identifier ID: {}", appDevelopment.getId());
        try {
            DevelopmentEntity entity = developmentMapper.toEntity(appDevelopment);
            entity.setId(appDevelopment.getId());
            entity = developmentJPARepository.save(entity);

            saveAuditRecord(entity, "DELETE");
        } catch (DataAccessException e) {
            log.error("Repository error: Unhandled transaction rollback execution during deactivation for ID: {}", appDevelopment.getId(), e);
            throw e;
        }
    }

    @Override
    public AppDevelopment findById(Long id) {
        try {
            return developmentJPARepository.findById(id)
                    .map(developmentMapper::toModel)
                    .orElse(null);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure tracking contextual matching reference rows for index ID: {}", id, e);
            throw e;
        }
    }

    @Override
    public AppDevelopment findByApplicationId(Long applicationId) {
        log.info("Repository: Dynamic search pattern stream across application development relations for Application ID: {}", applicationId);
        try {
            return developmentJPARepository.findByApplicationId(applicationId).map(developmentMapper::toModel).orElse(null);
        } catch (DataAccessException e) {
            log.error("Repository error: Broken chunk streaming query handling for application development layouts", e);
            throw e;
        }
    }

    private void saveAuditRecord(DevelopmentEntity entity, String action) {
        try {
            DevelopmentAudEntity aud = new DevelopmentAudEntity();

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

            developmentAudJPARepository.save(aud);
        } catch (DataAccessException e) {
            log.error("Repository trace error: Critical trace audit ledger commit crash mapping application development event tracer", e);
            throw e;
        }
    }
}

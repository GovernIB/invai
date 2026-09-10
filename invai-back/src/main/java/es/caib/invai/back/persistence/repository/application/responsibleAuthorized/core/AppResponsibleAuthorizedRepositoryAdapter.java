package es.caib.invai.back.persistence.repository.application.responsibleAuthorized.core;

import es.caib.invai.back.persistence.model.application.responsibleAuthorized.core.AppResponsibleAuthorizedAudEntity;
import es.caib.invai.back.persistence.model.application.responsibleAuthorized.core.AppResponsibleAuthorizedEntity;
import es.caib.invai.back.service.mapper.application.responsibleAuthorized.core.AppResponsibleAuthorizedMapper;
import es.caib.invai.back.service.model.application.responsibleAuthorized.core.AppResponsibleAuthorized;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * {@link AppResponsibleAuthorizedRepository} implementation delegating persistence to the
 * {@link AppResponsibleAuthorizedJPARepository} and writing a historical audit record via
 * {@link AppResponsibleAuthorizedAudJPARepository} on every create/update/delete mutation.
 *
 * @since 1.0.3
 */
@Repository
@Slf4j
public class AppResponsibleAuthorizedRepositoryAdapter implements AppResponsibleAuthorizedRepository {

    /** JPA repository providing CRUD access to the anchor entity. */
    @Autowired
    private AppResponsibleAuthorizedJPARepository appResponsibleAuthorizedJPARepository;

    /** JPA repository used to persist the historical audit trail for the anchor entity. */
    @Autowired
    private AppResponsibleAuthorizedAudJPARepository appResponsibleAuthorizedAudJPARepository;

    /** Mapper converting between the anchor entity and its business domain model. */
    @Autowired
    private AppResponsibleAuthorizedMapper appResponsibleAuthorizedMapper;

    /** {@inheritDoc} */
    @Override
    public AppResponsibleAuthorized create(AppResponsibleAuthorized appResponsibleAuthorized) {
        log.info("Repository: Persisting new application responsible tab anchor entity");
        AppResponsibleAuthorizedEntity entity = appResponsibleAuthorizedMapper.toEntity(appResponsibleAuthorized);
        entity = appResponsibleAuthorizedJPARepository.save(entity);
        saveAuditRecord(entity, "INSERT");
        return appResponsibleAuthorizedMapper.toModel(entity);
    }

    /** {@inheritDoc} */
    @Override
    public AppResponsibleAuthorized update(AppResponsibleAuthorized appResponsibleAuthorized, Long id) {
        log.info("Repository: Merging changes into existing application responsible tab anchor for ID: {}", id);
        AppResponsibleAuthorizedEntity entity = appResponsibleAuthorizedMapper.toEntity(appResponsibleAuthorized);
        entity.setId(id);
        entity = appResponsibleAuthorizedJPARepository.save(entity);
        saveAuditRecord(entity, "UPDATE");
        return appResponsibleAuthorizedMapper.toModel(entity);
    }

    /** {@inheritDoc} */
    @Override
    public void delete(AppResponsibleAuthorized appResponsibleAuthorized) {
        log.info("Repository: Soft deleting application responsible tab anchor with ID: {}", appResponsibleAuthorized.getId());
        AppResponsibleAuthorizedEntity entity = appResponsibleAuthorizedMapper.toEntity(appResponsibleAuthorized);
        entity.setId(appResponsibleAuthorized.getId());
        entity = appResponsibleAuthorizedJPARepository.save(entity);
        saveAuditRecord(entity, "DELETE");
    }

    /** {@inheritDoc} */
    @Override
    public AppResponsibleAuthorized findById(Long id) {
        return appResponsibleAuthorizedJPARepository.findById(id).map(appResponsibleAuthorizedMapper::toModel).orElse(null);
    }

    /** {@inheritDoc} */
    @Override
    public AppResponsibleAuthorized findByApplicationId(Long applicationId) {
        return appResponsibleAuthorizedJPARepository.findByApplicationId(applicationId).map(appResponsibleAuthorizedMapper::toModel).orElse(null);
    }

    /**
     * Builds and persists a historical audit trail row mirroring the current state of the
     * given anchor entity, tagged with the type of mutation that triggered it.
     *
     * @param entity the anchor entity state to snapshot
     * @param action the type of mutation being recorded (e.g. {@code "INSERT"}, {@code "UPDATE"}, {@code "DELETE"})
     */
    private void saveAuditRecord(AppResponsibleAuthorizedEntity entity, String action) {
        AppResponsibleAuthorizedAudEntity aud = new AppResponsibleAuthorizedAudEntity();
        aud.setAppResponsibleAuthorizedId(entity.getId());
        aud.setApplicationId(entity.getApplication().getId());
        aud.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now());
        aud.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy() : Utils.resolveCurrentUsername());
        aud.setUpdatedAt(entity.getUpdatedAt());
        aud.setUpdatedBy(entity.getUpdatedBy());
        aud.setDeletedAt(entity.getDeletedAt());
        aud.setDeletedBy(entity.getDeletedBy());
        aud.setAudAction(action);
        aud.setAuditDate(LocalDateTime.now());
        aud.setAuditUser(Utils.resolveCurrentUsername());
        appResponsibleAuthorizedAudJPARepository.save(aud);
    }
}

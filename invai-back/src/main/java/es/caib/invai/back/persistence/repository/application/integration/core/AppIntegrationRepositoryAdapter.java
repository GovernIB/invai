package es.caib.invai.back.persistence.repository.application.integration.core;

import es.caib.invai.back.persistence.model.application.integration.core.AppIntegrationAudEntity;
import es.caib.invai.back.persistence.model.application.integration.core.AppIntegrationEntity;
import es.caib.invai.back.service.mapper.application.integration.core.AppIntegrationMapper;
import es.caib.invai.back.service.model.application.integration.core.AppIntegration;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Infrastructure repository Adapter implementing the outbound port boundary {@link AppIntegrationRepository}.
 *
 * @since 1.0.5
 */
@Repository
@Slf4j
public class AppIntegrationRepositoryAdapter implements AppIntegrationRepository {

    /** Spring Data JPA repository used to access live {@link AppIntegrationEntity} records. */
    @Autowired
    private AppIntegrationJPARepository appIntegrationJPARepository;

    /** Spring Data JPA repository used to persist {@link AppIntegrationAudEntity} audit snapshots. */
    @Autowired
    private AppIntegrationAudJPARepository appIntegrationAudJPARepository;

    /** Mapper used to convert between AppIntegration domain models and JPA entities. */
    @Autowired
    private AppIntegrationMapper appIntegrationMapper;

    @Override
    public AppIntegration create(AppIntegration appIntegration) {
        log.info("Repository: Persisting new application integration anchor");
        AppIntegrationEntity entity = appIntegrationMapper.toEntity(appIntegration);
        entity = appIntegrationJPARepository.save(entity);

        saveAuditRecord(entity, "INSERT");
        return appIntegrationMapper.toModel(entity);
    }

    @Override
    public AppIntegration update(AppIntegration appIntegration, Long id) {
        log.info("Repository: Updating application integration anchor ID: {}", id);
        AppIntegrationEntity entity = appIntegrationMapper.toEntity(appIntegration);
        entity.setId(id);
        entity = appIntegrationJPARepository.save(entity);

        saveAuditRecord(entity, "UPDATE");
        return appIntegrationMapper.toModel(entity);
    }

    @Override
    public void delete(AppIntegration appIntegration) {
        log.info("Repository: Soft deleting application integration anchor ID: {}", appIntegration.getId());
        AppIntegrationEntity entity = appIntegrationMapper.toEntity(appIntegration);
        entity.setId(appIntegration.getId());
        entity = appIntegrationJPARepository.save(entity);

        saveAuditRecord(entity, "DELETE");
    }

    @Override
    public AppIntegration findById(Long id) {
        return appIntegrationJPARepository.findById(id)
                .map(appIntegrationMapper::toModel)
                .orElse(null);
    }

    @Override
    public AppIntegration findByApplicationId(Long applicationId) {
        return appIntegrationJPARepository.findByApplicationId(applicationId)
                .map(appIntegrationMapper::toModel)
                .orElse(null);
    }

    /**
     * Builds and persists an audit trail row snapshotting the current state of the given entity
     * for the given action.
     *
     * @param entity the entity whose current state is captured in the audit row
     * @param action the action that triggered the audit (e.g. "INSERT", "UPDATE", "DELETE")
     */
    private void saveAuditRecord(AppIntegrationEntity entity, String action) {
        AppIntegrationAudEntity aud = new AppIntegrationAudEntity();

        aud.setAppIntegrationId(entity.getId());
        aud.setApplicationId(entity.getApplication().getId());
        aud.setObservation(entity.getObservation());

        aud.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now());
        aud.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy() : Utils.resolveCurrentUsername());
        aud.setUpdatedAt(entity.getUpdatedAt());
        aud.setUpdatedBy(entity.getUpdatedBy());
        aud.setDeletedAt(entity.getDeletedAt());
        aud.setDeletedBy(entity.getDeletedBy());

        aud.setAudAction(action);
        aud.setAuditDate(LocalDateTime.now());
        aud.setAuditUser(Utils.resolveCurrentUsername());

        appIntegrationAudJPARepository.save(aud);
    }
}

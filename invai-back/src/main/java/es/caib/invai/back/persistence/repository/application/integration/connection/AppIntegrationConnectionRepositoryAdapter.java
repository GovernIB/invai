package es.caib.invai.back.persistence.repository.application.integration.connection;

import es.caib.invai.back.persistence.model.application.integration.connection.AppIntegrationConnectionAudEntity;
import es.caib.invai.back.persistence.model.application.integration.connection.AppIntegrationConnectionEntity;
import es.caib.invai.back.service.mapper.application.integration.connection.AppIntegrationConnectionMapper;
import es.caib.invai.back.service.model.application.integration.connection.AppIntegrationConnection;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Infrastructure repository Adapter implementing the outbound port boundary
 * {@link AppIntegrationConnectionRepository}.
 *
 * @since 1.0.5
 */
@Repository
@Slf4j
public class AppIntegrationConnectionRepositoryAdapter implements AppIntegrationConnectionRepository {

    @Autowired
    private AppIntegrationConnectionJPARepository appIntegrationConnectionJPARepository;

    @Autowired
    private AppIntegrationConnectionAudJPARepository appIntegrationConnectionAudJPARepository;

    @Autowired
    private AppIntegrationConnectionMapper appIntegrationConnectionMapper;

    @Override
    public AppIntegrationConnection create(AppIntegrationConnection appIntegrationConnection) {
        log.info("Repository: Persisting new application integration connection");
        AppIntegrationConnectionEntity entity = appIntegrationConnectionMapper.toEntity(appIntegrationConnection);
        entity = appIntegrationConnectionJPARepository.save(entity);

        saveAuditRecord(entity, "INSERT");
        return appIntegrationConnectionMapper.toModel(entity);
    }

    @Override
    public AppIntegrationConnection update(AppIntegrationConnection appIntegrationConnection, Long id) {
        log.info("Repository: Updating application integration connection ID: {}", id);
        AppIntegrationConnectionEntity entity = appIntegrationConnectionMapper.toEntity(appIntegrationConnection);
        entity.setId(id);
        entity = appIntegrationConnectionJPARepository.save(entity);

        saveAuditRecord(entity, "UPDATE");
        return appIntegrationConnectionMapper.toModel(entity);
    }

    @Override
    public void delete(AppIntegrationConnection appIntegrationConnection) {
        log.info("Repository: Soft deleting application integration connection ID: {}", appIntegrationConnection.getId());
        AppIntegrationConnectionEntity entity = appIntegrationConnectionMapper.toEntity(appIntegrationConnection);
        entity.setId(appIntegrationConnection.getId());
        entity = appIntegrationConnectionJPARepository.save(entity);

        saveAuditRecord(entity, "DELETE");
    }

    @Override
    public AppIntegrationConnection findById(Long id) {
        return appIntegrationConnectionJPARepository.findById(id)
                .map(appIntegrationConnectionMapper::toModel)
                .orElse(null);
    }

    @Override
    public Page<AppIntegrationConnection> findAll(Long appIntegrationId, AppIntegrationConnectionCriteria criteria, Pageable pageable) {
        Specification<AppIntegrationConnectionEntity> spec = AppIntegrationConnectionSpecification.filterByCriteria(appIntegrationId, criteria);
        return appIntegrationConnectionJPARepository.findAll(spec, pageable).map(appIntegrationConnectionMapper::toModel);
    }

    /**
     * Builds and persists an audit snapshot capturing the current state of the given entity.
     * Optional foreign key fields ({@code application}/{@code externalSystem}) are read
     * null-safely, since exactly one of them is ever set.
     *
     * @param entity the entity whose current state is captured in the audit row
     * @param action the type of mutation being audited (e.g. {@code INSERT}, {@code UPDATE}, {@code DELETE})
     */
    private void saveAuditRecord(AppIntegrationConnectionEntity entity, String action) {
        AppIntegrationConnectionAudEntity aud = new AppIntegrationConnectionAudEntity();

        aud.setAppIntegrationConnectionId(entity.getId());
        aud.setAppIntegrationId(entity.getAppIntegration().getId());
        aud.setApplicationId(entity.getApplication() != null ? entity.getApplication().getId() : null);
        aud.setExternalSystemId(entity.getExternalSystem() != null ? entity.getExternalSystem().getId() : null);
        aud.setTechnologyId(entity.getTechnology().getId());
        aud.setUsername(entity.getUsername());

        aud.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now());
        aud.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy() : Utils.resolveCurrentUsername());
        aud.setUpdatedAt(entity.getUpdatedAt());
        aud.setUpdatedBy(entity.getUpdatedBy());
        aud.setDeletedAt(entity.getDeletedAt());
        aud.setDeletedBy(entity.getDeletedBy());

        aud.setAudAction(action);
        aud.setAuditDate(LocalDateTime.now());
        aud.setAuditUser(Utils.resolveCurrentUsername());

        appIntegrationConnectionAudJPARepository.save(aud);
    }
}

package es.caib.invai.back.persistence.repository.application.integration.requiredRole;

import es.caib.invai.back.persistence.model.application.integration.requiredRole.AppIntegrationRequiredRoleAudEntity;
import es.caib.invai.back.persistence.model.application.integration.requiredRole.AppIntegrationRequiredRoleEntity;
import es.caib.invai.back.service.mapper.application.integration.requiredRole.AppIntegrationRequiredRoleMapper;
import es.caib.invai.back.service.model.application.integration.requiredRole.AppIntegrationRequiredRole;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Infrastructure repository Adapter implementing the outbound port boundary
 * {@link AppIntegrationRequiredRoleRepository}.
 *
 * @since 1.0.5
 */
@Repository
@Slf4j
public class AppIntegrationRequiredRoleRepositoryAdapter implements AppIntegrationRequiredRoleRepository {

    @Autowired
    private AppIntegrationRequiredRoleJPARepository appIntegrationRequiredRoleJPARepository;

    @Autowired
    private AppIntegrationRequiredRoleAudJPARepository appIntegrationRequiredRoleAudJPARepository;

    @Autowired
    private AppIntegrationRequiredRoleMapper appIntegrationRequiredRoleMapper;

    @Override
    public AppIntegrationRequiredRole create(AppIntegrationRequiredRole appIntegrationRequiredRole) {
        log.info("Repository: Persisting new application integration required role");
        AppIntegrationRequiredRoleEntity entity = appIntegrationRequiredRoleMapper.toEntity(appIntegrationRequiredRole);
        entity = appIntegrationRequiredRoleJPARepository.save(entity);

        saveAuditRecord(entity, "INSERT");
        return appIntegrationRequiredRoleMapper.toModel(entity);
    }

    @Override
    public void delete(AppIntegrationRequiredRole appIntegrationRequiredRole) {
        log.info("Repository: Soft deleting application integration required role ID: {}", appIntegrationRequiredRole.getId());
        AppIntegrationRequiredRoleEntity entity = appIntegrationRequiredRoleMapper.toEntity(appIntegrationRequiredRole);
        entity.setId(appIntegrationRequiredRole.getId());
        entity = appIntegrationRequiredRoleJPARepository.save(entity);

        saveAuditRecord(entity, "DELETE");
    }

    @Override
    public List<AppIntegrationRequiredRole> findAllActiveByAppIntegrationConnectionId(Long appIntegrationConnectionId) {
        return appIntegrationRequiredRoleJPARepository.findAllByAppIntegrationConnection_IdAndDeletedAtIsNull(appIntegrationConnectionId).stream()
                .map(appIntegrationRequiredRoleMapper::toModel)
                .toList();
    }

    /**
     * Builds and persists an audit snapshot capturing the current state of the given entity.
     *
     * @param entity the entity whose current state is captured in the audit row
     * @param action the type of mutation being audited (e.g. {@code INSERT}, {@code UPDATE}, {@code DELETE})
     */
    private void saveAuditRecord(AppIntegrationRequiredRoleEntity entity, String action) {
        AppIntegrationRequiredRoleAudEntity aud = new AppIntegrationRequiredRoleAudEntity();

        aud.setAppIntegrationRequiredRoleId(entity.getId());
        aud.setAppIntegrationConnectionId(entity.getAppIntegrationConnection().getId());
        aud.setRoleId(entity.getRoleId());

        aud.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now());
        aud.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy() : Utils.resolveCurrentUsername());
        aud.setUpdatedAt(entity.getUpdatedAt());
        aud.setUpdatedBy(entity.getUpdatedBy());
        aud.setDeletedAt(entity.getDeletedAt());
        aud.setDeletedBy(entity.getDeletedBy());

        aud.setAudAction(action);
        aud.setAuditDate(LocalDateTime.now());
        aud.setAuditUser(Utils.resolveCurrentUsername());

        appIntegrationRequiredRoleAudJPARepository.save(aud);
    }
}

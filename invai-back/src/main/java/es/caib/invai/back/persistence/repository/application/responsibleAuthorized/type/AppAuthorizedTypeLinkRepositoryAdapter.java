package es.caib.invai.back.persistence.repository.application.responsibleAuthorized.type;

import es.caib.invai.back.persistence.model.application.responsibleAuthorized.type.AppAuthorizedTypeLinkAudEntity;
import es.caib.invai.back.persistence.model.application.responsibleAuthorized.type.AppAuthorizedTypeLinkEntity;
import es.caib.invai.back.service.mapper.application.responsibleAuthorized.type.AppAuthorizedTypeLinkMapper;
import es.caib.invai.back.service.model.application.responsibleAuthorized.type.AppAuthorizedTypeLink;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * {@link AppAuthorizedTypeLinkRepository} implementation delegating persistence to the
 * {@link AppAuthorizedTypeLinkJPARepository}, keeping the associated audit trail up to date.
 *
 * @since 1.0.3
 */
@Repository
@Slf4j
public class AppAuthorizedTypeLinkRepositoryAdapter implements AppAuthorizedTypeLinkRepository {

    /** JPA repository providing CRUD access to the authorization type join entity. */
    @Autowired
    private AppAuthorizedTypeLinkJPARepository appAuthorizedTypeLinkJPARepository;

    /** JPA repository providing persistence for the audit trail entity. */
    @Autowired
    private AppAuthorizedTypeLinkAudJPARepository appAuthorizedTypeLinkAudJPARepository;

    /** Mapper converting between the join entity and its flat business domain model. */
    @Autowired
    private AppAuthorizedTypeLinkMapper appAuthorizedTypeLinkMapper;

    /** {@inheritDoc} */
    @Override
    public AppAuthorizedTypeLink create(AppAuthorizedTypeLink appAuthorizedTypeLink) {
        log.info("Repository: Persisting new application authorized type join row into database");
        AppAuthorizedTypeLinkEntity entity = appAuthorizedTypeLinkMapper.toEntity(appAuthorizedTypeLink);
        entity = appAuthorizedTypeLinkJPARepository.save(entity);

        saveAuditRecord(entity, "INSERT");
        return appAuthorizedTypeLinkMapper.toModel(entity);
    }

    /** {@inheritDoc} */
    @Override
    public void delete(AppAuthorizedTypeLink appAuthorizedTypeLink) {
        log.info("Repository: Soft deleting application authorized type join row with ID: {}", appAuthorizedTypeLink.getId());
        AppAuthorizedTypeLinkEntity entity = appAuthorizedTypeLinkMapper.toEntity(appAuthorizedTypeLink);
        entity = appAuthorizedTypeLinkJPARepository.save(entity);

        saveAuditRecord(entity, "DELETE");
    }

    /** {@inheritDoc} */
    @Override
    public List<AppAuthorizedTypeLink> findAllActiveByAppAuthorizedId(Long appAuthorizedId) {
        return appAuthorizedTypeLinkJPARepository.findAllByAppAuthorizedIdAndDeletedAtIsNull(appAuthorizedId)
                .stream()
                .map(appAuthorizedTypeLinkMapper::toModel)
                .toList();
    }

    /**
     * Builds and persists an audit trail row snapshotting the current state of the given entity.
     *
     * @param entity the entity whose current state is captured in the audit row
     * @param action the action that triggered the audit (e.g. "INSERT", "UPDATE", "DELETE")
     */
    private void saveAuditRecord(AppAuthorizedTypeLinkEntity entity, String action) {
        AppAuthorizedTypeLinkAudEntity aud = new AppAuthorizedTypeLinkAudEntity();

        aud.setAppAuthorizedTypeLinkId(entity.getId());
        aud.setAppAuthorizedId(entity.getAppAuthorized().getId());
        aud.setAuthorizationTypeId(entity.getAuthorizationType().getId());

        aud.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now());
        aud.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy() : Utils.resolveCurrentUsername());
        aud.setUpdatedAt(entity.getUpdatedAt());
        aud.setUpdatedBy(entity.getUpdatedBy());
        aud.setDeletedAt(entity.getDeletedAt());
        aud.setDeletedBy(entity.getDeletedBy());

        aud.setAudAction(action);
        aud.setAuditDate(LocalDateTime.now());
        aud.setAuditUser(Utils.resolveCurrentUsername());

        appAuthorizedTypeLinkAudJPARepository.save(aud);
    }
}

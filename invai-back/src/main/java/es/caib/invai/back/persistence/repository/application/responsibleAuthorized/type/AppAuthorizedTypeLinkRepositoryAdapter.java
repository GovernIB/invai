package es.caib.invai.back.persistence.repository.application.responsibleAuthorized.type;

import es.caib.invai.back.persistence.model.application.responsibleAuthorized.type.AppAuthorizedTypeLinkEntity;
import es.caib.invai.back.service.mapper.application.responsibleAuthorized.type.AppAuthorizedTypeLinkMapper;
import es.caib.invai.back.service.model.application.responsibleAuthorized.type.AppAuthorizedTypeLink;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * {@link AppAuthorizedTypeLinkRepository} implementation delegating persistence to the
 * {@link AppAuthorizedTypeLinkJPARepository}. Deliberately lightweight: no audit trail is written,
 * and detaching a type is a plain hard delete rather than a soft-delete.
 *
 * @since 1.0.3
 */
@Repository
@Slf4j
public class AppAuthorizedTypeLinkRepositoryAdapter implements AppAuthorizedTypeLinkRepository {

    /** JPA repository providing CRUD access to the authorization type join entity. */
    @Autowired
    private AppAuthorizedTypeLinkJPARepository appAuthorizedTypeLinkJPARepository;

    /** Mapper converting between the join entity and its flat business domain model. */
    @Autowired
    private AppAuthorizedTypeLinkMapper appAuthorizedTypeLinkMapper;

    /** {@inheritDoc} */
    @Override
    public AppAuthorizedTypeLink create(AppAuthorizedTypeLink appAuthorizedTypeLink) {
        log.info("Repository: Persisting new application authorized type join row into database");
        AppAuthorizedTypeLinkEntity entity = appAuthorizedTypeLinkMapper.toEntity(appAuthorizedTypeLink);
        entity = appAuthorizedTypeLinkJPARepository.save(entity);
        return appAuthorizedTypeLinkMapper.toModel(entity);
    }

    /** {@inheritDoc} */
    @Override
    public void delete(AppAuthorizedTypeLink appAuthorizedTypeLink) {
        log.info("Repository: Deleting application authorized type join row with ID: {}", appAuthorizedTypeLink.getId());
        appAuthorizedTypeLinkJPARepository.deleteById(appAuthorizedTypeLink.getId());
    }

    /** {@inheritDoc} */
    @Override
    public List<AppAuthorizedTypeLink> findAllByAppAuthorizedId(Long appAuthorizedId) {
        return appAuthorizedTypeLinkJPARepository.findAllByAppAuthorizedId(appAuthorizedId)
                .stream()
                .map(appAuthorizedTypeLinkMapper::toModel)
                .toList();
    }
}

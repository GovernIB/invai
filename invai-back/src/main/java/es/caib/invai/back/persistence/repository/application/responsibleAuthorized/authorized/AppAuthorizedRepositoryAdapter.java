package es.caib.invai.back.persistence.repository.application.responsibleAuthorized.authorized;

import es.caib.invai.back.persistence.model.application.responsibleAuthorized.authorized.AppAuthorizedAudEntity;
import es.caib.invai.back.persistence.model.application.responsibleAuthorized.authorized.AppAuthorizedEntity;
import es.caib.invai.back.service.mapper.application.responsibleAuthorized.authorized.AppAuthorizedMapper;
import es.caib.invai.back.service.model.application.responsibleAuthorized.authorized.AppAuthorized;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * {@link AppAuthorizedRepository} implementation delegating persistence to the
 * {@link AppAuthorizedJPARepository} and writing a historical audit record via
 * {@link AppAuthorizedAudJPARepository} on every create/update/delete mutation.
 *
 * @since 1.0.3
 */
@Repository
@Slf4j
public class AppAuthorizedRepositoryAdapter implements AppAuthorizedRepository {

    /** JPA repository providing CRUD and specification-based query access to the authorized entity. */
    @Autowired
    private AppAuthorizedJPARepository appAuthorizedJPARepository;

    /** JPA repository used to persist the historical audit trail for the authorized entity. */
    @Autowired
    private AppAuthorizedAudJPARepository appAuthorizedAudJPARepository;

    /** Mapper converting between the authorized entity and its business domain model. */
    @Autowired
    private AppAuthorizedMapper appAuthorizedMapper;

    /** {@inheritDoc} */
    @Override
    public AppAuthorized create(AppAuthorized appAuthorized) {
        log.info("Repository: Persisting new application authorized entity into database");
        AppAuthorizedEntity entity = appAuthorizedMapper.toEntity(appAuthorized);
        entity = appAuthorizedJPARepository.save(entity);

        saveAuditRecord(entity, "INSERT");
        return appAuthorizedMapper.toModel(entity);
    }

    /** {@inheritDoc} */
    @Override
    public AppAuthorized update(AppAuthorized appAuthorized, Long id) {
        log.info("Repository: Merging changes into existing application authorized record for ID: {}", id);
        AppAuthorizedEntity entity = appAuthorizedMapper.toEntity(appAuthorized);
        entity.setId(id);
        entity = appAuthorizedJPARepository.save(entity);

        saveAuditRecord(entity, "UPDATE");
        return appAuthorizedMapper.toModel(entity);
    }

    /** {@inheritDoc} */
    @Override
    public void delete(AppAuthorized appAuthorized) {
        log.info("Repository: Soft deleting application authorized entity with ID: {}", appAuthorized.getId());
        AppAuthorizedEntity entity = appAuthorizedMapper.toEntity(appAuthorized);
        entity.setId(appAuthorized.getId());
        entity = appAuthorizedJPARepository.save(entity);

        saveAuditRecord(entity, "DELETE");
    }

    /** {@inheritDoc} */
    @Override
    public AppAuthorized findById(Long id) {
        return appAuthorizedJPARepository.findById(id)
                .map(appAuthorizedMapper::toModel)
                .orElse(null);
    }

    /** {@inheritDoc} */
    @Override
    public Page<AppAuthorized> findAll(Long appResponsibleAuthorizedId, AppAuthorizedCriteria criteria, Pageable pageable) {
        log.debug("Repository: Dynamic search pattern stream across authorized relations for AppResponsibleAuthorized ID: {}", appResponsibleAuthorizedId);
        Specification<AppAuthorizedEntity> spec = AppAuthorizedSpecification.filterByCriteria(appResponsibleAuthorizedId, criteria);
        Page<AppAuthorizedEntity> entityPage = appAuthorizedJPARepository.findAll(spec, pageable);
        return entityPage.map(appAuthorizedMapper::toModel);
    }

    /** {@inheritDoc} */
    @Override
    public boolean existsByAppResponsibleAuthorizedAndPerson(Long appResponsibleAuthorizedId, Long personId) {
        return appAuthorizedJPARepository.existsByAppResponsibleAuthorizedIdAndPersonIdAndDeletedAtIsNull(appResponsibleAuthorizedId, personId);
    }

    /** {@inheritDoc} */
    @Override
    public boolean existsByAppResponsibleAuthorizedAndPersonAndIdNot(Long appResponsibleAuthorizedId, Long personId, Long id) {
        return appAuthorizedJPARepository.existsByAppResponsibleAuthorizedIdAndPersonIdAndIdNotAndDeletedAtIsNull(appResponsibleAuthorizedId, personId, id);
    }

    /** {@inheritDoc} */
    @Override
    public List<AppAuthorized> findAllActiveByPersonId(Long personId) {
        return appAuthorizedJPARepository.findAllByPersonIdAndDeletedAtIsNull(personId).stream()
                .map(appAuthorizedMapper::toModel)
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    public AppAuthorized findActiveByAppResponsibleAuthorizedAndPerson(Long appResponsibleAuthorizedId, Long personId) {
        return appAuthorizedJPARepository.findByAppResponsibleAuthorizedIdAndPersonIdAndDeletedAtIsNull(appResponsibleAuthorizedId, personId)
                .map(appAuthorizedMapper::toModel)
                .orElse(null);
    }

    /** {@inheritDoc} */
    @Override
    public List<AppAuthorized> findAllActiveByAppResponsibleAuthorized(Long appResponsibleAuthorizedId) {
        return appAuthorizedJPARepository.findAllByAppResponsibleAuthorizedIdAndDeletedAtIsNull(appResponsibleAuthorizedId).stream()
                .map(appAuthorizedMapper::toModel)
                .toList();
    }

    /**
     * Builds and persists a historical audit trail row mirroring the current state of the
     * given authorized entity, tagged with the type of mutation that triggered it.
     *
     * @param entity the authorized entity state to snapshot
     * @param action the type of mutation being recorded (e.g. {@code "INSERT"}, {@code "UPDATE"}, {@code "DELETE"})
     */
    private void saveAuditRecord(AppAuthorizedEntity entity, String action) {
        AppAuthorizedAudEntity aud = new AppAuthorizedAudEntity();

        aud.setAppAuthorizedId(entity.getId());
        aud.setAppResponsibleAuthorizedId(entity.getAppResponsibleAuthorized().getId());
        aud.setPersonId(entity.getPerson().getId());
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

        appAuthorizedAudJPARepository.save(aud);
    }
}

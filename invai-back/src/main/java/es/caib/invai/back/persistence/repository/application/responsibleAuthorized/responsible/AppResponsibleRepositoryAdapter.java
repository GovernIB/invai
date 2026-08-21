package es.caib.invai.back.persistence.repository.application.responsibleAuthorized.responsible;

import es.caib.invai.back.persistence.model.application.responsibleAuthorized.responsible.AppResponsibleAudEntity;
import es.caib.invai.back.persistence.model.application.responsibleAuthorized.responsible.AppResponsibleEntity;
import es.caib.invai.back.service.mapper.application.responsibleAuthorized.responsible.AppResponsibleMapper;
import es.caib.invai.back.service.model.application.responsibleAuthorized.responsible.AppResponsible;
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
 * Infrastructure outbound adapter implementing {@link AppResponsibleRepository}, translating
 * between {@link AppResponsible} domain models and {@link AppResponsibleEntity} JPA entities,
 * and writing a historical {@link AppResponsibleAudEntity} row on every create/update/delete.
 *
 * @since 1.0.3
 */
@Repository
@Slf4j
public class AppResponsibleRepositoryAdapter implements AppResponsibleRepository {

    /** JPA repository providing CRUD and specification-based query access to the responsible entity. */
    @Autowired
    private AppResponsibleJPARepository appResponsibleJPARepository;
    /** JPA repository used to persist the historical audit trail for the responsible entity. */
    @Autowired
    private AppResponsibleAudJPARepository appResponsibleAudJPARepository;
    /** Mapper converting between the responsible entity and its business domain model. */
    @Autowired
    private AppResponsibleMapper appResponsibleMapper;

    @Override
    public AppResponsible create(AppResponsible appResponsible) {
        AppResponsibleEntity entity = appResponsibleMapper.toEntity(appResponsible);
        entity = appResponsibleJPARepository.save(entity);
        saveAuditRecord(entity, "INSERT");
        return appResponsibleMapper.toModel(entity);
    }

    @Override
    public AppResponsible update(AppResponsible appResponsible, Long id) {
        AppResponsibleEntity entity = appResponsibleMapper.toEntity(appResponsible);
        entity.setId(id);
        entity = appResponsibleJPARepository.save(entity);
        saveAuditRecord(entity, "UPDATE");
        return appResponsibleMapper.toModel(entity);
    }

    @Override
    public void delete(AppResponsible appResponsible) {
        AppResponsibleEntity entity = appResponsibleMapper.toEntity(appResponsible);
        entity.setId(appResponsible.getId());
        entity = appResponsibleJPARepository.save(entity);
        saveAuditRecord(entity, "DELETE");
    }

    @Override
    public AppResponsible findById(Long id) {
        return appResponsibleJPARepository.findById(id).map(appResponsibleMapper::toModel).orElse(null);
    }

    @Override
    public Page<AppResponsible> findAll(Long appResponsibleAuthorizedId, AppResponsibleCriteria criteria, Pageable pageable) {
        Specification<AppResponsibleEntity> spec = AppResponsibleSpecification.filterByCriteria(appResponsibleAuthorizedId, criteria);
        Page<AppResponsibleEntity> entityPage = appResponsibleJPARepository.findAll(spec, pageable);
        return entityPage.map(appResponsibleMapper::toModel);
    }

    @Override
    public boolean existsByAppResponsibleAuthorizedAndResponsibleTypeAndIdNot(Long appResponsibleAuthorizedId, Long responsibleTypeId, Long id) {
        return appResponsibleJPARepository.existsByAppResponsibleAuthorizedIdAndResponsibleTypeIdAndDeletedAtIsNullAndIdNot(appResponsibleAuthorizedId, responsibleTypeId, id);
    }

    @Override
    public AppResponsible findActiveByAppResponsibleAuthorizedAndResponsibleType(Long appResponsibleAuthorizedId, Long responsibleTypeId) {
        return appResponsibleJPARepository.findByAppResponsibleAuthorizedIdAndResponsibleTypeIdAndDeletedAtIsNull(appResponsibleAuthorizedId, responsibleTypeId)
                .map(appResponsibleMapper::toModel)
                .orElse(null);
    }

    @Override
    public List<AppResponsible> findAllActiveByPersonId(Long personId) {
        return appResponsibleJPARepository.findAllByPersonIdAndDeletedAtIsNull(personId).stream()
                .map(appResponsibleMapper::toModel)
                .toList();
    }

    /**
     * Builds and persists a historical audit trail row mirroring the current state of the
     * given responsible entity, tagged with the type of mutation that triggered it.
     *
     * @param entity the responsible entity state to snapshot
     * @param action the type of mutation being recorded (e.g. {@code "INSERT"}, {@code "UPDATE"}, {@code "DELETE"})
     */
    private void saveAuditRecord(AppResponsibleEntity entity, String action) {
        AppResponsibleAudEntity aud = new AppResponsibleAudEntity();
        aud.setAppResponsibleId(entity.getId());
        aud.setAppResponsibleAuthorizedId(entity.getAppResponsibleAuthorized().getId());
        aud.setPersonId(entity.getPerson().getId());
        aud.setResponsibleTypeId(entity.getResponsibleType().getId());
        aud.setJobTitle(entity.getJobTitle());
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
        appResponsibleAudJPARepository.save(aud);
    }
}

package es.caib.invai.back.persistence.repository.maintenance.integration.externalSystem;

import es.caib.invai.back.persistence.model.maintenance.integration.externalSystem.ExternalSystemAudEntity;
import es.caib.invai.back.persistence.model.maintenance.integration.externalSystem.ExternalSystemEntity;
import es.caib.invai.back.service.mapper.maintenance.integration.externalSystem.ExternalSystemMapper;
import es.caib.invai.back.service.model.maintenance.integration.externalSystem.ExternalSystem;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Infrastructure repository Adapter implementing the outbound port boundary {@link ExternalSystemRepository}.
 *
 * @since 1.0.5
 */
@Repository
@Slf4j
public class ExternalSystemRepositoryAdapter implements ExternalSystemRepository {

    /** Spring Data JPA repository used to access live {@link ExternalSystemEntity} records. */
    @Autowired
    private ExternalSystemJPARepository externalSystemJPARepository;

    /** Spring Data JPA repository used to persist {@link ExternalSystemAudEntity} audit snapshots. */
    @Autowired
    private ExternalSystemAudJPARepository externalSystemAudJPARepository;

    /** Mapper used to convert between External System domain models and JPA entities. */
    @Autowired
    private ExternalSystemMapper externalSystemMapper;

    @Override
    public ExternalSystem findById(Long id) {
        return externalSystemJPARepository.findById(id)
                .map(externalSystemMapper::toModel)
                .orElse(null);
    }

    @Override
    public Page<ExternalSystem> findAll(ExternalSystemCriteria filter, Pageable pageable) {
        Specification<ExternalSystemEntity> spec = ExternalSystemSpecification.filterByCriteria(filter);
        return externalSystemJPARepository.findAll(spec, pageable).map(externalSystemMapper::toModel);
    }

    @Override
    public boolean existsByNameAndDeletedAtIsNull(String name) {
        return externalSystemJPARepository.existsByNameAndDeletedAtIsNull(name);
    }

    @Override
    public boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id) {
        return externalSystemJPARepository.existsByNameAndIdNotAndDeletedAtIsNull(name, id);
    }

    @Override
    public ExternalSystem create(ExternalSystem externalSystem) {
        log.info("Repository: Persisting new external system entity into database");
        ExternalSystemEntity entity = externalSystemMapper.toEntity(externalSystem);
        entity = externalSystemJPARepository.save(entity);

        saveAuditRecord(entity, "INSERT");

        return externalSystemMapper.toModel(entity);
    }

    @Override
    public ExternalSystem update(ExternalSystem externalSystem, Long id) {
        log.info("Repository: Merging changes into existing external system record for ID: {}", id);
        ExternalSystemEntity entity = externalSystemMapper.toEntity(externalSystem);
        entity.setId(id);
        entity = externalSystemJPARepository.save(entity);

        saveAuditRecord(entity, "UPDATE");

        return externalSystemMapper.toModel(entity);
    }

    @Override
    public void delete(ExternalSystem externalSystem) {
        log.info("Repository: Soft deleting external system entity with ID: {}", externalSystem.getId());
        ExternalSystemEntity entity = externalSystemMapper.toEntity(externalSystem);
        entity.setId(externalSystem.getId());
        entity = externalSystemJPARepository.save(entity);

        saveAuditRecord(entity, "DELETE");
    }

    /**
     * Builds and persists an audit snapshot capturing the current state of the given entity.
     *
     * @param entity the external system entity state to snapshot
     * @param action the type of mutation being audited (e.g. {@code INSERT}, {@code UPDATE}, {@code DELETE})
     */
    private void saveAuditRecord(ExternalSystemEntity entity, String action) {
        ExternalSystemAudEntity aud = new ExternalSystemAudEntity();

        aud.setExternalSystemId(entity.getId());
        aud.setName(entity.getName());
        aud.setCompanyId(entity.getCompany().getId());

        aud.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now());
        aud.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy() : Utils.resolveCurrentUsername());
        aud.setUpdatedAt(entity.getUpdatedAt());
        aud.setUpdatedBy(entity.getUpdatedBy());
        aud.setDeletedAt(entity.getDeletedAt());
        aud.setDeletedBy(entity.getDeletedBy());

        aud.setAudAction(action);
        aud.setAuditDate(LocalDateTime.now());
        aud.setAuditUser(Utils.resolveCurrentUsername());

        externalSystemAudJPARepository.save(aud);
    }
}

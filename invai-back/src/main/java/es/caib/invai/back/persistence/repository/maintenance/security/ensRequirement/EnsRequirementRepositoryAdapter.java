package es.caib.invai.back.persistence.repository.maintenance.security.ensRequirement;

import es.caib.invai.back.service.model.maintenance.security.ensRequirement.EnsRequirement;
import es.caib.invai.back.persistence.model.maintenance.security.ensRequirement.EnsRequirementEntity;
import es.caib.invai.back.persistence.model.maintenance.security.ensRequirement.EnsRequirementAudEntity;
import es.caib.invai.back.service.mapper.maintenance.security.ensRequirement.EnsRequirementMapper;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Infrastructure repository Adapter implementing the outbound port boundary {@link EnsRequirementRepository}.
 *
 * @since 1.0.4
 */
@Repository
@Slf4j
public class EnsRequirementRepositoryAdapter implements EnsRequirementRepository {

    /** JPA repository used for CRUD operations on live ENS requirement entry entities. */
    @Autowired
    private EnsRequirementJPARepository ensRequirementJPARepository;

    /** JPA repository used to persist audit snapshots of ENS requirement entry changes. */
    @Autowired
    private EnsRequirementAudJPARepository ensRequirementAudJPARepository;

    /** Mapper used to convert between EnsRequirement domain models and entities. */
    @Autowired
    private EnsRequirementMapper ensRequirementMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public EnsRequirement findById(Long id) {
        return ensRequirementJPARepository.findById(id)
                .map(ensRequirementMapper::toModel)
                .orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<EnsRequirement> findAll(EnsRequirementCriteria filter, Pageable pageable) {
        Specification<EnsRequirementEntity> spec = EnsRequirementSpecification.filterByCriteria(filter);
        return ensRequirementJPARepository.findAll(spec, pageable).map(ensRequirementMapper::toModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsByNameAndDeletedAtIsNull(String name) {
        return ensRequirementJPARepository.existsByNameAndDeletedAtIsNull(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id) {
        return ensRequirementJPARepository.existsByNameAndIdNotAndDeletedAtIsNull(name, id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EnsRequirement create(EnsRequirement ensRequirement) {
        log.info("Repository: Persisting new ENS requirement entry entity into database");
        EnsRequirementEntity entity = ensRequirementMapper.toEntity(ensRequirement);
        entity = ensRequirementJPARepository.save(entity);

        saveAuditRecord(entity, "INSERT");

        return ensRequirementMapper.toModel(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EnsRequirement update(EnsRequirement ensRequirement, Long id) {
        log.info("Repository: Merging changes into existing ENS requirement entry record for ID: {}", id);
        EnsRequirementEntity entity = ensRequirementMapper.toEntity(ensRequirement);
        entity.setId(id);
        entity = ensRequirementJPARepository.save(entity);

        saveAuditRecord(entity, "UPDATE");

        return ensRequirementMapper.toModel(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(EnsRequirement ensRequirement) {
        log.info("Repository: Soft deleting ENS requirement entry entity with ID: {}", ensRequirement.getId());
        EnsRequirementEntity entity = ensRequirementMapper.toEntity(ensRequirement);
        entity.setId(ensRequirement.getId());
        entity = ensRequirementJPARepository.save(entity);

        saveAuditRecord(entity, "DELETE");
    }

    /**
     * Builds and persists an audit snapshot capturing the current state of the given entity
     * along with the operation that triggered it.
     *
     * @param entity the ENS requirement entry entity whose state is being audited
     * @param action the type of operation performed (e.g. {@code INSERT}, {@code UPDATE}, {@code DELETE})
     */
    private void saveAuditRecord(EnsRequirementEntity entity, String action) {
        EnsRequirementAudEntity aud = new EnsRequirementAudEntity();

        aud.setEnsRequirementId(entity.getId());
        aud.setName(entity.getName());
        aud.setNameEs(entity.getNameEs());

        aud.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now());
        aud.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy() : Utils.resolveCurrentUsername());
        aud.setUpdatedAt(entity.getUpdatedAt());
        aud.setUpdatedBy(entity.getUpdatedBy());
        aud.setDeletedAt(entity.getDeletedAt());
        aud.setDeletedBy(entity.getDeletedBy());

        aud.setAudAction(action);
        aud.setAuditDate(LocalDateTime.now());
        aud.setAuditUser(Utils.resolveCurrentUsername());

        ensRequirementAudJPARepository.save(aud);
    }
}

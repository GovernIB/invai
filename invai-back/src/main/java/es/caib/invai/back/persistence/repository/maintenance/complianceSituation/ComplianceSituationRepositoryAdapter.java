package es.caib.invai.back.persistence.repository.maintenance.complianceSituation;

import es.caib.invai.back.service.model.maintenance.complianceSituation.ComplianceSituation;
import es.caib.invai.back.persistence.model.maintenance.complianceSituation.ComplianceSituationEntity;
import es.caib.invai.back.persistence.model.maintenance.complianceSituation.ComplianceSituationAudEntity;
import es.caib.invai.back.service.mapper.maintenance.complianceSituation.ComplianceSituationMapper;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Infrastructure repository Adapter implementing the outbound port boundary {@link ComplianceSituationRepository}.
 *
 * @since 1.0.4
 */
@Repository
@Slf4j
public class ComplianceSituationRepositoryAdapter implements ComplianceSituationRepository {

    /** JPA repository used for CRUD operations on live compliance situation entry entities. */
    @Autowired
    private ComplianceSituationJPARepository complianceSituationJPARepository;

    /** JPA repository used to persist audit snapshots of compliance situation entry changes. */
    @Autowired
    private ComplianceSituationAudJPARepository complianceSituationAudJPARepository;

    /** Mapper used to convert between ComplianceSituation domain models and entities. */
    @Autowired
    private ComplianceSituationMapper complianceSituationMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public ComplianceSituation findById(Long id) {
        return complianceSituationJPARepository.findById(id)
                .map(complianceSituationMapper::toModel)
                .orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<ComplianceSituation> findAll(ComplianceSituationCriteria filter, Pageable pageable) {
        Specification<ComplianceSituationEntity> spec = ComplianceSituationSpecification.filterByCriteria(filter);
        return complianceSituationJPARepository.findAll(spec, pageable).map(complianceSituationMapper::toModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsByNameAndDeletedAtIsNull(String name) {
        return complianceSituationJPARepository.existsByNameAndDeletedAtIsNull(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id) {
        return complianceSituationJPARepository.existsByNameAndIdNotAndDeletedAtIsNull(name, id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ComplianceSituation create(ComplianceSituation complianceSituation) {
        log.info("Repository: Persisting new compliance situation entry entity into database");
        ComplianceSituationEntity entity = complianceSituationMapper.toEntity(complianceSituation);
        entity = complianceSituationJPARepository.save(entity);

        saveAuditRecord(entity, "INSERT");

        return complianceSituationMapper.toModel(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ComplianceSituation update(ComplianceSituation complianceSituation, Long id) {
        log.info("Repository: Merging changes into existing compliance situation entry record for ID: {}", id);
        ComplianceSituationEntity entity = complianceSituationMapper.toEntity(complianceSituation);
        entity.setId(id);
        entity = complianceSituationJPARepository.save(entity);

        saveAuditRecord(entity, "UPDATE");

        return complianceSituationMapper.toModel(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(ComplianceSituation complianceSituation) {
        log.info("Repository: Soft deleting compliance situation entry entity with ID: {}", complianceSituation.getId());
        ComplianceSituationEntity entity = complianceSituationMapper.toEntity(complianceSituation);
        entity.setId(complianceSituation.getId());
        entity = complianceSituationJPARepository.save(entity);

        saveAuditRecord(entity, "DELETE");
    }

    /**
     * Builds and persists an audit snapshot capturing the current state of the given entity
     * along with the operation that triggered it.
     *
     * @param entity the compliance situation entry entity whose state is being audited
     * @param action the type of operation performed (e.g. {@code INSERT}, {@code UPDATE}, {@code DELETE})
     */
    private void saveAuditRecord(ComplianceSituationEntity entity, String action) {
        ComplianceSituationAudEntity aud = new ComplianceSituationAudEntity();

        aud.setComplianceSituationId(entity.getId());
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

        complianceSituationAudJPARepository.save(aud);
    }
}

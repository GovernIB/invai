package es.caib.invai.api.persistence.repository.technology;

import es.caib.invai.api.service.model.Technology;
import es.caib.invai.api.persistence.model.TechnologyEntity;
import es.caib.invai.api.persistence.model.TechnologyAudEntity;
import es.caib.invai.api.service.mapper.TechnologyMapper;
import es.caib.invai.api.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Infrastructure repository Adapter implementing the outbound port boundary {@link TechnologyRepository}.
 *
 * @since 1.0.2
 */
@Repository
@Slf4j
public class TechnologyRepositoryAdapter implements TechnologyRepository {

    @Autowired
    private TechnologyJPARepository technologyJPARepository;

    @Autowired
    private TechnologyAudJPARepository technologyAudJPARepository;

    @Autowired
    private TechnologyMapper technologyMapper;

    @Override
    public Technology findById(Long id) {
        return technologyJPARepository.findById(id)
                .map(technologyMapper::toModel)
                .orElse(null);
    }

    @Override
    public Page<Technology> findAll(TechnologyCriteria filter, Pageable pageable) {
        Specification<TechnologyEntity> spec = TechnologySpecification.filterByCriteria(filter);
        return technologyJPARepository.findAll(spec, pageable).map(technologyMapper::toModel);
    }

    @Override
    public boolean existsByNameAndDeletedAtIsNull(String name) {
        return technologyJPARepository.existsByNameAndDeletedAtIsNull(name);
    }

    @Override
    public boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id) {
        return technologyJPARepository.existsByNameAndIdNotAndDeletedAtIsNull(name, id);
    }

    @Override
    public boolean existsByLayerId(Long layerId) {
        return technologyJPARepository.existsByLayerId(layerId);
    }

    @Override
    public Technology create(Technology technology) {
        log.info("Repository: Persisting new technology entity into database");
        TechnologyEntity entity = technologyMapper.toEntity(technology);
        entity = technologyJPARepository.save(entity);

        saveAuditRecord(entity, "INSERT");

        return technologyMapper.toModel(entity);
    }

    @Override
    public Technology update(Technology technology, Long id) {
        log.info("Repository: Merging changes into existing technology record for ID: {}", id);
        TechnologyEntity entity = technologyMapper.toEntity(technology);
        entity.setId(id);
        entity = technologyJPARepository.save(entity);

        saveAuditRecord(entity, "UPDATE");

        return technologyMapper.toModel(entity);
    }

    @Override
    public void delete(Technology technology) {
        log.info("Repository: Soft deleting technology entity with ID: {}", technology.getId());
        TechnologyEntity entity = technologyMapper.toEntity(technology);
        entity.setId(technology.getId());
        entity = technologyJPARepository.save(entity);

        saveAuditRecord(entity, "DELETE");
    }

    private void saveAuditRecord(TechnologyEntity entity, String action) {
        TechnologyAudEntity aud = new TechnologyAudEntity();

        aud.setTechnologyId(entity.getId());
        aud.setName(entity.getName());
        aud.setLayerId(entity.getLayer() != null ? entity.getLayer().getId() : null);

        aud.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now());
        aud.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy() : Utils.resolveCurrentUsername());
        aud.setUpdatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt() : LocalDateTime.now());
        aud.setUpdatedBy(entity.getUpdatedBy() != null ? entity.getUpdatedBy() : Utils.resolveCurrentUsername());
        aud.setDeletedAt(entity.getDeletedAt());
        aud.setDeletedBy(entity.getDeletedBy());

        aud.setAudAction(action);
        aud.setAuditDate(LocalDateTime.now());
        aud.setAuditUser(Utils.resolveCurrentUsername());

        technologyAudJPARepository.save(aud);
    }
}

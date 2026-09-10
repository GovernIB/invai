package es.caib.invai.back.persistence.repository.maintenance.development.technology;

import es.caib.invai.back.service.model.maintenance.development.technology.Technology;
import es.caib.invai.back.persistence.model.maintenance.development.technology.TechnologyEntity;
import es.caib.invai.back.persistence.model.maintenance.development.technology.TechnologyAudEntity;
import es.caib.invai.back.service.mapper.maintenance.development.technology.TechnologyMapper;
import es.caib.invai.back.utils.Utils;
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

    /** Spring Data JPA repository providing CRUD access to live {@link TechnologyEntity} records. */
    @Autowired
    private TechnologyJPARepository technologyJPARepository;

    /** Spring Data JPA repository providing access to historical {@link TechnologyAudEntity} snapshots. */
    @Autowired
    private TechnologyAudJPARepository technologyAudJPARepository;

    /** Mapper responsible for converting between Technology domain models and entities. */
    @Autowired
    private TechnologyMapper technologyMapper;

    /**
     * Finds a technology by its identifier.
     *
     * @param id the technology identifier
     * @return the matching technology, or {@code null} if none is found
     */
    @Override
    public Technology findById(Long id) {
        return technologyJPARepository.findById(id)
                .map(technologyMapper::toModel)
                .orElse(null);
    }

    /**
     * Finds a paginated list of technologies matching the given filter criteria.
     *
     * @param filter   the search criteria to apply
     * @param pageable the pagination and sorting configuration
     * @return a page of matching technologies
     */
    @Override
    public Page<Technology> findAll(TechnologyCriteria filter, Pageable pageable) {
        Specification<TechnologyEntity> spec = TechnologySpecification.filterByCriteria(filter);
        return technologyJPARepository.findAll(spec, pageable).map(technologyMapper::toModel);
    }

    /**
     * Checks whether a non-deleted technology exists with the given name.
     *
     * @param name the name to check
     * @return {@code true} if a matching non-deleted technology exists
     */
    @Override
    public boolean existsByNameAndDeletedAtIsNull(String name) {
        return technologyJPARepository.existsByNameAndDeletedAtIsNull(name);
    }

    /**
     * Checks whether a non-deleted technology other than the given ID exists with the given name.
     *
     * @param name the name to check
     * @param id   the identifier to exclude from the check
     * @return {@code true} if a matching non-deleted technology exists
     */
    @Override
    public boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id) {
        return technologyJPARepository.existsByNameAndIdNotAndDeletedAtIsNull(name, id);
    }

    /**
     * Checks whether any technology is associated with the given architecture layer.
     *
     * @param layerId the layer identifier to check
     * @return {@code true} if at least one technology references the layer
     */
    @Override
    public boolean existsByLayerId(Long layerId) {
        return technologyJPARepository.existsByLayerId(layerId);
    }

    /**
     * Persists a new technology and records an INSERT audit snapshot.
     *
     * @param technology the technology to create
     * @return the created technology
     */
    @Override
    public Technology create(Technology technology) {
        log.info("Repository: Persisting new technology entity into database");
        TechnologyEntity entity = technologyMapper.toEntity(technology);
        entity = technologyJPARepository.save(entity);

        saveAuditRecord(entity, "INSERT");

        return technologyMapper.toModel(entity);
    }

    /**
     * Persists changes to an existing technology and records an UPDATE audit snapshot.
     *
     * @param technology the technology data to persist
     * @param id         the identifier of the technology to update
     * @return the updated technology
     */
    @Override
    public Technology update(Technology technology, Long id) {
        log.info("Repository: Merging changes into existing technology record for ID: {}", id);
        TechnologyEntity entity = technologyMapper.toEntity(technology);
        entity.setId(id);
        entity = technologyJPARepository.save(entity);

        saveAuditRecord(entity, "UPDATE");

        return technologyMapper.toModel(entity);
    }

    /**
     * Soft deletes a technology (which is expected to already carry its deletion metadata)
     * and records a DELETE audit snapshot.
     *
     * @param technology the technology to persist as deleted
     */
    @Override
    public void delete(Technology technology) {
        log.info("Repository: Soft deleting technology entity with ID: {}", technology.getId());
        TechnologyEntity entity = technologyMapper.toEntity(technology);
        entity.setId(technology.getId());
        entity = technologyJPARepository.save(entity);

        saveAuditRecord(entity, "DELETE");
    }

    /**
     * Builds and persists an audit snapshot capturing the current state of the given entity.
     *
     * @param entity the technology entity state to snapshot
     * @param action the action that triggered the snapshot (e.g. INSERT, UPDATE, DELETE)
     */
    private void saveAuditRecord(TechnologyEntity entity, String action) {
        TechnologyAudEntity aud = new TechnologyAudEntity();

        aud.setTechnologyId(entity.getId());
        aud.setName(entity.getName());
        aud.setLayerId(entity.getLayer() != null ? entity.getLayer().getId() : null);

        aud.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now());
        aud.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy() : Utils.resolveCurrentUsername());
        aud.setUpdatedAt(entity.getUpdatedAt());
        aud.setUpdatedBy(entity.getUpdatedBy());
        aud.setDeletedAt(entity.getDeletedAt());
        aud.setDeletedBy(entity.getDeletedBy());

        aud.setAudAction(action);
        aud.setAuditDate(LocalDateTime.now());
        aud.setAuditUser(Utils.resolveCurrentUsername());

        technologyAudJPARepository.save(aud);
    }
}

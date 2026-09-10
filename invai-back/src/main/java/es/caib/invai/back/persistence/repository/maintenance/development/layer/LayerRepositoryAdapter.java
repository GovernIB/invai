package es.caib.invai.back.persistence.repository.maintenance.development.layer;

import es.caib.invai.back.service.model.maintenance.development.layer.Layer;
import es.caib.invai.back.persistence.model.maintenance.development.layer.LayerEntity;
import es.caib.invai.back.persistence.model.maintenance.development.layer.LayerAudEntity;
import es.caib.invai.back.service.mapper.maintenance.development.layer.LayerMapper;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Infrastructure repository Adapter implementing the outbound port boundary {@link LayerRepository}.
 *
 * @since 1.0.2
 */
@Repository
@Slf4j
public class LayerRepositoryAdapter implements LayerRepository {

    /** JPA repository providing CRUD and specification-based query access to the layer entity. */
    @Autowired
    private LayerJPARepository layerJPARepository;

    /** JPA repository used to persist the historical audit trail for the layer entity. */
    @Autowired
    private LayerAudJPARepository layerAudJPARepository;

    /** Mapper converting between the layer entity and its business domain model. */
    @Autowired
    private LayerMapper layerMapper;

    @Override
    public Layer findById(Long id) {
        return layerJPARepository.findById(id)
                .map(layerMapper::toModel)
                .orElse(null);
    }

    @Override
    public Page<Layer> findAll(LayerCriteria filter, Pageable pageable) {
        Specification<LayerEntity> spec = LayerSpecification.filterByCriteria(filter);
        return layerJPARepository.findAll(spec, pageable).map(layerMapper::toModel);
    }

    @Override
    public boolean existsByNameAndDeletedAtIsNull(String name) {
        return layerJPARepository.existsByNameAndDeletedAtIsNull(name);
    }

    @Override
    public boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id) {
        return layerJPARepository.existsByNameAndIdNotAndDeletedAtIsNull(name, id);
    }

    @Override
    public Layer create(Layer layer) {
        log.info("Repository: Persisting new layer entity into database");
        LayerEntity entity = layerMapper.toEntity(layer);
        entity = layerJPARepository.save(entity);

        saveAuditRecord(entity, "INSERT");

        return layerMapper.toModel(entity);
    }

    @Override
    public Layer update(Layer layer, Long id) {
        log.info("Repository: Merging changes into existing layer record for ID: {}", id);
        LayerEntity entity = layerMapper.toEntity(layer);
        entity.setId(id);
        entity = layerJPARepository.save(entity);

        saveAuditRecord(entity, "UPDATE");

        return layerMapper.toModel(entity);
    }

    @Override
    public void delete(Layer layer) {
        log.info("Repository: Soft deleting layer entity with ID: {}", layer.getId());
        LayerEntity entity = layerMapper.toEntity(layer);
        entity.setId(layer.getId());
        entity = layerJPARepository.save(entity);

        saveAuditRecord(entity, "DELETE");
    }

    /**
     * Builds and persists a historical audit trail row mirroring the current state of the
     * given layer entity, tagged with the type of mutation that triggered it.
     *
     * @param entity the layer entity state to snapshot
     * @param action the type of mutation being recorded (e.g. {@code "INSERT"}, {@code "UPDATE"}, {@code "DELETE"})
     */
    private void saveAuditRecord(LayerEntity entity, String action) {
        LayerAudEntity aud = new LayerAudEntity();

        aud.setLayerId(entity.getId());
        aud.setName(entity.getName());

        aud.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now());
        aud.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy() : Utils.resolveCurrentUsername());
        aud.setUpdatedAt(entity.getUpdatedAt());
        aud.setUpdatedBy(entity.getUpdatedBy());
        aud.setDeletedAt(entity.getDeletedAt());
        aud.setDeletedBy(entity.getDeletedBy());

        aud.setAudAction(action);
        aud.setAuditDate(LocalDateTime.now());
        aud.setAuditUser(Utils.resolveCurrentUsername());

        layerAudJPARepository.save(aud);
    }
}

package es.caib.invai.api.persistence.repository.layer;

import es.caib.invai.api.service.model.Layer;
import es.caib.invai.api.persistence.model.LayerEntity;
import es.caib.invai.api.persistence.model.LayerAudEntity;
import es.caib.invai.api.service.mapper.LayerMapper;
import es.caib.invai.api.utils.Utils;
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

    @Autowired
    private LayerJPARepository layerJPARepository;

    @Autowired
    private LayerAudJPARepository layerAudJPARepository;

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

    private void saveAuditRecord(LayerEntity entity, String action) {
        LayerAudEntity aud = new LayerAudEntity();

        aud.setLayerId(entity.getId());
        aud.setName(entity.getName());

        aud.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now());
        aud.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy() : Utils.resolveCurrentUsername());
        aud.setUpdatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt() : LocalDateTime.now());
        aud.setUpdatedBy(entity.getUpdatedBy() != null ? entity.getUpdatedBy() : Utils.resolveCurrentUsername());
        aud.setDeletedAt(entity.getDeletedAt());
        aud.setDeletedBy(entity.getDeletedBy());

        aud.setAudAction(action);
        aud.setAuditDate(LocalDateTime.now());
        aud.setAuditUser(Utils.resolveCurrentUsername());

        layerAudJPARepository.save(aud);
    }
}

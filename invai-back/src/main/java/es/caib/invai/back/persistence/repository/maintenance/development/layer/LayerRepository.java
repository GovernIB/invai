package es.caib.invai.back.persistence.repository.maintenance.development.layer;

import es.caib.invai.back.service.model.maintenance.development.layer.Layer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Core business domain outbound Port boundary interface declaring relational persistence mechanisms
 * for the architecture Layer catalog.
 *
 * @since 1.0.2
 */
public interface LayerRepository {

    Layer findById(Long id);

    Page<Layer> findAll(LayerCriteria filter, Pageable pageable);

    Layer create(Layer layer);

    Layer update(Layer layer, Long id);

    void delete(Layer layer);

    boolean existsByNameAndDeletedAtIsNull(String name);

    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id);
}

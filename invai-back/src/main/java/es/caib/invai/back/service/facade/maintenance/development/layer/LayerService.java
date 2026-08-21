package es.caib.invai.back.service.facade.maintenance.development.layer;

import es.caib.invai.back.interna.maintenance.development.layer.DTO.LayerInputDTO;
import es.caib.invai.back.interna.maintenance.development.layer.DTO.LayerOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.development.layer.LayerCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting architecture Layers.
 *
 * @since 1.0.2
 */
public interface LayerService {

    LayerOutputDTO getById(Long id);

    Page<LayerOutputDTO> getAll(LayerCriteria filter, Pageable pageable);

    LayerOutputDTO create(LayerInputDTO inputDTO);

    LayerOutputDTO update(Long id, LayerInputDTO inputDTO);

    void delete(Long id);

    LayerOutputDTO reactivate(Long id);
}

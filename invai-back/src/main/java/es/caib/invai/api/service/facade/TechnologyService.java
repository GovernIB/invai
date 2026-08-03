package es.caib.invai.api.service.facade;

import es.caib.invai.api.interna.maintenance.technology.DTO.TechnologyInputDTO;
import es.caib.invai.api.interna.maintenance.technology.DTO.TechnologyOutputDTO;
import es.caib.invai.api.persistence.repository.technology.TechnologyCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting catalog Technologies.
 *
 * @since 1.0.2
 */
public interface TechnologyService {

    TechnologyOutputDTO getById(Long id);

    Page<TechnologyOutputDTO> getAll(TechnologyCriteria filter, Pageable pageable);

    TechnologyOutputDTO create(TechnologyInputDTO inputDTO);

    TechnologyOutputDTO update(Long id, TechnologyInputDTO inputDTO);

    void delete(Long id);

    TechnologyOutputDTO reactivate(Long id);
}

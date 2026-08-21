package es.caib.invai.back.service.facade.catalog.modality;

import es.caib.invai.back.interna.catalog.modality.DTO.ModalityOutputDTO;

import java.util.List;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting the Modality lookup dictionary.
 *
 * @since 1.0.3
 */
public interface ModalityService {

    /**
     * Resolves every registered modality lookup entry, sorted by name.
     *
     * @return the full list of mapped {@link ModalityOutputDTO} entries
     */
    List<ModalityOutputDTO> getAll();
}

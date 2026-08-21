package es.caib.invai.back.service.facade.catalog.standardAdaption;

import es.caib.invai.back.interna.catalog.standardAdaption.DTO.StandardAdaptionOutputDTO;

import java.util.List;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting the Standard Adaption lookup dictionary.
 *
 * @since 1.0.3
 */
public interface StandardAdaptionService {

    /**
     * Resolves every registered standard adaption lookup entry, sorted by name.
     *
     * @return the full list of mapped {@link StandardAdaptionOutputDTO} entries
     */
    List<StandardAdaptionOutputDTO> getAll();
}

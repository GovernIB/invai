package es.caib.invai.back.service.facade.catalog.status;

import es.caib.invai.back.interna.catalog.status.DTO.StatusOutputDTO;

import java.util.List;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting the Status lookup dictionary.
 *
 * @since 1.0.3
 */
public interface StatusService {

    /**
     * Resolves every registered status lookup entry, sorted by name.
     *
     * @return the full list of mapped {@link StatusOutputDTO} entries
     */
    List<StatusOutputDTO> getAll();
}

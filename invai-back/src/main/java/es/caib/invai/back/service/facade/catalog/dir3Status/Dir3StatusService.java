package es.caib.invai.back.service.facade.catalog.dir3Status;

import es.caib.invai.back.interna.catalog.dir3Status.DTO.Dir3StatusOutputDTO;

import java.util.List;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting the DIR3 validation status lookup dictionary.
 *
 * @since 1.0.5
 */
public interface Dir3StatusService {

    /**
     * Resolves every registered DIR3 status lookup entry, sorted by name.
     *
     * @return the full list of mapped {@link Dir3StatusOutputDTO} entries
     */
    List<Dir3StatusOutputDTO> getAll();
}

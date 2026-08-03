package es.caib.invai.api.service.facade;

import es.caib.invai.api.interna.maintenance.serverType.DTO.ServerTypeOutputDTO;

import java.util.List;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting the Server Type lookup dictionary.
 *
 * @since 1.0.2
 */
public interface ServerTypeService {

    /**
     * Resolves every registered server type lookup entry, sorted by name.
     *
     * @return the full list of mapped {@link ServerTypeOutputDTO} entries
     */
    List<ServerTypeOutputDTO> getAll();
}

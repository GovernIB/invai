package es.caib.invai.back.service.facade.catalog.responsibleType;

import es.caib.invai.back.interna.catalog.responsibleType.DTO.ResponsibleTypeOutputDTO;

import java.util.List;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting the Responsible Type lookup dictionary.
 *
 * @since 1.0.3
 */
public interface ResponsibleTypeService {

    /**
     * Resolves every registered responsible type lookup entry, sorted by name.
     *
     * @return the full list of mapped {@link ResponsibleTypeOutputDTO} entries
     */
    List<ResponsibleTypeOutputDTO> getAll();
}

package es.caib.invai.back.service.facade.application.system_database.core;

import es.caib.invai.back.interna.application.system_database.core.DTO.AppInformationSystemDbInputDTO;
import es.caib.invai.back.interna.application.system_database.core.DTO.AppInformationSystemDbOutputDTO;

/**
 * Domain Boundary Outbound Port interfacing the internal transactional domain operations.
 *
 * @since 1.0.2
 */
public interface AppInformationSystemDbService {

    /**
     * Retrieves a paginated sequence of information system database groupings scoped to a single
     * parent application.
     *
     * @return a paginated payload containing corresponding transfer representations
     */
    AppInformationSystemDbOutputDTO getById(Long id);

    /**
     * Creates a new information system database grouping from the given input payload.
     *
     * @param inputDTO the creation payload
     * @return the created grouping, mapped to its output transfer representation
     */
    AppInformationSystemDbOutputDTO create(AppInformationSystemDbInputDTO inputDTO);

    /**
     * Updates the information system database grouping identified by {@code id} with
     * the given input payload.
     *
     * @param id       identifier of the grouping to update
     * @param inputDTO the update payload
     * @return the updated grouping, mapped to its output transfer representation
     */
    AppInformationSystemDbOutputDTO update(Long id, AppInformationSystemDbInputDTO inputDTO);

    /**
     * Deletes (logically) the information system database grouping identified by {@code id}.
     *
     * @param id identifier of the grouping to delete
     */
    void delete(Long id);
}

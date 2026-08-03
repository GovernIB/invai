package es.caib.invai.api.service.facade;

import es.caib.invai.api.interna.maintenance.server.DTO.ServerInputDTO;
import es.caib.invai.api.interna.maintenance.server.DTO.ServerOutputDTO;
import es.caib.invai.api.persistence.repository.server.ServerCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting host Server components.
 * Acts as the primary service boundary exposed to external API web controllers.
 *
 * @since 1.0.2
 */
public interface ServerService {

    /**
     * Retrieves a server by its unique internal identifier.
     *
     * @param id primary key tracking index mapping the server profile
     * @return the mapped presentation outbound DTO schema representation
     */
    ServerOutputDTO getById(Long id);

    /**
     * Retrieves the complete list of all servers matching pagination thresholds.
     *
     * @param filter   dynamic search criteria constraints
     * @param pageable pagination threshold constraints and structural sorting rules
     * @return a page wrapper grouping matching outbound data DTO schemas
     */
    Page<ServerOutputDTO> getAll(ServerCriteria filter, Pageable pageable);

    /**
     * Creates a new server record based on the input data.
     *
     * @param inputDTO The required data to register the server.
     * @return The created server along with its generated ID.
     */
    ServerOutputDTO create(ServerInputDTO inputDTO);

    /**
     * Updates the modifiable fields of an existing server identified by its ID.
     *
     * @param id       The unique identifier of the server to be modified.
     * @param inputDTO The new data to apply to the record.
     * @return The updated server snapshot details.
     */
    ServerOutputDTO update(Long id, ServerInputDTO inputDTO);

    /**
     * Performs a logical deletion of the record in the system by changing its status to inactive.
     *
     * @param id The unique identifier of the server to deactivate.
     */
    void delete(Long id);

    /**
     * Reactivates a logically soft-deleted server back to active state.
     *
     * @param id the target identifier mapping the server instance intended for reactivation
     * @return the reactivated domain representation mapped into an {@link ServerOutputDTO}
     */
    ServerOutputDTO reactivate(Long id);
}

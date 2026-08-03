package es.caib.invai.api.service.facade;

import es.caib.invai.api.interna.maintenance.environment.DTO.EnvironmentInputDTO;
import es.caib.invai.api.interna.maintenance.environment.DTO.EnvironmentOutputDTO;
import es.caib.invai.api.persistence.repository.environment.EnvironmentCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting Environment components.
 * Acts as the primary service boundary exposed to external API web controllers.
 *
 * @since 1.0.1
 */
public interface EnvironmentService {

    /**
     * Retrieves an environment by its unique internal identifier.
     *
     * @param id primary key tracking index mapping the environment profile
     * @return the mapped presentation outbound DTO schema representation
     */
    EnvironmentOutputDTO getById(Long id);

    /**
     * Retrieves the complete list of all environments matching pagination thresholds.
     *
     * @param pageable pagination threshold constraints and structural sorting rules
     * @return a page wrapper grouping matching outbound data DTO schemas
     */
    Page<EnvironmentOutputDTO> getAll(EnvironmentCriteria filter, Pageable pageable);

    /**
     * Creates a new environment record based on the input data.
     *
     * @param inputDTO The required data to register the environment.
     * @return The created environment along with its generated ID.
     */
    EnvironmentOutputDTO create(EnvironmentInputDTO inputDTO);

    /**
     * Updates the modifiable fields of an existing environment identified by its ID.
     *
     * @param id       The unique identifier of the environment to be modified.
     * @param inputDTO The new data to apply to the record.
     * @return The updated environment snapshot details.
     */
    EnvironmentOutputDTO update(Long id, EnvironmentInputDTO inputDTO);

    /**
     * Performs a logical deletion of the record in the system by changing its status to inactive.
     *
     * @param id The unique identifier of the environment to deactivate.
     */
    void delete(Long id);

    /**
     * Reactivates a logically soft-deleted environment back to active state.
     *
     * @param id the target identifier mapping the environment instance intended for reactivation
     * @return the reactivated domain representation mapped into an {@link EnvironmentOutputDTO}
     */
    EnvironmentOutputDTO reactivate(Long id);
}
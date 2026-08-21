package es.caib.invai.back.service.facade.maintenance.systems.system;

import es.caib.invai.back.interna.maintenance.systems.system.DTO.SystemInputDTO;
import es.caib.invai.back.interna.maintenance.systems.system.DTO.SystemOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.systems.system.SystemCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting System components.
 * Acts as the primary system service boundary exposed to external API web controllers.
 *
 * @since 1.0.1
 */
public interface SystemService {

    /**
     * Retrieves a system by its unique internal identifier.
     * Throws an exception if the system does not exist or is inactive.
     *
     * @param id primary key tracking index mapping the system profile
     * @return the mapped presentation outbound DTO schema representation
     */
    SystemOutputDTO getById(Long id);

    /**
     * Retrieves the complete list of all systems in the inventory matching pagination thresholds.
     * By default, this method omits systems that have undergone a logical delete (INACTIVE).
     *
     * @param filter   dynamic search criteria narrowing the resulting system record collection
     * @param pageable pagination threshold constraints and structural sorting rules
     * @return a page wrapper grouping matching outbound data DTO schemas
     */
    Page<SystemOutputDTO> getAll(SystemCriteria filter, Pageable pageable);

    /**
     * Creates a new system record in the inventory based on the input data.
     * Performs uniqueness validations for both the code and the prefix.
     *
     * @param inputDTO The required data to register the system.
     * @return The created system along with its generated ID and audit metadata.
     */
    SystemOutputDTO create(SystemInputDTO inputDTO);

    /**
     * Updates the modifiable fields of an existing system identified by its ID.
     * Protects data integrity by preventing duplicate codes or prefixes on external records.
     *
     * @param id       The unique identifier of the system to be modified.
     * @param inputDTO The new data to apply to the record.
     * @return The updated system with refreshed technical metadata.
     */
    SystemOutputDTO update(Long id, SystemInputDTO inputDTO);

    /**
     * Performs a logical deletion of the record in the system by changing its status to inactive (INACTIVE).
     * Does not perform a physical deletion in the database, in compliance with GOIB regulatory requirements.
     *
     * @param id The unique identifier of the system to deactivate.
     */
    void delete(Long id);

    /**
     * Reactivates a logically soft-deleted system record back to active state.
     *
     * @param id the target identifier mapping the system instance intended for reactivation
     * @return the reactivated domain representation mapped into an {@link SystemOutputDTO}
     * @throws es.caib.invai.back.exception.BusinessRuleException if matching system cannot be found or is already active
     */
    SystemOutputDTO reactivate(Long id);
}
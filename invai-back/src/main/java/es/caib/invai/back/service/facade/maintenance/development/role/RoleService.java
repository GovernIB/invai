package es.caib.invai.back.service.facade.maintenance.development.role;

import es.caib.invai.back.interna.maintenance.development.role.DTO.RoleInputDTO;
import es.caib.invai.back.interna.maintenance.development.role.DTO.RoleOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.development.role.RoleCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting provider Roles.
 *
 * @since 1.0.2
 */
public interface RoleService {

    /**
     * Retrieves a role by its unique internal identifier.
     *
     * @param id primary key tracking index mapping the role profile
     * @return the mapped presentation outbound DTO schema representation
     */
    RoleOutputDTO getById(Long id);

    /**
     * Retrieves the complete list of all roles matching pagination thresholds.
     *
     * @param filter   dynamic search criteria constraints
     * @param pageable pagination threshold constraints and structural sorting rules
     * @return a page wrapper grouping matching outbound data DTO schemas
     */
    Page<RoleOutputDTO> getAll(RoleCriteria filter, Pageable pageable);

    /**
     * Creates a new role record based on the input data.
     *
     * @param inputDTO the required data to register the role
     * @return the created role along with its generated ID
     */
    RoleOutputDTO create(RoleInputDTO inputDTO);

    /**
     * Updates the modifiable fields of an existing role identified by its ID.
     *
     * @param id       the unique identifier of the role to be modified
     * @param inputDTO the new data to apply to the record
     * @return the updated role snapshot details
     */
    RoleOutputDTO update(Long id, RoleInputDTO inputDTO);

    /**
     * Performs a logical deletion of the record in the system by changing its status to inactive.
     *
     * @param id the unique identifier of the role to deactivate
     */
    void delete(Long id);

    /**
     * Reactivates a logically soft-deleted role back to active state.
     *
     * @param id the target identifier mapping the role instance intended for reactivation
     * @return the reactivated domain representation mapped into a {@link RoleOutputDTO}
     */
    RoleOutputDTO reactivate(Long id);
}

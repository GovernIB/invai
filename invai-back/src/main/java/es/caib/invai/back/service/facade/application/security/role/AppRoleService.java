package es.caib.invai.back.service.facade.application.security.role;

import es.caib.invai.back.interna.application.security.role.DTO.AppRoleInputDTO;
import es.caib.invai.back.interna.application.security.role.DTO.AppRoleOutputDTO;
import es.caib.invai.back.persistence.repository.application.security.role.AppRoleCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Domain Boundary Outbound Port interfacing the internal transactional domain operations.
 *
 * @since 1.0.4
 */
public interface AppRoleService {

    /**
     * Retrieves a paginated sequence of role assignments scoped to a single parent security anchor.
     *
     * @param appSecurityId mandatory parent security anchor identifier scoping the result set
     * @param criteria      the multi-parameter business query filter boundaries
     * @param pageable      pagination structural constraints
     * @return a paginated payload containing corresponding transfer representations
     */
    Page<AppRoleOutputDTO> getAll(Long appSecurityId, AppRoleCriteria criteria, Pageable pageable);

    /**
     * Creates a new application-role assignment from the given input payload.
     *
     * @param inputDTO the creation payload
     * @return the created assignment, mapped to its output transfer representation
     */
    AppRoleOutputDTO create(AppRoleInputDTO inputDTO);

    /**
     * Updates the application-role assignment identified by {@code id} with the given
     * input payload.
     *
     * @param id       identifier of the assignment to update
     * @param inputDTO the update payload
     * @return the updated assignment, mapped to its output transfer representation
     */
    AppRoleOutputDTO update(Long id, AppRoleInputDTO inputDTO);

    /**
     * Deletes (logically) the application-role assignment identified by {@code id}.
     *
     * @param id identifier of the assignment to delete
     */
    void delete(Long id);
}

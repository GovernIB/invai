package es.caib.invai.back.persistence.repository.application.security.role;

import es.caib.invai.back.service.model.application.security.role.AppRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Persistence port abstracting CRUD and paginated search operations
 * for {@link AppRole} aggregates.
 *
 * @since 1.0.4
 */
public interface AppRoleRepository {

    /**
     * Persists a new application-role assignment.
     *
     * @param appRole the model to persist
     * @return the persisted model, including its generated identifier
     */
    AppRole create(AppRole appRole);

    /**
     * Updates the application-role assignment identified by {@code id}.
     *
     * @param appRole the model carrying the updated values
     * @param id      identifier of the record to update
     * @return the updated model
     */
    AppRole update(AppRole appRole, Long id);

    /**
     * Deletes (logically) the given application-role assignment.
     *
     * @param appRole the model to delete
     */
    void delete(AppRole appRole);

    /**
     * Resolves an application-role assignment by its identifier.
     *
     * @param id identifier of the record to resolve
     * @return the matching model, or {@code null} if not found
     */
    AppRole findById(Long id);

    /**
     * Resolves a paginated, filtered list of application-role assignments scoped to a
     * single parent security anchor.
     *
     * @param appSecurityId mandatory parent security anchor identifier scoping the result set
     * @param criteria      additional filter criteria
     * @param pageable      pagination and sorting instructions
     * @return the paginated page of matching models
     */
    Page<AppRole> findAll(Long appSecurityId, AppRoleCriteria criteria, Pageable pageable);
}

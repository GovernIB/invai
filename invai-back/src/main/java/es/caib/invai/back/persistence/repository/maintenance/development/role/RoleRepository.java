package es.caib.invai.back.persistence.repository.maintenance.development.role;

import es.caib.invai.back.service.model.maintenance.development.role.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Core business domain outbound Port boundary interface declaring relational persistence mechanisms
 * for the provider Role layer.
 *
 * @since 1.0.2
 */
public interface RoleRepository {

    /**
     * Resolves an active role record matching an identification primary index.
     *
     * @param id the unique role identifier
     * @return the matching {@link Role} domain model, or {@code null} if not found
     */
    Role findById(Long id);

    /**
     * Streams partitioned chunk metrics using pagination layout boundaries.
     *
     * @param filter   the search criteria used to narrow the results
     * @param pageable the pagination and sorting parameters
     * @return a page of matching {@link Role} domain models
     */
    Page<Role> findAll(RoleCriteria filter, Pageable pageable);

    /**
     * Registers a new role profile structure inside relational tracking systems.
     *
     * @param role the role domain model to persist
     * @return the persisted role, including its generated identifier
     */
    Role create(Role role);

    /**
     * Commits changes onto an active domain record identified by a target primary sequence index.
     *
     * @param role the role domain model holding the updated data
     * @param id   the identifier of the role record to update
     * @return the updated role domain model
     */
    Role update(Role role, Long id);

    /**
     * Flags a tracking profile context record as soft-deleted inside persistence layers.
     *
     * @param role the role domain model to soft-delete
     */
    void delete(Role role);

    /**
     * Confirms uniqueness of the name across active role registries.
     *
     * @param name the role name to check
     * @return {@code true} if an active role with that name exists
     */
    boolean existsByNameAndDeletedAtIsNull(String name);

    /**
     * Checks for duplicate name conflicts excluding a target primary reference row.
     *
     * @param name the role name to check
     * @param id   the identifier to exclude from the check
     * @return {@code true} if an active role other than the given identifier owns that name
     */
    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id);
}

package es.caib.invai.back.persistence.repository.maintenance.security.ensRequirement;

import es.caib.invai.back.service.model.maintenance.security.ensRequirement.EnsRequirement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Core business domain outbound Port boundary interface declaring relational persistence mechanisms
 * for the EnsRequirement catalog layer.
 *
 * @since 1.0.4
 */
public interface EnsRequirementRepository {

    /**
     * Finds an ENS requirement entry by its identifier.
     *
     * @param id the ENS requirement entry identifier
     * @return the matching domain model, or {@code null} if not found
     */
    EnsRequirement findById(Long id);

    /**
     * Finds ENS requirement entries matching the given filter criteria, paginated.
     *
     * @param filter search criteria used to narrow down results
     * @param pageable pagination and sorting instructions
     * @return a page of matching ENS requirement entries
     */
    Page<EnsRequirement> findAll(EnsRequirementCriteria filter, Pageable pageable);

    /**
     * Persists a new ENS requirement entry.
     *
     * @param ensRequirement the domain model to persist
     * @return the persisted domain model
     */
    EnsRequirement create(EnsRequirement ensRequirement);

    /**
     * Persists changes to an existing ENS requirement entry.
     *
     * @param ensRequirement the domain model containing the updated data
     * @param id the identifier of the ENS requirement entry to update
     * @return the updated domain model
     */
    EnsRequirement update(EnsRequirement ensRequirement, Long id);

    /**
     * Deletes (logically) the given ENS requirement entry.
     *
     * @param ensRequirement the domain model to delete
     */
    void delete(EnsRequirement ensRequirement);

    /**
     * Checks whether an active (not logically deleted) ENS requirement entry with the given name exists.
     *
     * @param name the name to check
     * @return {@code true} if a matching active record exists
     */
    boolean existsByNameAndDeletedAtIsNull(String name);

    /**
     * Checks whether an active (not logically deleted) ENS requirement entry with the given name exists,
     * excluding the record with the given ID.
     *
     * @param name the name to check
     * @param id the identifier to exclude from the check
     * @return {@code true} if a matching active record exists other than the one with the given ID
     */
    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id);
}

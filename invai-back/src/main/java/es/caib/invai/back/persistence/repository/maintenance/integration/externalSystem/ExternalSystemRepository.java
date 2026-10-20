package es.caib.invai.back.persistence.repository.maintenance.integration.externalSystem;

import es.caib.invai.back.service.model.maintenance.integration.externalSystem.ExternalSystem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Core business domain outbound Port boundary interface declaring relational persistence mechanisms
 * for the External System catalog layer.
 *
 * @since 1.0.5
 */
public interface ExternalSystemRepository {

    /**
     * Finds an external system by its identifier.
     *
     * @param id the external system identifier
     * @return the matching external system, or {@code null} if none is found
     */
    ExternalSystem findById(Long id);

    /**
     * Finds external systems matching the given filter criteria, paginated.
     *
     * @param filter   search and status filter criteria
     * @param pageable pagination and sorting information
     * @return a page of matching external systems
     */
    Page<ExternalSystem> findAll(ExternalSystemCriteria filter, Pageable pageable);

    /**
     * Persists a new external system.
     *
     * @param externalSystem the external system to create
     * @return the persisted external system
     */
    ExternalSystem create(ExternalSystem externalSystem);

    /**
     * Persists changes to an existing external system.
     *
     * @param externalSystem the external system data to persist
     * @param id             the identifier of the external system to update
     * @return the updated external system
     */
    ExternalSystem update(ExternalSystem externalSystem, Long id);

    /**
     * Logically deletes the given external system.
     *
     * @param externalSystem the external system to delete
     */
    void delete(ExternalSystem externalSystem);

    /**
     * Checks whether an active (non-deleted) external system exists with the given name.
     *
     * @param name the external system name to check
     * @return {@code true} if a matching non-deleted external system exists
     */
    boolean existsByNameAndDeletedAtIsNull(String name);

    /**
     * Checks whether an active (non-deleted) external system exists with the given name and a
     * different identifier.
     *
     * @param name the external system name to check
     * @param id   the identifier to exclude from the check
     * @return {@code true} if a matching non-deleted external system exists with a different ID
     */
    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id);
}

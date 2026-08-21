package es.caib.invai.back.persistence.repository.maintenance.development.technology;

import es.caib.invai.back.service.model.maintenance.development.technology.Technology;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Core business domain outbound Port boundary interface declaring relational persistence mechanisms
 * for the catalog Technology layer.
 *
 * @since 1.0.2
 */
public interface TechnologyRepository {

    /**
     * Finds a technology by its identifier.
     *
     * @param id the technology identifier
     * @return the matching technology, or {@code null} if none is found
     */
    Technology findById(Long id);

    /**
     * Finds a paginated list of technologies matching the given filter criteria.
     *
     * @param filter   the search criteria to apply
     * @param pageable the pagination and sorting configuration
     * @return a page of matching technologies
     */
    Page<Technology> findAll(TechnologyCriteria filter, Pageable pageable);

    /**
     * Persists a new technology.
     *
     * @param technology the technology to create
     * @return the created technology
     */
    Technology create(Technology technology);

    /**
     * Persists changes to an existing technology.
     *
     * @param technology the technology data to persist
     * @param id         the identifier of the technology to update
     * @return the updated technology
     */
    Technology update(Technology technology, Long id);

    /**
     * Deletes (or logically deletes, depending on the implementation) a technology.
     *
     * @param technology the technology to delete
     */
    void delete(Technology technology);

    /**
     * Checks whether a non-deleted technology exists with the given name.
     *
     * @param name the name to check
     * @return {@code true} if a matching non-deleted technology exists
     */
    boolean existsByNameAndDeletedAtIsNull(String name);

    /**
     * Checks whether a non-deleted technology other than the given ID exists with the given name.
     *
     * @param name the name to check
     * @param id   the identifier to exclude from the check
     * @return {@code true} if a matching non-deleted technology exists
     */
    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id);

    /**
     * Checks whether any technology is associated with the given architecture layer.
     *
     * @param layerId the layer identifier to check
     * @return {@code true} if at least one technology references the layer
     */
    boolean existsByLayerId(Long layerId);
}

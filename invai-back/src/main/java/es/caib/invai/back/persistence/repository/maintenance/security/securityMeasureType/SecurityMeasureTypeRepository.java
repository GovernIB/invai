package es.caib.invai.back.persistence.repository.maintenance.security.securityMeasureType;

import es.caib.invai.back.service.model.maintenance.security.securityMeasureType.SecurityMeasureType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Core business domain outbound Port boundary interface declaring relational persistence mechanisms
 * for the SecurityMeasureType catalog layer.
 *
 * @since 1.0.4
 */
public interface SecurityMeasureTypeRepository {

    /**
     * Finds a security measure type entry by its identifier.
     *
     * @param id the security measure type entry identifier
     * @return the matching domain model, or {@code null} if not found
     */
    SecurityMeasureType findById(Long id);

    /**
     * Finds security measure type entries matching the given filter criteria, paginated.
     *
     * @param filter search criteria used to narrow down results
     * @param pageable pagination and sorting instructions
     * @return a page of matching security measure type entries
     */
    Page<SecurityMeasureType> findAll(SecurityMeasureTypeCriteria filter, Pageable pageable);

    /**
     * Persists a new security measure type entry.
     *
     * @param securityMeasureType the domain model to persist
     * @return the persisted domain model
     */
    SecurityMeasureType create(SecurityMeasureType securityMeasureType);

    /**
     * Persists changes to an existing security measure type entry.
     *
     * @param securityMeasureType the domain model containing the updated data
     * @param id the identifier of the security measure type entry to update
     * @return the updated domain model
     */
    SecurityMeasureType update(SecurityMeasureType securityMeasureType, Long id);

    /**
     * Deletes (logically) the given security measure type entry.
     *
     * @param securityMeasureType the domain model to delete
     */
    void delete(SecurityMeasureType securityMeasureType);

    /**
     * Checks whether an active (not logically deleted) security measure type entry with the given name exists.
     *
     * @param name the name to check
     * @return {@code true} if a matching active record exists
     */
    boolean existsByNameAndDeletedAtIsNull(String name);

    /**
     * Checks whether an active (not logically deleted) security measure type entry with the given name exists,
     * excluding the record with the given ID.
     *
     * @param name the name to check
     * @param id the identifier to exclude from the check
     * @return {@code true} if a matching active record exists other than the one with the given ID
     */
    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id);
}

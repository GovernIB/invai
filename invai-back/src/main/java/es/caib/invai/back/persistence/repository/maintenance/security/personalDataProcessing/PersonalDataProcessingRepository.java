package es.caib.invai.back.persistence.repository.maintenance.security.personalDataProcessing;

import es.caib.invai.back.service.model.maintenance.security.personalDataProcessing.PersonalDataProcessing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Core business domain outbound Port boundary interface declaring relational persistence mechanisms
 * for the PersonalDataProcessing catalog layer.
 *
 * @since 1.0.4
 */
public interface PersonalDataProcessingRepository {

    /**
     * Finds an personal data processing entry by its identifier.
     *
     * @param id the personal data processing entry identifier
     * @return the matching domain model, or {@code null} if not found
     */
    PersonalDataProcessing findById(Long id);

    /**
     * Finds personal data processing entries matching the given filter criteria, paginated.
     *
     * @param filter search criteria used to narrow down results
     * @param pageable pagination and sorting instructions
     * @return a page of matching personal data processing entries
     */
    Page<PersonalDataProcessing> findAll(PersonalDataProcessingCriteria filter, Pageable pageable);

    /**
     * Persists a new personal data processing entry.
     *
     * @param personalDataProcessing the domain model to persist
     * @return the persisted domain model
     */
    PersonalDataProcessing create(PersonalDataProcessing personalDataProcessing);

    /**
     * Persists changes to an existing personal data processing entry.
     *
     * @param personalDataProcessing the domain model containing the updated data
     * @param id the identifier of the personal data processing entry to update
     * @return the updated domain model
     */
    PersonalDataProcessing update(PersonalDataProcessing personalDataProcessing, Long id);

    /**
     * Deletes (logically) the given personal data processing entry.
     *
     * @param personalDataProcessing the domain model to delete
     */
    void delete(PersonalDataProcessing personalDataProcessing);

    /**
     * Checks whether an active (not logically deleted) personal data processing entry with the given name exists.
     *
     * @param name the name to check
     * @return {@code true} if a matching active record exists
     */
    boolean existsByNameAndDeletedAtIsNull(String name);

    /**
     * Checks whether an active (not logically deleted) personal data processing entry with the given name exists,
     * excluding the record with the given ID.
     *
     * @param name the name to check
     * @param id the identifier to exclude from the check
     * @return {@code true} if a matching active record exists other than the one with the given ID
     */
    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id);
}

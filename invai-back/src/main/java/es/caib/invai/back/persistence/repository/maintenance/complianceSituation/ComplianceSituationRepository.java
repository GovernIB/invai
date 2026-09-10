package es.caib.invai.back.persistence.repository.maintenance.complianceSituation;

import es.caib.invai.back.service.model.maintenance.complianceSituation.ComplianceSituation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Core business domain outbound Port boundary interface declaring relational persistence mechanisms
 * for the ComplianceSituation catalog layer.
 *
 * @since 1.0.4
 */
public interface ComplianceSituationRepository {

    /**
     * Finds a compliance situation entry by its identifier.
     *
     * @param id the compliance situation entry identifier
     * @return the matching domain model, or {@code null} if not found
     */
    ComplianceSituation findById(Long id);

    /**
     * Finds compliance situation entries matching the given filter criteria, paginated.
     *
     * @param filter search criteria used to narrow down results
     * @param pageable pagination and sorting instructions
     * @return a page of matching compliance situation entries
     */
    Page<ComplianceSituation> findAll(ComplianceSituationCriteria filter, Pageable pageable);

    /**
     * Persists a new compliance situation entry.
     *
     * @param complianceSituation the domain model to persist
     * @return the persisted domain model
     */
    ComplianceSituation create(ComplianceSituation complianceSituation);

    /**
     * Persists changes to an existing compliance situation entry.
     *
     * @param complianceSituation the domain model containing the updated data
     * @param id the identifier of the compliance situation entry to update
     * @return the updated domain model
     */
    ComplianceSituation update(ComplianceSituation complianceSituation, Long id);

    /**
     * Deletes (logically) the given compliance situation entry.
     *
     * @param complianceSituation the domain model to delete
     */
    void delete(ComplianceSituation complianceSituation);

    /**
     * Checks whether an active (not logically deleted) compliance situation entry with the given name exists.
     *
     * @param name the name to check
     * @return {@code true} if a matching active record exists
     */
    boolean existsByNameAndDeletedAtIsNull(String name);

    /**
     * Checks whether an active (not logically deleted) compliance situation entry with the given name exists,
     * excluding the record with the given ID.
     *
     * @param name the name to check
     * @param id the identifier to exclude from the check
     * @return {@code true} if a matching active record exists other than the one with the given ID
     */
    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id);
}

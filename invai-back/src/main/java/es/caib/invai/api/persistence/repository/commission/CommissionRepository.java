package es.caib.invai.api.persistence.repository.commission;

import es.caib.invai.api.service.model.Commission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Core business domain outbound Port boundary interface declaring relational persistence mechanisms
 * for the commission asset layer.
 * <p>
 * Decouples domain engine transactional components from relational frameworks and driver specifics
 * by working exclusively with domain-agnostic or plain business models ({@link Commission}).
 * </p>
 *
 * @since 1.0.0
 */
public interface CommissionRepository {

    /**
     * Resolves an active commission business model matching the given primary key structure.
     *
     * @param id unique database sequence row identifier tracking the asset
     * @return the mapped domain {@link Commission} instance, or {@code null} if missing or soft-deleted
     */
    Commission findById(Long id);

    /**
     * Extracts a paginated sequence wrapper matrix across active technical commission nodes.
     *
     * @param pageable sorting configuration boundaries and limit arguments
     * @return a page structure containing mapped domain {@link Commission} entities
     */
    Page<Commission> findAll(Pageable pageable);

    /**
     * Commits a clean commission specification record state parameters map to database systems.
     *
     * @param commission transient domain model capturing target properties variables data
     * @return a detailed persistent domain snapshot representation mirroring saved data variables
     */
    Commission create(Commission commission);

    /**
     * Synchronizes and updates data structures for an active database commission identity row.
     *
     * @param commission property modifications schema containing target update variables data
     * @param id         persistent identity identifier index tracking state records
     * @return a fresh modified data model instance mapping persistent state representations
     */
    Commission update(Commission commission, Long id);

    /**
     * Finalizes execution pathways logic scopes routing parameter assets towards data deletion states.
     *
     * @param commission the data schema reference instance targeted for deactivation
     */
    void delete(Commission commission);

    /**
     * Evaluates active namespace tracking records to check for descriptive commission naming collisions.
     *
     * @param name target description name query criteria
     * @return {@code true} if conflicts exist, {@code false} otherwise
     */
    boolean existsByNameAndDeletedAtIsNull(String name);

    /**
     * Verifies if alternative active entities share a requested name flag, filtering target references.
     *
     * @param name target description name query criteria
     * @param id   row entry identification key index to ignore
     * @return {@code true} if duplicates exist outside the index parameter, {@code false} otherwise
     */
    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id);
}
package es.caib.invai.api.persistence.repository.systemType;

import es.caib.invai.api.service.model.SystemType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Core business domain outbound Port boundary interface declaring relational persistence mechanisms
 * for the system type infrastructure layer.
 * <p>
 * Decouples domain engine transactional components from relational frameworks and driver specifics
 * by working exclusively with domain-agnostic or plain business models ({@link SystemType}).
 * </p>
 *
 * @since 1.0.0
 */
public interface SystemTypeRepository {

    /**
     * Resolves an active system type business model matching the given primary key structure.
     *
     * @param id unique database sequence row identifier tracking the asset
     * @return the mapped domain {@link SystemType} instance, or {@code null} if missing or soft-deleted
     */
    SystemType findById(Long id);

    /**
     * Extracts a paginated sequence wrapper matrix across active infrastructure system type nodes.
     *
     * @param pageable sorting configuration boundaries and limit arguments
     * @return a page structure containing mapped domain {@link SystemType} entities
     */
    Page<SystemType> findAll(Pageable pageable);

    /**
     * Commits a clean architecture system type specification record state parameters map to database systems.
     *
     * @param systemType transient domain model capturing target properties variables data
     * @return a detailed persistent domain snapshot representation mirroring saved data variables
     */
    SystemType create(SystemType systemType);

    /**
     * Synchronizes and updates data structures for an active database system type identity row.
     *
     * @param systemType property modifications schema containing target update variables data
     * @param id         persistent identity identifier index tracking state records
     * @return a fresh modified data model instance mapping persistent state representations
     */
    SystemType update(SystemType systemType, Long id);

    /**
     * Finalizes execution pathways logic scopes routing parameter assets towards data deletion states.
     *
     * @param systemType the data schema reference instance targeted for deactivation
     */
    void delete(SystemType systemType);

    /**
     * Evaluates active namespace tracking records to check for descriptive system type naming collisions.
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
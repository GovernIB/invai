package es.caib.invai.api.persistence.repository.field;

import es.caib.invai.api.service.model.Field;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Core business domain outbound Port boundary interface declaring relational persistence mechanisms
 * for the structural field asset layer.
 * <p>
 * Decouples domain engine transactional components from relational frameworks and driver specifics
 * by working exclusively with domain-agnostic or plain business models ({@link Field}).
 * </p>
 *
 * @since 1.0.0
 */
public interface FieldRepository {

    /**
     * Resolves an active business field model matching the given primary key structure.
     *
     * @param id unique database sequence row identifier tracking the asset
     * @return the mapped domain {@link Field} instance, or {@code null} if missing or soft-deleted
     */
    Field findById(Long id);

    /**
     * Extracts a paginated sequence wrapper matrix across active functional area field nodes.
     *
     * @param pageable sorting configuration boundaries and limit arguments
     * @return a page structure containing mapped domain {@link Field} entities
     */
    Page<Field> findAll(Pageable pageable);

    /**
     * Commits a clean functional area field specification record state parameters map to database systems.
     *
     * @param field transient domain model capturing target properties variables data
     * @return a detailed persistent domain snapshot representation mirroring saved data variables
     */
    Field create(Field field);

    /**
     * Synchronizes and updates data structures for an active database field identity row.
     *
     * @param field property modifications schema containing target update variables data
     * @param id    persistent identity identifier index tracking state records
     * @return a fresh modified data model instance mapping persistent state representations
     */
    Field update(Field field, Long id);

    /**
     * Finalizes execution pathways logic scopes routing parameter assets towards data deletion states.
     *
     * @param field the data schema reference instance targeted for deactivation
     */
    void delete(Field field);

    /**
     * Evaluates active namespace tracking records to check for descriptive field naming collisions.
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
package es.caib.invai.back.persistence.repository.application.system_database.system;

import es.caib.invai.back.service.model.application.system_database.system.AppSystem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Persistence port abstracting CRUD, paginated search and uniqueness-check operations
 * for {@link AppSystem} aggregates.
 *
 * @since 1.0.2
 */
public interface AppSystemRepository {

    /**
     * Persists a new application-system link.
     *
     * @param appSystem the model to persist
     * @return the persisted model, including its generated identifier
     */
    AppSystem create(AppSystem appSystem);

    /**
     * Updates the application-system link identified by {@code id}.
     *
     * @param appSystem the model carrying the updated values
     * @param id        identifier of the record to update
     * @return the updated model
     */
    AppSystem update(AppSystem appSystem, Long id);

    /**
     * Deletes (logically) the given application-system link.
     *
     * @param appSystem the model to delete
     */
    void delete(AppSystem appSystem);

    /**
     * Resolves an application-system link by its identifier.
     *
     * @param id identifier of the record to resolve
     * @return the matching model, or {@code null} if not found
     */
    AppSystem findById(Long id);

    /**
     * Resolves a paginated, filtered list of application-system links scoped to a
     * single parent information system database grouping.
     *
     * @param informationSystemDbId mandatory parent grouping identifier scoping the result set
     * @param criteria              additional filter criteria
     * @param pageable              pagination and sorting instructions
     * @return the paginated page of matching models
     */
    Page<AppSystem> findAll(Long informationSystemDbId, AppSystemCriteria criteria, Pageable pageable);

    /**
     * Checks whether an application-system link already exists for the given
     * information system database grouping and system.
     *
     * @param informationSystemDbId identifier of the information system database grouping
     * @param systemId               identifier of the system
     * @return {@code true} if a matching link exists, {@code false} otherwise
     */
    boolean existsByInformationSystemDbAndSystem(Long informationSystemDbId, Long systemId);

    /**
     * Checks whether an application-system link already exists for the given
     * information system database grouping and system, excluding a specific record.
     *
     * @param informationSystemDbId identifier of the information system database grouping
     * @param systemId               identifier of the system
     * @param id                     identifier of the record to exclude from the check
     * @return {@code true} if a matching link exists, {@code false} otherwise
     */
    boolean existsByInformationSystemDbAndSystemAndIdNot(Long informationSystemDbId, Long systemId, Long id);
}

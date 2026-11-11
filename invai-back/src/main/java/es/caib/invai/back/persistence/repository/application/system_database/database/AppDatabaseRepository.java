package es.caib.invai.back.persistence.repository.application.system_database.database;

import es.caib.invai.back.service.model.application.system_database.database.AppDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Persistence port abstracting CRUD, paginated search and uniqueness-check operations
 * for {@link AppDatabase} aggregates.
 *
 * @since 1.0.2
 */
public interface AppDatabaseRepository {

    /**
     * Persists a new application-database link.
     *
     * @param appDatabase the model to persist
     * @return the persisted model, including its generated identifier
     */
    AppDatabase create(AppDatabase appDatabase);

    /**
     * Updates the application-database link identified by {@code id}.
     *
     * @param appDatabase the model carrying the updated values
     * @param id          identifier of the record to update
     * @return the updated model
     */
    AppDatabase update(AppDatabase appDatabase, Long id);

    /**
     * Deletes (logically) the given application-database link.
     *
     * @param appDatabase the model to delete
     */
    void delete(AppDatabase appDatabase);

    /**
     * Resolves an application-database link by its identifier.
     *
     * @param id identifier of the record to resolve
     * @return the matching model, or {@code null} if not found
     */
    AppDatabase findById(Long id);

    /**
     * Resolves a paginated, filtered list of application-database links scoped to a
     * single parent information system database grouping.
     *
     * @param informationSystemDbId mandatory parent grouping identifier scoping the result set
     * @param criteria              additional filter criteria
     * @param pageable              pagination and sorting instructions
     * @return the paginated page of matching models
     */
    Page<AppDatabase> findAll(Long informationSystemDbId, AppDatabaseCriteria criteria, Pageable pageable);

    /**
     * Resolves the application-database link of the given information system database grouping and
     * database, whether it is active or logically deleted.
     *
     * @param informationSystemDbId identifier of the information system database grouping
     * @param databaseId            identifier of the database
     * @return the matching model, or {@code null} if the database was never linked to the grouping
     */
    AppDatabase findByInformationSystemDbIdAndDatabaseId(Long informationSystemDbId, Long databaseId);
}

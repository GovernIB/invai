package es.caib.invai.back.persistence.repository.application.system_database.core;

import es.caib.invai.back.service.model.application.system_database.core.AppInformationSystemDb;

/**
 * Persistence port abstracting CRUD and lookup operations for
 * {@link AppInformationSystemDb} aggregates.
 *
 * @since 1.0.2
 */
public interface AppInformationSystemDbRepository {

    /**
     * Persists a new information system database grouping.
     *
     * @param appInformationSystemDb the model to persist
     * @return the persisted model, including its generated identifier
     */
    AppInformationSystemDb create(AppInformationSystemDb appInformationSystemDb);

    /**
     * Updates the information system database grouping identified by {@code id}.
     *
     * @param appInformationSystemDb the model carrying the updated values
     * @param id                     identifier of the record to update
     * @return the updated model
     */
    AppInformationSystemDb update(AppInformationSystemDb appInformationSystemDb, Long id);

    /**
     * Deletes (logically) the given information system database grouping.
     *
     * @param appInformationSystemDb the model to delete
     */
    void delete(AppInformationSystemDb appInformationSystemDb);

    /**
     * Resolves an information system database grouping by its identifier.
     *
     * @param id identifier of the record to resolve
     * @return the matching model, or {@code null} if not found
     */
    AppInformationSystemDb findById(Long id);

    /**
     * Resolves the information system database grouping owned by the given application.
     *
     * @param applicationId identifier of the parent application
     * @return the matching model, or {@code null} if not found
     */
    AppInformationSystemDb findByApplicationId(Long applicationId);
}

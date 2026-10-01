package es.caib.invai.back.persistence.repository.application.data;

import es.caib.invai.back.service.model.application.data.AppData;

/**
 * Persistence port abstracting CRUD and lookup operations for {@link AppData} aggregates.
 *
 * @since 1.0.5
 */
public interface AppDataRepository {

    /**
     * Persists a new open data anchor.
     *
     * @param appData the model to persist
     * @return the persisted model, including its generated identifier
     */
    AppData create(AppData appData);

    /**
     * Updates the open data anchor identified by {@code id}.
     *
     * @param appData the model carrying the updated values
     * @param id          identifier of the record to update
     * @return the updated model
     */
    AppData update(AppData appData, Long id);

    /**
     * Deletes (logically) the given open data anchor.
     *
     * @param appData the model to delete
     */
    void delete(AppData appData);

    /**
     * Resolves an open data anchor by its identifier.
     *
     * @param id identifier of the record to resolve
     * @return the matching model, or {@code null} if not found
     */
    AppData findById(Long id);

    /**
     * Resolves the open data anchor owned by the given application, active or soft-deleted.
     *
     * @param applicationId identifier of the parent application
     * @return the matching model, or {@code null} if none exists (soft-deleted rows still match)
     */
    AppData findByApplicationId(Long applicationId);
}

package es.caib.invai.back.persistence.repository.application.integration.core;

import es.caib.invai.back.service.model.application.integration.core.AppIntegration;

/**
 * Persistence port abstracting CRUD and lookup operations for {@link AppIntegration} aggregates.
 *
 * @since 1.0.5
 */
public interface AppIntegrationRepository {

    /**
     * Persists a new integration anchor.
     *
     * @param appIntegration the model to persist
     * @return the persisted model, including its generated identifier
     */
    AppIntegration create(AppIntegration appIntegration);

    /**
     * Updates the integration anchor identified by {@code id}.
     *
     * @param appIntegration the model carrying the updated values
     * @param id             identifier of the record to update
     * @return the updated model
     */
    AppIntegration update(AppIntegration appIntegration, Long id);

    /**
     * Deletes (logically) the given integration anchor.
     *
     * @param appIntegration the model to delete
     */
    void delete(AppIntegration appIntegration);

    /**
     * Resolves an integration anchor by its identifier.
     *
     * @param id identifier of the record to resolve
     * @return the matching model, or {@code null} if not found
     */
    AppIntegration findById(Long id);

    /**
     * Resolves the integration anchor owned by the given application, active or soft-deleted.
     *
     * @param applicationId identifier of the parent application
     * @return the matching model, or {@code null} if none exists (soft-deleted rows still match)
     */
    AppIntegration findByApplicationId(Long applicationId);
}

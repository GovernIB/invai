package es.caib.invai.back.persistence.repository.application.security.core;

import es.caib.invai.back.service.model.application.security.core.AppSecurity;

/**
 * Persistence port abstracting CRUD and lookup operations for
 * {@link AppSecurity} aggregates.
 *
 * @since 1.0.4
 */
public interface AppSecurityRepository {

    /**
     * Persists a new security anchor.
     *
     * @param appSecurity the model to persist
     * @return the persisted model, including its generated identifier
     */
    AppSecurity create(AppSecurity appSecurity);

    /**
     * Updates the security anchor identified by {@code id}.
     *
     * @param appSecurity the model carrying the updated values
     * @param id          identifier of the record to update
     * @return the updated model
     */
    AppSecurity update(AppSecurity appSecurity, Long id);

    /**
     * Deletes (logically) the given security anchor.
     *
     * @param appSecurity the model to delete
     */
    void delete(AppSecurity appSecurity);

    /**
     * Resolves a security anchor by its identifier.
     *
     * @param id identifier of the record to resolve
     * @return the matching model, or {@code null} if not found
     */
    AppSecurity findById(Long id);

    /**
     * Resolves the security anchor owned by the given application.
     *
     * @param applicationId identifier of the parent application
     * @return the matching model, or {@code null} if not found
     */
    AppSecurity findByApplicationId(Long applicationId);
}

package es.caib.invai.back.persistence.repository.application.accessibility;

import es.caib.invai.back.service.model.application.accessibility.AppAccessibility;

/**
 * Persistence port abstracting CRUD and lookup operations for
 * {@link AppAccessibility} aggregates.
 *
 * @since 1.0.4
 */
public interface AppAccessibilityRepository {

    /**
     * Persists a new accessibility anchor.
     *
     * @param appAccessibility the model to persist
     * @return the persisted model, including its generated identifier
     */
    AppAccessibility create(AppAccessibility appAccessibility);

    /**
     * Updates the accessibility anchor identified by {@code id}.
     *
     * @param appAccessibility the model carrying the updated values
     * @param id                identifier of the record to update
     * @return the updated model
     */
    AppAccessibility update(AppAccessibility appAccessibility, Long id);

    /**
     * Deletes (logically) the given accessibility anchor.
     *
     * @param appAccessibility the model to delete
     */
    void delete(AppAccessibility appAccessibility);

    /**
     * Resolves an accessibility anchor by its identifier.
     *
     * @param id identifier of the record to resolve
     * @return the matching model, or {@code null} if not found
     */
    AppAccessibility findById(Long id);

    /**
     * Resolves the accessibility anchor owned by the given application, active or soft-deleted —
     * used by the facade's {@code create} to check whether an anchor already exists before
     * allowing a new one.
     *
     * @param applicationId identifier of the parent application
     * @return the matching model, or {@code null} if none exists (soft-deleted rows still match)
     */
    AppAccessibility findByApplicationId(Long applicationId);
}

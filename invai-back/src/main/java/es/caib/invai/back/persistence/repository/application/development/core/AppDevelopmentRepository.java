package es.caib.invai.back.persistence.repository.application.development.core;

import es.caib.invai.back.service.model.application.development.core.AppDevelopment;

/**
 * Core business domain outbound port boundary interface declaring relational persistence
 * mechanisms for the {@link AppDevelopment} anchor.
 *
 * @since 1.0.2
 */
public interface AppDevelopmentRepository {

    /**
     * Persists a new development record.
     *
     * @param appDevelopment the domain model to persist
     * @return the persisted domain model, including its generated identifier
     */
    AppDevelopment create(AppDevelopment appDevelopment);

    /**
     * Persists changes to an existing development record.
     *
     * @param appDevelopment the domain model carrying the updated values
     * @param id             the identifier of the record being updated
     * @return the persisted domain model reflecting the applied changes
     */
    AppDevelopment update(AppDevelopment appDevelopment, Long id);

    /**
     * Persists the logical (soft) deletion of a development record.
     *
     * @param appDevelopment the domain model, with deletion tracking fields already set
     */
    void delete(AppDevelopment appDevelopment);

    /**
     * Resolves a single development record by its primary key.
     *
     * @param id the record identifier
     * @return the matching domain model, or {@code null} if not found
     */
    AppDevelopment findById(Long id);

    /**
     * Resolves the development record associated with a given parent application.
     *
     * @param applicationId the parent application identifier
     * @return the matching domain model, or {@code null} if not found
     */
    AppDevelopment findByApplicationId(Long applicationId);
}

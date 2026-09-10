package es.caib.invai.back.persistence.repository.application.development.core;

import es.caib.invai.back.service.model.application.development.core.AppDevelopment;

/**
 * Persistence port for the "AppDevelopment" tab: create/update/delete and lookup by the record's
 * own id or by its parent application id ({@link #findByApplicationId}, used by the facade to
 * enforce the 1-to-1 relationship on {@code create}). Implemented by
 * {@code AppDevelopmentRepositoryAdapter}, which also writes an audit-trail row on every write.
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

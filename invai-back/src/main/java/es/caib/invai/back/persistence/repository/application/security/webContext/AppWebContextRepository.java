package es.caib.invai.back.persistence.repository.application.security.webContext;

import es.caib.invai.back.service.model.application.security.webContext.AppWebContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Persistence port abstracting CRUD and paginated search operations for
 * {@link AppWebContext} aggregates.
 *
 * @since 1.0.4
 */
public interface AppWebContextRepository {

    /**
     * Persists a new application-web context link.
     *
     * @param appWebContext the model to persist
     * @return the persisted model, including its generated identifier
     */
    AppWebContext create(AppWebContext appWebContext);

    /**
     * Updates the application-web context link identified by {@code id}.
     *
     * @param appWebContext the model carrying the updated values
     * @param id            identifier of the record to update
     * @return the updated model
     */
    AppWebContext update(AppWebContext appWebContext, Long id);

    /**
     * Deletes (logically) the given application-web context link.
     *
     * @param appWebContext the model to delete
     */
    void delete(AppWebContext appWebContext);

    /**
     * Resolves an application-web context link by its identifier.
     *
     * @param id identifier of the record to resolve
     * @return the matching model, or {@code null} if not found
     */
    AppWebContext findById(Long id);

    /**
     * Resolves a paginated, filtered list of application-web context links scoped to a
     * single parent security anchor. Used by Security's read-only listing endpoint.
     *
     * @param appSecurityId mandatory parent security anchor identifier scoping the result set
     * @param criteria      additional filter criteria
     * @param pageable      pagination and sorting instructions
     * @return the paginated page of matching models
     */
    Page<AppWebContext> findAllByAppSecurityId(Long appSecurityId, AppWebContextCriteria criteria, Pageable pageable);

    /**
     * Resolves a paginated, filtered list of application-web context links scoped to a
     * single parent development anchor. Used by Development's CRUD listing endpoint.
     *
     * @param appDevelopmentId mandatory parent development anchor identifier scoping the result set
     * @param criteria         additional filter criteria
     * @param pageable         pagination and sorting instructions
     * @return the paginated page of matching models
     */
    Page<AppWebContext> findAllByAppDevelopmentId(Long appDevelopmentId, AppWebContextCriteria criteria, Pageable pageable);
}

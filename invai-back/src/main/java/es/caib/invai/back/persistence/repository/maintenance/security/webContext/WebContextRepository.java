package es.caib.invai.back.persistence.repository.maintenance.security.webContext;

import es.caib.invai.back.service.model.maintenance.security.webContext.WebContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Core business domain outbound Port boundary interface declaring relational persistence mechanisms
 * for the WebContext catalog layer.
 *
 * @since 1.0.4
 */
public interface WebContextRepository {

    /**
     * Finds an web context by its identifier.
     *
     * @param id the web context identifier
     * @return the matching domain model, or {@code null} if not found
     */
    WebContext findById(Long id);

    /**
     * Finds web contexts matching the given filter criteria, paginated.
     *
     * @param filter search criteria used to narrow down results
     * @param pageable pagination and sorting instructions
     * @return a page of matching web contexts
     */
    Page<WebContext> findAll(WebContextCriteria filter, Pageable pageable);

    /**
     * Persists a new web context.
     *
     * @param webContext the domain model to persist
     * @return the persisted domain model
     */
    WebContext create(WebContext webContext);

    /**
     * Persists changes to an existing web context.
     *
     * @param webContext the domain model containing the updated data
     * @param id the identifier of the web context to update
     * @return the updated domain model
     */
    WebContext update(WebContext webContext, Long id);

    /**
     * Deletes (logically) the given web context.
     *
     * @param webContext the domain model to delete
     */
    void delete(WebContext webContext);

    /**
     * Checks whether an active (not logically deleted) web context with the given name exists.
     *
     * @param name the name to check
     * @return {@code true} if a matching active record exists
     */
    boolean existsByNameAndDeletedAtIsNull(String name);

    /**
     * Checks whether an active (not logically deleted) web context with the given name exists,
     * excluding the record with the given ID.
     *
     * @param name the name to check
     * @param id the identifier to exclude from the check
     * @return {@code true} if a matching active record exists other than the one with the given ID
     */
    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id);
}

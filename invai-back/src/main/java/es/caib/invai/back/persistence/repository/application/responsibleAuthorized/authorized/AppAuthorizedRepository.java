package es.caib.invai.back.persistence.repository.application.responsibleAuthorized.authorized;

import es.caib.invai.back.service.model.application.responsibleAuthorized.authorized.AppAuthorized;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Persistence port abstraction decoupling the service layer from the JPA infrastructure used to
 * store the {@link AppAuthorized} anchor entity.
 *
 * @since 1.0.3
 */
public interface AppAuthorizedRepository {

    /**
     * Persists a new authorized person record and writes the corresponding audit trail entry.
     *
     * @param appAuthorized the authorization to create
     * @return the persisted authorization, including its generated identifier
     */
    AppAuthorized create(AppAuthorized appAuthorized);

    /**
     * Merges changes into an existing authorized person record and writes the corresponding
     * audit trail entry.
     *
     * @param appAuthorized the authorization data to merge
     * @param id identifier of the authorization to update
     * @return the updated authorization
     */
    AppAuthorized update(AppAuthorized appAuthorized, Long id);

    /**
     * Soft-deletes an authorized person record and writes the corresponding audit trail entry.
     *
     * @param appAuthorized the authorization to delete
     */
    void delete(AppAuthorized appAuthorized);

    /**
     * Resolves an authorized person record by its identifier.
     *
     * @param id the authorization identifier
     * @return the matching authorization, or {@code null} if not found
     */
    AppAuthorized findById(Long id);

    /**
     * Resolves a paginated, filtered listing of authorized persons scoped to a single anchor.
     *
     * @param appResponsibleAuthorizedId the parent anchor identifier scoping the result set
     * @param criteria the search filters to apply
     * @param pageable the pagination and sorting parameters
     * @return the matching page of authorizations
     */
    Page<AppAuthorized> findAll(Long appResponsibleAuthorizedId, AppAuthorizedCriteria criteria, Pageable pageable);

    /**
     * Checks whether an active authorization already exists for the given anchor/person pair.
     *
     * @param appResponsibleAuthorizedId the parent anchor identifier
     * @param personId the person identifier
     * @return {@code true} if a matching active authorization exists
     */
    boolean existsByAppResponsibleAuthorizedAndPerson(Long appResponsibleAuthorizedId, Long personId);

    /**
     * Checks whether another active authorization already exists for the given anchor/person
     * pair, excluding a specific row identifier.
     *
     * @param appResponsibleAuthorizedId the parent anchor identifier
     * @param personId the person identifier
     * @param id identifier to exclude from the check
     * @return {@code true} if a matching active authorization exists
     */
    boolean existsByAppResponsibleAuthorizedAndPersonAndIdNot(Long appResponsibleAuthorizedId, Long personId, Long id);

    /** Finds every active authorization currently held by the given person, across all applications. */
    List<AppAuthorized> findAllActiveByPersonId(Long personId);

    /** Finds the currently active authorization (if any) held by the given person on the given anchor. */
    AppAuthorized findActiveByAppResponsibleAuthorizedAndPerson(Long appResponsibleAuthorizedId, Long personId);

    /** Finds every active authorization currently held on the given anchor, across every person. */
    List<AppAuthorized> findAllActiveByAppResponsibleAuthorized(Long appResponsibleAuthorizedId);
}

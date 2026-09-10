package es.caib.invai.back.persistence.repository.application.responsibleAuthorized.responsible;

import es.caib.invai.back.service.model.application.responsibleAuthorized.responsible.AppResponsible;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * Persistence port abstraction decoupling the service layer from the JPA infrastructure used to
 * store {@code AppResponsible} assignments.
 *
 * @since 1.0.3
 */
public interface AppResponsibleRepository {

    /**
     * Persists a new responsible assignment and writes the corresponding audit trail entry.
     *
     * @param appResponsible the assignment to create
     * @return the persisted assignment, including its generated identifier
     */
    AppResponsible create(AppResponsible appResponsible);

    /**
     * Merges changes into an existing responsible assignment and writes the corresponding audit
     * trail entry.
     *
     * @param appResponsible the assignment data to merge
     * @param id identifier of the assignment to update
     * @return the updated assignment
     */
    AppResponsible update(AppResponsible appResponsible, Long id);

    /**
     * Soft-deletes a responsible assignment and writes the corresponding audit trail entry.
     *
     * @param appResponsible the assignment to delete
     */
    void delete(AppResponsible appResponsible);

    /**
     * Resolves a responsible assignment by its identifier.
     *
     * @param id the assignment identifier
     * @return the matching assignment, or {@code null} if not found
     */
    AppResponsible findById(Long id);

    /**
     * Resolves a paginated, filtered listing of responsible assignments scoped to a single anchor.
     *
     * @param appResponsibleAuthorizedId the parent anchor identifier scoping the result set
     * @param criteria the search filters to apply
     * @param pageable the pagination and sorting parameters
     * @return the matching page of assignments
     */
    Page<AppResponsible> findAll(Long appResponsibleAuthorizedId, AppResponsibleCriteria criteria, Pageable pageable);

    /**
     * Checks whether another active assignment already holds the given responsible type on the
     * given anchor, excluding a specific row identifier.
     *
     * @param appResponsibleAuthorizedId the parent anchor identifier
     * @param responsibleTypeId the responsible type identifier
     * @param id identifier to exclude from the check
     * @return {@code true} if a matching active assignment exists
     */
    boolean existsByAppResponsibleAuthorizedAndResponsibleTypeAndIdNot(Long appResponsibleAuthorizedId, Long responsibleTypeId, Long id);

    /** Finds every active assignment currently held by the given person, across all applications. */
    List<AppResponsible> findAllActiveByPersonId(Long personId);

    /** Finds the currently active assignment (if any) holding the given responsible type on the given anchor. */
    AppResponsible findActiveByAppResponsibleAuthorizedAndResponsibleType(Long appResponsibleAuthorizedId, Long responsibleTypeId);
}

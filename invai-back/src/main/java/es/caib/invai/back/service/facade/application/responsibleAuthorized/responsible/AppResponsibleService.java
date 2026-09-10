package es.caib.invai.back.service.facade.application.responsibleAuthorized.responsible;

import es.caib.invai.back.interna.application.responsibleAuthorized.responsible.DTO.AppResponsibleDeleteDTO;
import es.caib.invai.back.interna.application.responsibleAuthorized.responsible.DTO.AppResponsibleInputDTO;
import es.caib.invai.back.interna.application.responsibleAuthorized.responsible.DTO.AppResponsibleOutputDTO;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.responsible.AppResponsibleCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Facade boundary declaring the transactional use cases for managing person-to-responsible-type
 * assignments ({@code AppResponsible}) on an application's "Responsables" tab. Enforces that at
 * most one active person may hold a given responsible type per anchor.
 *
 * @since 1.0.3
 */
public interface AppResponsibleService {

    /**
     * Retrieves a paginated, filtered listing of responsible assignments scoped to a single
     * "Responsables i Autoritzats" anchor. Unless the inactive status is explicitly requested, the
     * listing is driven by the full responsible type catalog rather than by persisted rows: every
     * registered type is always returned, carrying its active holder when one exists or an empty
     * placeholder otherwise.
     *
     * @param appResponsibleAuthorizedId mandatory parent anchor identifier scoping the result set
     * @param criteria      optional filter values (status, person, responsible type, free-text search)
     * @param pageable      pagination and sorting parameters
     * @return the matching page of responsible assignment rows
     */
    Page<AppResponsibleOutputDTO> getAll(Long appResponsibleAuthorizedId, AppResponsibleCriteria criteria, Pageable pageable);

    /**
     * Creates a new responsible assignment. If another person already holds the requested
     * responsible type on the same anchor, it is automatically deactivated in favor of the new one.
     * When {@code personId} is not supplied, resolves-or-creates the person from the inline
     * name/e-mail fields instead.
     *
     * @param inputDTO validated create payload
     * @return the newly created assignment
     */
    AppResponsibleOutputDTO create(AppResponsibleInputDTO inputDTO);

    /**
     * Updates an active responsible assignment (the responsible type is immutable; only the
     * person and job title can change).
     *
     * @param id       identifier of the assignment to update
     * @param inputDTO validated update payload
     * @return the updated assignment
     */
    AppResponsibleOutputDTO update(Long id, AppResponsibleInputDTO inputDTO);

    /**
     * Soft-deletes an active responsible assignment ("donar de baixa"), optionally recording a
     * free-text observation.
     *
     * @param id  identifier of the assignment to deactivate
     * @param dto optional payload carrying the deletion observation
     */
    void delete(Long id, AppResponsibleDeleteDTO dto);

    /**
     * Reactivates a previously deactivated responsible assignment, re-checking that no other
     * active assignment has since taken over the same responsible type on the same anchor.
     *
     * @param id identifier of the assignment to reactivate
     * @return the reactivated assignment
     */
    AppResponsibleOutputDTO reactivate(Long id);
}

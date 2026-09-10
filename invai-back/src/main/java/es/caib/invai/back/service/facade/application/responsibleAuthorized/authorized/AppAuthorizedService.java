package es.caib.invai.back.service.facade.application.responsibleAuthorized.authorized;

import es.caib.invai.back.interna.application.responsibleAuthorized.authorized.DTO.AppAuthorizedDeleteDTO;
import es.caib.invai.back.interna.application.responsibleAuthorized.authorized.DTO.AppAuthorizedInputDTO;
import es.caib.invai.back.interna.application.responsibleAuthorized.authorized.DTO.AppAuthorizedOutputDTO;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.authorized.AppAuthorizedCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Facade boundary declaring the transactional use cases for managing person authorizations
 * ({@code AppAuthorized}) linked to an application's "Responsables i Autoritzats" anchor,
 * including reconciliation of the multi-select authorization types attached to each assignment.
 *
 * @since 1.0.3
 */
public interface AppAuthorizedService {

    /**
     * Retrieves a paginated, filtered listing of authorized-person assignments scoped to a single
     * "Responsables i Autoritzats" anchor.
     *
     * @param appResponsibleAuthorizedId mandatory parent anchor identifier scoping the result set
     * @param criteria      optional filter values (status, person, free-text search)
     * @param pageable      pagination and sorting parameters
     * @return the matching page of authorizations, each carrying its resolved authorization types
     */
    Page<AppAuthorizedOutputDTO> getAll(Long appResponsibleAuthorizedId, AppAuthorizedCriteria criteria, Pageable pageable);

    /**
     * Creates a new authorized-person assignment together with its initial set of authorization
     * types. If the target person already holds an active authorization on the same anchor, it is
     * automatically deactivated in favor of the new one. When {@code personId} is not supplied,
     * resolves-or-creates the person from the inline name/e-mail fields instead.
     *
     * @param inputDTO validated create payload
     * @return the newly created assignment, including its resolved authorization types
     */
    AppAuthorizedOutputDTO create(AppAuthorizedInputDTO inputDTO);

    /**
     * Updates an active authorized assignment (the anchor and the person are immutable; only
     * {@code observation} changes), reconciling its attached authorization types against the
     * requested list (hard-deleting join rows no longer requested, inserting missing ones, leaving
     * the rest untouched).
     *
     * @param id       identifier of the assignment to update
     * @param inputDTO validated update payload
     * @return the updated assignment, including its reconciled authorization types
     */
    AppAuthorizedOutputDTO update(Long id, AppAuthorizedInputDTO inputDTO);

    /**
     * Soft-deletes an active authorized assignment ("donar de baixa"), optionally recording a
     * free-text observation.
     *
     * @param id  identifier of the assignment to deactivate
     * @param dto optional payload carrying the deletion observation
     */
    void delete(Long id, AppAuthorizedDeleteDTO dto);

    /**
     * Reactivates a previously deactivated authorized assignment, re-checking the
     * (appResponsibleAuthorizedId, personId) uniqueness rule before restoring it.
     *
     * @param id identifier of the assignment to reactivate
     * @return the reactivated assignment
     */
    AppAuthorizedOutputDTO reactivate(Long id);
}

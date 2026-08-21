package es.caib.invai.back.service.facade.application.responsibleAuthorized.authorized;

import es.caib.invai.back.interna.application.responsibleAuthorized.authorized.DTO.AppAuthorizedDeleteDTO;
import es.caib.invai.back.interna.application.responsibleAuthorized.authorized.DTO.AppAuthorizedInputDTO;
import es.caib.invai.back.interna.application.responsibleAuthorized.authorized.DTO.AppAuthorizedOutputDTO;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.authorized.AppAuthorizedCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Domain Boundary Outbound Port interfacing the internal transactional domain operations
 * targeting person authorizations linked to applications.
 *
 * @since 1.0.3
 */
public interface AppAuthorizedService {

    /**
     * Retrieves a paginated sequence of authorized assignment records scoped to a single parent application.
     *
     * @param appResponsibleAuthorizedId mandatory parent Responsables tab anchor identifier scoping the result set
     * @param criteria      the multi-parameter business query filter boundaries
     * @param pageable      pagination structural constraints
     * @return a paginated payload containing corresponding transfer representations
     */
    Page<AppAuthorizedOutputDTO> getAll(Long appResponsibleAuthorizedId, AppAuthorizedCriteria criteria, Pageable pageable);

    /**
     * Registers a new authorized assignment anchor together with its initial set of authorization types.
     *
     * @param inputDTO validated data configuration schema
     * @return outbound structural representation of the newly created assignment
     */
    AppAuthorizedOutputDTO create(AppAuthorizedInputDTO inputDTO);

    /**
     * Updates an active authorized assignment anchor, reconciling its attached authorization types
     * against the requested list (soft-deleting removed ones, inserting new ones, leaving the rest untouched).
     *
     * @param id       primary corporate tracking reference key
     * @param inputDTO mutated parameter dataset structures
     * @return updated transfer data mapping payload state
     */
    AppAuthorizedOutputDTO update(Long id, AppAuthorizedInputDTO inputDTO);

    /**
     * Transitions a target authorized assignment into an inactive state ("donar de baixa"),
     * optionally capturing a free-text observation.
     *
     * @param id  target primary structural key to process for deletion
     * @param dto optional payload carrying the deletion observation
     */
    void delete(Long id, AppAuthorizedDeleteDTO dto);

    /**
     * Reactivates a previously deactivated authorized assignment, re-checking the
     * (appResponsibleAuthorizedId, personId) uniqueness rule before restoring it.
     *
     * @param id target primary structural key to process for reactivation
     * @return the reactivated transfer data mapping payload state
     */
    AppAuthorizedOutputDTO reactivate(Long id);
}

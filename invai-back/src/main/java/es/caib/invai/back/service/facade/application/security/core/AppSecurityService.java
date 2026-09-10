package es.caib.invai.back.service.facade.application.security.core;

import es.caib.invai.back.interna.application.security.core.DTO.AppSecurityInputDTO;
import es.caib.invai.back.interna.application.security.core.DTO.AppSecurityOutputDTO;

/**
 * Domain Boundary Outbound Port interfacing the internal transactional domain operations.
 *
 * @since 1.0.4
 */
public interface AppSecurityService {

    /**
     * Retrieves a paginated sequence of security anchors scoped to a single
     * parent application.
     *
     * @return a paginated payload containing corresponding transfer representations
     */
    AppSecurityOutputDTO getById(Long id);

    /**
     * Creates a new security anchor from the given input payload.
     *
     * @param inputDTO the creation payload
     * @return the created anchor, mapped to its output transfer representation
     */
    AppSecurityOutputDTO create(AppSecurityInputDTO inputDTO);

    /**
     * Updates the security anchor identified by {@code id} with
     * the given input payload.
     *
     * @param id       identifier of the anchor to update
     * @param inputDTO the update payload
     * @return the updated anchor, mapped to its output transfer representation
     */
    AppSecurityOutputDTO update(Long id, AppSecurityInputDTO inputDTO);

    /**
     * Deletes (logically) the security anchor identified by {@code id}.
     *
     * @param id identifier of the anchor to delete
     */
    void delete(Long id);
}

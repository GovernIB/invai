package es.caib.invai.back.service.facade.application.security.webContext;

import es.caib.invai.back.interna.application.security.webContext.DTO.AppWebContextInputDTO;
import es.caib.invai.back.interna.application.security.webContext.DTO.AppWebContextOutputDTO;
import es.caib.invai.back.persistence.repository.application.security.webContext.AppWebContextCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Domain Boundary Outbound Port interfacing the internal transactional domain operations.
 *
 * @since 1.0.4
 */
public interface AppWebContextService {

    /**
     * Retrieves a paginated sequence of web context links scoped to a single parent security anchor.
     *
     * @param appSecurityId mandatory parent security anchor identifier scoping the result set
     * @param criteria      the multi-parameter business query filter boundaries
     * @param pageable      pagination structural constraints
     * @return a paginated payload containing corresponding transfer representations
     */
    Page<AppWebContextOutputDTO> getAll(Long appSecurityId, AppWebContextCriteria criteria, Pageable pageable);

    /**
     * Creates a new application-web context link from the given input payload.
     *
     * @param inputDTO the creation payload
     * @return the created link, mapped to its output transfer representation
     */
    AppWebContextOutputDTO create(AppWebContextInputDTO inputDTO);

    /**
     * Updates the application-web context link identified by {@code id} with the given
     * input payload.
     *
     * @param id       identifier of the link to update
     * @param inputDTO the update payload
     * @return the updated link, mapped to its output transfer representation
     */
    AppWebContextOutputDTO update(Long id, AppWebContextInputDTO inputDTO);

    /**
     * Deletes (logically) the application-web context link identified by {@code id}.
     *
     * @param id identifier of the link to delete
     */
    void delete(Long id);
}

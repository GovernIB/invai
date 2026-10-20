package es.caib.invai.back.service.facade.application.development.webContext;

import es.caib.invai.back.interna.application.development.webContext.DTO.AppWebContextInputDTO;
import es.caib.invai.back.interna.application.development.webContext.DTO.AppWebContextOutputDTO;
import es.caib.invai.back.persistence.repository.application.security.webContext.AppWebContextCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Domain Boundary Outbound Port for Development's view of web context assignments: full CRUD,
 * the only side from which a web context link can be created, edited, or deleted. Security's view
 * (read-only listing plus the one-way validation action) lives in
 * {@code service.facade.application.security.webContext.AppWebContextService}.
 *
 * @since 1.0.5
 */
public interface AppWebContextService {

    /**
     * Retrieves a paginated sequence of web context links scoped to a single parent development anchor.
     *
     * @param appDevelopmentId mandatory parent development anchor identifier scoping the result set
     * @param criteria         the multi-parameter business query filter boundaries
     * @param pageable         pagination structural constraints
     * @return a paginated payload containing corresponding transfer representations
     */
    Page<AppWebContextOutputDTO> getAll(Long appDevelopmentId, AppWebContextCriteria criteria, Pageable pageable);

    /**
     * Creates a new application-web context link from the given input payload. Resolves both the
     * given development anchor and its sibling security anchor (same application) automatically.
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

package es.caib.invai.back.service.facade.application.security.webContext;

import es.caib.invai.back.interna.application.development.webContext.DTO.AppWebContextOutputDTO;
import es.caib.invai.back.interna.application.security.webContext.DTO.AppWebContextValidateInputDTO;
import es.caib.invai.back.persistence.repository.application.security.webContext.AppWebContextCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Domain Boundary Outbound Port interfacing the internal transactional domain operations exposed
 * to Security: a read-only listing plus the one-way validation action. Creating, editing, and
 * deleting web context assignments is only exposed from Development - see
 * {@code service.facade.application.development.webContext.AppWebContextService}.
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
     * Marks the application-web context link identified by {@code id} as validated. One-way:
     * refuses if the record is already validated.
     *
     * @param id       identifier of the link to validate
     * @param inputDTO payload carrying the mandatory free-text validation justification
     */
    void validate(Long id, AppWebContextValidateInputDTO inputDTO);
}

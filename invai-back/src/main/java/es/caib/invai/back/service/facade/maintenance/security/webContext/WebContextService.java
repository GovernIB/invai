package es.caib.invai.back.service.facade.maintenance.security.webContext;

import es.caib.invai.back.interna.maintenance.security.webContext.DTO.WebContextInputDTO;
import es.caib.invai.back.interna.maintenance.security.webContext.DTO.WebContextOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.security.webContext.WebContextCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting WebContexts.
 *
 * @since 1.0.4
 */
public interface WebContextService {

    /**
     * Retrieves an web context by its identifier.
     *
     * @param id the web context identifier
     * @return the matching web context as an output DTO
     */
    WebContextOutputDTO getById(Long id);

    /**
     * Retrieves a paginated list of web contexts matching the given filter criteria.
     *
     * @param filter search criteria used to narrow down results
     * @param pageable pagination and sorting instructions
     * @return a page of matching web contexts as output DTOs
     */
    Page<WebContextOutputDTO> getAll(WebContextCriteria filter, Pageable pageable);

    /**
     * Creates a new web context.
     *
     * @param inputDTO the data for the new web context
     * @return the created web context as an output DTO
     */
    WebContextOutputDTO create(WebContextInputDTO inputDTO);

    /**
     * Updates an existing web context.
     *
     * @param id the identifier of the web context to update
     * @param inputDTO the new data to apply
     * @return the updated web context as an output DTO
     */
    WebContextOutputDTO update(Long id, WebContextInputDTO inputDTO);

    /**
     * Logically deletes an web context by its identifier.
     *
     * @param id the identifier of the web context to delete
     */
    void delete(Long id);

    /**
     * Reactivates a previously deleted web context.
     *
     * @param id the identifier of the web context to reactivate
     * @return the reactivated web context as an output DTO
     */
    WebContextOutputDTO reactivate(Long id);
}

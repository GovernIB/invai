package es.caib.invai.back.service.facade.maintenance.development.technology;

import es.caib.invai.back.interna.maintenance.development.technology.DTO.TechnologyInputDTO;
import es.caib.invai.back.interna.maintenance.development.technology.DTO.TechnologyOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.development.technology.TechnologyCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting catalog Technologies.
 *
 * @since 1.0.2
 */
public interface TechnologyService {

    /**
     * Retrieves a single technology by its identifier.
     *
     * @param id the technology identifier
     * @return the matching technology as an output DTO
     */
    TechnologyOutputDTO getById(Long id);

    /**
     * Retrieves a paginated list of technologies matching the given filter criteria.
     *
     * @param filter   the search criteria to apply
     * @param pageable the pagination and sorting configuration
     * @return a page of matching technologies as output DTOs
     */
    Page<TechnologyOutputDTO> getAll(TechnologyCriteria filter, Pageable pageable);

    /**
     * Creates a new technology record.
     *
     * @param inputDTO the data for the technology to create
     * @return the created technology as an output DTO
     */
    TechnologyOutputDTO create(TechnologyInputDTO inputDTO);

    /**
     * Updates an existing technology record.
     *
     * @param id       the identifier of the technology to update
     * @param inputDTO the new data for the technology
     * @return the updated technology as an output DTO
     */
    TechnologyOutputDTO update(Long id, TechnologyInputDTO inputDTO);

    /**
     * Logically deletes a technology.
     *
     * @param id the identifier of the technology to delete
     */
    void delete(Long id);

    /**
     * Reactivates a previously logically-deleted technology.
     *
     * @param id the identifier of the technology to reactivate
     * @return the reactivated technology as an output DTO
     */
    TechnologyOutputDTO reactivate(Long id);
}

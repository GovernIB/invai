package es.caib.invai.back.service.facade.maintenance.integration.externalSystem;

import es.caib.invai.back.interna.maintenance.integration.externalSystem.DTO.ExternalSystemInputDTO;
import es.caib.invai.back.interna.maintenance.integration.externalSystem.DTO.ExternalSystemOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.integration.externalSystem.ExternalSystemCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting External Systems.
 *
 * @since 1.0.5
 */
public interface ExternalSystemService {

    /**
     * Retrieves an external system by its identifier.
     *
     * @param id the external system identifier
     * @return the matching external system as a response DTO
     */
    ExternalSystemOutputDTO getById(Long id);

    /**
     * Retrieves a paginated list of external systems matching the given filter criteria.
     *
     * @param filter   search and status filter criteria
     * @param pageable pagination and sorting information
     * @return a page of matching external systems as response DTOs
     */
    Page<ExternalSystemOutputDTO> getAll(ExternalSystemCriteria filter, Pageable pageable);

    /**
     * Creates a new external system record.
     *
     * @param inputDTO the external system data to create
     * @return the created external system as a response DTO
     */
    ExternalSystemOutputDTO create(ExternalSystemInputDTO inputDTO);

    /**
     * Updates an existing external system with the given input data.
     *
     * @param id       the identifier of the external system to update
     * @param inputDTO the new external system data
     * @return the updated external system as a response DTO
     */
    ExternalSystemOutputDTO update(Long id, ExternalSystemInputDTO inputDTO);

    /**
     * Logically deletes an external system by its identifier.
     *
     * @param id the identifier of the external system to delete
     */
    void delete(Long id);

    /**
     * Reactivates a previously deleted external system.
     *
     * @param id the identifier of the external system to reactivate
     * @return the reactivated external system as a response DTO
     */
    ExternalSystemOutputDTO reactivate(Long id);
}

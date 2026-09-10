package es.caib.invai.back.service.facade.maintenance.security.ensRequirement;

import es.caib.invai.back.interna.maintenance.security.ensRequirement.DTO.EnsRequirementInputDTO;
import es.caib.invai.back.interna.maintenance.security.ensRequirement.DTO.EnsRequirementOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.security.ensRequirement.EnsRequirementCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting EnsRequirements.
 *
 * @since 1.0.4
 */
public interface EnsRequirementService {

    /**
     * Retrieves an ENS requirement entry by its identifier.
     *
     * @param id the ENS requirement entry identifier
     * @return the matching ENS requirement entry as an output DTO
     */
    EnsRequirementOutputDTO getById(Long id);

    /**
     * Retrieves a paginated list of ENS requirement entries matching the given filter criteria.
     *
     * @param filter search criteria used to narrow down results
     * @param pageable pagination and sorting instructions
     * @return a page of matching ENS requirement entries as output DTOs
     */
    Page<EnsRequirementOutputDTO> getAll(EnsRequirementCriteria filter, Pageable pageable);

    /**
     * Creates a new ENS requirement entry.
     *
     * @param inputDTO the data for the new ENS requirement entry
     * @return the created ENS requirement entry as an output DTO
     */
    EnsRequirementOutputDTO create(EnsRequirementInputDTO inputDTO);

    /**
     * Updates an existing ENS requirement entry.
     *
     * @param id the identifier of the ENS requirement entry to update
     * @param inputDTO the new data to apply
     * @return the updated ENS requirement entry as an output DTO
     */
    EnsRequirementOutputDTO update(Long id, EnsRequirementInputDTO inputDTO);

    /**
     * Logically deletes an ENS requirement entry by its identifier.
     *
     * @param id the identifier of the ENS requirement entry to delete
     */
    void delete(Long id);

    /**
     * Reactivates a previously deleted ENS requirement entry.
     *
     * @param id the identifier of the ENS requirement entry to reactivate
     * @return the reactivated ENS requirement entry as an output DTO
     */
    EnsRequirementOutputDTO reactivate(Long id);
}

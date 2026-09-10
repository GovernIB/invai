package es.caib.invai.back.service.facade.maintenance.security.personalDataProcessing;

import es.caib.invai.back.interna.maintenance.security.personalDataProcessing.DTO.PersonalDataProcessingInputDTO;
import es.caib.invai.back.interna.maintenance.security.personalDataProcessing.DTO.PersonalDataProcessingOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.security.personalDataProcessing.PersonalDataProcessingCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting PersonalDataProcessings.
 *
 * @since 1.0.4
 */
public interface PersonalDataProcessingService {

    /**
     * Retrieves an personal data processing entry by its identifier.
     *
     * @param id the personal data processing entry identifier
     * @return the matching personal data processing entry as an output DTO
     */
    PersonalDataProcessingOutputDTO getById(Long id);

    /**
     * Retrieves a paginated list of personal data processing entries matching the given filter criteria.
     *
     * @param filter search criteria used to narrow down results
     * @param pageable pagination and sorting instructions
     * @return a page of matching personal data processing entries as output DTOs
     */
    Page<PersonalDataProcessingOutputDTO> getAll(PersonalDataProcessingCriteria filter, Pageable pageable);

    /**
     * Creates a new personal data processing entry.
     *
     * @param inputDTO the data for the new personal data processing entry
     * @return the created personal data processing entry as an output DTO
     */
    PersonalDataProcessingOutputDTO create(PersonalDataProcessingInputDTO inputDTO);

    /**
     * Updates an existing personal data processing entry.
     *
     * @param id the identifier of the personal data processing entry to update
     * @param inputDTO the new data to apply
     * @return the updated personal data processing entry as an output DTO
     */
    PersonalDataProcessingOutputDTO update(Long id, PersonalDataProcessingInputDTO inputDTO);

    /**
     * Logically deletes an personal data processing entry by its identifier.
     *
     * @param id the identifier of the personal data processing entry to delete
     */
    void delete(Long id);

    /**
     * Reactivates a previously deleted personal data processing entry.
     *
     * @param id the identifier of the personal data processing entry to reactivate
     * @return the reactivated personal data processing entry as an output DTO
     */
    PersonalDataProcessingOutputDTO reactivate(Long id);
}

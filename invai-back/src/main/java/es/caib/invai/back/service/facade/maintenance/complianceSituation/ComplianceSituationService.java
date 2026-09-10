package es.caib.invai.back.service.facade.maintenance.complianceSituation;

import es.caib.invai.back.interna.maintenance.complianceSituation.DTO.ComplianceSituationInputDTO;
import es.caib.invai.back.interna.maintenance.complianceSituation.DTO.ComplianceSituationOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.complianceSituation.ComplianceSituationCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting ComplianceSituations.
 *
 * @since 1.0.4
 */
public interface ComplianceSituationService {

    /**
     * Retrieves a compliance situation entry by its identifier.
     *
     * @param id the compliance situation entry identifier
     * @return the matching compliance situation entry as an output DTO
     */
    ComplianceSituationOutputDTO getById(Long id);

    /**
     * Retrieves a paginated list of compliance situation entries matching the given filter criteria.
     *
     * @param filter search criteria used to narrow down results
     * @param pageable pagination and sorting instructions
     * @return a page of matching compliance situation entries as output DTOs
     */
    Page<ComplianceSituationOutputDTO> getAll(ComplianceSituationCriteria filter, Pageable pageable);

    /**
     * Creates a new compliance situation entry.
     *
     * @param inputDTO the data for the new compliance situation entry
     * @return the created compliance situation entry as an output DTO
     */
    ComplianceSituationOutputDTO create(ComplianceSituationInputDTO inputDTO);

    /**
     * Updates an existing compliance situation entry.
     *
     * @param id the identifier of the compliance situation entry to update
     * @param inputDTO the new data to apply
     * @return the updated compliance situation entry as an output DTO
     */
    ComplianceSituationOutputDTO update(Long id, ComplianceSituationInputDTO inputDTO);

    /**
     * Logically deletes a compliance situation entry by its identifier.
     *
     * @param id the identifier of the compliance situation entry to delete
     */
    void delete(Long id);

    /**
     * Reactivates a previously deleted compliance situation entry.
     *
     * @param id the identifier of the compliance situation entry to reactivate
     * @return the reactivated compliance situation entry as an output DTO
     */
    ComplianceSituationOutputDTO reactivate(Long id);
}

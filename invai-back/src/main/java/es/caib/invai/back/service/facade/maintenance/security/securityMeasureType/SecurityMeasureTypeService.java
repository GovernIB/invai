package es.caib.invai.back.service.facade.maintenance.security.securityMeasureType;

import es.caib.invai.back.interna.maintenance.security.securityMeasureType.DTO.SecurityMeasureTypeInputDTO;
import es.caib.invai.back.interna.maintenance.security.securityMeasureType.DTO.SecurityMeasureTypeOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.security.securityMeasureType.SecurityMeasureTypeCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting SecurityMeasureTypes.
 *
 * @since 1.0.4
 */
public interface SecurityMeasureTypeService {

    /**
     * Retrieves a security measure type entry by its identifier.
     *
     * @param id the security measure type entry identifier
     * @return the matching security measure type entry as an output DTO
     */
    SecurityMeasureTypeOutputDTO getById(Long id);

    /**
     * Retrieves a paginated list of security measure type entries matching the given filter criteria.
     *
     * @param filter search criteria used to narrow down results
     * @param pageable pagination and sorting instructions
     * @return a page of matching security measure type entries as output DTOs
     */
    Page<SecurityMeasureTypeOutputDTO> getAll(SecurityMeasureTypeCriteria filter, Pageable pageable);

    /**
     * Creates a new security measure type entry.
     *
     * @param inputDTO the data for the new security measure type entry
     * @return the created security measure type entry as an output DTO
     */
    SecurityMeasureTypeOutputDTO create(SecurityMeasureTypeInputDTO inputDTO);

    /**
     * Updates an existing security measure type entry.
     *
     * @param id the identifier of the security measure type entry to update
     * @param inputDTO the new data to apply
     * @return the updated security measure type entry as an output DTO
     */
    SecurityMeasureTypeOutputDTO update(Long id, SecurityMeasureTypeInputDTO inputDTO);

    /**
     * Logically deletes a security measure type entry by its identifier.
     *
     * @param id the identifier of the security measure type entry to delete
     */
    void delete(Long id);

    /**
     * Reactivates a previously deleted security measure type entry.
     *
     * @param id the identifier of the security measure type entry to reactivate
     * @return the reactivated security measure type entry as an output DTO
     */
    SecurityMeasureTypeOutputDTO reactivate(Long id);
}

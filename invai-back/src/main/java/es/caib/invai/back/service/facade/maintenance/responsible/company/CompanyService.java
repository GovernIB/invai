package es.caib.invai.back.service.facade.maintenance.responsible.company;

import es.caib.invai.back.interna.maintenance.responsible.company.DTO.CompanyInputDTO;
import es.caib.invai.back.interna.maintenance.responsible.company.DTO.CompanyOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.responsible.company.CompanyCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting Companies.
 *
 * @since 1.0.3
 */
public interface CompanyService {

    /**
     * Retrieves a company by its identifier.
     *
     * @param id the company identifier
     * @return the matching company as a response DTO
     */
    CompanyOutputDTO getById(Long id);

    /**
     * Retrieves a paginated list of companies matching the given filter criteria.
     *
     * @param filter   search and status filter criteria
     * @param pageable pagination and sorting information
     * @return a page of matching companies as response DTOs
     */
    Page<CompanyOutputDTO> getAll(CompanyCriteria filter, Pageable pageable);

    /**
     * Creates a new company record.
     *
     * @param inputDTO the company data to create
     * @return the created company as a response DTO
     */
    CompanyOutputDTO create(CompanyInputDTO inputDTO);

    /**
     * Updates an existing company with the given input data.
     *
     * @param id       the identifier of the company to update
     * @param inputDTO the new company data
     * @return the updated company as a response DTO
     */
    CompanyOutputDTO update(Long id, CompanyInputDTO inputDTO);

    /**
     * Logically deletes a company by its identifier.
     *
     * @param id the identifier of the company to delete
     */
    void delete(Long id);

    /**
     * Reactivates a previously deleted company.
     *
     * @param id the identifier of the company to reactivate
     * @return the reactivated company as a response DTO
     */
    CompanyOutputDTO reactivate(Long id);
}

package es.caib.invai.back.persistence.repository.maintenance.responsible.company;

import es.caib.invai.back.service.model.maintenance.responsible.company.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Core business domain outbound Port boundary interface declaring relational persistence mechanisms
 * for the Company catalog layer.
 *
 * @since 1.0.3
 */
public interface CompanyRepository {

    /**
     * Finds a company by its identifier.
     *
     * @param id the company identifier
     * @return the matching company, or {@code null} if none is found
     */
    Company findById(Long id);

    /**
     * Finds companies matching the given filter criteria, paginated.
     *
     * @param filter   search and status filter criteria
     * @param pageable pagination and sorting information
     * @return a page of matching companies
     */
    Page<Company> findAll(CompanyCriteria filter, Pageable pageable);

    /**
     * Persists a new company.
     *
     * @param company the company to create
     * @return the persisted company
     */
    Company create(Company company);

    /**
     * Persists changes to an existing company.
     *
     * @param company the company data to persist
     * @param id      the identifier of the company to update
     * @return the updated company
     */
    Company update(Company company, Long id);

    /**
     * Logically deletes the given company.
     *
     * @param company the company to delete
     */
    void delete(Company company);

    /**
     * Checks whether an active (non-deleted) company exists with the given name.
     *
     * @param name the company name to check
     * @return {@code true} if a matching non-deleted company exists
     */
    boolean existsByNameAndDeletedAtIsNull(String name);

    /**
     * Checks whether an active (non-deleted) company exists with the given name and a different identifier.
     *
     * @param name the company name to check
     * @param id   the identifier to exclude from the check
     * @return {@code true} if a matching non-deleted company exists with a different ID
     */
    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id);
}

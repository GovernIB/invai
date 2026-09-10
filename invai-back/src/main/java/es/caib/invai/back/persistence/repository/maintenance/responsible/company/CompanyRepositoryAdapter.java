package es.caib.invai.back.persistence.repository.maintenance.responsible.company;

import es.caib.invai.back.service.model.maintenance.responsible.company.Company;
import es.caib.invai.back.persistence.model.maintenance.responsible.company.CompanyEntity;
import es.caib.invai.back.persistence.model.maintenance.responsible.company.CompanyAudEntity;
import es.caib.invai.back.service.mapper.maintenance.responsible.company.CompanyMapper;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Infrastructure repository Adapter implementing the outbound port boundary {@link CompanyRepository}.
 *
 * @since 1.0.3
 */
@Repository
@Slf4j
public class CompanyRepositoryAdapter implements CompanyRepository {

    /** Spring Data JPA repository used to access live {@link CompanyEntity} records. */
    @Autowired
    private CompanyJPARepository companyJPARepository;

    /** Spring Data JPA repository used to persist {@link CompanyAudEntity} audit snapshots. */
    @Autowired
    private CompanyAudJPARepository companyAudJPARepository;

    /** Mapper used to convert between Company domain models and JPA entities. */
    @Autowired
    private CompanyMapper companyMapper;

    /**
     * Finds a company by its identifier.
     *
     * @param id the company identifier
     * @return the matching company, or {@code null} if none is found
     */
    @Override
    public Company findById(Long id) {
        return companyJPARepository.findById(id)
                .map(companyMapper::toModel)
                .orElse(null);
    }

    /**
     * Finds companies matching the given filter criteria, paginated.
     *
     * @param filter   search and status filter criteria
     * @param pageable pagination and sorting information
     * @return a page of matching companies
     */
    @Override
    public Page<Company> findAll(CompanyCriteria filter, Pageable pageable) {
        Specification<CompanyEntity> spec = CompanySpecification.filterByCriteria(filter);
        return companyJPARepository.findAll(spec, pageable).map(companyMapper::toModel);
    }

    /**
     * Checks whether an active (non-deleted) company exists with the given name.
     *
     * @param name the company name to check
     * @return {@code true} if a matching non-deleted company exists
     */
    @Override
    public boolean existsByNameAndDeletedAtIsNull(String name) {
        return companyJPARepository.existsByNameAndDeletedAtIsNull(name);
    }

    /**
     * Checks whether an active (non-deleted) company exists with the given name and a different identifier.
     *
     * @param name the company name to check
     * @param id   the identifier to exclude from the check
     * @return {@code true} if a matching non-deleted company exists with a different ID
     */
    @Override
    public boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id) {
        return companyJPARepository.existsByNameAndIdNotAndDeletedAtIsNull(name, id);
    }

    /**
     * Persists a new company and records an {@code INSERT} audit snapshot.
     *
     * @param company the company to create
     * @return the persisted company
     */
    @Override
    public Company create(Company company) {
        log.info("Repository: Persisting new company entity into database");
        CompanyEntity entity = companyMapper.toEntity(company);
        entity = companyJPARepository.save(entity);

        saveAuditRecord(entity, "INSERT");

        return companyMapper.toModel(entity);
    }

    /**
     * Persists changes to an existing company and records an {@code UPDATE} audit snapshot.
     *
     * @param company the company data to persist
     * @param id      the identifier of the company to update
     * @return the updated company
     */
    @Override
    public Company update(Company company, Long id) {
        log.info("Repository: Merging changes into existing company record for ID: {}", id);
        CompanyEntity entity = companyMapper.toEntity(company);
        entity.setId(id);
        entity = companyJPARepository.save(entity);

        saveAuditRecord(entity, "UPDATE");

        return companyMapper.toModel(entity);
    }

    /**
     * Logically deletes the given company and records a {@code DELETE} audit snapshot.
     *
     * @param company the company to delete
     */
    @Override
    public void delete(Company company) {
        log.info("Repository: Soft deleting company entity with ID: {}", company.getId());
        CompanyEntity entity = companyMapper.toEntity(company);
        entity.setId(company.getId());
        entity = companyJPARepository.save(entity);

        saveAuditRecord(entity, "DELETE");
    }

    /**
     * Builds and persists an audit snapshot capturing the current state of the given entity.
     *
     * @param entity the company entity state to snapshot
     * @param action the type of mutation being audited (e.g. {@code INSERT}, {@code UPDATE}, {@code DELETE})
     */
    private void saveAuditRecord(CompanyEntity entity, String action) {
        CompanyAudEntity aud = new CompanyAudEntity();

        aud.setCompanyId(entity.getId());
        aud.setName(entity.getName());

        aud.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now());
        aud.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy() : Utils.resolveCurrentUsername());
        aud.setUpdatedAt(entity.getUpdatedAt());
        aud.setUpdatedBy(entity.getUpdatedBy());
        aud.setDeletedAt(entity.getDeletedAt());
        aud.setDeletedBy(entity.getDeletedBy());

        aud.setAudAction(action);
        aud.setAuditDate(LocalDateTime.now());
        aud.setAuditUser(Utils.resolveCurrentUsername());

        companyAudJPARepository.save(aud);
    }
}

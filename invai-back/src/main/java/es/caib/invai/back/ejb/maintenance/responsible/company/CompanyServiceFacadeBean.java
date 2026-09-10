package es.caib.invai.back.ejb.maintenance.responsible.company;

import es.caib.invai.back.interna.maintenance.responsible.company.DTO.CompanyInputDTO;
import es.caib.invai.back.interna.maintenance.responsible.company.DTO.CompanyOutputDTO;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.authorized.AppAuthorizedRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.responsible.AppResponsibleRepository;
import es.caib.invai.back.persistence.repository.maintenance.responsible.person.PersonRepository;
import es.caib.invai.back.service.mapper.maintenance.responsible.company.CompanyMapper;
import es.caib.invai.back.persistence.repository.maintenance.responsible.company.CompanyCriteria;
import es.caib.invai.back.persistence.repository.maintenance.responsible.company.CompanyRepository;
import es.caib.invai.back.service.facade.maintenance.responsible.company.CompanyService;
import es.caib.invai.back.service.model.application.responsibleAuthorized.authorized.AppAuthorized;
import es.caib.invai.back.service.model.application.responsibleAuthorized.responsible.AppResponsible;
import es.caib.invai.back.service.model.maintenance.responsible.company.Company;
import es.caib.invai.back.service.model.maintenance.responsible.person.Person;
import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.utils.Constants;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Facade service implementation for administrative Companies.
 *
 * @since 1.0.3
 */
@Service
@Slf4j
@Transactional
public class CompanyServiceFacadeBean implements CompanyService {

    /** Mapper used to convert between Company domain models, entities, and DTOs. */
    @Autowired
    private CompanyMapper companyMapper;

    /** Repository port used to persist and query Company domain models. */
    @Autowired
    private CompanyRepository companyRepository;

    /** Repository used to resolve and cascade-delete the company's own persons on delete. */
    @Autowired
    private PersonRepository personRepository;

    /** Repository used to check for/cascade-close active responsibilities held by the company's persons. */
    @Autowired
    private AppResponsibleRepository appResponsibleRepository;

    /** Repository used to check for/cascade-close active authorizations held by the company's persons. */
    @Autowired
    private AppAuthorizedRepository appAuthorizedRepository;

    /**
     * Retrieves a company by its identifier.
     *
     * @param id the company identifier
     * @return the matching company as a response DTO
     * @throws BusinessRuleException if no company exists with the given ID
     */
    @Override
    @Transactional(readOnly = true)
    public CompanyOutputDTO getById(Long id) {
        log.debug("Facade: Fetching company by ID: {}", id);
        Company company = companyRepository.findById(id);

        if (company == null) {
            throw new BusinessRuleException(Constants.ERR_COMPANY_NOT_FOUND);
        }

        return companyMapper.toResponse(company);
    }

    /**
     * Retrieves a paginated list of companies matching the given filter criteria.
     *
     * @param filter   search and status filter criteria
     * @param pageable pagination and sorting information
     * @return a page of matching companies as response DTOs
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CompanyOutputDTO> getAll(CompanyCriteria filter, Pageable pageable) {
        log.debug("Facade: Fetching companies via pagination boundaries");
        Page<Company> domainPage = companyRepository.findAll(filter, pageable);
        return domainPage.map(companyMapper::toResponse);
    }

    /**
     * Creates a new company record after sanitizing the input and checking for name duplicates.
     *
     * @param inputDTO the company data to create
     * @return the created company as a response DTO
     * @throws BusinessRuleException if a non-deleted company with the same name already exists
     */
    @Override
    public CompanyOutputDTO create(CompanyInputDTO inputDTO) {
        log.info("Facade: Creating new company record with name: {}", inputDTO.getName());

        Utils.sanitize(inputDTO);

        if (companyRepository.existsByNameAndDeletedAtIsNull(inputDTO.getName())) {
            throw new BusinessRuleException(Constants.ERR_COMPANY_DUPLICATED);
        }

        Company model = companyMapper.toModelFromInput(inputDTO);

        Company savedModel = companyRepository.create(model);
        return companyMapper.toResponse(savedModel);
    }

    /**
     * Updates an existing company with the given input data.
     *
     * @param id       the identifier of the company to update
     * @param inputDTO the new company data
     * @return the updated company as a response DTO
     * @throws BusinessRuleException if the company does not exist or the new name is already used by another non-deleted company
     */
    @Override
    public CompanyOutputDTO update(Long id, CompanyInputDTO inputDTO) {
        log.info("Facade: Updating company with ID: {}", id);

        Company existing = companyRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_COMPANY_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        if (companyRepository.existsByNameAndIdNotAndDeletedAtIsNull(inputDTO.getName(), id)) {
            throw new BusinessRuleException(Constants.ERR_COMPANY_DUPLICATED);
        }

        companyMapper.updateModelFromInput(inputDTO, existing);
        return companyMapper.toResponse(companyRepository.update(existing, id));
    }

    /**
     * Logically deletes a company by setting its deletion timestamp and author, after verifying
     * none of its persons hold a responsibility/authorization that this deletion would leave
     * dangling, then cascades the deletion onto those persons themselves.
     * <p>
     * A company cannot be deleted while any of its persons holds an active {@code AppResponsible}
     * assignment on a still-active application (no exception). It also cannot be deleted while any
     * of its persons holds an active {@code AppAuthorized} assignment on a still-active application
     * unless another person, external to this company (a different company, or Personal CAIB),
     * also holds an active authorization on that same application. Rows tied to an already-inactive
     * application never block deletion. Once these checks pass, every such row still active on the
     * company's persons (i.e. only ones tied to an inactive application or backed by an external
     * person) is closed out, and the persons themselves are soft-deleted, before the company itself
     * is soft-deleted.
     * </p>
     *
     * @param id the identifier of the company to delete
     * @throws BusinessRuleException if the company does not exist, is already deleted, or one of
     * its persons holds a blocking responsibility/authorization
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Logically deleting company with ID: {}", id);
        Company existing = companyRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_COMPANY_NOT_FOUND);
        }

        if (existing.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_COMPANY_NOT_ACTIVE);
        }

        List<Person> persons = personRepository.findAllActiveByCompanyId(id);
        assertNoBlockingResponsibilities(persons);
        assertNoBlockingAuthorizations(id, persons);
        cascadeDeletePersons(persons);

        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy(Utils.resolveCurrentUsername());

        companyRepository.delete(existing);
    }

    /**
     * Rule: a company cannot be deleted while any of its persons holds an active
     * {@code AppResponsible} assignment whose parent application is itself still active. No
     * exception — unlike authorizations, there is no notion of an "external backup" responsible.
     *
     * @param persons the company's active persons
     * @throws BusinessRuleException if any such assignment is found
     */
    private void assertNoBlockingResponsibilities(List<Person> persons) {
        for (Person person : persons) {
            for (AppResponsible responsible : appResponsibleRepository.findAllActiveByPersonId(person.getId())) {
                if (responsible.getAppResponsibleAuthorized().getDeletedAt() == null) {
                    throw new BusinessRuleException(Constants.ERR_COMPANY_HAS_RESPONSIBLE);
                }
            }
        }
    }

    /**
     * Rule: a company cannot be deleted while any of its persons holds an active
     * {@code AppAuthorized} assignment whose parent application is itself still active, unless
     * another active authorization on that same application belongs to a person external to this
     * company (a different company, or Personal CAIB).
     *
     * @param companyId the identifier of the company being deleted
     * @param persons the company's active persons
     * @throws BusinessRuleException if any such assignment has no external backup
     */
    private void assertNoBlockingAuthorizations(Long companyId, List<Person> persons) {
        for (Person person : persons) {
            for (AppAuthorized authorized : appAuthorizedRepository.findAllActiveByPersonId(person.getId())) {
                if (authorized.getAppResponsibleAuthorized().getDeletedAt() != null) {
                    continue;
                }
                boolean hasExternalBackup = appAuthorizedRepository
                        .findAllActiveByAppResponsibleAuthorized(authorized.getAppResponsibleAuthorized().getId()).stream()
                        .filter(other -> !other.getId().equals(authorized.getId()))
                        .anyMatch(other -> other.getPerson().isPersonalCaib()
                                || other.getPerson().getCompany() == null
                                || !companyId.equals(other.getPerson().getCompany().getId()));
                if (!hasExternalBackup) {
                    throw new BusinessRuleException(Constants.ERR_COMPANY_HAS_AUTHORIZED);
                }
            }
        }
    }

    /**
     * Cascades the company deletion onto its persons: closes out every remaining active
     * {@code AppResponsible}/{@code AppAuthorized} row still held by them (safe to do so, having
     * already passed the blocking checks above), then soft-deletes each person.
     *
     * @param persons the company's active persons to cascade-delete
     */
    private void cascadeDeletePersons(List<Person> persons) {
        LocalDateTime now = LocalDateTime.now();
        String currentUser = Utils.resolveCurrentUsername();

        for (Person person : persons) {
            for (AppResponsible responsible : appResponsibleRepository.findAllActiveByPersonId(person.getId())) {
                responsible.setDeletedAt(now);
                responsible.setDeletedBy(currentUser);
                appResponsibleRepository.delete(responsible);
            }
            for (AppAuthorized authorized : appAuthorizedRepository.findAllActiveByPersonId(person.getId())) {
                authorized.setDeletedAt(now);
                authorized.setDeletedBy(currentUser);
                appAuthorizedRepository.delete(authorized);
            }
            person.setDeletedAt(now);
            person.setDeletedBy(currentUser);
            personRepository.delete(person);
        }
    }

    /**
     * Reactivates a previously deleted company by clearing its deletion timestamp and author.
     *
     * @param id the identifier of the company to reactivate
     * @return the reactivated company as a response DTO
     * @throws BusinessRuleException if the company does not exist or is not currently deleted
     */
    @Override
    public CompanyOutputDTO reactivate(Long id) {
        log.info("Facade: Reactivating company with ID: {}", id);
        Company existing = companyRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_COMPANY_NOT_FOUND);
        }

        if (existing.getDeletedAt() == null) {
            throw new BusinessRuleException(Constants.ERR_COMPANY_ACTIVE);
        }

        existing.setDeletedAt(null);
        existing.setDeletedBy(null);

        Company updatedModel = companyRepository.update(existing, id);
        return companyMapper.toResponse(updatedModel);
    }
}

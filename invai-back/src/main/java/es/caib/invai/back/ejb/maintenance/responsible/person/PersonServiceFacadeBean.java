package es.caib.invai.back.ejb.maintenance.responsible.person;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.maintenance.responsible.person.DTO.PersonCombinedSearchOutputDTO;
import es.caib.invai.back.interna.maintenance.responsible.person.DTO.PersonInputDTO;
import es.caib.invai.back.interna.maintenance.responsible.person.DTO.PersonOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.responsible.person.PersonCriteria;
import es.caib.invai.back.persistence.repository.maintenance.responsible.person.PersonRepository;
import es.caib.invai.back.rest.soffid.SoffidClient;
import es.caib.invai.back.rest.soffid.SoffidUser;
import es.caib.invai.back.service.facade.maintenance.responsible.person.PersonService;
import es.caib.invai.back.service.mapper.maintenance.responsible.person.PersonMapper;
import es.caib.invai.back.service.model.maintenance.responsible.company.Company;
import es.caib.invai.back.service.model.maintenance.responsible.person.Person;
import es.caib.invai.back.utils.Constants;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * Facade service implementation for managing Person catalog core configurations.
 * Enforces transactional safety bounds and verifies logical deletion metrics.
 *
 * @since 1.0.3
 */
@Service
@Slf4j
@Transactional
public class PersonServiceFacadeBean implements PersonService {

    /** Mapper used to convert between Person domain models and their DTO representations. */
    @Autowired
    private PersonMapper personMapper;

    /** Repository providing persistence operations for Person records. */
    @Autowired
    private PersonRepository personRepository;

    /** Port used to list/search CAIB personnel against the Soffid SCIM API. */
    @Autowired
    private SoffidClient soffidClient;

    /**
     * Retrieves a person by its unique database identifier.
     *
     * @param id the unique person record identifier
     * @return the mapped {@link PersonOutputDTO} response payload
     * @throws BusinessRuleException if no person matches the given identifier
     */
    @Override
    @Transactional(readOnly = true)
    public PersonOutputDTO getById(Long id) {
        log.debug("Facade: Fetching person by ID: {}", id);
        Person person = personRepository.findById(id);

        if (person == null) {
            throw new BusinessRuleException(Constants.ERR_PERSON_NOT_FOUND);
        }

        return personMapper.toResponse(person);
    }

    /**
     * Retrieves a paginated list of persons matching the given filter criteria.
     *
     * @param filter   the search criteria used to narrow the results
     * @param pageable the pagination and sorting parameters
     * @return a page of mapped {@link PersonOutputDTO} results
     */
    @Override
    @Transactional(readOnly = true)
    public Page<PersonOutputDTO> getAll(PersonCriteria filter, Pageable pageable) {
        log.debug("Facade: Fetching persons via pagination boundaries");
        Page<Person> domainPage = personRepository.findAll(filter, pageable);
        return domainPage.map(personMapper::toResponse);
    }

    /**
     * Validates and creates a new person record.
     * Sanitizes the input, enforces the company requirement for non-CAIB persons, and checks email uniqueness.
     *
     * @param inputDTO the data used to create the new person record
     * @return the persisted person mapped into a {@link PersonOutputDTO}
     * @throws BusinessRuleException if the person is not CAIB personnel and no company is provided,
     * or if the email is already registered to another person
     */
    @Override
    public PersonOutputDTO create(PersonInputDTO inputDTO) {
        log.info("Facade: Creating new person record with email: {}", inputDTO.getEmail());

        Utils.sanitize(inputDTO);

        if (!inputDTO.isPersonalCaib() && inputDTO.getCompanyId() == null) {
            throw new BusinessRuleException(Constants.ERR_PERSON_COMPANY_REQUIRED_WHEN_NOT_CAIB);
        }

        if (personRepository.existsByEmail(inputDTO.getEmail())) {
            throw new BusinessRuleException(Constants.ERR_PERSON_DUPLICATED);
        }

        Person model = personMapper.toModelFromInput(inputDTO);
        Person savedModel = personRepository.create(model);
        return personMapper.toResponse(savedModel);
    }

    /**
     * Validates and updates an existing person record with the given input.
     *
     * @param id       the identifier of the person to update
     * @param inputDTO the data used to update the person record
     * @return the updated person mapped into a {@link PersonOutputDTO}
     * @throws BusinessRuleException if the person does not exist, is not CAIB personnel and no company is
     * provided, or the email is already owned by another person
     */
    @Override
    public PersonOutputDTO update(Long id, PersonInputDTO inputDTO) {
        log.info("Facade: Updating person with ID: {}", id);

        Person existing = personRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_PERSON_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        if (!inputDTO.isPersonalCaib() && inputDTO.getCompanyId() == null) {
            throw new BusinessRuleException(Constants.ERR_PERSON_COMPANY_REQUIRED_WHEN_NOT_CAIB);
        }

        if (personRepository.existsByEmailAndIdNot(inputDTO.getEmail(), id)) {
            throw new BusinessRuleException(Constants.ERR_PERSON_OWNED_BY_OTHER);
        }

        personMapper.updateModelFromInput(inputDTO, existing);
        return personMapper.toResponse(personRepository.update(existing, id));
    }

    /**
     * Logically deletes (soft-deletes) an active person record.
     *
     * @param id the identifier of the person to delete
     * @throws BusinessRuleException if the person does not exist or is already deleted
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Logically deleting person with ID: {}", id);
        Person existing = personRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_PERSON_NOT_FOUND);
        }

        if (existing.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_PERSON_NOT_ACTIVE);
        }

        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy(Utils.resolveCurrentUsername());

        personRepository.delete(existing);
    }

    /**
     * Reactivates a logically deleted person record back to active state.
     *
     * @param id the identifier of the person to reactivate
     * @return the reactivated person mapped into a {@link PersonOutputDTO}
     * @throws BusinessRuleException if the person does not exist or is already active
     */
    @Override
    public PersonOutputDTO reactivate(Long id) {
        log.info("Facade: Reactivating person with ID: {}", id);
        Person existing = personRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_PERSON_NOT_FOUND);
        }

        if (existing.getDeletedAt() == null) {
            throw new BusinessRuleException(Constants.ERR_PERSON_ACTIVE);
        }

        existing.setDeletedAt(null);
        existing.setDeletedBy(null);

        return personMapper.toResponse(personRepository.update(existing, id));
    }

    /**
     * Lists CAIB personnel against the Soffid SCIM API, to feed both the Person "getAll" style
     * listing and the "assign a responsible/authorized person" typeahead. Does not touch the local
     * {@code Person} catalog: results are unpersisted Soffid candidates, mapped into
     * {@link PersonOutputDTO} with {@code id}, {@code company} and {@code deletedAt} left
     * {@code null} (no local row exists yet) and {@code personalCaib} set to {@code true} (Soffid
     * only surfaces CAIB staff) - the same "null id means not yet cached locally" convention
     * already used elsewhere in this codebase for external-source search results. A blank/absent
     * {@code fullName} lists every active Soffid user, still bounded by {@code pageable} - this
     * pagination is what keeps that unfiltered case from returning Soffid's entire ~85k-user
     * directory in one response.
     *
     * @param fullName the text to search for, matched (word by word) against the full name, or
     * {@code null}/blank to list every active Soffid user
     * @param pageable the pagination parameters
     * @return the requested page of matching Soffid candidates, mapped into {@link PersonOutputDTO}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<PersonOutputDTO> searchSoffid(String fullName, Pageable pageable) {
        log.debug("Facade: Listing/searching Soffid personnel (fullName='{}')", fullName);

        return soffidClient.search(fullName, pageable).map(this::toSoffidSearchResult);
    }

    @Override
    @Transactional(readOnly = true)
    public PersonCombinedSearchOutputDTO searchCombined(String search, Pageable pageable) {
        log.debug("Facade: Combined local/Soffid person search (search='{}')", search);

        if (StringUtils.isBlank(search)) {
            CompletableFuture<Page<PersonOutputDTO>> soffidFuture = CompletableFuture.supplyAsync(
                    () -> soffidClient.search(null, pageable).map(this::toSoffidSearchResult));
            Page<PersonOutputDTO> database = personRepository.findAll(new PersonCriteria(), pageable).map(personMapper::toResponse);
            return new PersonCombinedSearchOutputDTO(database, Utils.join(soffidFuture));
        }

        PersonCriteria criteria = new PersonCriteria();
        criteria.setSearch(search);
        Page<Person> localMatches = personRepository.findAll(criteria, pageable);
        if (localMatches.hasContent()) {
            return new PersonCombinedSearchOutputDTO(localMatches.map(personMapper::toResponse), Page.empty(pageable));
        }

        Page<PersonOutputDTO> soffid = soffidClient.search(search, pageable).map(this::toSoffidSearchResult);
        return new PersonCombinedSearchOutputDTO(Page.empty(pageable), soffid);
    }

    /**
     * Maps a raw Soffid SCIM user into the outbound {@link PersonOutputDTO} shape submitted back by
     * the frontend when the user picks a candidate. {@code id}, {@code company} and
     * {@code deletedAt} are left {@code null} since no local {@code Person} row exists yet;
     * {@code personalCaib} is set to {@code true} since Soffid only surfaces CAIB staff.
     *
     * @param soffidUser the raw Soffid SCIM user to map
     * @return the mapped {@link PersonOutputDTO}
     */
    private PersonOutputDTO toSoffidSearchResult(SoffidUser soffidUser) {
        PersonOutputDTO dto = new PersonOutputDTO();
        dto.setFirstName(soffidUser.getFirstName());
        dto.setLastName(soffidUser.getLastName());
        dto.setEmail(soffidUser.getEmailAddress());
        dto.setPersonalCaib(true);
        return dto;
    }

    @Override
    public Person resolveOrCreatePerson(String firstName, String lastName, String email, boolean personalCaib, Long companyId) {
        Person existing = personRepository.findByEmail(email);
        if (existing != null) {
            return existing;
        }
        if (!personalCaib && companyId == null) {
            throw new BusinessRuleException(Constants.ERR_PERSON_COMPANY_REQUIRED_WHEN_NOT_CAIB);
        }
        Person newPerson = new Person();
        newPerson.setFirstName(firstName);
        newPerson.setLastName(lastName);
        newPerson.setEmail(email);
        newPerson.setPersonalCaib(personalCaib);
        if (!personalCaib) {
            Company company = new Company();
            company.setId(companyId);
            newPerson.setCompany(company);
        }
        return personRepository.create(newPerson);
    }

    @Override
    public void syncPersonalCaib(Long personId, boolean personalCaib) {
        Person person = personRepository.findById(personId);
        if (person == null || person.isPersonalCaib() == personalCaib) {
            return;
        }
        if (!personalCaib && person.getCompany() == null) {
            throw new BusinessRuleException(Constants.ERR_PERSON_COMPANY_REQUIRED_WHEN_NOT_CAIB);
        }
        person.setPersonalCaib(personalCaib);
        personRepository.update(person, personId);
    }
}

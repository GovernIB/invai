package es.caib.invai.back.ejb.maintenance.responsible.person;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.maintenance.responsible.person.DTO.PersonInputDTO;
import es.caib.invai.back.interna.maintenance.responsible.person.DTO.PersonOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.responsible.person.PersonCriteria;
import es.caib.invai.back.persistence.repository.maintenance.responsible.person.PersonRepository;
import es.caib.invai.back.service.facade.maintenance.responsible.person.PersonService;
import es.caib.invai.back.service.mapper.maintenance.responsible.person.PersonMapper;
import es.caib.invai.back.service.model.maintenance.responsible.person.Person;
import es.caib.invai.back.utils.Constants;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
        log.info("Facade: Fetching person by ID: {}", id);
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
        log.info("Facade: Fetching persons via pagination boundaries");
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
}

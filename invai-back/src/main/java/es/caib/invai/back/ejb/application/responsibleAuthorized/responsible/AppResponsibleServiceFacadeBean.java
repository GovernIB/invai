package es.caib.invai.back.ejb.application.responsibleAuthorized.responsible;

import es.caib.invai.back.interna.application.responsibleAuthorized.responsible.DTO.AppResponsibleDeleteDTO;
import es.caib.invai.back.interna.application.responsibleAuthorized.responsible.DTO.AppResponsibleInputDTO;
import es.caib.invai.back.interna.application.responsibleAuthorized.responsible.DTO.AppResponsibleOutputDTO;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.responsible.AppResponsibleCriteria;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.responsible.AppResponsibleRepository;
import es.caib.invai.back.persistence.repository.catalog.responsibleType.ResponsibleTypeRepository;
import es.caib.invai.back.persistence.repository.maintenance.responsible.person.PersonRepository;
import es.caib.invai.back.service.mapper.application.responsibleAuthorized.responsible.AppResponsibleMapper;
import es.caib.invai.back.service.facade.application.responsibleAuthorized.responsible.AppResponsibleService;
import es.caib.invai.back.service.model.application.responsibleAuthorized.responsible.AppResponsible;
import es.caib.invai.back.service.model.catalog.responsibleType.ResponsibleType;
import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import es.caib.invai.back.service.model.maintenance.responsible.company.Company;
import es.caib.invai.back.service.model.maintenance.responsible.person.Person;
import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.utils.Constants;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Facade service implementation for managing person-to-responsible-type assignments on the
 * "Responsables" tab of an application. Enforces the business rule that at most one active
 * person may hold a given responsible type per anchor: on create, any existing active holder of
 * the requested responsible type is automatically deactivated in favor of the new one, and on
 * update the responsible type itself is immutable (only the person/job title can change),
 * eliminating the possibility of a duplicate. Reactivate still re-checks the rule, since another
 * assignment may have taken over the slot while this one was deactivated.
 * <p>
 * The default listing ({@code getAll} with no status filter, or an active-status filter) is
 * driven by the full {@code ResponsibleType} catalog rather than by persisted assignment rows:
 * every registered responsible type is always returned, carrying the currently active assignment
 * for that type on the given anchor when one exists, or an empty placeholder (no id, no person)
 * when no one currently holds it. Requesting the inactive status explicitly falls back to the
 * plain persisted-row listing, so historical/deactivated assignments remain browsable for
 * reactivation.
 * </p>
 * <p>
 * Create and update also keep the linked person's {@code isPersonalCaib} flag in sync with the
 * value submitted alongside the assignment (see {@link #syncPersonalCaib}). On create,
 * {@code personId} is itself optional: when absent, {@link #resolvePersonId} resolves an existing
 * active person by e-mail, or creates a new one, from {@code personFirstName}/{@code personLastName}/
 * {@code personEmail} (and {@code companyId} when not Personal CAIB) — this is what lets a CAIB
 * person sourced from Soffid, who has no local {@code Person} row yet, be assigned without a
 * pre-existing {@code personId}.
 * </p>
 *
 * @since 1.0.3
 */
@Service
@Slf4j
@Transactional
public class AppResponsibleServiceFacadeBean implements AppResponsibleService {

    /** Mapper converting between AppResponsible domain models, entities, and DTOs. */
    @Autowired
    private AppResponsibleMapper appResponsibleMapper;
    /** Repository port used to access AppResponsible persistence operations. */
    @Autowired
    private AppResponsibleRepository appResponsibleRepository;
    /** Repository providing the full responsible type catalog driving the default listing. */
    @Autowired
    private ResponsibleTypeRepository responsibleTypeRepository;
    /** Repository used to keep the linked person's {@code isPersonalCaib} flag in sync. */
    @Autowired
    private PersonRepository personRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AppResponsibleOutputDTO> getAll(Long appResponsibleAuthorizedId, AppResponsibleCriteria criteria, Pageable pageable) {
        if (criteria != null && StatusEnum.INACTIVE.getId().equals(criteria.getStatusId())) {
            Page<AppResponsible> domainPage = appResponsibleRepository.findAll(appResponsibleAuthorizedId, criteria, pageable);
            return domainPage.map(appResponsibleMapper::toResponse);
        }

        List<AppResponsibleOutputDTO> rows = responsibleTypeRepository.findAll().stream()
                .map(type -> buildRow(appResponsibleAuthorizedId, type))
                .filter(row -> matchesCriteria(row, criteria))
                .toList();

        return paginate(rows, pageable);
    }

    /**
     * Builds the outbound row for a single responsible type: the currently active assignment on
     * the given anchor, if any, or an empty placeholder (no id, no person) otherwise.
     *
     * @param appResponsibleAuthorizedId the parent anchor identifier scoping the lookup
     * @param type the responsible type to resolve a row for
     * @return the resolved assignment row, or an empty placeholder row for the given type
     */
    private AppResponsibleOutputDTO buildRow(Long appResponsibleAuthorizedId, ResponsibleType type) {
        AppResponsible existing = appResponsibleRepository.findActiveByAppResponsibleAuthorizedAndResponsibleType(
                appResponsibleAuthorizedId, type.getId());
        if (existing != null) {
            return appResponsibleMapper.toResponse(existing);
        }
        AppResponsibleOutputDTO placeholder = new AppResponsibleOutputDTO();
        placeholder.setAppResponsibleAuthorizedId(appResponsibleAuthorizedId);
        placeholder.setResponsibleType(type);
        return placeholder;
    }

    /**
     * Applies the search criteria to a catalog-driven row. {@code statusId} is intentionally not
     * re-checked here (it already routed the caller to this path). Placeholder rows have no
     * person, so a {@code personId} or {@code search} filter naturally excludes them.
     *
     * @param row the resolved row to test
     * @param criteria the filters to apply, possibly {@code null}
     * @return {@code true} if the row matches every provided filter
     */
    private boolean matchesCriteria(AppResponsibleOutputDTO row, AppResponsibleCriteria criteria) {
        if (criteria == null) {
            return true;
        }
        if (criteria.getResponsibleTypeId() != null && !criteria.getResponsibleTypeId().equals(row.getResponsibleType().getId())) {
            return false;
        }
        if (criteria.getPersonId() != null && (row.getPerson() == null || !criteria.getPersonId().equals(row.getPerson().getId()))) {
            return false;
        }
        String search = criteria.getSearch();
        if (search != null && !search.isBlank()) {
            String needle = search.trim().toLowerCase();
            boolean typeMatches = row.getResponsibleType().getName() != null
                    && row.getResponsibleType().getName().toLowerCase().contains(needle);
            boolean personMatches = row.getPerson() != null
                    && ((row.getPerson().getFirstName() != null && row.getPerson().getFirstName().toLowerCase().contains(needle))
                    || (row.getPerson().getLastName() != null && row.getPerson().getLastName().toLowerCase().contains(needle)));
            return typeMatches || personMatches;
        }
        return true;
    }

    /**
     * Slices an in-memory row list into the requested page, since the catalog-driven listing has
     * no backing query to paginate at the database level.
     *
     * @param rows the full, already-filtered row list
     * @param pageable the pagination parameters to apply
     * @return the requested page of rows
     */
    private Page<AppResponsibleOutputDTO> paginate(List<AppResponsibleOutputDTO> rows, Pageable pageable) {
        int start = (int) Math.min(pageable.getOffset(), rows.size());
        int end = Math.min(start + pageable.getPageSize(), rows.size());
        return new PageImpl<>(rows.subList(start, end), pageable, rows.size());
    }

    @Override
    public AppResponsibleOutputDTO create(AppResponsibleInputDTO inputDTO) {
        Utils.sanitize(inputDTO);
        inputDTO.setPersonId(resolvePersonId(inputDTO));
        syncPersonalCaib(inputDTO.getPersonId(), inputDTO.isPersonalCaib());
        requirePersonalCaibIfRequiredByType(inputDTO.getResponsibleTypeId(), inputDTO.getPersonId());
        AppResponsible currentHolder = appResponsibleRepository.findActiveByAppResponsibleAuthorizedAndResponsibleType(
                inputDTO.getAppResponsibleAuthorizedId(), inputDTO.getResponsibleTypeId());
        if (currentHolder != null) {
            deactivate(currentHolder, null);
        }
        AppResponsible model = appResponsibleMapper.toModelFromInput(inputDTO);
        AppResponsible savedModel = appResponsibleRepository.create(model);
        return appResponsibleMapper.toResponse(savedModel);
    }

    @Override
    public AppResponsibleOutputDTO update(Long id, AppResponsibleInputDTO inputDTO) {
        AppResponsible existing = appResponsibleRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_APPRESPONSIBLE_NOT_FOUND);
        }
        Utils.sanitize(inputDTO);
        if (inputDTO.getPersonId() == null) {
            throw new BusinessRuleException(Constants.ERR_APPRESPONSIBLE_PERSON_DATA_REQUIRED);
        }
        syncPersonalCaib(inputDTO.getPersonId(), inputDTO.isPersonalCaib());
        requirePersonalCaibIfRequiredByType(existing.getResponsibleType(), inputDTO.getPersonId());
        appResponsibleMapper.updateModelFromInput(inputDTO, existing);
        return appResponsibleMapper.toResponse(appResponsibleRepository.update(existing, id));
    }

    /**
     * Resolves the person to assign: returns {@code personId} as-is when provided; otherwise
     * resolves an existing active person by {@code personEmail}, or creates a new one from
     * {@code personFirstName}/{@code personLastName}/{@code personEmail} (and {@code companyId}
     * when not Personal CAIB) — this is the path used when the assignment targets a person with no
     * local {@code Person} row yet (e.g. CAIB staff sourced from Soffid).
     *
     * @param inputDTO the create payload, providing either {@code personId} or the inline person data
     * @return the identifier of the person to assign
     * @throws BusinessRuleException if neither {@code personId} nor a usable name/email set is
     * provided, or if creating a non-CAIB person without a company
     */
    private Long resolvePersonId(AppResponsibleInputDTO inputDTO) {
        if (inputDTO.getPersonId() != null) {
            return inputDTO.getPersonId();
        }
        if (isBlank(inputDTO.getPersonFirstName()) || isBlank(inputDTO.getPersonLastName()) || isBlank(inputDTO.getPersonEmail())) {
            throw new BusinessRuleException(Constants.ERR_APPRESPONSIBLE_PERSON_DATA_REQUIRED);
        }
        Person existing = personRepository.findByEmail(inputDTO.getPersonEmail());
        if (existing != null) {
            return existing.getId();
        }
        if (!inputDTO.isPersonalCaib() && inputDTO.getCompanyId() == null) {
            throw new BusinessRuleException(Constants.ERR_PERSON_COMPANY_REQUIRED_WHEN_NOT_CAIB);
        }
        Person newPerson = new Person();
        newPerson.setFirstName(inputDTO.getPersonFirstName());
        newPerson.setLastName(inputDTO.getPersonLastName());
        newPerson.setEmail(inputDTO.getPersonEmail());
        newPerson.setPersonalCaib(inputDTO.isPersonalCaib());
        if (!inputDTO.isPersonalCaib()) {
            Company company = new Company();
            company.setId(inputDTO.getCompanyId());
            newPerson.setCompany(company);
        }
        return personRepository.create(newPerson).getId();
    }

    /**
     * Keeps the linked person's {@code isPersonalCaib} flag in sync with the value submitted
     * alongside the assignment. A no-op when the value already matches what is stored. Reuses the
     * same business rule enforced by the Person maintenance itself: a person cannot be flagged as
     * external (non-CAIB) without an associated company.
     *
     * @param personId the linked person's identifier
     * @param personalCaib the submitted CAIB flag value
     * @throws BusinessRuleException if setting the person to non-CAIB and it has no company
     */
    private void syncPersonalCaib(Long personId, boolean personalCaib) {
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

    /**
     * Enforces the responsible-type-specific restriction that some responsible types (e.g.
     * "Responsable de la informació") may only be held by Personal CAIB persons. Resolves the
     * responsible type by ID first.
     *
     * @param responsibleTypeId the responsible type identifier to check
     * @param personId the identifier of the person being assigned
     * @throws BusinessRuleException if the type requires Personal CAIB and the person isn't one
     */
    private void requirePersonalCaibIfRequiredByType(Long responsibleTypeId, Long personId) {
        requirePersonalCaibIfRequiredByType(responsibleTypeRepository.findById(responsibleTypeId), personId);
    }

    /**
     * Enforces the responsible-type-specific restriction that some responsible types (e.g.
     * "Responsable de la informació") may only be held by Personal CAIB persons.
     *
     * @param type the already-resolved responsible type to check
     * @param personId the identifier of the person being assigned
     * @throws BusinessRuleException if the type requires Personal CAIB and the person isn't one
     */
    private void requirePersonalCaibIfRequiredByType(ResponsibleType type, Long personId) {
        if (type == null || !type.isRequiresPersonalCaib()) {
            return;
        }
        Person person = personRepository.findById(personId);
        if (person == null || !person.isPersonalCaib()) {
            throw new BusinessRuleException(Constants.ERR_APPRESPONSIBLE_REQUIRES_PERSONAL_CAIB);
        }
    }

    /**
     * Returns whether the given string is {@code null}, empty, or blank.
     *
     * @param value the string to check
     * @return {@code true} if the string carries no usable content
     */
    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @Override
    public void delete(Long id, AppResponsibleDeleteDTO dto) {
        AppResponsible existing = appResponsibleRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_APPRESPONSIBLE_NOT_FOUND);
        }
        if (existing.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_APPRESPONSIBLE_NOT_ACTIVE);
        }
        deactivate(existing, dto != null ? dto.getObservation() : null);
    }

    /**
     * Soft-deletes the given assignment, stamping the deletion audit fields and the given
     * observation (possibly {@code null}), used both by the explicit delete flow and by the
     * automatic swap-out performed on create when another person already holds the same
     * responsible type.
     *
     * @param existing the assignment to soft-delete
     * @param observation free-text reason to record, or {@code null}
     */
    private void deactivate(AppResponsible existing, String observation) {
        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy(Utils.resolveCurrentUsername());
        existing.setObservation(observation);
        appResponsibleRepository.delete(existing);
    }

    @Override
    public AppResponsibleOutputDTO reactivate(Long id) {
        AppResponsible existing = appResponsibleRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_APPRESPONSIBLE_NOT_FOUND);
        }
        if (existing.getDeletedAt() == null) {
            throw new BusinessRuleException(Constants.ERR_APPRESPONSIBLE_ACTIVE);
        }
        if (appResponsibleRepository.existsByAppResponsibleAuthorizedAndResponsibleTypeAndIdNot(
                existing.getAppResponsibleAuthorized().getId(), existing.getResponsibleType().getId(), id)) {
            throw new BusinessRuleException(Constants.ERR_APPRESPONSIBLE_DUPLICATED);
        }
        existing.setDeletedAt(null);
        existing.setDeletedBy(null);
        return appResponsibleMapper.toResponse(appResponsibleRepository.update(existing, id));
    }
}

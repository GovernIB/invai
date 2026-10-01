package es.caib.invai.back.ejb.application.responsibleAuthorized.responsible;

import es.caib.invai.back.interna.application.responsibleAuthorized.responsible.DTO.AppResponsibleDeleteDTO;
import es.caib.invai.back.interna.application.responsibleAuthorized.responsible.DTO.AppResponsibleInputDTO;
import es.caib.invai.back.interna.application.responsibleAuthorized.responsible.DTO.AppResponsibleOutputDTO;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.authorized.AppAuthorizedRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.dir3.Dir3ValidationRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.responsible.AppResponsibleCriteria;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.responsible.AppResponsibleRepository;
import es.caib.invai.back.persistence.repository.catalog.responsibleType.ResponsibleTypeRepository;
import es.caib.invai.back.service.mapper.application.responsibleAuthorized.responsible.AppResponsibleMapper;
import es.caib.invai.back.service.facade.application.responsibleAuthorized.responsible.AppResponsibleService;
import es.caib.invai.back.service.facade.maintenance.responsible.person.PersonService;
import es.caib.invai.back.service.model.application.responsibleAuthorized.authorized.AppAuthorized;
import es.caib.invai.back.service.model.application.responsibleAuthorized.dir3.Dir3Validation;
import es.caib.invai.back.service.model.application.responsibleAuthorized.responsible.AppResponsible;
import es.caib.invai.back.service.model.catalog.dir3Status.Dir3ValidationStatus;
import es.caib.invai.back.service.model.catalog.responsibleType.ResponsibleType;
import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.service.model.maintenance.responsible.person.Person;
import es.caib.invai.back.utils.Constants;
import es.caib.invai.back.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
 * value submitted alongside the assignment (see {@link PersonService#updatePersonalCaibToPerson}). On create,
 * {@code personId} is itself optional: when absent, {@link #getOrCreatePerson} resolves an existing
 * active person by e-mail, or creates a new one, from {@code personFirstName}/{@code personLastName}/
 * {@code personEmail} (and {@code companyId} when not Personal CAIB) — this is what lets a CAIB
 * person sourced from Soffid, who has no local {@code Person} row yet, be assigned without a
 * pre-existing {@code personId}.
 * </p>
 * <p>
 * DIR3 validation is resolved per person, not per assignment (see {@link #checkDir3Validation}):
 * a person already validated (automatically or manually) through another responsible type, or
 * through the authorized slot, on the same anchor keeps that same validation when assigned a new
 * responsible type here, instead of starting a fresh, independently-revalidatable one.
 * </p>
 *
 * @since 1.0.3
 */
@Service
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
    /** Facade used to resolve/create the linked person and keep its Personal CAIB flag in sync. */
    @Autowired
    private PersonService personService;
    /** Repository used to create/update the DIR3 validation record linked to each assignment. */
    @Autowired
    private Dir3ValidationRepository dir3ValidationRepository;
    /** Repository used to detect an already-resolved DIR3 validation on the person's authorized-side assignment when sharing (see {@link #checkDir3Validation}). */
    @Autowired
    private AppAuthorizedRepository appAuthorizedRepository;

    /**
     * Retrieves a paginated, filtered listing of responsible assignments scoped to a single
     * anchor. Falls back to the plain persisted-row listing when the inactive status is
     * explicitly requested; otherwise builds one row per registered responsible type (see
     * {@link #buildRow}), filters in-memory (see {@link #matchesCriteria}), and paginates the
     * result in-memory.
     */
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

        return Utils.paginate(rows, pageable);
    }

    /**
     * Creates a new responsible assignment, enforcing the Personal CAIB restriction for the
     * requested responsible type first (see {@link #requirePersonalCaibIfRequiredByType}), then
     * resolving/creating the person when {@code personId} is absent (see {@link #getOrCreatePerson}),
     * and deactivating any active holder of the same responsible type on the same anchor. The DIR3
     * validation status recorded for the new assignment is taken as-is from {@code inputDTO}
     * (see {@link AppResponsibleInputDTO#getDir3Status()}), not recomputed here. Resolving/creating
     * the person is deferred until after the Personal CAIB check passes, so a rejected request
     * never inserts a new {@code Person} row; the linked person's {@code isPersonalCaib} flag is
     * only synced once the assignment itself has been persisted.
     *
     * @throws BusinessRuleException if the responsible type requires a Personal CAIB person, and
     * the resolved person isn't one, or if person data is missing/invalid (see {@link #getOrCreatePerson})
     */
    @Override
    public AppResponsibleOutputDTO create(AppResponsibleInputDTO inputDTO) {
        Utils.sanitize(inputDTO);
        inputDTO.setPersonalCaib(Boolean.TRUE.equals(inputDTO.getPersonalCaib()));
        requirePersonalCaibIfRequiredByType(responsibleTypeRepository.findById(inputDTO.getResponsibleTypeId()), inputDTO.getPersonalCaib());

        inputDTO.setPersonId(getOrCreatePerson(inputDTO).getId());

        AppResponsible currentResponsibleHolder = appResponsibleRepository.findActiveByAppResponsibleAuthorizedAndResponsibleType(
                inputDTO.getAppResponsibleAuthorizedId(), inputDTO.getResponsibleTypeId());

        if (currentResponsibleHolder != null) {
            deactivate(currentResponsibleHolder, null);
        }

        AppResponsible model = appResponsibleMapper.toModelFromInput(inputDTO);

        Dir3ValidationStatus dir3Status = Dir3ValidationStatus.resolveFromInputFlag(inputDTO.getDir3Status());

        Dir3Validation dir3Validation = checkDir3Validation(
                inputDTO.getAppResponsibleAuthorizedId(), inputDTO.getPersonId(), dir3Status);
        model.setDir3Validation(dir3Validation);

        AppResponsible savedModel = appResponsibleRepository.create(model);

        personService.updatePersonalCaibToPerson(inputDTO.getPersonId(), inputDTO.getPersonalCaib());

        return appResponsibleMapper.toResponse(savedModel);
    }

    /**
     * Updates an existing assignment's mutable fields. Unlike {@link #create}, {@code personId} is
     * mandatory here (no inline person resolution). Both the assigned person and the responsible
     * type are immutable on update (see {@link AppResponsibleMapper#updateModelFromInput}) - the
     * person only ever changes by deactivating this assignment and creating a new one - so only the
     * job title and observation can actually change. Since the person can't change, the DIR3
     * validation state is left untouched here; it only ever changes via
     * {@link es.caib.invai.back.service.facade.application.responsibleAuthorized.dir3.Dir3ValidationService#validateManually}
     * or the owning application's administrative unit changing.
     *
     * @throws BusinessRuleException if no assignment exists with the given ID, {@code personId} is
     * missing or doesn't match the assignment's current person, or the assignment's responsible
     * type requires a Personal CAIB person and the resolved person isn't one
     */
    @Override
    public AppResponsibleOutputDTO update(Long id, AppResponsibleInputDTO inputDTO) {
        AppResponsible existing = appResponsibleRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_APPRESPONSIBLE_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);
        inputDTO.setPersonalCaib(Boolean.TRUE.equals(inputDTO.getPersonalCaib()));

        if (inputDTO.getPersonId() == null) {
            throw new BusinessRuleException(Constants.ERR_APPRESPONSIBLE_PERSON_DATA_REQUIRED);
        }

        if (!inputDTO.getPersonId().equals(existing.getPerson().getId())) {
            throw new BusinessRuleException(Constants.ERR_APPRESPONSIBLE_PERSON_IMMUTABLE);
        }

        requirePersonalCaibIfRequiredByType(existing.getResponsibleType(), inputDTO.getPersonalCaib());

        appResponsibleMapper.updateModelFromInput(inputDTO, existing);
        AppResponsible updatedModel = appResponsibleRepository.update(existing, id);

        personService.updatePersonalCaibToPerson(inputDTO.getPersonId(), inputDTO.getPersonalCaib());

        return appResponsibleMapper.toResponse(updatedModel);
    }

    /**
     * Soft-deletes an active assignment, recording the given observation (see {@link #deactivate}).
     *
     * @throws BusinessRuleException if no assignment exists with the given ID, or it is already inactive
     */
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
     * Reactivates a previously deactivated assignment, rejecting the operation if another active
     * assignment has since taken over the same responsible type on the same anchor.
     *
     * @throws BusinessRuleException if no assignment exists with the given ID, it is already
     * active, or another active assignment now holds the same responsible type on the same anchor
     */
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
     * Resolves the person to assign: verifies {@code personId} actually exists and returns it
     * as-is when provided (see {@link PersonService#getPersonById}), so an invalid/stale id is
     * rejected with a clean {@link BusinessRuleException} here rather than surfacing as a
     * foreign-key violation on {@link #create}'s insert; otherwise resolves an existing active
     * person by {@code personEmail}, or creates a new one from {@code personFirstName}/
     * {@code personLastName}/{@code personEmail} (and {@code companyId} when not Personal CAIB) —
     * this is the path used when the assignment targets a person with no local {@code Person} row
     * yet (e.g. CAIB staff sourced from Soffid).
     *
     * @param inputDTO the creation payload, providing either {@code personId} or the inline person data
     * @return the person to assign
     * @throws BusinessRuleException if {@code personId} is provided but no such person exists, if
     * neither {@code personId} nor a usable name/email set is provided, or if creating a non-CAIB
     * person without a company
     */
    private Person getOrCreatePerson(AppResponsibleInputDTO inputDTO) {
        if (inputDTO.getPersonId() != null) {
            return personService.getPersonById(inputDTO.getPersonId());
        }
        if (StringUtils.isBlank(inputDTO.getPersonFirstName())
                || StringUtils.isBlank(inputDTO.getPersonLastName())
                || StringUtils.isBlank(inputDTO.getPersonEmail())) {
            throw new BusinessRuleException(Constants.ERR_APPRESPONSIBLE_PERSON_DATA_REQUIRED);
        }
        return personService.getOrCreatePerson(inputDTO.getPersonFirstName(), inputDTO.getPersonLastName(),
                inputDTO.getPersonEmail(), inputDTO.getPersonalCaib(), inputDTO.getCompanyId(),
                inputDTO.getPersonUserName());
    }

    /**
     * Resolves the DIR3 validation to attach to a newly created assignment. What must be validated
     * is the person, not any one role they hold on this anchor: if the person already holds another
     * active assignment here - a different responsible type, or the authorized slot - its
     * already-resolved validation (possibly {@code MANUAL}) is reused as-is rather than minting a
     * fresh one that would need revalidating independently. Only when the person holds no other
     * active assignment on this anchor is {@code requestedStatus} used to create a brand-new record.
     *
     * @param appResponsibleAuthorizedId the anchor the new assignment is being created on
     * @param personId the person being assigned
     * @param requestedStatus the DIR3 status computed by the caller, used only when nothing to share exists yet
     * @return the DIR3 validation to attach to the new assignment
     */
    private Dir3Validation checkDir3Validation(Long appResponsibleAuthorizedId, Long personId, Dir3ValidationStatus requestedStatus) {
        List<AppResponsible> siblingResponsibleAssignments =
                appResponsibleRepository.findAllActiveByAppResponsibleAuthorizedAndPerson(appResponsibleAuthorizedId, personId);
        if (!siblingResponsibleAssignments.isEmpty()) {
            return siblingResponsibleAssignments.get(0).getDir3Validation();
        }
        AppAuthorized existingAuthorized =
                appAuthorizedRepository.findActiveByAppResponsibleAuthorizedAndPerson(appResponsibleAuthorizedId, personId);
        if (existingAuthorized != null) {
            return existingAuthorized.getDir3Validation();
        }
        return dir3ValidationRepository.create(Dir3Validation.builder().dir3Status(requestedStatus).build());
    }

    /**
     * Enforces the responsible-type-specific restriction that some responsible types (e.g.
     * "Responsable de la informació") may only be held by Personal CAIB persons. Checked against
     * the incoming {@code personalCaib} flag rather than the person's currently persisted value,
     * so this validation runs - and can reject the request - before {@link PersonService#updatePersonalCaibToPerson}
     * writes that flag to the database.
     *
     * @param type the already-resolved responsible type to check
     * @param personalCaib whether the person being assigned is being submitted as Personal CAIB
     * @throws BusinessRuleException if the type requires Personal CAIB and {@code personalCaib} is {@code false}
     */
    private void requirePersonalCaibIfRequiredByType(ResponsibleType type, boolean personalCaib) {
        if (type == null || !type.isRequiresPersonalCaib()) {
            return;
        }
        if (!personalCaib) {
            throw new BusinessRuleException(Constants.ERR_APPRESPONSIBLE_REQUIRES_PERSONAL_CAIB);
        }
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


}

package es.caib.invai.back.ejb.application.responsibleAuthorized.authorized;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.responsibleAuthorized.authorized.DTO.AppAuthorizedDeleteDTO;
import es.caib.invai.back.interna.application.responsibleAuthorized.authorized.DTO.AppAuthorizedInputDTO;
import es.caib.invai.back.interna.application.responsibleAuthorized.authorized.DTO.AppAuthorizedOutputDTO;
import es.caib.invai.back.interna.maintenance.responsible.authorizationType.DTO.AuthorizationTypeOutputDTO;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.authorized.AppAuthorizedCriteria;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.authorized.AppAuthorizedRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.dir3.Dir3ValidationRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.responsible.AppResponsibleRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.type.AppAuthorizedTypeLinkRepository;
import es.caib.invai.back.persistence.repository.maintenance.responsible.authorizationType.AuthorizationTypeRepository;
import es.caib.invai.back.service.facade.application.responsibleAuthorized.authorized.AppAuthorizedService;
import es.caib.invai.back.service.facade.maintenance.responsible.person.PersonService;
import es.caib.invai.back.service.mapper.application.responsibleAuthorized.authorized.AppAuthorizedMapper;
import es.caib.invai.back.service.mapper.maintenance.responsible.authorizationType.AuthorizationTypeMapper;
import es.caib.invai.back.service.model.application.responsibleAuthorized.authorized.AppAuthorized;
import es.caib.invai.back.service.model.application.responsibleAuthorized.dir3.Dir3Validation;
import es.caib.invai.back.service.model.application.responsibleAuthorized.responsible.AppResponsible;
import es.caib.invai.back.service.model.application.responsibleAuthorized.type.AppAuthorizedTypeLink;
import es.caib.invai.back.service.model.catalog.dir3Status.Dir3ValidationStatus;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Facade service implementation for managing person authorizations linked to applications.
 * <p>
 * Owns the multi-select authorization type reconciliation logic: on create it inserts one
 * {@code AppAuthorizedTypeLink} join row per requested id, and on update it compares the
 * requested id set against the currently existing join rows (see {@link #checkAssignedAuthorizationTypes}) -
 * an unchanged set touches nothing at all, otherwise it soft-deletes the ones no longer requested
 * and inserts the ones missing, leaving the rest untouched. Audited and soft-deleted since 1.0.5
 * (previously a plain hard-delete join table with no audit trail of its own).
 * </p>
 * <p>
 * Enforces the business rule that at most one active authorization may exist per (anchor, person)
 * pair: on create, any existing active authorization for that person is automatically deactivated
 * in favor of the new one; reactivate re-checks the same pair for a duplicate. Update never
 * changes the anchor or the person (see {@link AppAuthorizedMapper#updateModelFromInput}), so no
 * duplicate can arise there and no re-check is needed.
 * </p>
 * <p>
 * Create also keeps the linked person's {@code isPersonalCaib} flag in sync with the value
 * submitted alongside the assignment (see {@link PersonService#updatePersonalCaibToPerson}); update never touches it,
 * consistent with the person being immutable on update. On create, {@code personId} is itself
 * optional: when absent, {@link #getOrCreatePerson} resolves an existing active person by e-mail, or
 * creates a new one, from {@code personFirstName}/{@code personLastName}/{@code personEmail} (and
 * {@code companyId} when not Personal CAIB) — this is what lets a CAIB person sourced from Soffid,
 * who has no local {@code Person} row yet, be assigned without a pre-existing {@code personId}.
 * </p>
 * <p>
 * DIR3 validation is resolved per person, not per role (see {@link #checkDir3Validation}):
 * a person already validated (automatically or manually) through an active responsible-type
 * assignment on the same anchor keeps that same validation when authorized here too, instead of
 * starting a fresh, independently-revalidatable one.
 * </p>
 *
 * @since 1.0.3
 */
@Service
@Slf4j
@Transactional
public class AppAuthorizedServiceFacadeBean implements AppAuthorizedService {

    /** Mapper converting between AppAuthorized domain models, entities, and DTOs. */
    @Autowired
    private AppAuthorizedMapper appAuthorizedMapper;

    /** Repository port used to access AppAuthorized persistence operations. */
    @Autowired
    private AppAuthorizedRepository appAuthorizedRepository;

    /** Repository port used to attach/detach authorization types on an AppAuthorized anchor. */
    @Autowired
    private AppAuthorizedTypeLinkRepository appAuthorizedTypeLinkRepository;

    /** Repository used to resolve the authorization type catalog entries attached to an anchor. */
    @Autowired
    private AuthorizationTypeRepository authorizationTypeRepository;

    /** Mapper used to convert resolved authorization types into their output DTO representation. */
    @Autowired
    private AuthorizationTypeMapper authorizationTypeMapper;

    /** Facade used to resolve/create the linked person and keep its Personal CAIB flag in sync on create. */
    @Autowired
    private PersonService personService;

    /** Repository used to create/update the DIR3 validation record linked to each assignment. */
    @Autowired
    private Dir3ValidationRepository dir3ValidationRepository;

    /** Repository used to detect an already-resolved DIR3 validation on the person's responsible-side assignments when sharing (see {@link #checkDir3Validation}). */
    @Autowired
    private AppResponsibleRepository appResponsibleRepository;

    /**
     * Retrieves a paginated, filtered listing of authorized-person assignments scoped to a single
     * anchor, resolving each row's attached authorization types via {@link #buildResponse}.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AppAuthorizedOutputDTO> getAll(Long appResponsibleAuthorizedId, AppAuthorizedCriteria criteria, Pageable pageable) {
        log.debug("Facade: Fetching paged application authorized records for AppResponsibleAuthorized ID: {}", appResponsibleAuthorizedId);
        Page<AppAuthorized> domainPage = appAuthorizedRepository.findAll(appResponsibleAuthorizedId, criteria, pageable);
        return domainPage.map(this::buildResponse);
    }

    /**
     * Creates a new authorization, resolving/creating the person when {@code personId} is absent
     * (see {@link #getOrCreatePerson}), deactivating any active authorization the same person
     * already holds on the same anchor, then attaching the requested authorization types. The DIR3
     * validation status recorded for the new authorization is taken as-is from {@code inputDTO}
     * (see {@link AppAuthorizedInputDTO#getDir3Status()}), not recomputed here. The linked person's
     * {@code isPersonalCaib} flag is only synced once the authorization itself has been persisted.
     */
    @Override
    public AppAuthorizedOutputDTO create(AppAuthorizedInputDTO inputDTO) {
        log.info("Facade: Creating new application authorized assignment for AppResponsibleAuthorized ID: {} and Person ID: {}",
                inputDTO.getAppResponsibleAuthorizedId(), inputDTO.getPersonId());

        Utils.sanitize(inputDTO);
        inputDTO.setPersonalCaib(Boolean.TRUE.equals(inputDTO.getPersonalCaib()));
        inputDTO.setPersonId(getOrCreatePerson(inputDTO).getId());

        AppAuthorized currentAuthorizedHolder = appAuthorizedRepository.findActiveByAppResponsibleAuthorizedAndPerson(
                inputDTO.getAppResponsibleAuthorizedId(), inputDTO.getPersonId());

        if (currentAuthorizedHolder != null) {
            deactivate(currentAuthorizedHolder, null);
        }

        Dir3ValidationStatus dir3Status = Dir3ValidationStatus.resolveFromInputFlag(inputDTO.getDir3Status());

        Dir3Validation dir3Validation = checkDir3Validation(
                inputDTO.getAppResponsibleAuthorizedId(), inputDTO.getPersonId(), dir3Status);

        AppAuthorized domainModel = appAuthorizedMapper.toModelFromInput(inputDTO);
        domainModel.setDir3Validation(dir3Validation);
        AppAuthorized savedModel = appAuthorizedRepository.create(domainModel);

        personService.updatePersonalCaibToPerson(inputDTO.getPersonId(), inputDTO.getPersonalCaib());

        for (Long authorizationTypeId : inputDTO.getAuthorizationTypeIds()) {
            attachAuthorizationType(savedModel.getId(), authorizationTypeId);
        }

        return buildResponse(savedModel);
    }

    /**
     * Updates an existing authorization's mutable fields (the anchor and person are immutable, see
     * {@link AppAuthorizedMapper#updateModelFromInput}) and reconciles its attached authorization
     * types against the requested list (see {@link #checkAssignedAuthorizationTypes}).
     *
     * @throws BusinessRuleException if no authorization exists with the given ID, or {@code personId}
     * is provided and doesn't match the authorization's current person
     */
    @Override
    public AppAuthorizedOutputDTO update(Long id, AppAuthorizedInputDTO inputDTO) {
        log.info(Constants.LOG_FACADE_DEACTIVATE, id);

        AppAuthorized existingModel = appAuthorizedRepository.findById(id);
        if (existingModel == null) {
            throw new BusinessRuleException(Constants.ERR_APPAUTHORIZED_NOT_FOUND);
        }
        if (inputDTO.getPersonId() != null && !inputDTO.getPersonId().equals(existingModel.getPerson().getId())) {
            throw new BusinessRuleException(Constants.ERR_APPAUTHORIZED_PERSON_IMMUTABLE);
        }

        Utils.sanitize(inputDTO);

        appAuthorizedMapper.updateModelFromInput(inputDTO, existingModel);
        AppAuthorized updatedModel = appAuthorizedRepository.update(existingModel, id);

        checkAssignedAuthorizationTypes(id, inputDTO.getAuthorizationTypeIds());

        return buildResponse(updatedModel);
    }

    /**
     * Soft-deletes an active authorization, recording the given observation (see
     * {@link #deactivate}).
     *
     * @throws BusinessRuleException if no authorization exists with the given ID, or it is already inactive
     */
    @Override
    public void delete(Long id, AppAuthorizedDeleteDTO dto) {
        log.info(Constants.LOG_FACADE_DEACTIVATE, id);

        AppAuthorized existingModel = appAuthorizedRepository.findById(id);
        if (existingModel == null) {
            throw new BusinessRuleException(Constants.ERR_APPAUTHORIZED_NOT_FOUND);
        }

        if (existingModel.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_APPAUTHORIZED_NOT_ACTIVE);
        }

        deactivate(existingModel, dto != null ? dto.getObservation() : null);
    }

    /**
     * Reactivates a previously deactivated authorization, rejecting the operation if another
     * active authorization has since been created for the same (anchor, person) pair.
     *
     * @throws BusinessRuleException if no authorization exists with the given ID, it is already
     * active, or another active authorization now holds the same (anchor, person) pair
     */
    @Override
    public AppAuthorizedOutputDTO reactivate(Long id) {
        log.info("Facade: Reactivating application authorized assignment for ID: {}", id);

        AppAuthorized existingModel = appAuthorizedRepository.findById(id);
        if (existingModel == null) {
            throw new BusinessRuleException(Constants.ERR_APPAUTHORIZED_NOT_FOUND);
        }

        if (existingModel.getDeletedAt() == null) {
            throw new BusinessRuleException(Constants.ERR_APPAUTHORIZED_ACTIVE);
        }

        if (appAuthorizedRepository.existsByAppResponsibleAuthorizedAndPersonAndIdNot(
                existingModel.getAppResponsibleAuthorized().getId(), existingModel.getPerson().getId(), id)) {
            throw new BusinessRuleException(Constants.ERR_APPAUTHORIZED_DUPLICATED);
        }

        existingModel.setDeletedAt(null);
        existingModel.setDeletedBy(null);
        AppAuthorized updatedModel = appAuthorizedRepository.update(existingModel, id);

        return buildResponse(updatedModel);
    }

    /**
     * Soft-deletes the given authorization, stamping the deletion audit fields and the given
     * observation (possibly {@code null}), used both by the explicit delete flow and by the
     * automatic swap-out performed on creation when another person already holds the same
     * responsible type.
     *
     * @param existing the authorization to soft-delete
     * @param observation free-text reason to record, or {@code null}
     */
    private void deactivate(AppAuthorized existing, String observation) {
        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy(Utils.resolveCurrentUsername());
        existing.setObservation(observation);
        appAuthorizedRepository.delete(existing);
    }

    /**
     * Resolves the person to authorize: verifies {@code personId} actually exists and returns it
     * as-is when provided (see {@link PersonService#getPersonById}), so an invalid/stale id is
     * rejected with a clean {@link BusinessRuleException} here rather than surfacing as a
     * foreign-key violation on {@link #create}'s insert; otherwise resolves an existing active
     * person by {@code personEmail}, or creates a new one from {@code personFirstName}/
     * {@code personLastName}/{@code personEmail} (and {@code companyId} when not Personal CAIB) —
     * this is the path used when the assignment targets a person with no local {@code Person} row
     * yet (e.g. CAIB staff sourced from Soffid).
     *
     * @param inputDTO the create payload, providing either {@code personId} or the inline person data
     * @return the person to authorize
     * @throws BusinessRuleException if {@code personId} is provided but no such person exists, if
     * neither {@code personId} nor a usable name/email set is provided, or if creating a non-CAIB
     * person without a company
     */
    private Person getOrCreatePerson(AppAuthorizedInputDTO inputDTO) {
        if (inputDTO.getPersonId() != null) {
            return personService.getPersonById(inputDTO.getPersonId());
        }
        if (StringUtils.isBlank(inputDTO.getPersonFirstName()) || StringUtils.isBlank(inputDTO.getPersonLastName()) || StringUtils.isBlank(inputDTO.getPersonEmail())) {
            throw new BusinessRuleException(Constants.ERR_APPAUTHORIZED_PERSON_DATA_REQUIRED);
        }
        return personService.getOrCreatePerson(inputDTO.getPersonFirstName(), inputDTO.getPersonLastName(),
                inputDTO.getPersonEmail(), inputDTO.getPersonalCaib(), inputDTO.getCompanyId(),
                inputDTO.getPersonUserName());
    }

    /**
     * Resolves the DIR3 validation to attach to a newly created authorization. What must be
     * validated is the person, not the authorized slot specifically: if the person already holds an
     * active responsible-type assignment on this same anchor, its already-resolved validation
     * (possibly {@code MANUAL}) is reused as-is rather than minting a fresh one that would need
     * revalidating independently. The authorized side itself is checked by the caller before this
     * point (see {@link #create}'s deactivation of {@code currentAuthorizedHolder}), so no active
     * authorization for this person can remain on the anchor at this point.
     *
     * @param appResponsibleAuthorizedId the anchor the new authorization is being created on
     * @param personId the person being authorized
     * @param requestedStatus the DIR3 status computed by the caller, used only when nothing to share exists yet
     * @return the DIR3 validation to attach to the new authorization
     */
    private Dir3Validation checkDir3Validation(Long appResponsibleAuthorizedId, Long personId, Dir3ValidationStatus requestedStatus) {
        List<AppResponsible> existingResponsibleAssignments =
                appResponsibleRepository.findAllActiveByAppResponsibleAuthorizedAndPerson(appResponsibleAuthorizedId, personId);
        if (!existingResponsibleAssignments.isEmpty()) {
            return existingResponsibleAssignments.get(0).getDir3Validation();
        }
        return dir3ValidationRepository.create(Dir3Validation.builder().dir3Status(requestedStatus).build());
    }

    /**
     * Diffs the requested authorization type id set against the currently existing join rows for
     * the given anchor: if the two sets are identical, nothing is touched at all (the common case
     * on a plain {@code PUT}, since {@code authorizationTypeIds} is required on every update even
     * when the edit is to an unrelated field). Otherwise, soft-deletes any join row whose id is no
     * longer requested and inserts a new join row for any requested id with no existing join row
     * yet; ids present in both sets are left untouched.
     */
    private void checkAssignedAuthorizationTypes(Long appAuthorizedId, List<Long> requestedAuthorizationTypeIds) {
        List<AppAuthorizedTypeLink> currentJoins = appAuthorizedTypeLinkRepository.findAllActiveByAppAuthorizedId(appAuthorizedId);

        Set<Long> currentTypeIds = currentJoins.stream().map(AppAuthorizedTypeLink::getAuthorizationTypeId).collect(Collectors.toSet());
        Set<Long> requestedTypeIds = new HashSet<>(requestedAuthorizationTypeIds);
        if (currentTypeIds.equals(requestedTypeIds)) {
            return;
        }

        for (AppAuthorizedTypeLink join : currentJoins) {
            if (!requestedTypeIds.contains(join.getAuthorizationTypeId())) {
                detachAuthorizationType(join);
            }
        }

        for (Long requestedId : requestedTypeIds) {
            if (!currentTypeIds.contains(requestedId)) {
                attachAuthorizationType(appAuthorizedId, requestedId);
            }
        }
    }

    /**
     * Inserts a single authorization type join row attaching the given type to the given anchor.
     *
     * @param appAuthorizedId the anchor identifier
     * @param authorizationTypeId the authorization type identifier to attach
     */
    private void attachAuthorizationType(Long appAuthorizedId, Long authorizationTypeId) {
        AppAuthorizedTypeLink join = AppAuthorizedTypeLink.builder()
                .appAuthorizedId(appAuthorizedId)
                .authorizationTypeId(authorizationTypeId)
                .build();
        appAuthorizedTypeLinkRepository.create(join);
    }

    /**
     * Soft-deletes a single authorization type join row, stamping the deletion audit fields.
     *
     * @param join the join row to soft-delete
     */
    private void detachAuthorizationType(AppAuthorizedTypeLink join) {
        join.setDeletedAt(LocalDateTime.now());
        join.setDeletedBy(Utils.resolveCurrentUsername());
        appAuthorizedTypeLinkRepository.delete(join);
    }

    /**
     * Builds the outbound response for an anchor, resolving and attaching the list of authorization
     * types currently attached to it. Resolves every attached type in a single batched query
     * ({@link AuthorizationTypeRepository#findAllByIdIn}) rather than one {@code findById} call per
     * type, to avoid an N+1 query per row when called from a paginated {@link #getAll}.
     */
    private AppAuthorizedOutputDTO buildResponse(AppAuthorized model) {
        AppAuthorizedOutputDTO response = appAuthorizedMapper.toResponse(model);

        List<AppAuthorizedTypeLink> currentJoins =
                appAuthorizedTypeLinkRepository.findAllActiveByAppAuthorizedId(model.getId());

        List<Long> authorizationTypeIds = currentJoins.stream()
                .map(AppAuthorizedTypeLink::getAuthorizationTypeId)
                .toList();

        List<AuthorizationTypeOutputDTO> authorizationTypes = authorizationTypeRepository.findAllByIdIn(authorizationTypeIds).stream()
                .map(authorizationTypeMapper::toResponse)
                .toList();

        response.setAuthorizationTypes(authorizationTypes);
        return response;
    }
}

package es.caib.invai.back.ejb.application.responsibleAuthorized.authorized;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.responsibleAuthorized.authorized.DTO.AppAuthorizedDeleteDTO;
import es.caib.invai.back.interna.application.responsibleAuthorized.authorized.DTO.AppAuthorizedInputDTO;
import es.caib.invai.back.interna.application.responsibleAuthorized.authorized.DTO.AppAuthorizedOutputDTO;
import es.caib.invai.back.interna.maintenance.responsible.authorizationType.DTO.AuthorizationTypeOutputDTO;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.authorized.AppAuthorizedCriteria;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.authorized.AppAuthorizedRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.type.AppAuthorizedTypeLinkRepository;
import es.caib.invai.back.persistence.repository.maintenance.responsible.authorizationType.AuthorizationTypeRepository;
import es.caib.invai.back.persistence.repository.maintenance.responsible.person.PersonRepository;
import es.caib.invai.back.service.facade.application.responsibleAuthorized.authorized.AppAuthorizedService;
import es.caib.invai.back.service.mapper.application.responsibleAuthorized.authorized.AppAuthorizedMapper;
import es.caib.invai.back.service.mapper.maintenance.responsible.authorizationType.AuthorizationTypeMapper;
import es.caib.invai.back.service.model.application.responsibleAuthorized.authorized.AppAuthorized;
import es.caib.invai.back.service.model.application.responsibleAuthorized.type.AppAuthorizedTypeLink;
import es.caib.invai.back.service.model.maintenance.responsible.authorizationType.AuthorizationType;
import es.caib.invai.back.service.model.maintenance.responsible.company.Company;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Facade service implementation for managing person authorizations linked to applications.
 * <p>
 * Owns the multi-select authorization type reconciliation logic: on create it inserts one
 * {@code AppAuthorizedTypeLink} join row per requested id, and on update it diffs the
 * requested id list against the currently existing join rows, hard-deleting the ones no longer
 * requested and inserting the ones missing, leaving the rest untouched. This is a plain
 * intermediate join table: it has no audit trail of its own.
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
 * submitted alongside the assignment (see {@link #syncPersonalCaib}); update never touches it,
 * consistent with the person being immutable on update. On create, {@code personId} is itself
 * optional: when absent, {@link #resolvePersonId} resolves an existing active person by e-mail, or
 * creates a new one, from {@code personFirstName}/{@code personLastName}/{@code personEmail} (and
 * {@code companyId} when not Personal CAIB) — this is what lets a CAIB person sourced from Soffid,
 * who has no local {@code Person} row yet, be assigned without a pre-existing {@code personId}.
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

    /** Repository used to keep the linked person's {@code isPersonalCaib} flag in sync on create. */
    @Autowired
    private PersonRepository personRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AppAuthorizedOutputDTO> getAll(Long appResponsibleAuthorizedId, AppAuthorizedCriteria criteria, Pageable pageable) {
        log.info("Facade: Fetching paged application authorized records for AppResponsibleAuthorized ID: {}", appResponsibleAuthorizedId);
        Page<AppAuthorized> domainPage = appAuthorizedRepository.findAll(appResponsibleAuthorizedId, criteria, pageable);
        return domainPage.map(this::buildResponse);
    }

    @Override
    public AppAuthorizedOutputDTO create(AppAuthorizedInputDTO inputDTO) {
        log.info("Facade: Creating new application authorized assignment for AppResponsibleAuthorized ID: {} and Person ID: {}",
                inputDTO.getAppResponsibleAuthorizedId(), inputDTO.getPersonId());

        Utils.sanitize(inputDTO);
        inputDTO.setPersonId(resolvePersonId(inputDTO));
        syncPersonalCaib(inputDTO.getPersonId(), inputDTO.isPersonalCaib());

        AppAuthorized currentAuthorizedHolder = appAuthorizedRepository.findActiveByAppResponsibleAuthorizedAndPerson(
                inputDTO.getAppResponsibleAuthorizedId(), inputDTO.getPersonId());
        if (currentAuthorizedHolder != null) {
            deactivate(currentAuthorizedHolder, null);
        }

        AppAuthorized domainModel = appAuthorizedMapper.toModelFromInput(inputDTO);
        AppAuthorized savedModel = appAuthorizedRepository.create(domainModel);

        for (Long authorizationTypeId : nullSafe(inputDTO.getAuthorizationTypeIds())) {
            attachAuthorizationType(savedModel.getId(), authorizationTypeId);
        }

        return buildResponse(savedModel);
    }

    @Override
    public AppAuthorizedOutputDTO update(Long id, AppAuthorizedInputDTO inputDTO) {
        log.info(Constants.LOG_FACADE_DEACTIVATE, id);

        AppAuthorized existingModel = appAuthorizedRepository.findById(id);
        if (existingModel == null) {
            throw new BusinessRuleException(Constants.ERR_APPAUTHORIZED_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        appAuthorizedMapper.updateModelFromInput(inputDTO, existingModel);
        AppAuthorized updatedModel = appAuthorizedRepository.update(existingModel, id);

        reconcileAuthorizationTypes(id, nullSafe(inputDTO.getAuthorizationTypeIds()));

        return buildResponse(updatedModel);
    }

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
     * automatic swap-out performed on create when another person already holds the same
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
     * Resolves the person to authorize: returns {@code personId} as-is when provided; otherwise
     * resolves an existing active person by {@code personEmail}, or creates a new one from
     * {@code personFirstName}/{@code personLastName}/{@code personEmail} (and {@code companyId}
     * when not Personal CAIB) — this is the path used when the assignment targets a person with no
     * local {@code Person} row yet (e.g. CAIB staff sourced from Soffid).
     *
     * @param inputDTO the create payload, providing either {@code personId} or the inline person data
     * @return the identifier of the person to authorize
     * @throws BusinessRuleException if neither {@code personId} nor a usable name/email set is
     * provided, or if creating a non-CAIB person without a company
     */
    private Long resolvePersonId(AppAuthorizedInputDTO inputDTO) {
        if (inputDTO.getPersonId() != null) {
            return inputDTO.getPersonId();
        }
        if (isBlank(inputDTO.getPersonFirstName()) || isBlank(inputDTO.getPersonLastName()) || isBlank(inputDTO.getPersonEmail())) {
            throw new BusinessRuleException(Constants.ERR_APPAUTHORIZED_PERSON_DATA_REQUIRED);
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
     * Returns whether the given string is {@code null}, empty, or blank.
     *
     * @param value the string to check
     * @return {@code true} if the string carries no usable content
     */
    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Diffs the requested authorization type id list against the currently existing join rows for
     * the given anchor: hard-deletes any join row whose id is no longer requested, inserts a new
     * join row for any requested id with no existing join row yet, and leaves the rest untouched.
     */
    private void reconcileAuthorizationTypes(Long appAuthorizedId, List<Long> requestedAuthorizationTypeIds) {
        List<AppAuthorizedTypeLink> currentJoins =
                appAuthorizedTypeLinkRepository.findAllByAppAuthorizedId(appAuthorizedId);

        Set<Long> requestedIds = new HashSet<>(requestedAuthorizationTypeIds);
        Set<Long> currentIds = new HashSet<>();
        for (AppAuthorizedTypeLink join : currentJoins) {
            currentIds.add(join.getAuthorizationTypeId());
        }

        for (AppAuthorizedTypeLink join : currentJoins) {
            if (!requestedIds.contains(join.getAuthorizationTypeId())) {
                appAuthorizedTypeLinkRepository.delete(join);
            }
        }

        for (Long requestedId : requestedIds) {
            if (!currentIds.contains(requestedId)) {
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
     * Builds the outbound response for an anchor, resolving and attaching the list of authorization
     * types currently attached to it.
     */
    private AppAuthorizedOutputDTO buildResponse(AppAuthorized model) {
        AppAuthorizedOutputDTO response = appAuthorizedMapper.toResponse(model);

        List<AppAuthorizedTypeLink> currentJoins =
                appAuthorizedTypeLinkRepository.findAllByAppAuthorizedId(model.getId());

        List<AuthorizationTypeOutputDTO> authorizationTypes = currentJoins.stream()
                .map(join -> {
                    AuthorizationType authorizationType = authorizationTypeRepository.findById(join.getAuthorizationTypeId());
                    return authorizationType != null ? authorizationTypeMapper.toResponse(authorizationType) : null;
                })
                .filter(java.util.Objects::nonNull)
                .toList();

        response.setAuthorizationTypes(authorizationTypes);
        return response;
    }

    /**
     * Returns the given list, or an empty list if it is {@code null}.
     *
     * @param ids the list to guard, possibly {@code null}
     * @return the given list, or an empty list
     */
    private static List<Long> nullSafe(List<Long> ids) {
        return ids != null ? ids : List.of();
    }
}

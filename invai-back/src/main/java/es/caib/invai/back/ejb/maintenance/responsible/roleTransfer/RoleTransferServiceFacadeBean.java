package es.caib.invai.back.ejb.maintenance.responsible.roleTransfer;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.maintenance.responsible.person.DTO.PersonDir3CheckOutputDTO;
import es.caib.invai.back.interna.maintenance.responsible.authorizationType.DTO.AuthorizationTypeOutputDTO;
import es.caib.invai.back.interna.maintenance.responsible.roleTransfer.DTO.RoleAssignmentOutputDTO;
import es.caib.invai.back.interna.maintenance.responsible.roleTransfer.DTO.RoleTransferInputDTO;
import es.caib.invai.back.interna.maintenance.responsible.roleTransfer.DTO.RoleTransferItemDTO;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.authorized.AppAuthorizedRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.dir3.Dir3ValidationRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.responsible.AppResponsibleRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.type.AppAuthorizedTypeLinkRepository;
import es.caib.invai.back.persistence.repository.maintenance.responsible.authorizationType.AuthorizationTypeRepository;
import es.caib.invai.back.persistence.repository.maintenance.responsible.person.PersonRepository;
import es.caib.invai.back.rest.soffid.SoffidClient;
import es.caib.invai.back.rest.soffid.SoffidUser;
import es.caib.invai.back.service.facade.application.responsibleAuthorized.authorized.AppAuthorizedService;
import es.caib.invai.back.service.facade.application.responsibleAuthorized.responsible.AppResponsibleService;
import es.caib.invai.back.service.facade.maintenance.responsible.person.PersonService;
import es.caib.invai.back.service.facade.maintenance.responsible.roleTransfer.RoleTransferService;
import es.caib.invai.back.service.mapper.maintenance.responsible.authorizationType.AuthorizationTypeMapper;
import es.caib.invai.back.service.mapper.maintenance.responsible.roleTransfer.RoleAssignmentMapper;
import es.caib.invai.back.service.model.application.responsibleAuthorized.authorized.AppAuthorized;
import es.caib.invai.back.service.model.application.responsibleAuthorized.core.AppResponsibleAuthorized;
import es.caib.invai.back.service.model.application.responsibleAuthorized.dir3.Dir3Validation;
import es.caib.invai.back.service.model.application.responsibleAuthorized.responsible.AppResponsible;
import es.caib.invai.back.service.model.application.responsibleAuthorized.type.AppAuthorizedTypeLink;
import es.caib.invai.back.service.model.catalog.validationStatus.ValidationStatusEnum;
import es.caib.invai.back.service.model.maintenance.responsible.authorizationType.AuthorizationType;
import es.caib.invai.back.service.model.maintenance.responsible.person.Person;
import es.caib.invai.back.service.model.maintenance.responsible.roleTransfer.RoleAssignmentType;
import es.caib.invai.back.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Objects;

/**
 * Facade service implementation for the "Transferència de rols" maintenance screen. Owns the
 * fan-out between {@link AppResponsibleRepository} and {@link AppAuthorizedRepository}: each batch
 * item is dispatched to the repository matching its {@link RoleAssignmentType}, and the whole batch
 * runs inside a single transaction so a failure on any item rolls back the entire transfer.
 * <p>
 * When reassigning, the destination person's DIR3 is validated against the administrative unit
 * of the application each assignment belongs to (see {@link #getTargetDir3Validation}); the
 * status the origin person had never is carried over. A destination person who already holds
 * another assignment on the same application keeps that shared validation as it is if it is
 * {@code MANUAL}, and gets it recalculated otherwise.
 * </p>
 * <p>
 * Revoking delegates to the regular delete use cases of {@link AppResponsibleService} and
 * {@link AppAuthorizedService}, so it behaves exactly like deleting the assignment from the
 * application itself.
 * </p>
 *
 * @since 1.0.3
 */
@Service
@Slf4j
@Transactional
public class RoleTransferServiceFacadeBean implements RoleTransferService {

    /**
     * Repository port used to access AppResponsible persistence operations.
     */
    @Autowired
    private AppResponsibleRepository appResponsibleRepository;

    /**
     * Repository port used to access AppAuthorized persistence operations.
     */
    @Autowired
    private AppAuthorizedRepository appAuthorizedRepository;

    /**
     * Repository port used to resolve the authorization types attached to an AppAuthorized anchor.
     */
    @Autowired
    private AppAuthorizedTypeLinkRepository appAuthorizedTypeLinkRepository;

    /**
     * Repository used to resolve authorization type catalog names.
     */
    @Autowired
    private AuthorizationTypeRepository authorizationTypeRepository;

    /**
     * Mapper converting resolved authorization types into their output DTO representation.
     */
    @Autowired
    private AuthorizationTypeMapper authorizationTypeMapper;

    /**
     * Mapper building the role-transfer rows out of the responsible and authorized assignments.
     */
    @Autowired
    private RoleAssignmentMapper roleAssignmentMapper;

    /**
     * Repository used to resolve/create the destination person by e-mail.
     */
    @Autowired
    private PersonRepository personRepository;

    /**
     * Port used to look up the destination person against Soffid when no local match exists.
     */
    @Autowired
    private SoffidClient soffidClient;

    /**
     * Facade used to check the destination person's DIR3 against the application's administrative unit.
     */
    @Autowired
    private PersonService personService;

    /**
     * Repository used to create the DIR3 validation record of the destination person.
     */
    @Autowired
    private Dir3ValidationRepository dir3ValidationRepository;

    /**
     * Regular responsible use cases, reused so a revocation is exactly a delete of the assignment.
     */
    @Autowired
    private AppResponsibleService appResponsibleService;

    /**
     * Regular authorized use cases, reused so a revocation is exactly a delete of the assignment.
     */
    @Autowired
    private AppAuthorizedService appAuthorizedService;

    @Override
    @Transactional(readOnly = true)
    public List<RoleAssignmentOutputDTO> getAssignmentsByPerson(Long personId) {
        log.debug("Facade: Fetching every active role assignment held by person ID: {}", personId);

        List<RoleAssignmentOutputDTO> assignments = new ArrayList<>();

        appResponsibleRepository.findAllActiveByPersonId(personId).stream()
                .map(roleAssignmentMapper::fromResponsible)
                .forEach(assignments::add);

        List<AppAuthorized> authorizedList = appAuthorizedRepository.findAllActiveByPersonId(personId);
        Map<Long, List<AuthorizationTypeOutputDTO>> authorizationTypesByAuthorized = getAuthorizationTypes(authorizedList);
        authorizedList.stream()
                .map(authorized -> roleAssignmentMapper.fromAuthorized(
                        authorized, authorizationTypesByAuthorized.getOrDefault(authorized.getId(), List.of())))
                .forEach(assignments::add);

        return assignments;
    }

    @Override
    public void transfer(RoleTransferInputDTO inputDTO) {
        log.info("Facade: Transferring {} role assignment(s) (revoke={})", inputDTO.getItems().size(), inputDTO.isRevoke());

        if (!inputDTO.isRevoke() && StringUtils.isBlank(inputDTO.getToPersonEmailAddress())) {
            throw new BusinessRuleException(Constants.ERR_ROLETRANSFER_TARGET_REQUIRED);
        }

        Person toPerson = inputDTO.isRevoke() ? null : getTargetPerson(inputDTO.getToPersonEmailAddress());

        for (RoleTransferItemDTO item : inputDTO.getItems()) {
            if (item.getType() == RoleAssignmentType.RESPONSIBLE) {
                transferResponsible(item.getId(), inputDTO.isRevoke(), toPerson);
            } else {
                transferAuthorized(item.getId(), inputDTO.isRevoke(), toPerson);
            }
        }
    }

    /**
     * Resolves the destination person for a transfer by e-mail: reuses an existing active local
     * {@link Person} when one already matches, otherwise looks the e-mail up against Soffid and, if
     * found there, persists it locally as a new CAIB person before the transfer proceeds.
     *
     * @param email the destination person's e-mail address
     * @return the (possibly newly created) local person to assign
     * @throws BusinessRuleException if no person with the given e-mail exists locally or in Soffid
     */
    private Person getTargetPerson(String email) {
        Person existing = personRepository.findByEmail(email);
        if (Objects.nonNull(existing)) {
            return existing;
        }
        SoffidUser soffidUser = soffidClient.searchByEmail(email);
        if (Objects.isNull(soffidUser)) {
            throw new BusinessRuleException(Constants.ERR_ROLETRANSFER_TARGET_NOT_FOUND);
        }
        Person newPerson = new Person();
        newPerson.setFirstName(soffidUser.getFirstName());
        newPerson.setLastName(soffidUser.getLastName());
        newPerson.setEmail(soffidUser.getEmailAddress());
        newPerson.setPersonalCaib(true);
        return personRepository.create(newPerson);
    }

    /**
     * Transfers or revokes a single AppResponsible assignment.
     *
     * @param id       the AppResponsible identifier to transfer or revoke
     * @param revoke   whether to soft-delete the assignment instead of reassigning it
     * @param toPerson the already-resolved destination person, ignored when revoking
     */
    private void transferResponsible(Long id, boolean revoke, Person toPerson) {
        if (revoke) {
            appResponsibleService.delete(id, null);
            return;
        }

        AppResponsible existing = appResponsibleRepository.findById(id);
        if (Objects.isNull(existing)) {
            throw new BusinessRuleException(Constants.ERR_APPRESPONSIBLE_NOT_FOUND);
        }
        if (Objects.nonNull(existing.getDeletedAt())) {
            throw new BusinessRuleException(Constants.ERR_APPRESPONSIBLE_NOT_ACTIVE);
        }

        existing.setDir3Validation(getTargetDir3Validation(existing.getAppResponsibleAuthorized(), toPerson));
        existing.getPerson().setId(toPerson.getId());
        appResponsibleRepository.update(existing, id);
    }

    /**
     * Transfers or revokes a single AppAuthorized assignment. When transferring, verifies the
     * target person does not already hold an active authorization on the same anchor.
     *
     * @param id       the AppAuthorized identifier to transfer or revoke
     * @param revoke   whether to soft-delete the assignment instead of reassigning it
     * @param toPerson the already-resolved destination person, ignored when revoking
     */
    private void transferAuthorized(Long id, boolean revoke, Person toPerson) {
        if (revoke) {
            appAuthorizedService.delete(id, null);
            return;
        }

        AppAuthorized existing = appAuthorizedRepository.findById(id);
        if (Objects.isNull(existing)) {
            throw new BusinessRuleException(Constants.ERR_APPAUTHORIZED_NOT_FOUND);
        }
        if (Objects.nonNull(existing.getDeletedAt())) {
            throw new BusinessRuleException(Constants.ERR_APPAUTHORIZED_NOT_ACTIVE);
        }

        Long anchorId = existing.getAppResponsibleAuthorized().getId();
        if (appAuthorizedRepository.existsByAppResponsibleAuthorizedAndPersonAndIdNot(anchorId, toPerson.getId(), id)) {
            throw new BusinessRuleException(Constants.ERR_APPAUTHORIZED_DUPLICATED);
        }
        existing.setDir3Validation(getTargetDir3Validation(existing.getAppResponsibleAuthorized(), toPerson));
        existing.getPerson().setId(toPerson.getId());
        appAuthorizedRepository.update(existing, id);
    }

    /**
     * Resolves the DIR3 validation the destination person gets on the application being
     * transferred to, regardless of the validation the origin person had. What is validated is
     * the person, not any one role they hold on the anchor, so a person who already holds another
     * active assignment on the same anchor (a different responsible type, or the authorized slot)
     * shares that assignment's validation record: a {@code MANUAL} one is kept untouched (it was
     * set by a person on purpose), any other is brought in line with the person's DIR3 checked
     * live against the application's administrative unit, or left as it was if that check can't
     * be done. Only when the person has no other assignment on the anchor - i.e. no trace of them
     * is left for that application - is a brand-new record created, with the live check's result.
     *
     * @param anchor   the "Responsables i Autoritzats" anchor of the application being transferred to
     * @param toPerson the destination person
     * @return the DIR3 validation to attach to the transferred assignment
     */
    private Dir3Validation getTargetDir3Validation(AppResponsibleAuthorized anchor, Person toPerson) {
        String admUnitCode = anchor.getApplication().getAdmUnitCode();
        Dir3Validation existing = getExistingDir3Validation(anchor, toPerson);
        if (Objects.isNull(existing)) {
            ValidationStatusEnum status = checkDir3Status(toPerson, admUnitCode, null);
            return dir3ValidationRepository.create(Dir3Validation.builder().dir3Status(status).build());
        }
        if (existing.getDir3Status() == ValidationStatusEnum.MANUAL) {
            return existing;
        }
        ValidationStatusEnum status = checkDir3Status(toPerson, admUnitCode, existing.getDir3Status());
        if (existing.getDir3Status() != status) {
            existing.setDir3Status(status);
            dir3ValidationRepository.update(existing, existing.getId());
        }
        return existing;
    }

    /**
     * Gets the DIR3 validation the person already has on the anchor through another active
     * assignment (a responsible type, or the authorized slot).
     *
     * @param anchor the "Responsables i Autoritzats" anchor
     * @param person the person to look the validation of up
     * @return the validation they already have on the anchor, or {@code null} if they hold no other assignment there
     */
    private Dir3Validation getExistingDir3Validation(AppResponsibleAuthorized anchor, Person person) {
        List<AppResponsible> responsibleSiblings =
                appResponsibleRepository.findAllActiveByAppResponsibleAuthorizedAndPerson(anchor.getId(), person.getId());
        if (!responsibleSiblings.isEmpty()) {
            return responsibleSiblings.get(0).getDir3Validation();
        }
        AppAuthorized authorizedSibling =
                appAuthorizedRepository.findActiveByAppResponsibleAuthorizedAndPerson(anchor.getId(), person.getId());
        return Objects.nonNull(authorizedSibling) ? authorizedSibling.getDir3Validation() : null;
    }

    /**
     * Checks the person's DIR3 against the given administrative unit. A failure of the live check
     * (e.g., Soffid unavailable) never blocks the transfer: it yields {@code fallbackOnFailure}
     * when there is one, and is otherwise treated as inconclusive, i.e. the same as a mismatch
     * (see {@link ValidationStatusEnum#resolve}).
     *
     * @param person            the person to check
     * @param admUnitCode       the application's administrative unit code, possibly blank
     * @param fallbackOnFailure the status to return if the live check fails, or {@code null} to treat it as inconclusive
     * @return the resulting DIR3 validation status
     */
    private ValidationStatusEnum checkDir3Status(Person person, String admUnitCode, ValidationStatusEnum fallbackOnFailure) {
        PersonDir3CheckOutputDTO checkResult = null;
        if (person.isPersonalCaib() && StringUtils.isNotBlank(admUnitCode)) {
            try {
                checkResult = personService.checkDir3(person.getEmail(), admUnitCode);
            } catch (RuntimeException ex) {
                log.warn("Facade: Could not verify DIR3 while transferring a role to person email: {}", person.getEmail());
                if (Objects.nonNull(fallbackOnFailure)) {
                    return fallbackOnFailure;
                }
            }
        }
        return ValidationStatusEnum.resolve(person.isPersonalCaib(), admUnitCode, checkResult);
    }

    /**
     * Resolves the full output representation of the authorization types currently attached to each
     * of the given AppAuthorized anchors, using two batched queries in total (the join rows of all
     * the anchors, then the distinct catalog types they point to) instead of a few per anchor. A
     * join row pointing to a type that no longer exists is skipped.
     *
     * @param authorizedList the AppAuthorized anchors to resolve the types of
     * @return the output DTOs of the attached authorization types, keyed by AppAuthorized identifier;
     * anchors with no types have no entry
     */
    private Map<Long, List<AuthorizationTypeOutputDTO>> getAuthorizationTypes(List<AppAuthorized> authorizedList) {
        if (authorizedList.isEmpty()) {
            return Map.of();
        }
        List<AppAuthorizedTypeLink> links = appAuthorizedTypeLinkRepository
                .findAllActiveByAppAuthorizedIdIn(authorizedList.stream().map(AppAuthorized::getId).toList());
        List<Long> typeIds = links.stream().map(AppAuthorizedTypeLink::getAuthorizationTypeId).distinct().toList();
        if (typeIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, AuthorizationTypeOutputDTO> typesById = authorizationTypeRepository.findAllByIdIn(typeIds).stream()
                .collect(Collectors.toMap(AuthorizationType::getId, authorizationTypeMapper::toResponse));
        return links.stream()
                .filter(link -> typesById.containsKey(link.getAuthorizationTypeId()))
                .collect(Collectors.groupingBy(AppAuthorizedTypeLink::getAppAuthorizedId,
                        Collectors.mapping(link -> typesById.get(link.getAuthorizationTypeId()), Collectors.toList())));
    }
}

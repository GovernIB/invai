package es.caib.invai.back.ejb.maintenance.responsible.roleTransfer;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.maintenance.responsible.authorizationType.DTO.AuthorizationTypeOutputDTO;
import es.caib.invai.back.interna.maintenance.responsible.roleTransfer.DTO.RoleAssignmentOutputDTO;
import es.caib.invai.back.interna.maintenance.responsible.roleTransfer.DTO.RoleTransferInputDTO;
import es.caib.invai.back.interna.maintenance.responsible.roleTransfer.DTO.RoleTransferItemDTO;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.authorized.AppAuthorizedRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.responsible.AppResponsibleRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.type.AppAuthorizedTypeLinkRepository;
import es.caib.invai.back.persistence.repository.maintenance.responsible.authorizationType.AuthorizationTypeRepository;
import es.caib.invai.back.persistence.repository.maintenance.responsible.person.PersonRepository;
import es.caib.invai.back.rest.soffid.SoffidClient;
import es.caib.invai.back.rest.soffid.SoffidUser;
import es.caib.invai.back.service.facade.maintenance.responsible.roleTransfer.RoleTransferService;
import es.caib.invai.back.service.mapper.catalog.responsibleType.ResponsibleTypeMapper;
import es.caib.invai.back.service.mapper.maintenance.responsible.authorizationType.AuthorizationTypeMapper;
import es.caib.invai.back.service.model.application.responsibleAuthorized.authorized.AppAuthorized;
import es.caib.invai.back.service.model.application.responsibleAuthorized.responsible.AppResponsible;
import es.caib.invai.back.service.model.maintenance.responsible.person.Person;
import es.caib.invai.back.service.model.maintenance.responsible.roleTransfer.RoleAssignmentType;
import es.caib.invai.back.utils.Constants;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Facade service implementation for the "Transferència de rols" maintenance screen. Owns the
 * fan-out between {@link AppResponsibleRepository} and {@link AppAuthorizedRepository}: each batch
 * item is dispatched to the repository matching its {@link RoleAssignmentType}, and the whole batch
 * runs inside a single transaction so a failure on any item rolls back the entire transfer.
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
     * Mapper converting the linked responsible type into its output DTO representation.
     */
    @Autowired
    private ResponsibleTypeMapper responsibleTypeMapper;

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

    @Override
    @Transactional(readOnly = true)
    public List<RoleAssignmentOutputDTO> getAssignmentsByPerson(Long personId) {
        log.debug("Facade: Fetching every active role assignment held by person ID: {}", personId);

        List<RoleAssignmentOutputDTO> assignments = new ArrayList<>();

        for (AppResponsible responsible : appResponsibleRepository.findAllActiveByPersonId(personId)) {
            assignments.add(RoleAssignmentOutputDTO.builder()
                    .id(responsible.getId())
                    .type(RoleAssignmentType.RESPONSIBLE)
                    .applicationId(responsible.getAppResponsibleAuthorized().getApplication().getId())
                    .applicationName(responsible.getAppResponsibleAuthorized().getApplication().getName())
                    .responsibleType(responsibleTypeMapper.toResponse(responsible.getResponsibleType()))
                    .build());
        }

        for (AppAuthorized authorized : appAuthorizedRepository.findAllActiveByPersonId(personId)) {
            assignments.add(RoleAssignmentOutputDTO.builder()
                    .id(authorized.getId())
                    .type(RoleAssignmentType.AUTHORIZED)
                    .applicationId(authorized.getAppResponsibleAuthorized().getApplication().getId())
                    .applicationName(authorized.getAppResponsibleAuthorized().getApplication().getName())
                    .authorizationTypes(resolveAuthorizationTypes(authorized.getId()))
                    .build());
        }

        return assignments;
    }

    @Override
    public void transfer(RoleTransferInputDTO inputDTO) {
        log.info("Facade: Transferring {} role assignment(s) (revoke={})", inputDTO.getItems().size(), inputDTO.isRevoke());

        if (!inputDTO.isRevoke() && StringUtils.isBlank(inputDTO.getToPersonEmailAddress())) {
            throw new BusinessRuleException(Constants.ERR_ROLETRANSFER_TARGET_REQUIRED);
        }

        Long toPersonId = inputDTO.isRevoke() ? null : resolveTargetPersonId(inputDTO.getToPersonEmailAddress());

        for (RoleTransferItemDTO item : inputDTO.getItems()) {
            if (item.getType() == RoleAssignmentType.RESPONSIBLE) {
                transferResponsible(item.getId(), inputDTO.isRevoke(), toPersonId);
            } else {
                transferAuthorized(item.getId(), inputDTO.isRevoke(), toPersonId);
            }
        }
    }

    /**
     * Resolves the destination person for a transfer by e-mail: reuses an existing active local
     * {@link Person} when one already matches, otherwise looks the e-mail up against Soffid and, if
     * found there, persists it locally as a new CAIB person before the transfer proceeds.
     *
     * @param email the destination person's e-mail address
     * @return the identifier of the (possibly newly created) local person to assign
     * @throws BusinessRuleException if no person with the given e-mail exists locally or in Soffid
     */
    private Long resolveTargetPersonId(String email) {
        Person existing = personRepository.findByEmail(email);
        if (existing != null) {
            return existing.getId();
        }
        SoffidUser soffidUser = soffidClient.findByEmail(email);
        if (soffidUser == null) {
            throw new BusinessRuleException(Constants.ERR_ROLETRANSFER_TARGET_NOT_FOUND);
        }
        Person newPerson = new Person();
        newPerson.setFirstName(soffidUser.getFirstName());
        newPerson.setLastName(soffidUser.getLastName());
        newPerson.setEmail(soffidUser.getEmailAddress());
        newPerson.setPersonalCaib(true);
        return personRepository.create(newPerson).getId();
    }

    /**
     * Transfers or revokes a single AppResponsible assignment.
     *
     * @param id         the AppResponsible identifier to transfer or revoke
     * @param revoke     whether to soft-delete the assignment instead of reassigning it
     * @param toPersonId the already-resolved destination person identifier, ignored when revoking
     */
    private void transferResponsible(Long id, boolean revoke, Long toPersonId) {
        AppResponsible existing = appResponsibleRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_APPRESPONSIBLE_NOT_FOUND);
        }
        if (existing.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_APPRESPONSIBLE_NOT_ACTIVE);
        }

        if (revoke) {
            existing.setDeletedAt(LocalDateTime.now());
            existing.setDeletedBy(Utils.resolveCurrentUsername());
            appResponsibleRepository.delete(existing);
            return;
        }
        existing.getPerson().setId(toPersonId);
        appResponsibleRepository.update(existing, id);

    }

    /**
     * Transfers or revokes a single AppAuthorized assignment. When transferring, verifies the
     * target person does not already hold an active authorization on the same anchor.
     *
     * @param id         the AppAuthorized identifier to transfer or revoke
     * @param revoke     whether to soft-delete the assignment instead of reassigning it
     * @param toPersonId the already-resolved destination person identifier, ignored when revoking
     */
    private void transferAuthorized(Long id, boolean revoke, Long toPersonId) {
        AppAuthorized existing = appAuthorizedRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_APPAUTHORIZED_NOT_FOUND);
        }
        if (existing.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_APPAUTHORIZED_NOT_ACTIVE);
        }

        if (revoke) {
            existing.setDeletedAt(LocalDateTime.now());
            existing.setDeletedBy(Utils.resolveCurrentUsername());
            appAuthorizedRepository.delete(existing);
            return;
        }

        Long anchorId = existing.getAppResponsibleAuthorized().getId();
        if (appAuthorizedRepository.existsByAppResponsibleAuthorizedAndPersonAndIdNot(anchorId, toPersonId, id)) {
            throw new BusinessRuleException(Constants.ERR_APPAUTHORIZED_DUPLICATED);
        }
        existing.getPerson().setId(toPersonId);
        appAuthorizedRepository.update(existing, id);
    }

    /**
     * Resolves the full output representation of the authorization types currently attached to an
     * AppAuthorized anchor.
     *
     * @param appAuthorizedId the AppAuthorized anchor identifier
     * @return the output DTOs of the attached authorization types
     */
    private List<AuthorizationTypeOutputDTO> resolveAuthorizationTypes(Long appAuthorizedId) {
        return appAuthorizedTypeLinkRepository.findAllByAppAuthorizedId(appAuthorizedId).stream()
                .map(link -> authorizationTypeRepository.findById(link.getAuthorizationTypeId()))
                .filter(Objects::nonNull)
                .map(authorizationTypeMapper::toResponse)
                .toList();
    }
}

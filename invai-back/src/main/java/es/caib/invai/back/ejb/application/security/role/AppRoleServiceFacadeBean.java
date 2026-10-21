package es.caib.invai.back.ejb.application.security.role;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import es.caib.invai.back.exception.SoffidClientException;
import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import es.caib.invai.back.rest.soffid.SoffidClient;
import es.caib.invai.back.service.model.maintenance.security.securityRole.SecurityRole;
import es.caib.invai.back.service.model.application.security.core.AppSecurity;
import es.caib.invai.back.rest.soffid.SoffidRole;
import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.security.role.DTO.AppRoleInputDTO;
import es.caib.invai.back.interna.application.security.role.DTO.AppRoleOutputDTO;
import es.caib.invai.back.persistence.repository.application.security.core.AppSecurityRepository;
import es.caib.invai.back.persistence.repository.application.security.role.AppRoleCriteria;
import es.caib.invai.back.persistence.repository.application.security.role.AppRoleRepository;
import es.caib.invai.back.service.facade.application.security.role.AppRoleService;
import es.caib.invai.back.service.mapper.application.security.role.AppRoleMapper;
import es.caib.invai.back.service.mapper.maintenance.security.securityRole.SecurityRoleMapper;
import es.caib.invai.back.service.model.application.security.role.AppRole;
import es.caib.invai.back.service.model.catalog.validationStatus.ValidationStatusEnum;
import es.caib.invai.back.utils.Constants;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Facade service implementation for managing security role assignments linked to application
 * security anchors. Orchestrates transactional mechanics, model mapping mutations, and
 * validation constraints verification.
 *
 * @since 1.0.4
 */
@Service
@Slf4j
@Transactional
public class AppRoleServiceFacadeBean implements AppRoleService {

    /** MapStruct mapper handling transformations between entities, domain models, and DTO layouts. */
    @Autowired
    private AppRoleMapper appRoleMapper;

    /** MapStruct mapper copying the data of a Soffid role into the security role saved with an assignment. */
    @Autowired
    private SecurityRoleMapper securityRoleMapper;

    /** Soffid roles asked per call when looking for those named after an application's prefix. */
    private static final int ROLE_PAGE_SIZE = 100;

    /** Safety stop on the number of role pages read, so a misbehaving Soffid can never keep this looping. */
    private static final int MAX_ROLE_PAGES = 20;

    /** Infrastructure outbound repository port managing relational lifecycle data operations. */
    @Autowired
    private AppRoleRepository appRoleRepository;

    /** Repository port used to reach, from a security anchor, the prefix of the application it belongs to. */
    @Autowired
    private AppSecurityRepository appSecurityRepository;

    /** Port used to get from Soffid the data of each role being assigned. */
    @Autowired
    private SoffidClient soffidClient;

    /**
     * Streams partitioned chunk metrics using pagination layout boundaries, scoped to a single
     * parent security anchor. Never returns role assignments belonging to other security anchors.
     *
     * @param appSecurityId mandatory parent security anchor identifier scoping the result set
     * @param criteria      the multi-parameter business query filter boundaries
     * @param pageable      pagination layout boundaries and sorting rules configuration
     * @return a partitioned matrix page containing mapped output definitions
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AppRoleOutputDTO> getAll(Long appSecurityId, AppRoleCriteria criteria, Pageable pageable) {
        log.debug("Facade: Executing dynamic search pattern pipeline across application role components for appSecurity ID: {}", appSecurityId);
        Page<AppRole> domainPage = appRoleRepository.findAll(appSecurityId, criteria, pageable);
        return domainPage.map(appRoleMapper::toResponse);
    }

    /**
     * Registers a new role assignment tracking profile linking a security anchor to a security role.
     * Performs sanitization before commit hooks trigger. Its validation state is resolved against the
     * application's prefix; being added by hand, a role that doesn't match it is saved already
     * {@code MANUAL} (nobody needs to vouch for what a person has just chosen).
     *
     * @param inputDTO property dataset containing assignment mappings and configuration variables
     * @return the newly created snapshot parameters state model
     */
    @Override
    public AppRoleOutputDTO create(AppRoleInputDTO inputDTO) {
        log.info("Facade: Initializing deployment checks before persisting application role tracking metadata for Security ID: {} to Soffid role ID: {}",
                inputDTO.getAppSecurityId(), inputDTO.getSecurityRoleId());

        Utils.sanitize(inputDTO);

        return assign(inputDTO.getAppSecurityId(), getSoffidRole(inputDTO.getSecurityRoleId()),
                getPrefix(inputDTO.getAppSecurityId()), true);
    }

    /** {@inheritDoc} */
    @Override
    public AppRoleOutputDTO assign(Long appSecurityId, SoffidRole soffidRole) {
        return assign(appSecurityId, soffidRole, getPrefix(appSecurityId), false);
    }

    /**
     * Saves the assignment of a Soffid role to a security anchor, with its validation state resolved
     * against the application's prefix.
     *
     * @param prefix the prefix of the application owning the anchor
     * @param manual {@code true} if a person chose this role, so one that doesn't match the prefix is
     *               saved already {@code MANUAL}; {@code false} if it was assigned automatically
     */
    private AppRoleOutputDTO assign(Long appSecurityId, SoffidRole soffidRole, String prefix, boolean manual) {
        log.info("Facade: Assigning Soffid role ID: {} to Security ID: {}", soffidRole.getId(), appSecurityId);

        AppSecurity appSecurity = new AppSecurity();
        appSecurity.setId(appSecurityId);
        AppRole domainModel = new AppRole();
        domainModel.setAppSecurity(appSecurity);
        domainModel.setSecurityRole(securityRoleMapper.toModelFromSoffid(soffidRole));
        applyValidation(domainModel, prefix, manual);
        return appRoleMapper.toResponse(appRoleRepository.create(domainModel));
    }

    /** {@inheritDoc} */
    @Override
    public List<AppRoleOutputDTO> assignByPrefix(Long appSecurityId, String prefix) {
        log.info("Facade: Assigning the Soffid roles named after prefix '{}' to Security ID: {}", prefix, appSecurityId);

        List<SoffidRole> soffidRoles = getSoffidRolesByPrefix(prefix);
        Set<Long> assignedSoffidRoleIds = getAssignedSoffidRoleIds(appSecurityId);
        List<AppRoleOutputDTO> assigned = soffidRoles.stream()
                .filter(role -> !assignedSoffidRoleIds.contains(role.getId()))
                .map(role -> assign(appSecurityId, role, prefix, false))
                .toList();
        log.info("Facade: {} of the {} Soffid role(s) named after prefix '{}' newly assigned to Security ID: {}",
                assigned.size(), soffidRoles.size(), prefix, appSecurityId);
        return assigned;
    }

    /**
     * Modifies mutable tracking assignment variables belonging to an active existing metadata allocation mapping.
     * Its validation state is resolved again against the application's prefix, as when creating it by
     * hand, dropping any earlier manual validation (it was about the previous role).
     *
     * @param id       targeted structural identifier element index
     * @param inputDTO property data variables mapping structural items to be merged
     * @return current modified configuration state properties details wrapper
     * @throws BusinessRuleException if the target record is missing
     */
    @Override
    public AppRoleOutputDTO update(Long id, AppRoleInputDTO inputDTO) {
        log.info("Facade: Initiating security validation rules check prior to executing transaction merge on ID: {}", id);

        AppRole existingModel = appRoleRepository.findById(id);
        if (Objects.isNull(existingModel)) {
            throw new BusinessRuleException(Constants.ERR_APP_ROLE_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        appRoleMapper.updateModelFromInput(inputDTO, existingModel);
        SecurityRole securityRole = securityRoleMapper.toModelFromSoffid(getSoffidRole(inputDTO.getSecurityRoleId()));
        if (Objects.nonNull(existingModel.getSecurityRole())) {
            securityRole.setId(existingModel.getSecurityRole().getId());
        }
        existingModel.setSecurityRole(securityRole);
        applyValidation(existingModel, getPrefix(existingModel.getAppSecurity().getId()), true);
        return appRoleMapper.toResponse(appRoleRepository.update(existingModel, id));
    }

    /** {@inheritDoc} */
    @Override
    public void validate(Long id) {
        log.info("Facade: Manually validating application role ID: {}", id);

        AppRole existingModel = appRoleRepository.findById(id);
        if (Objects.isNull(existingModel)) {
            throw new BusinessRuleException(Constants.ERR_APP_ROLE_NOT_FOUND);
        }
        if (Objects.nonNull(existingModel.getDeletedAt())) {
            throw new BusinessRuleException(Constants.ERR_APP_ROLE_NOT_ACTIVE);
        }
        if (existingModel.getValidationStatus() != ValidationStatusEnum.NOT_VALIDATED) {
            throw new BusinessRuleException(Constants.ERR_APP_ROLE_MANUAL_VALIDATION_NOT_ALLOWED);
        }

        existingModel.setValidationStatus(ValidationStatusEnum.MANUAL);
        existingModel.setManualValidatedAt(LocalDateTime.now());
        existingModel.setManualValidatedBy(Utils.resolveCurrentUsername());

        appRoleRepository.update(existingModel, id);
    }

    /** {@inheritDoc} */
    @Override
    public void recalculateValidation(Long appSecurityId, String prefix) {
        log.info("Facade: Recalculating the validation state of the roles of Security ID: {} against prefix '{}'", appSecurityId, prefix);

        AppRoleCriteria criteria = AppRoleCriteria.builder().statusId(StatusEnum.ACTIVE.getId()).build();
        for (AppRole appRole : appRoleRepository.findAll(appSecurityId, criteria, Pageable.unpaged())) {
            if (appRole.getValidationStatus() == ValidationStatusEnum.MANUAL) {
                continue;
            }
            ValidationStatusEnum resolved = resolveStatus(appRole.getSecurityRole().getName(), prefix);
            if (resolved != appRole.getValidationStatus()) {
                appRole.setValidationStatus(resolved);
                appRoleRepository.update(appRole, appRole.getId());
            }
        }
    }

    /**
     * Gets the prefix of the application owning the given security anchor.
     *
     * @return the prefix, or {@code null} if the anchor or its application can't be resolved
     */
    private String getPrefix(Long appSecurityId) {
        AppSecurity appSecurity = appSecurityRepository.findById(appSecurityId);
        return Objects.nonNull(appSecurity) && Objects.nonNull(appSecurity.getApplication()) ? appSecurity.getApplication().getPrefix() : null;
    }

    /**
     * Resolves the validation state of a role against an application's prefix: {@code VALIDATED} if
     * the role's name starts with the prefix followed by an underscore (case-sensitive, like the
     * matching done when assigning the roles automatically), {@code NOT_VALIDATED} if it doesn't,
     * and {@code NOT_APPLY} if the prefix is unknown, so there is nothing to compare it with.
     */
    private ValidationStatusEnum resolveStatus(String roleName, String prefix) {
        if (StringUtils.isBlank(prefix)) {
            return ValidationStatusEnum.NOT_APPLY;
        }
        return StringUtils.startsWith(roleName, prefix + "_") ? ValidationStatusEnum.VALIDATED : ValidationStatusEnum.NOT_VALIDATED;
    }

    /**
     * Sets the validation state of an assignment being created or rewritten, dropping any manual
     * validation it had.
     *
     * @param manual {@code true} if a person chose the role by hand, so one that doesn't match the
     *               prefix becomes {@code MANUAL}, stamped with the moment and the current user;
     *               {@code false} if it was assigned automatically
     */
    private void applyValidation(AppRole appRole, String prefix, boolean manual) {
        ValidationStatusEnum status = resolveStatus(appRole.getSecurityRole().getName(), prefix);
        appRole.setManualValidatedAt(null);
        appRole.setManualValidatedBy(null);
        if (manual && status == ValidationStatusEnum.NOT_VALIDATED) {
            status = ValidationStatusEnum.MANUAL;
            appRole.setManualValidatedAt(LocalDateTime.now());
            appRole.setManualValidatedBy(Utils.resolveCurrentUsername());
        }
        appRole.setValidationStatus(status);
    }

    /**
     * Gets, from Soffid, the role with the given Soffid id: its data is never taken from the caller.
     *
     * @throws BusinessRuleException if Soffid has no role with that id
     */
    private SoffidRole getSoffidRole(Long soffidRoleId) {
        return soffidClient.getRolesByIds(List.of(soffidRoleId)).stream()
                .filter(role -> soffidRoleId.equals(role.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessRuleException(Constants.ERR_SOFFID_ROLE_NOT_FOUND));
    }

    /**
     * Finds, paging through Soffid's answer, the roles whose name starts with the given prefix and an
     * underscore. Soffid matches the text anywhere in the name, so its answer is narrowed down to
     * the names that really start with it, and repeated roles are dropped.
     *
     * @throws es.caib.invai.back.exception.SoffidTimeoutException if Soffid takes too long to answer
     * @throws BusinessRuleException                               if Soffid can't be queried
     */
    private List<SoffidRole> getSoffidRolesByPrefix(String prefix) {
        String namePrefix = prefix + "_";
        Map<Long, SoffidRole> rolesById = new LinkedHashMap<>();
        try {
            for (int page = 0; page < MAX_ROLE_PAGES; page++) {
                List<SoffidRole> content = soffidClient.searchRoles(namePrefix, PageRequest.of(page, ROLE_PAGE_SIZE)).getContent();
                content.stream()
                        .filter(role -> Objects.nonNull(role.getId()) && StringUtils.startsWith(role.getName(), namePrefix))
                        .forEach(role -> rolesById.putIfAbsent(role.getId(), role));
                if (content.size() < ROLE_PAGE_SIZE) {
                    break;
                }
            }
        } catch (SoffidClientException e) {
            log.error("Could not search the Soffid roles named after prefix '{}'", prefix, e);
            throw new BusinessRuleException(Constants.ERR_APP_SOFFID_ROLES_UNAVAILABLE);
        }
        return List.copyOf(rolesById.values());
    }

    /**
     * Gets the Soffid ids of the roles the given security anchor currently has assigned (active
     * assignments only).
     */
    private Set<Long> getAssignedSoffidRoleIds(Long appSecurityId) {
        AppRoleCriteria criteria = AppRoleCriteria.builder().statusId(StatusEnum.ACTIVE.getId()).build();
        return appRoleRepository.findAll(appSecurityId, criteria, Pageable.unpaged()).stream()
                .map(role -> role.getSecurityRole().getRoleId())
                .collect(Collectors.toSet());
    }

    /**
     * Executes soft deactivation over targeted application role assignments.
     * Updates structural tracking audit traces to mirror administrative termination states.
     *
     * @param id persistent tracking row database reference index targeting removal execution paths
     * @throws BusinessRuleException if the target data element cannot be resolved or has already undergone soft deactivation routines
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Evaluating infrastructure components prior to setting logical deletion properties for target: {}", id);

        AppRole existingModel = appRoleRepository.findById(id);
        if (Objects.isNull(existingModel)) {
            throw new BusinessRuleException(Constants.ERR_APP_ROLE_NOT_FOUND);
        }

        if (Objects.nonNull(existingModel.getDeletedAt())) {
            throw new BusinessRuleException(Constants.ERR_APP_ROLE_NOT_ACTIVE);
        }

        existingModel.setDeletedAt(LocalDateTime.now());
        existingModel.setDeletedBy(Utils.resolveCurrentUsername());

        appRoleRepository.delete(existingModel);
        log.info("Facade: Completed logical deprecation process tree successfully for entity tracking mapping");
    }
}

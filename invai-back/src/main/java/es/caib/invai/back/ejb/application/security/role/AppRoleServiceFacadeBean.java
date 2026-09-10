package es.caib.invai.back.ejb.application.security.role;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.security.role.DTO.AppRoleInputDTO;
import es.caib.invai.back.interna.application.security.role.DTO.AppRoleOutputDTO;
import es.caib.invai.back.persistence.repository.application.security.role.AppRoleCriteria;
import es.caib.invai.back.persistence.repository.application.security.role.AppRoleRepository;
import es.caib.invai.back.service.facade.application.security.role.AppRoleService;
import es.caib.invai.back.service.mapper.application.security.role.AppRoleMapper;
import es.caib.invai.back.service.model.application.security.role.AppRole;
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

    /** Infrastructure outbound repository port managing relational lifecycle data operations. */
    @Autowired
    private AppRoleRepository appRoleRepository;

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
     * Performs sanitization before commit hooks trigger.
     *
     * @param inputDTO property dataset containing assignment mappings and configuration variables
     * @return the newly created snapshot parameters state model
     */
    @Override
    public AppRoleOutputDTO create(AppRoleInputDTO inputDTO) {
        log.info("Facade: Initializing deployment checks before persisting application role tracking metadata for Security ID: {} to Role ID: {}",
                inputDTO.getAppSecurityId(), inputDTO.getSecurityRoleId());

        Utils.sanitize(inputDTO);

        AppRole domainModel = appRoleMapper.toModelFromInput(inputDTO);
        AppRole savedModel = appRoleRepository.create(domainModel);
        return appRoleMapper.toResponse(savedModel);
    }

    /**
     * Modifies mutable tracking assignment variables belonging to an active existing metadata allocation mapping.
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
        if (existingModel == null) {
            throw new BusinessRuleException(Constants.ERR_APP_ROLE_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        appRoleMapper.updateModelFromInput(inputDTO, existingModel);
        return appRoleMapper.toResponse(appRoleRepository.update(existingModel, id));
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
        if (existingModel == null) {
            throw new BusinessRuleException(Constants.ERR_APP_ROLE_NOT_FOUND);
        }

        if (existingModel.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_APP_ROLE_NOT_ACTIVE);
        }

        existingModel.setDeletedAt(LocalDateTime.now());
        existingModel.setDeletedBy(Utils.resolveCurrentUsername());

        appRoleRepository.delete(existingModel);
        log.info("Facade: Completed logical deprecation process tree successfully for entity tracking mapping");
    }
}

package es.caib.invai.back.ejb.application.security.risk;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.security.risk.DTO.AppSecurityRiskInputDTO;
import es.caib.invai.back.interna.application.security.risk.DTO.AppSecurityRiskOutputDTO;
import es.caib.invai.back.persistence.repository.application.security.risk.AppSecurityRiskCriteria;
import es.caib.invai.back.persistence.repository.application.security.risk.AppSecurityRiskRepository;
import es.caib.invai.back.service.facade.application.security.risk.AppSecurityRiskService;
import es.caib.invai.back.service.mapper.application.security.risk.AppSecurityRiskMapper;
import es.caib.invai.back.service.model.application.security.risk.AppSecurityRisk;
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
 * Facade service implementation for managing application security risk records.
 * Orchestrates transactional mechanics, model mapping mutations, and validation constraints verification.
 *
 * @since 1.0.4
 */
@Service
@Slf4j
@Transactional
public class AppSecurityRiskServiceFacadeBean implements AppSecurityRiskService {

    /** MapStruct mapper handling transformations between entities, domain models, and DTO layouts. */
    @Autowired
    private AppSecurityRiskMapper appSecurityRiskMapper;

    /** Infrastructure outbound repository port managing relational lifecycle data operations. */
    @Autowired
    private AppSecurityRiskRepository appSecurityRiskRepository;

    /**
     * Streams partitioned chunk metrics using pagination layout boundaries, scoped to a single
     * parent security anchor. Never returns security risks belonging to other security anchors.
     *
     * @param appSecurityId mandatory parent security anchor identifier scoping the result set
     * @param pageable      pagination layout boundaries and sorting rules configuration
     * @return a partitioned matrix page containing mapped output definitions
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AppSecurityRiskOutputDTO> getAll(Long appSecurityId, AppSecurityRiskCriteria criteria, Pageable pageable) {
        log.debug("Facade: Executing dynamic search pattern pipeline across application security risk components for appSecurity ID: {}", appSecurityId);
        Page<AppSecurityRisk> domainPage = appSecurityRiskRepository.findAll(appSecurityId, criteria, pageable);
        return domainPage.map(appSecurityRiskMapper::toResponse);
    }

    /**
     * Registers a new security risk linked to a security anchor.
     *
     * @param inputDTO properties dataset containing risk mappings and configuration variables
     * @return the newly created snapshot parameters state model
     */
    @Override
    public AppSecurityRiskOutputDTO create(AppSecurityRiskInputDTO inputDTO) {
        log.info("Facade: Initializing deployment checks before persisting application security risk metadata for Security ID: {}",
                inputDTO.getAppSecurityId());

        Utils.sanitize(inputDTO);

        AppSecurityRisk domainModel = appSecurityRiskMapper.toModelFromInput(inputDTO);
        AppSecurityRisk savedModel = appSecurityRiskRepository.create(domainModel);
        return appSecurityRiskMapper.toResponse(savedModel);
    }

    /**
     * Modifies mutable tracking variables belonging to an active existing security risk record.
     *
     * @param id       targeted structural identifier element index
     * @param inputDTO property data variables mapping structural items to be merged
     * @return current modified configuration state properties details wrapper
     * @throws BusinessRuleException if target record is missing or logically deactivated
     */
    @Override
    public AppSecurityRiskOutputDTO update(Long id, AppSecurityRiskInputDTO inputDTO) {
        log.info("Facade: Initiating security validation rules check prior to executing transaction merge on ID: {}", id);

        AppSecurityRisk existingModel = appSecurityRiskRepository.findById(id);
        if (existingModel == null) {
            throw new BusinessRuleException(Constants.ERR_APP_SECURITY_RISK_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        appSecurityRiskMapper.updateModelFromInput(inputDTO, existingModel);
        return appSecurityRiskMapper.toResponse(appSecurityRiskRepository.update(existingModel, id));
    }

    /**
     * Executes soft deactivation over targeted application security risk records.
     * Updates structural tracking audit traces to mirror administrative termination states.
     *
     * @param id persistent tracking row database reference index targeting removal execution paths
     * @throws BusinessRuleException if target data element cannot be resolved or has already undergone soft deactivation routines
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Evaluating infrastructure components prior to setting logical deletion properties for target: {}", id);

        AppSecurityRisk existingModel = appSecurityRiskRepository.findById(id);
        if (existingModel == null) {
            throw new BusinessRuleException(Constants.ERR_APP_SECURITY_RISK_NOT_FOUND);
        }

        if (existingModel.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_APP_SECURITY_RISK_NOT_ACTIVE);
        }

        existingModel.setDeletedAt(LocalDateTime.now());
        existingModel.setDeletedBy(Utils.resolveCurrentUsername());

        appSecurityRiskRepository.delete(existingModel);
        log.info("Facade: Completed logical deprecation process tree successfully for entity tracking mapping");
    }
}

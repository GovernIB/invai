package es.caib.invai.back.ejb.application.security.measure;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.security.measure.DTO.AppSecurityMeasureInputDTO;
import es.caib.invai.back.interna.application.security.measure.DTO.AppSecurityMeasureOutputDTO;
import es.caib.invai.back.persistence.repository.application.security.measure.AppSecurityMeasureCriteria;
import es.caib.invai.back.persistence.repository.application.security.measure.AppSecurityMeasureRepository;
import es.caib.invai.back.service.facade.application.security.measure.AppSecurityMeasureService;
import es.caib.invai.back.service.mapper.application.security.measure.AppSecurityMeasureMapper;
import es.caib.invai.back.service.model.application.security.measure.AppSecurityMeasure;
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
 * Facade service implementation for managing application security measure entries linked to
 * security anchors. Orchestrates transactional mechanics, model mapping mutations, and
 * validation constraints verification.
 *
 * @since 1.0.4
 */
@Service
@Slf4j
@Transactional
public class AppSecurityMeasureServiceFacadeBean implements AppSecurityMeasureService {

    /** MapStruct mapper handling transformations between entities, domain models, and DTO layouts. */
    @Autowired
    private AppSecurityMeasureMapper appSecurityMeasureMapper;

    /** Infrastructure outbound repository port managing relational lifecycle data operations. */
    @Autowired
    private AppSecurityMeasureRepository appSecurityMeasureRepository;

    /**
     * Streams partitioned chunk metrics using pagination layout boundaries, scoped to a single
     * parent security anchor. Never returns security measures belonging to other anchors.
     *
     * @param appSecurityId mandatory parent security anchor identifier scoping the result set
     * @param criteria      the multi-parameter business query filter boundaries
     * @param pageable      pagination layout boundaries and sorting rules configuration
     * @return a partitioned matrix page containing mapped output definitions
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AppSecurityMeasureOutputDTO> getAll(Long appSecurityId, AppSecurityMeasureCriteria criteria, Pageable pageable) {
        log.debug("Facade: Executing dynamic search pattern pipeline across application security measure components for appSecurity ID: {}", appSecurityId);
        Page<AppSecurityMeasure> domainPage = appSecurityMeasureRepository.findAll(appSecurityId, criteria, pageable);
        return domainPage.map(appSecurityMeasureMapper::toResponse);
    }

    /**
     * Registers a new security measure linked to a security anchor.
     *
     * @param inputDTO properties dataset containing measure mappings and configuration variables
     * @return the newly created snapshot parameters state model
     */
    @Override
    public AppSecurityMeasureOutputDTO create(AppSecurityMeasureInputDTO inputDTO) {
        log.info("Facade: Initializing deployment checks before persisting application security measure metadata for App Security ID: {}",
                inputDTO.getAppSecurityId());

        Utils.sanitize(inputDTO);

        AppSecurityMeasure domainModel = appSecurityMeasureMapper.toModelFromInput(inputDTO);
        AppSecurityMeasure savedModel = appSecurityMeasureRepository.create(domainModel);
        return appSecurityMeasureMapper.toResponse(savedModel);
    }

    /**
     * Modifies mutable tracking variables belonging to an active existing security measure.
     *
     * @param id       targeted structural identifier element index
     * @param inputDTO property data variables mapping structural items to be merged
     * @return current modified configuration state properties details wrapper
     * @throws BusinessRuleException if target record is missing or logically deactivated
     */
    @Override
    public AppSecurityMeasureOutputDTO update(Long id, AppSecurityMeasureInputDTO inputDTO) {
        log.info("Facade: Initiating security validation rules check prior to executing transaction merge on ID: {}", id);

        AppSecurityMeasure existingModel = appSecurityMeasureRepository.findById(id);
        if (existingModel == null) {
            throw new BusinessRuleException(Constants.ERR_APP_SECURITY_MEASURE_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        appSecurityMeasureMapper.updateModelFromInput(inputDTO, existingModel);
        return appSecurityMeasureMapper.toResponse(appSecurityMeasureRepository.update(existingModel, id));
    }

    /**
     * Executes soft deactivation over the targeted security measure.
     * Updates structural tracking audit traces to mirror administrative termination states.
     *
     * @param id persistent tracking row database reference index targeting removal execution paths
     * @throws BusinessRuleException if target data element cannot be resolved or has already undergone soft deactivation routines
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Evaluating infrastructure components prior to setting logical deletion properties for target: {}", id);

        AppSecurityMeasure existingModel = appSecurityMeasureRepository.findById(id);
        if (existingModel == null) {
            throw new BusinessRuleException(Constants.ERR_APP_SECURITY_MEASURE_NOT_FOUND);
        }

        if (existingModel.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_APP_SECURITY_MEASURE_NOT_ACTIVE);
        }

        existingModel.setDeletedAt(LocalDateTime.now());
        existingModel.setDeletedBy(Utils.resolveCurrentUsername());

        appSecurityMeasureRepository.delete(existingModel);
        log.info("Facade: Completed logical deprecation process tree successfully for entity tracking mapping");
    }
}

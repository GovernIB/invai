package es.caib.invai.back.ejb.application.security.ensClassification;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.security.ensClassification.DTO.AppEnsClassificationInputDTO;
import es.caib.invai.back.interna.application.security.ensClassification.DTO.AppEnsClassificationOutputDTO;
import es.caib.invai.back.persistence.repository.application.security.ensClassification.AppEnsClassificationCriteria;
import es.caib.invai.back.persistence.repository.application.security.ensClassification.AppEnsClassificationRepository;
import es.caib.invai.back.service.facade.application.security.ensClassification.AppEnsClassificationService;
import es.caib.invai.back.service.mapper.application.security.ensClassification.AppEnsClassificationMapper;
import es.caib.invai.back.service.model.application.security.ensClassification.AppEnsClassification;
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
 * Facade service implementation for managing ENS classification records hanging off the
 * application security anchor. Orchestrates transactional mechanics, model mapping mutations,
 * and validation constraints verification.
 *
 * @since 1.0.4
 */
@Service
@Slf4j
@Transactional
public class AppEnsClassificationServiceFacadeBean implements AppEnsClassificationService {

    /** MapStruct mapper handling transformations between entities, domain models, and DTO layouts. */
    @Autowired
    private AppEnsClassificationMapper appEnsClassificationMapper;

    /** Infrastructure outbound repository port managing relational lifecycle data operations. */
    @Autowired
    private AppEnsClassificationRepository appEnsClassificationRepository;

    /**
     * Streams partitioned chunk metrics using pagination layout boundaries, scoped to a single
     * parent security anchor. Never returns ENS classification records belonging to other
     * security anchors.
     *
     * @param appSecurityId mandatory parent security anchor identifier scoping the result set
     * @param criteria      the multi-parameter business query filter boundaries
     * @param pageable      pagination layout boundaries and sorting rules configuration
     * @return a partitioned matrix page containing mapped output definitions
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AppEnsClassificationOutputDTO> getAll(Long appSecurityId, AppEnsClassificationCriteria criteria, Pageable pageable) {
        log.debug("Facade: Executing dynamic search pattern pipeline across ENS classification components for appSecurity ID: {}", appSecurityId);
        Page<AppEnsClassification> domainPage = appEnsClassificationRepository.findAll(appSecurityId, criteria, pageable);
        return domainPage.map(appEnsClassificationMapper::toResponse);
    }

    /**
     * Registers a new ENS classification record hanging off the given security anchor.
     * Performs sanitization before commit hooks trigger.
     *
     * @param inputDTO property dataset containing the ENS classification mappings and configuration variables
     * @return the newly created snapshot parameters state model
     */
    @Override
    public AppEnsClassificationOutputDTO create(AppEnsClassificationInputDTO inputDTO) {
        log.info("Facade: Initializing deployment checks before persisting ENS classification metadata for AppSecurity ID: {}",
                inputDTO.getAppSecurityId());

        Utils.sanitize(inputDTO);

        AppEnsClassification domainModel = appEnsClassificationMapper.toModelFromInput(inputDTO);
        AppEnsClassification savedModel = appEnsClassificationRepository.create(domainModel);
        return appEnsClassificationMapper.toResponse(savedModel);
    }

    /**
     * Modifies mutable tracking variables belonging to an active existing ENS classification record.
     *
     * @param id       targeted structural identifier element index
     * @param inputDTO property data variables mapping structural items to be merged
     * @return current modified configuration state properties details wrapper
     * @throws BusinessRuleException if the target record is missing
     */
    @Override
    public AppEnsClassificationOutputDTO update(Long id, AppEnsClassificationInputDTO inputDTO) {
        log.info("Facade: Initiating security validation rules check prior to executing transaction merge on ID: {}", id);

        AppEnsClassification existingModel = appEnsClassificationRepository.findById(id);
        if (existingModel == null) {
            throw new BusinessRuleException(Constants.ERR_APP_ENS_CLASSIFICATION_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        appEnsClassificationMapper.updateModelFromInput(inputDTO, existingModel);
        return appEnsClassificationMapper.toResponse(appEnsClassificationRepository.update(existingModel, id));
    }

    /**
     * Executes soft deactivation over the targeted ENS classification record.
     * Updates structural tracking audit traces to mirror administrative termination states.
     *
     * @param id persistent tracking row database reference index targeting removal execution paths
     * @throws BusinessRuleException if the target data element cannot be resolved or has already undergone soft deactivation routines
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Evaluating infrastructure components prior to setting logical deletion properties for target: {}", id);

        AppEnsClassification existingModel = appEnsClassificationRepository.findById(id);
        if (existingModel == null) {
            throw new BusinessRuleException(Constants.ERR_APP_ENS_CLASSIFICATION_NOT_FOUND);
        }

        if (existingModel.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_APP_ENS_CLASSIFICATION_NOT_ACTIVE);
        }

        existingModel.setDeletedAt(LocalDateTime.now());
        existingModel.setDeletedBy(Utils.resolveCurrentUsername());

        appEnsClassificationRepository.delete(existingModel);
        log.info("Facade: Completed logical deprecation process tree successfully for entity tracking mapping");
    }
}

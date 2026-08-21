package es.caib.invai.back.ejb.application.development.core;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.development.core.DTO.DevelopmentInputDTO;
import es.caib.invai.back.interna.application.development.core.DTO.DevelopmentOutputDTO;
import es.caib.invai.back.persistence.repository.application.development.core.AppDevelopmentRepository;
import es.caib.invai.back.service.facade.application.development.core.AppDevelopmentService;
import es.caib.invai.back.service.mapper.application.development.core.AppDevelopmentMapper;
import es.caib.invai.back.service.model.application.development.core.AppDevelopment;
import es.caib.invai.back.utils.Constants;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Facade service implementation for managing the main application development module detail.
 * Orchestrates transactional mechanics, model mapping mutations, and validation constraints verification.
 *
 * @since 1.0.2
 */
@Service
@Slf4j
@Transactional
public class AppDevelopmentServiceFacadeBean implements AppDevelopmentService {

    /** MapStruct mapper handling transformations between entities, domain models, and DTO layouts. */
    @Autowired
    private AppDevelopmentMapper appDevelopmentMapper;

    /** Infrastructure outbound repository port managing relational lifecycle data operations. */
    @Autowired
    private AppDevelopmentRepository appDevelopmentRepository;

    /**
     * Fetches a single development module detail record by its own primary key.
     *
     * @param id mandatory development record identifier
     * @return the mapped output representation of the resolved record
     */
    @Override
    @Transactional(readOnly = true)
    public DevelopmentOutputDTO getById(Long id) {
        log.info("Facade: Fetching development record for ID: {}", id);
        AppDevelopment domain = appDevelopmentRepository.findById(id);
        return appDevelopmentMapper.toResponse(domain);
    }

    /**
     * Registers a new development module detail record for a corporate application.
     *
     * @param inputDTO properties dataset containing the application, environment and lookup references
     * @return the newly created snapshot parameters state model
     */
    @Override
    public DevelopmentOutputDTO create(DevelopmentInputDTO inputDTO) {
        log.info("Facade: Creating new development for Application ID: {} and Environment ID: {}",
                inputDTO.getApplicationId(), inputDTO.getEnvironmentId());

        Utils.sanitize(inputDTO);

        AppDevelopment domainModel = appDevelopmentMapper.toModelFromInput(inputDTO);
        AppDevelopment savedModel = appDevelopmentRepository.create(domainModel);
        return appDevelopmentMapper.toResponse(savedModel);
    }

    /**
     * Modifies mutable tracking variables belonging to an active existing development record.
     *
     * @param id       targeted structural identifier element index
     * @param inputDTO property data variables mapping structural items to be merged
     * @return current modified configuration state properties details wrapper
     * @throws BusinessRuleException if target record is missing or logically deactivated
     */
    @Override
    public DevelopmentOutputDTO update(Long id, DevelopmentInputDTO inputDTO) {
        log.info("Facade: Updating development ID: {}", id);

        AppDevelopment existingModel = appDevelopmentRepository.findById(id);
        if (existingModel == null) {
            throw new BusinessRuleException(Constants.ERR_DEVELOPMENT_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        appDevelopmentMapper.updateModelFromInput(inputDTO, existingModel);
        return appDevelopmentMapper.toResponse(appDevelopmentRepository.update(existingModel, id));
    }

    /**
     * Executes soft deactivation over the targeted development record.
     *
     * @param id persistent tracking row database reference index targeting removal execution paths
     * @throws BusinessRuleException if target data element cannot be resolved or has already undergone soft deactivation
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Logically deleting development ID: {}", id);

        AppDevelopment existingModel = appDevelopmentRepository.findById(id);
        if (existingModel == null) {
            throw new BusinessRuleException(Constants.ERR_DEVELOPMENT_NOT_FOUND);
        }

        if (existingModel.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_DEVELOPMENT_NOT_ACTIVE);
        }

        existingModel.setDeletedAt(LocalDateTime.now());
        existingModel.setDeletedBy(Utils.resolveCurrentUsername());

        appDevelopmentRepository.delete(existingModel);
    }
}

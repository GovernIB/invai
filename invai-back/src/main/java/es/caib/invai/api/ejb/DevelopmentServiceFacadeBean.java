package es.caib.invai.api.ejb;

import es.caib.invai.api.exception.BusinessRuleException;
import es.caib.invai.api.interna.application.development.core.DTO.DevelopmentInputDTO;
import es.caib.invai.api.interna.application.development.core.DTO.DevelopmentOutputDTO;
import es.caib.invai.api.persistence.repository.application.development.core.DevelopmentRepository;
import es.caib.invai.api.service.facade.DevelopmentService;
import es.caib.invai.api.service.mapper.DevelopmentMapper;
import es.caib.invai.api.service.model.AppDevelopment;
import es.caib.invai.api.utils.Constants;
import es.caib.invai.api.utils.Utils;
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
public class DevelopmentServiceFacadeBean implements DevelopmentService {

    /** MapStruct mapper handling transformations between entities, domain models, and DTO layouts. */
    @Autowired
    private DevelopmentMapper developmentMapper;

    /** Infrastructure outbound repository port managing relational lifecycle data operations. */
    @Autowired
    private DevelopmentRepository developmentRepository;

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
        AppDevelopment domain = developmentRepository.findById(id);
        return developmentMapper.toResponse(domain);
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

        AppDevelopment domainModel = developmentMapper.toModelFromInput(inputDTO);
        AppDevelopment savedModel = developmentRepository.create(domainModel);
        return developmentMapper.toResponse(savedModel);
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

        AppDevelopment existingModel = developmentRepository.findById(id);
        if (existingModel == null) {
            throw new BusinessRuleException(Constants.ERR_DEVELOPMENT_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        developmentMapper.updateModelFromInput(inputDTO, existingModel);
        return developmentMapper.toResponse(developmentRepository.update(existingModel, id));
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

        AppDevelopment existingModel = developmentRepository.findById(id);
        if (existingModel == null) {
            throw new BusinessRuleException(Constants.ERR_DEVELOPMENT_NOT_FOUND);
        }

        if (existingModel.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_DEVELOPMENT_NOT_ACTIVE);
        }

        existingModel.setDeletedAt(LocalDateTime.now());
        existingModel.setDeletedBy(Utils.resolveCurrentUsername());

        developmentRepository.delete(existingModel);
    }
}

package es.caib.invai.back.ejb.application.development.technology;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.development.technology.DTO.AppTechnologyInputDTO;
import es.caib.invai.back.interna.application.development.technology.DTO.AppTechnologyOutputDTO;
import es.caib.invai.back.persistence.repository.application.development.technology.AppTechnologyCriteria;
import es.caib.invai.back.persistence.repository.application.development.technology.AppTechnologyRepository;
import es.caib.invai.back.service.facade.application.development.technology.AppTechnologyService;
import es.caib.invai.back.service.mapper.application.development.technology.AppTechnologyMapper;
import es.caib.invai.back.service.model.application.development.technology.AppTechnology;
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
 * Facade service implementation for managing technology stack entries linked to development modules.
 * Orchestrates transactional mechanics, model mapping mutations, and validation constraints verification.
 *
 * @since 1.0.2
 */
@Service
@Slf4j
@Transactional
public class AppTechnologyServiceFacadeBean implements AppTechnologyService {

    /** MapStruct mapper handling transformations between entities, domain models, and DTO layouts. */
    @Autowired
    private AppTechnologyMapper appTechnologyMapper;

    /** Infrastructure outbound repository port managing relational lifecycle data operations. */
    @Autowired
    private AppTechnologyRepository appTechnologyRepository;

    /**
     * Streams partitioned chunk metrics using pagination layout boundaries, scoped to a single
     * parent development module. Never returns technology records belonging to other development modules.
     *
     * @param appDevelopmentId mandatory parent development identifier scoping the result set
     * @param pageable         pagination layout boundaries and sorting rules configuration
     * @return a partitioned matrix page containing mapped output definitions
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AppTechnologyOutputDTO> getAll(Long appDevelopmentId, AppTechnologyCriteria criteria, Pageable pageable) {
        log.info("Facade: Fetching paged technology records for Development ID: {}", appDevelopmentId);
        Page<AppTechnology> domainPage = appTechnologyRepository.findAll(appDevelopmentId, criteria, pageable);
        return domainPage.map(appTechnologyMapper::toResponse);
    }

    /**
     * Registers a new technology stack entry linked to a development module.
     *
     * @param inputDTO properties dataset containing the development reference and technology details
     * @return the newly created snapshot parameters state model
     */
    @Override
    public AppTechnologyOutputDTO create(AppTechnologyInputDTO inputDTO) {
        log.info("Facade: Creating new technology entry for Development ID: {}", inputDTO.getAppDevelopmentId());

        Utils.sanitize(inputDTO);

        AppTechnology domainModel = appTechnologyMapper.toModelFromInput(inputDTO);
        AppTechnology savedModel = appTechnologyRepository.create(domainModel);
        return appTechnologyMapper.toResponse(savedModel);
    }

    /**
     * Modifies mutable tracking variables belonging to an active existing technology record.
     *
     * @param id       targeted structural identifier element index
     * @param inputDTO property data variables mapping structural items to be merged
     * @return current modified configuration state properties details wrapper
     * @throws BusinessRuleException if target record is missing or logically deactivated
     */
    @Override
    public AppTechnologyOutputDTO update(Long id, AppTechnologyInputDTO inputDTO) {
        log.info("Facade: Updating technology ID: {}", id);

        AppTechnology existingModel = appTechnologyRepository.findById(id);
        if (existingModel == null) {
            throw new BusinessRuleException(Constants.ERR_TECHNOLOGY_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        appTechnologyMapper.updateModelFromInput(inputDTO, existingModel);
        return appTechnologyMapper.toResponse(appTechnologyRepository.update(existingModel, id));
    }

    /**
     * Executes soft deactivation over the targeted technology record.
     *
     * @param id persistent tracking row database reference index targeting removal execution paths
     * @throws BusinessRuleException if target data element cannot be resolved or has already undergone soft deactivation
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Logically deleting technology ID: {}", id);

        AppTechnology existingModel = appTechnologyRepository.findById(id);
        if (existingModel == null) {
            throw new BusinessRuleException(Constants.ERR_TECHNOLOGY_NOT_FOUND);
        }

        if (existingModel.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_TECHNOLOGY_NOT_ACTIVE);
        }

        existingModel.setDeletedAt(LocalDateTime.now());
        existingModel.setDeletedBy(Utils.resolveCurrentUsername());

        appTechnologyRepository.delete(existingModel);
    }
}

package es.caib.invai.back.ejb.application.security.core;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.security.core.DTO.AppSecurityInputDTO;
import es.caib.invai.back.interna.application.security.core.DTO.AppSecurityOutputDTO;
import es.caib.invai.back.persistence.repository.application.security.core.AppSecurityRepository;
import es.caib.invai.back.service.facade.application.security.core.AppSecurityService;
import es.caib.invai.back.service.mapper.application.security.core.AppSecurityMapper;
import es.caib.invai.back.service.model.application.security.core.AppSecurity;
import es.caib.invai.back.utils.Constants;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Facade service implementation for managing application-to-security groupings.
 * Orchestrates transactional mechanics, model mapping mutations, and validation constraints verification.
 *
 * @since 1.0.4
 */
@Service
@Slf4j
@Transactional
public class AppSecurityServiceFacadeBean implements AppSecurityService {

    /**
     * MapStruct mapper handling transformations between entities, domain models, and DTO layouts.
     */
    @Autowired
    private AppSecurityMapper appSecurityMapper;

    /**
     * Infrastructure outbound repository port managing relational lifecycle data operations.
     */
    @Autowired
    private AppSecurityRepository appSecurityRepository;

    /**
     * Streams partitioned chunk metrics using pagination layout boundaries, scoped to a single
     * parent application. Never returns groupings belonging to other applications.
     *
     * @param id mandatory parent application identifier scoping the result set
     * @return a partitioned matrix page containing mapped output definitions
     */
    @Override
    @Transactional(readOnly = true)
    public AppSecurityOutputDTO getById(Long id) {
        log.debug("Facade: Fetching paged application security groupings for security ID: {}", id);
        AppSecurity domain = appSecurityRepository.findById(id);
        return (appSecurityMapper.toResponse(domain));
    }

    /**
     * Registers a new security anchor owned by a corporate application.
     *
     * @param inputDTO property dataset containing the application reference and observation notes
     * @return the newly created snapshot parameters state model
     * @throws BusinessRuleException if the target application already has an active security anchor
     */
    @Override
    public AppSecurityOutputDTO create(AppSecurityInputDTO inputDTO) {
        log.info("Facade: Creating new security anchor for Application ID: {}", inputDTO.getApplicationId());

        AppSecurity existing = appSecurityRepository.findByApplicationId(inputDTO.getApplicationId());
        if (existing != null && existing.getDeletedAt() == null) {
            throw new BusinessRuleException(Constants.ERR_APP_SECURITY_ALREADY_EXISTS);
        }

        Utils.sanitize(inputDTO);

        AppSecurity domainModel = appSecurityMapper.toModelFromInput(inputDTO);
        AppSecurity savedModel = appSecurityRepository.create(domainModel);
        return appSecurityMapper.toResponse(savedModel);
    }

    /**
     * Modifies mutable tracking variables belonging to an active existing anchor.
     *
     * @param id       targeted structural identifier element index
     * @param inputDTO property data variables mapping structural items to be merged
     * @return current modified configuration state properties details wrapper
     * @throws BusinessRuleException if the target record is missing or logically deactivated
     */
    @Override
    public AppSecurityOutputDTO update(Long id, AppSecurityInputDTO inputDTO) {
        log.info("Facade: Updating application security ID: {}", id);

        AppSecurity existingModel = appSecurityRepository.findById(id);
        if (existingModel == null) {
            throw new BusinessRuleException(Constants.ERR_APP_SECURITY_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        appSecurityMapper.updateModelFromInput(inputDTO, existingModel);
        return appSecurityMapper.toResponse(appSecurityRepository.update(existingModel, id));
    }

    /**
     * Executes soft deactivation over the targeted application security anchor.
     *
     * @param id persistent tracking row database reference index targeting removal execution paths
     * @throws BusinessRuleException if target data element cannot be resolved or has already undergone soft deactivation
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Logically deleting application security ID: {}", id);

        AppSecurity existingModel = appSecurityRepository.findById(id);
        if (existingModel == null) {
            throw new BusinessRuleException(Constants.ERR_APP_SECURITY_NOT_FOUND);
        }

        if (existingModel.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_APP_SECURITY_NOT_ACTIVE);
        }

        existingModel.setDeletedAt(LocalDateTime.now());
        existingModel.setDeletedBy(Utils.resolveCurrentUsername());

        appSecurityRepository.delete(existingModel);
    }
}

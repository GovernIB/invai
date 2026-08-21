package es.caib.invai.back.ejb.application.development.provider;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.development.provider.DTO.AppProviderInputDTO;
import es.caib.invai.back.interna.application.development.provider.DTO.AppProviderOutputDTO;
import es.caib.invai.back.persistence.repository.application.development.provider.AppProviderCriteria;
import es.caib.invai.back.persistence.repository.application.development.provider.AppProviderRepository;
import es.caib.invai.back.service.facade.application.development.provider.AppProviderService;
import es.caib.invai.back.service.mapper.application.development.provider.AppProviderMapper;
import es.caib.invai.back.service.model.application.development.provider.AppProvider;
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
 * Facade service implementation for managing provider assignments linked to development modules.
 * Orchestrates transactional mechanics, model mapping mutations, and validation constraints verification.
 *
 * @since 1.0.2
 */
@Service
@Slf4j
@Transactional
public class AppProviderServiceFacadeBean implements AppProviderService {

    /** MapStruct mapper handling transformations between entities, domain models, and DTO layouts. */
    @Autowired
    private AppProviderMapper appProviderMapper;

    /** Infrastructure outbound repository port managing relational lifecycle data operations. */
    @Autowired
    private AppProviderRepository appProviderRepository;

    /**
     * Streams partitioned chunk metrics using pagination layout boundaries, scoped to a single
     * parent development module. Never returns provider records belonging to other development modules.
     *
     * @param appDevelopmentId mandatory parent development identifier scoping the result set
     * @param pageable         pagination layout boundaries and sorting rules configuration
     * @return a partitioned matrix page containing mapped output definitions
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AppProviderOutputDTO> getAll(Long appDevelopmentId, AppProviderCriteria criteria, Pageable pageable) {
        log.info("Facade: Fetching paged provider records for Development ID: {}", appDevelopmentId);
        Page<AppProvider> domainPage = appProviderRepository.findAll(appDevelopmentId, criteria, pageable);
        return domainPage.map(appProviderMapper::toResponse);
    }

    /**
     * Registers a new provider assignment linked to a development module.
     *
     * @param inputDTO properties dataset containing the development reference and provider details
     * @return the newly created snapshot parameters state model
     */
    @Override
    public AppProviderOutputDTO create(AppProviderInputDTO inputDTO) {
        log.info("Facade: Creating new provider assignment for Development ID: {} and company: {}",
                inputDTO.getAppDevelopmentId(), inputDTO.getCompanyName());

        Utils.sanitize(inputDTO);

        AppProvider domainModel = appProviderMapper.toModelFromInput(inputDTO);
        AppProvider savedModel = appProviderRepository.create(domainModel);
        return appProviderMapper.toResponse(savedModel);
    }

    /**
     * Modifies mutable tracking variables belonging to an active existing provider record.
     *
     * @param id       targeted structural identifier element index
     * @param inputDTO property data variables mapping structural items to be merged
     * @return current modified configuration state properties details wrapper
     * @throws BusinessRuleException if target record is missing or logically deactivated
     */
    @Override
    public AppProviderOutputDTO update(Long id, AppProviderInputDTO inputDTO) {
        log.info("Facade: Updating provider ID: {}", id);

        AppProvider existingModel = appProviderRepository.findById(id);
        if (existingModel == null) {
            throw new BusinessRuleException(Constants.ERR_PROVIDER_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        appProviderMapper.updateModelFromInput(inputDTO, existingModel);
        return appProviderMapper.toResponse(appProviderRepository.update(existingModel, id));
    }

    /**
     * Executes soft deactivation over the targeted provider record.
     *
     * @param id persistent tracking row database reference index targeting removal execution paths
     * @throws BusinessRuleException if target data element cannot be resolved or has already undergone soft deactivation
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Logically deleting provider ID: {}", id);

        AppProvider existingModel = appProviderRepository.findById(id);
        if (existingModel == null) {
            throw new BusinessRuleException(Constants.ERR_PROVIDER_NOT_FOUND);
        }

        if (existingModel.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_PROVIDER_NOT_ACTIVE);
        }

        existingModel.setDeletedAt(LocalDateTime.now());
        existingModel.setDeletedBy(Utils.resolveCurrentUsername());

        appProviderRepository.delete(existingModel);
    }
}

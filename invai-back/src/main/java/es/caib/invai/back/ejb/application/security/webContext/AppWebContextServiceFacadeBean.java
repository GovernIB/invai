package es.caib.invai.back.ejb.application.security.webContext;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.security.webContext.DTO.AppWebContextInputDTO;
import es.caib.invai.back.interna.application.security.webContext.DTO.AppWebContextOutputDTO;
import es.caib.invai.back.persistence.repository.application.security.webContext.AppWebContextCriteria;
import es.caib.invai.back.persistence.repository.application.security.webContext.AppWebContextRepository;
import es.caib.invai.back.service.facade.application.security.webContext.AppWebContextService;
import es.caib.invai.back.service.mapper.application.security.webContext.AppWebContextMapper;
import es.caib.invai.back.service.model.application.security.webContext.AppWebContext;
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
 * Facade service implementation for managing web context assignments linked to application
 * security anchors. Orchestrates transactional mechanics, model mapping mutations, and
 * validation constraints verification.
 *
 * @since 1.0.4
 */
@Service
@Slf4j
@Transactional
public class AppWebContextServiceFacadeBean implements AppWebContextService {

    /** MapStruct mapper handling transformations between entities, domain models, and DTO layouts. */
    @Autowired
    private AppWebContextMapper appWebContextMapper;

    /** Infrastructure outbound repository port managing relational lifecycle data operations. */
    @Autowired
    private AppWebContextRepository appWebContextRepository;

    /**
     * Streams partitioned chunk metrics using pagination layout boundaries, scoped to a single
     * parent security anchor. Never returns web context links belonging to other security anchors.
     *
     * @param appSecurityId mandatory parent security anchor identifier scoping the result set
     * @param criteria      the multi-parameter business query filter boundaries
     * @param pageable      pagination layout boundaries and sorting rules configuration
     * @return a partitioned matrix page containing mapped output definitions
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AppWebContextOutputDTO> getAll(Long appSecurityId, AppWebContextCriteria criteria, Pageable pageable) {
        log.debug("Facade: Executing dynamic search pattern pipeline across application web context components for appSecurity ID: {}", appSecurityId);
        Page<AppWebContext> domainPage = appWebContextRepository.findAll(appSecurityId, criteria, pageable);
        return domainPage.map(appWebContextMapper::toResponse);
    }

    /**
     * Registers a new web context assignment linked to a security anchor.
     *
     * @param inputDTO property dataset containing the security anchor reference and web context details
     * @return the newly created snapshot parameters state model
     */
    @Override
    public AppWebContextOutputDTO create(AppWebContextInputDTO inputDTO) {
        log.info("Facade: Creating new web context entry for AppSecurity ID: {}", inputDTO.getAppSecurityId());

        Utils.sanitize(inputDTO);

        AppWebContext domainModel = appWebContextMapper.toModelFromInput(inputDTO);
        AppWebContext savedModel = appWebContextRepository.create(domainModel);
        return appWebContextMapper.toResponse(savedModel);
    }

    /**
     * Modifies mutable tracking link variables belonging to an active existing web context record.
     *
     * @param id       targeted structural identifier element index
     * @param inputDTO property data variables mapping structural items to be merged
     * @return current modified configuration state properties details wrapper
     * @throws BusinessRuleException if the target record is missing or logically deactivated
     */
    @Override
    public AppWebContextOutputDTO update(Long id, AppWebContextInputDTO inputDTO) {
        log.info("Facade: Updating web context ID: {}", id);

        AppWebContext existingModel = appWebContextRepository.findById(id);
        if (existingModel == null) {
            throw new BusinessRuleException(Constants.ERR_APP_WEB_CONTEXT_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        appWebContextMapper.updateModelFromInput(inputDTO, existingModel);
        return appWebContextMapper.toResponse(appWebContextRepository.update(existingModel, id));
    }

    /**
     * Executes soft deactivation over the targeted web context record.
     *
     * @param id persistent tracking row database reference index targeting removal execution paths
     * @throws BusinessRuleException if the target data element cannot be resolved or has already undergone soft deactivation
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Logically deleting web context ID: {}", id);

        AppWebContext existingModel = appWebContextRepository.findById(id);
        if (existingModel == null) {
            throw new BusinessRuleException(Constants.ERR_APP_WEB_CONTEXT_NOT_FOUND);
        }

        if (existingModel.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_APP_WEB_CONTEXT_NOT_ACTIVE);
        }

        existingModel.setDeletedAt(LocalDateTime.now());
        existingModel.setDeletedBy(Utils.resolveCurrentUsername());

        appWebContextRepository.delete(existingModel);
    }
}

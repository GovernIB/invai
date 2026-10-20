package es.caib.invai.back.ejb.application.development.webContext;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.development.webContext.DTO.AppWebContextInputDTO;
import es.caib.invai.back.interna.application.development.webContext.DTO.AppWebContextOutputDTO;
import es.caib.invai.back.persistence.repository.application.development.core.AppDevelopmentRepository;
import es.caib.invai.back.persistence.repository.application.security.core.AppSecurityRepository;
import es.caib.invai.back.persistence.repository.application.security.webContext.AppWebContextCriteria;
import es.caib.invai.back.persistence.repository.application.security.webContext.AppWebContextRepository;
import es.caib.invai.back.service.facade.application.development.webContext.AppWebContextService;
import es.caib.invai.back.service.mapper.application.security.webContext.AppWebContextMapper;
import es.caib.invai.back.service.model.application.development.core.AppDevelopment;
import es.caib.invai.back.service.model.application.security.core.AppSecurity;
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
 * Facade service implementation for Development's view of web context assignments: full CRUD, the
 * only side from which a web context link can be created, edited, or deleted. On {@code create},
 * resolves both the given development anchor and its sibling security anchor (same application)
 * automatically - the client never supplies {@code appSecurityId} directly. Security's view
 * (read-only listing plus the one-way validation action) is handled by
 * {@code ejb.application.security.webContext.AppWebContextServiceFacadeBean}.
 *
 * @since 1.0.5
 */
// Explicit bean name: Spring's default naming only looks at the simple class name, which collides
// with ejb.application.security.webContext.AppWebContextServiceFacadeBean otherwise.
@Service("developmentAppWebContextServiceFacadeBean")
@Slf4j
@Transactional
public class AppWebContextServiceFacadeBean implements AppWebContextService {

    /** MapStruct mapper handling transformations between entities, domain models, and DTO layouts. */
    @Autowired
    private AppWebContextMapper appWebContextMapper;

    /** Infrastructure outbound repository port managing relational lifecycle data operations. */
    @Autowired
    private AppWebContextRepository appWebContextRepository;

    /** Repository resolving the development anchor given on creation. */
    @Autowired
    private AppDevelopmentRepository appDevelopmentRepository;

    /** Repository resolving the sibling security anchor (same application) given on creation. */
    @Autowired
    private AppSecurityRepository appSecurityRepository;

    /**
     * Streams partitioned chunk metrics using pagination layout boundaries, scoped to a single
     * parent development anchor. Never returns web context links belonging to other development anchors.
     *
     * @param appDevelopmentId mandatory parent development anchor identifier scoping the result set
     * @param criteria         the multi-parameter business query filter boundaries
     * @param pageable         pagination layout boundaries and sorting rules configuration
     * @return a partitioned matrix page containing mapped output definitions
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AppWebContextOutputDTO> getAll(Long appDevelopmentId, AppWebContextCriteria criteria, Pageable pageable) {
        log.debug("Facade: Executing dynamic search pattern pipeline across application web context components for appDevelopment ID: {}", appDevelopmentId);
        Page<AppWebContext> domainPage = appWebContextRepository.findAllByAppDevelopmentId(appDevelopmentId, criteria, pageable);
        return domainPage.map(appWebContextMapper::toResponse);
    }

    /**
     * Registers a new web context assignment linked to a development module. Resolves the sibling
     * security anchor of the same application automatically.
     *
     * @param inputDTO property dataset containing the development anchor reference and web context details
     * @return the newly created snapshot parameters state model
     * @throws BusinessRuleException if the given development doesn't exist, or its application has
     * no security anchor yet
     */
    @Override
    public AppWebContextOutputDTO create(AppWebContextInputDTO inputDTO) {
        log.info("Facade: Creating new web context entry for AppDevelopment ID: {}", inputDTO.getAppDevelopmentId());

        AppDevelopment development = appDevelopmentRepository.findById(inputDTO.getAppDevelopmentId());
        if (development == null) {
            throw new BusinessRuleException(Constants.ERR_DEVELOPMENT_NOT_FOUND);
        }

        AppSecurity security = appSecurityRepository.findByApplicationId(development.getApplication().getId());
        if (security == null) {
            throw new BusinessRuleException(Constants.ERR_APP_WEB_CONTEXT_NO_SECURITY_ANCHOR);
        }

        Utils.sanitize(inputDTO);

        AppWebContext domainModel = appWebContextMapper.toModelFromInput(inputDTO);
        domainModel.setAppSecurity(security);
        AppWebContext savedModel = appWebContextRepository.create(domainModel);
        return appWebContextMapper.toResponse(savedModel);
    }

    /**
     * Modifies mutable tracking link variables belonging to an active existing web context record.
     * Never re-resolves the development or security anchor - a record can't be moved to a different
     * development through this method.
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

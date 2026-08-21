package es.caib.invai.back.ejb.application.core;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.core.DTO.ApplicationInputDTO;
import es.caib.invai.back.interna.application.core.DTO.ApplicationOutputDTO;
import es.caib.invai.back.persistence.repository.application.core.ApplicationCriteria;
import es.caib.invai.back.persistence.repository.application.core.ApplicationRepository;
import es.caib.invai.back.persistence.repository.application.development.core.AppDevelopmentRepository;
import es.caib.invai.back.persistence.repository.application.system_database.core.AppInformationSystemDbRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.core.AppResponsibleAuthorizedRepository;
import es.caib.invai.back.service.facade.application.core.ApplicationService;
import es.caib.invai.back.service.mapper.application.core.ApplicationMapper;
import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import es.caib.invai.back.utils.Constants;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import es.caib.invai.back.service.model.application.development.core.AppDevelopment;
import es.caib.invai.back.service.model.application.system_database.core.AppInformationSystemDb;
import es.caib.invai.back.service.model.application.responsibleAuthorized.core.AppResponsibleAuthorized;
import es.caib.invai.back.service.model.application.core.Application;

/**
 * Facade service implementation for managing system Application objects.
 * Handles status assignments, internal cascading operations, and dynamic criteria filtering.
 *
 * @since 1.0.1
 */
@Service
@Slf4j
@Transactional
public class ApplicationServiceFacadeBean implements ApplicationService {

    /** Mapper responsible for converting between Application domain models, entities, and DTOs. */
    @Autowired
    private ApplicationMapper applicationMapper;

    /** Outbound port repository handling persistence operations for the Application domain model. */
    @Autowired
    private ApplicationRepository applicationRepository;

    /** Repository handling persistence of the "AppInformationSystemDb" tab linked to an application. */
    @Autowired
    private AppInformationSystemDbRepository appInformationSystemDbRepository;

    /** Repository handling persistence of the "AppDevelopment" tab linked to an application. */
    @Autowired
    private AppDevelopmentRepository appDevelopmentRepository;

    /** Repository handling persistence of the "Responsables i Autoritzats" tab linked to an application. */
    @Autowired
    private AppResponsibleAuthorizedRepository appResponsibleAuthorizedRepository;

    /**
     * Retrieves an application by its unique identifier, together with its linked
     * information system database, development, and responsible/authorized records.
     *
     * @param id primary key of the application to fetch
     * @return the mapped {@link ApplicationOutputDTO} representation
     * @throws BusinessRuleException if no application exists with the given ID
     */
    @Override
    @Transactional(readOnly = true)
    public ApplicationOutputDTO getById(Long id) {
        log.info(Constants.LOG_FACADE_FETCH_BY_ID, id);
        Application application = applicationRepository.findById(id);

        if (application == null) {
            throw new BusinessRuleException(Constants.ERR_APP_NOT_FOUND);
        }

        AppInformationSystemDb appInformationSystemDb = fetchSingleAppInformationSystemDb(id);
        AppDevelopment appDevelopment = fetchSingleDevelopment(id);
        AppResponsibleAuthorized appResponsibleAuthorized = fetchSingleAppResponsibleAuthorized(id);

        return applicationMapper.toResponse(application, appInformationSystemDb, appDevelopment, appResponsibleAuthorized);
    }

    /**
     * Retrieves a paginated collection of applications matching the given dynamic search criteria.
     *
     * @param criteria dynamic filtering constraints applied to the application query
     * @param pageable pagination and sorting parameters
     * @return a page of mapped {@link ApplicationOutputDTO} results
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ApplicationOutputDTO> getAll(ApplicationCriteria criteria, Pageable pageable) {
        log.info("Facade: Fetching applications using dynamic criteria specifications");
        Page<Application> domainPage = applicationRepository.findAll(criteria, pageable);
        return domainPage.map(applicationMapper::toResponse);
    }

    /**
     * Creates a new application and automatically instantiates and persists default
     * AppInformationSystemDb, Development, and AppResponsibleAuthorized ("Responsables i Autoritzats" tab) records bound to it.
     */
    @Override
    public ApplicationOutputDTO create(ApplicationInputDTO inputDTO) {
        log.info(Constants.LOG_FACADE_CREATE, inputDTO.getCode());

        Utils.sanitize(inputDTO);

        if (applicationRepository.existsByCode(inputDTO.getCode())) {
            throw new BusinessRuleException(Constants.ERR_CODE_DUPLICATED);
        }
        if (applicationRepository.existsByPrefix(inputDTO.getPrefix())) {
            throw new BusinessRuleException(Constants.ERR_PREFIX_DUPLICATED);
        }

        Application model = applicationMapper.toModelFromInput(inputDTO);
        if (model.getStatus() == null) {
            model.setStatus(StatusEnum.ACTIVE);
        }
        Application savedModel = applicationRepository.create(model);

        AppInformationSystemDb infoDbModel = new AppInformationSystemDb();
        infoDbModel.setApplication(savedModel);
        AppInformationSystemDb savedInfoDb = appInformationSystemDbRepository.create(infoDbModel);

        AppDevelopment devModel = new AppDevelopment();
        devModel.setApplication(savedModel);
        AppDevelopment savedDev = appDevelopmentRepository.create(devModel);

        AppResponsibleAuthorized appResponsibleAuthorizedModel = new AppResponsibleAuthorized();
        appResponsibleAuthorizedModel.setApplication(savedModel);
        AppResponsibleAuthorized savedAppResponsibleAuthorized = appResponsibleAuthorizedRepository.create(appResponsibleAuthorizedModel);

        return applicationMapper.toResponse(savedModel, savedInfoDb, savedDev, savedAppResponsibleAuthorized);
    }

    /**
     * Updates an existing application and ensures linked internal records exist (or creates them if missing).
     */
    @Override
    public ApplicationOutputDTO update(Long id, ApplicationInputDTO inputDTO) {
        log.info(Constants.LOG_FACADE_UPDATE, id);

        Application existing = applicationRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_APP_NOT_FOUND);
        }

        if (existing.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_APP_NOT_ACTIVE);
        }

        Utils.sanitize(inputDTO);

        if (applicationRepository.existsByCodeAndIdNot(inputDTO.getCode(), id)) {
            throw new BusinessRuleException(Constants.ERR_CODE_OWNED_BY_OTHER);
        }
        if (applicationRepository.existsByPrefixAndIdNot(inputDTO.getPrefix(), id)) {
            throw new BusinessRuleException(Constants.ERR_PREFIX_OWNED_BY_OTHER);
        }

        applicationMapper.updateModelFromInput(inputDTO, existing);
        Application updatedModel = applicationRepository.update(existing, id);

        AppInformationSystemDb infoDb = fetchSingleAppInformationSystemDb(id);
        if (infoDb == null) {
            infoDb = new AppInformationSystemDb();
            infoDb.setApplication(updatedModel);
            infoDb = appInformationSystemDbRepository.create(infoDb);
        }

        AppDevelopment dev = fetchSingleDevelopment(id);
        if (dev == null) {
            dev = new AppDevelopment();
            dev.setApplication(updatedModel);
            dev = appDevelopmentRepository.create(dev);
        }

        AppResponsibleAuthorized appResponsibleAuthorized = fetchSingleAppResponsibleAuthorized(id);
        if (appResponsibleAuthorized == null) {
            appResponsibleAuthorized = new AppResponsibleAuthorized();
            appResponsibleAuthorized.setApplication(updatedModel);
            appResponsibleAuthorized = appResponsibleAuthorizedRepository.create(appResponsibleAuthorized);
        }

        return applicationMapper.toResponse(updatedModel, infoDb, dev, appResponsibleAuthorized);
    }

    /**
     * Soft deletes the application and sets deletedAt timestamp across all child records.
     */
    @Override
    public void delete(Long id) {
        log.info(Constants.LOG_FACADE_DEACTIVATE, id);
        Application existing = applicationRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_APP_NOT_FOUND);
        }

        if (existing.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_APP_NOT_ACTIVE);
        }

        LocalDateTime now = LocalDateTime.now();
        String currentUser = Utils.resolveCurrentUsername();

        existing.setStatus(StatusEnum.INACTIVE);
        existing.setDeletedAt(now);
        existing.setExpirationDate(now);
        existing.setDeletedBy(currentUser);
        applicationRepository.delete(existing);

        AppInformationSystemDb infoDb = fetchSingleAppInformationSystemDb(id);
        if (infoDb != null && infoDb.getDeletedAt() == null) {
            infoDb.setDeletedAt(now);
            infoDb.setDeletedBy(currentUser);
            appInformationSystemDbRepository.delete(infoDb);
        }

        AppDevelopment dev = fetchSingleDevelopment(id);
        if (dev != null && dev.getDeletedAt() == null) {
            dev.setDeletedAt(now);
            dev.setDeletedBy(currentUser);
            appDevelopmentRepository.delete(dev);
        }

        AppResponsibleAuthorized appResponsibleAuthorized = fetchSingleAppResponsibleAuthorized(id);
        if (appResponsibleAuthorized != null && appResponsibleAuthorized.getDeletedAt() == null) {
            appResponsibleAuthorized.setDeletedAt(now);
            appResponsibleAuthorized.setDeletedBy(currentUser);
            appResponsibleAuthorizedRepository.delete(appResponsibleAuthorized);
        }
    }

    /**
     * Reactivates the application and clears deletedAt timestamp across all child records.
     */
    @Override
    public ApplicationOutputDTO reactivate(Long id) {
        log.info("Facade: Reactivating application and associated records for ID: {}", id);

        Application existing = applicationRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_APP_NOT_FOUND);
        }

        if (existing.getDeletedAt() == null) {
            throw new BusinessRuleException(Constants.ERR_APP_ACTIVE);
        }

        existing.setStatus(StatusEnum.ACTIVE);
        existing.setDeletedAt(null);
        existing.setExpirationDate(null);
        existing.setDeletedBy(null);
        Application updatedModel = applicationRepository.update(existing, id);

        AppInformationSystemDb infoDb = fetchSingleAppInformationSystemDb(id);
        if (infoDb != null && infoDb.getDeletedAt() != null) {
            infoDb.setDeletedAt(null);
            infoDb.setDeletedBy(null);
            infoDb = appInformationSystemDbRepository.update(infoDb, infoDb.getId());
        }

        AppDevelopment dev = fetchSingleDevelopment(id);
        if (dev != null && dev.getDeletedAt() != null) {
            dev.setDeletedAt(null);
            dev.setDeletedBy(null);
            dev = appDevelopmentRepository.update(dev, dev.getId());
        }

        AppResponsibleAuthorized appResponsibleAuthorized = fetchSingleAppResponsibleAuthorized(id);
        if (appResponsibleAuthorized != null && appResponsibleAuthorized.getDeletedAt() != null) {
            appResponsibleAuthorized.setDeletedAt(null);
            appResponsibleAuthorized.setDeletedBy(null);
            appResponsibleAuthorized = appResponsibleAuthorizedRepository.update(appResponsibleAuthorized, appResponsibleAuthorized.getId());
        }

        return applicationMapper.toResponse(updatedModel, infoDb, dev, appResponsibleAuthorized);
    }

    /**
     * Fetches the {@code AppInformationSystemDb} anchor linked to the given application, if any.
     *
     * @param applicationId identifier of the owning application
     * @return the linked information system/database anchor, or {@code null} if none exists
     */
    private AppInformationSystemDb fetchSingleAppInformationSystemDb(Long applicationId) {
        return appInformationSystemDbRepository.findByApplicationId(applicationId);
    }

    /**
     * Fetches the {@code AppDevelopment} record linked to the given application, if any.
     *
     * @param applicationId identifier of the owning application
     * @return the linked development record, or {@code null} if none exists
     */
    private AppDevelopment fetchSingleDevelopment(Long applicationId) {
        return appDevelopmentRepository.findByApplicationId(applicationId);
    }

    /**
     * Fetches the {@code AppResponsibleAuthorized} anchor linked to the given application, if any.
     *
     * @param applicationId identifier of the owning application
     * @return the linked responsible/authorized anchor, or {@code null} if none exists
     */
    private AppResponsibleAuthorized fetchSingleAppResponsibleAuthorized(Long applicationId) {
        return appResponsibleAuthorizedRepository.findByApplicationId(applicationId);
    }
}
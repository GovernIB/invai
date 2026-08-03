package es.caib.invai.api.ejb;

import es.caib.invai.api.exception.BusinessRuleException;
import es.caib.invai.api.interna.application.core.DTO.ApplicationInputDTO;
import es.caib.invai.api.interna.application.core.DTO.ApplicationOutputDTO;
import es.caib.invai.api.persistence.repository.application.core.ApplicationCriteria;
import es.caib.invai.api.persistence.repository.application.core.ApplicationRepository;
import es.caib.invai.api.persistence.repository.application.development.core.DevelopmentRepository;
import es.caib.invai.api.persistence.repository.application.system_database.core.AppInformationSystemDbRepository;
import es.caib.invai.api.service.facade.ApplicationService;
import es.caib.invai.api.service.mapper.ApplicationMapper;
import es.caib.invai.api.service.model.*;
import es.caib.invai.api.utils.Constants;
import es.caib.invai.api.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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

    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private AppInformationSystemDbRepository appInformationSystemDbRepository;

    @Autowired
    private DevelopmentRepository developmentRepository;

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

        return applicationMapper.toResponse(application, appInformationSystemDb, appDevelopment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApplicationOutputDTO> getAll(ApplicationCriteria criteria, Pageable pageable) {
        log.info("Facade: Fetching applications using dynamic criteria specifications");
        Page<Application> domainPage = applicationRepository.findAll(criteria, pageable);
        return domainPage.map(applicationMapper::toResponse);
    }

    /**
     * Creates a new application and automatically instantiates and persists default
     * AppInformationSystemDb and Development records bound to it.
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
        AppDevelopment savedDev = developmentRepository.create(devModel);

        return applicationMapper.toResponse(savedModel, savedInfoDb, savedDev);
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
            dev = developmentRepository.create(dev);
        }

        return applicationMapper.toResponse(updatedModel, infoDb, dev);
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
            developmentRepository.delete(dev);
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
            dev = developmentRepository.update(dev, dev.getId());
        }

        return applicationMapper.toResponse(updatedModel, infoDb, dev);
    }

    private AppInformationSystemDb fetchSingleAppInformationSystemDb(Long applicationId) {
        return appInformationSystemDbRepository.findByApplicationId(applicationId);
    }

    private AppDevelopment fetchSingleDevelopment(Long applicationId) {
        return developmentRepository.findByApplicationId(applicationId);
    }
}
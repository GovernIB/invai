package es.caib.invai.back.ejb.application.core;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.core.DTO.ApplicationInputDTO;
import es.caib.invai.back.interna.application.core.DTO.ApplicationOutputDTO;
import es.caib.invai.back.interna.integrations.dir3.admUnit.DTO.AdmUnitOutputDTO;
import es.caib.invai.back.persistence.repository.application.accessibility.AppAccessibilityRepository;
import es.caib.invai.back.persistence.repository.application.core.ApplicationCriteria;
import es.caib.invai.back.persistence.repository.application.core.ApplicationRepository;
import es.caib.invai.back.persistence.repository.application.development.core.AppDevelopmentRepository;
import es.caib.invai.back.persistence.repository.application.security.ensClassification.AppEnsClassificationCriteria;
import es.caib.invai.back.persistence.repository.application.security.risk.AppSecurityRiskCriteria;
import es.caib.invai.back.persistence.repository.application.security.webContext.AppWebContextCriteria;
import es.caib.invai.back.persistence.repository.application.system_database.core.AppInformationSystemDbRepository;
import es.caib.invai.back.persistence.repository.application.system_database.database.AppDatabaseCriteria;
import es.caib.invai.back.persistence.repository.application.system_database.system.AppSystemCriteria;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.core.AppResponsibleAuthorizedRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.authorized.AppAuthorizedRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.responsible.AppResponsibleRepository;
import es.caib.invai.back.persistence.repository.application.security.core.AppSecurityRepository;
import es.caib.invai.back.persistence.repository.catalog.responsibleType.ResponsibleTypeRepository;
import es.caib.invai.back.service.facade.application.core.ApplicationService;
import es.caib.invai.back.service.facade.application.security.ensClassification.AppEnsClassificationService;
import es.caib.invai.back.service.facade.application.security.risk.AppSecurityRiskService;
import es.caib.invai.back.service.facade.application.security.webContext.AppWebContextService;
import es.caib.invai.back.service.facade.application.system_database.database.AppDatabaseService;
import es.caib.invai.back.service.facade.application.system_database.system.AppSystemService;
import es.caib.invai.back.service.facade.integrations.dir3.admUnit.AdmUnitService;
import es.caib.invai.back.service.mapper.application.core.ApplicationMapper;
import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import es.caib.invai.back.utils.Constants;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import es.caib.invai.back.service.model.application.accessibility.AppAccessibility;
import es.caib.invai.back.service.model.application.development.core.AppDevelopment;
import es.caib.invai.back.service.model.application.system_database.core.AppInformationSystemDb;
import es.caib.invai.back.service.model.application.responsibleAuthorized.core.AppResponsibleAuthorized;
import es.caib.invai.back.service.model.application.security.core.AppSecurity;
import es.caib.invai.back.service.model.application.core.Application;

/**
 * Facade service implementation for managing system Application objects.
 * Handles status assignments, internal cascading operations, and dynamic criteria filtering.
 * <p>
 * {@link #getById} additionally computes completeness flags for the Responsables,
 * Desenvolupament, Accessibilitat, Seguretat, and System Database (Sistemes/Bases de dades) tabs
 * ({@code missingResponsibleTypes}, {@code missingAuthorized}, {@code missingDevelopmentFields},
 * {@code missingAccessibilityFields}, {@code missingSecurityData}, {@code missingSystems},
 * {@code missingDatabases} on {@link ApplicationOutputDTO}) — see {@link #isDevelopmentIncomplete},
 * {@link #hasUnfilledResponsibleType}, {@link #hasNoActiveAuthorized}, {@link
 * #isAccessibilityIncomplete}, {@link #isSecurityIncomplete}, {@link #hasNoActiveSystem} and
 * {@link #hasNoActiveDatabase}. {@code create}/{@code update}/{@code reactivate} do not compute
 * these flags.
 * </p>
 *
 * @since 1.0.1
 */
@Service
@Slf4j
@Transactional
public class ApplicationServiceFacadeBean implements ApplicationService {

    /**
     * A single-row page request, used by the completeness checks below to probe "does at least one
     * active child record exist" without fetching more than necessary.
     */
    private static final Pageable FIRST_ROW = PageRequest.of(0, 1);

    /** Mapper responsible for converting between Application domain models, entities, and DTOs. */
    @Autowired
    private ApplicationMapper applicationMapper;

    /** Outbound port repository handling persistence operations for the Application domain model. */
    @Autowired
    private ApplicationRepository applicationRepository;

    /** Repository handling persistence of the "AppInformationSystemDb" tab linked to an application. */
    @Autowired
    private AppInformationSystemDbRepository appInformationSystemDbRepository;

    /** Facade used to check for at least one active system link on an information system database anchor. */
    @Autowired
    private AppSystemService appSystemService;

    /** Facade used to check for at least one active database link on an information system database anchor. */
    @Autowired
    private AppDatabaseService appDatabaseService;

    /** Repository handling persistence of the "AppDevelopment" tab linked to an application. */
    @Autowired
    private AppDevelopmentRepository appDevelopmentRepository;

    /** Repository handling persistence of the "Responsables i Autoritzats" tab linked to an application. */
    @Autowired
    private AppResponsibleAuthorizedRepository appResponsibleAuthorizedRepository;

    /** Repository providing the full responsible type catalog, used to detect unfilled types. */
    @Autowired
    private ResponsibleTypeRepository responsibleTypeRepository;

    /** Repository used to check for an active holder per responsible type. */
    @Autowired
    private AppResponsibleRepository appResponsibleRepository;

    /** Repository used to check for at least one active authorized person. */
    @Autowired
    private AppAuthorizedRepository appAuthorizedRepository;

    /** Repository handling persistence of the "AppSecurity" tab linked to an application. */
    @Autowired
    private AppSecurityRepository appSecurityRepository;

    /** Repository handling persistence of the "AppAccessibility" tab linked to an application. */
    @Autowired
    private AppAccessibilityRepository appAccessibilityRepository;

    /** Facade used to check for at least one active "Web Context" record on a security anchor. */
    @Autowired
    private AppWebContextService appWebContextService;

    /** Facade used to check for at least one active "ENS Classification" record on a security anchor. */
    @Autowired
    private AppEnsClassificationService appEnsClassificationService;

    /** Facade used to check for at least one active "Security Risk" record on a security anchor. */
    @Autowired
    private AppSecurityRiskService appSecurityRiskService;

    /** Facade used to validate/resolve the administrative unit and department linked to an application, live against DIR3CAIB. */
    @Autowired
    private AdmUnitService admUnitService;

    /**
     * Retrieves an application by its unique identifier, together with its linked
     * information system database, development, and responsible/authorized records. Every tab's
     * completeness flag is computed here and passed straight into
     * {@link ApplicationMapper#toResponse(Application, AppInformationSystemDb, AppDevelopment,
     * AppResponsibleAuthorized, AppSecurity, AppAccessibility, Boolean, Boolean, Boolean, Boolean,
     * Boolean, Boolean, Boolean)} so the response DTO comes back fully populated in one call (see {@link
     * #isDevelopmentIncomplete}, {@link #hasUnfilledResponsibleType}, {@link #hasNoActiveAuthorized},
     * {@link #isAccessibilityIncomplete}, {@link #isSecurityIncomplete}, {@link #hasNoActiveSystem}
     * and {@link #hasNoActiveDatabase}).
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
        AppSecurity appSecurity = fetchSingleAppSecurity(id);
        AppAccessibility appAccessibility = fetchSingleAppAccessibility(id);

        boolean missingDevelopmentFields = isDevelopmentIncomplete(appDevelopment);
        boolean missingResponsibleTypes = hasUnfilledResponsibleType(appResponsibleAuthorized);
        boolean missingAuthorized = hasNoActiveAuthorized(appResponsibleAuthorized);
        boolean missingAccessibilityFields = isAccessibilityIncomplete(appAccessibility);
        boolean missingSecurityData = isSecurityIncomplete(appSecurity);
        boolean missingSystems = hasNoActiveSystem(appInformationSystemDb);
        boolean missingDatabases = hasNoActiveDatabase(appInformationSystemDb);

        ApplicationOutputDTO response = applicationMapper.toResponse(application, appInformationSystemDb, appDevelopment, appResponsibleAuthorized, appSecurity, appAccessibility,
                missingDevelopmentFields, missingResponsibleTypes, missingAuthorized,
                missingAccessibilityFields, missingSecurityData,
                missingSystems, missingDatabases);
        response.setIncomplete(missingDevelopmentFields || missingResponsibleTypes || missingAuthorized
                || missingAccessibilityFields || missingSecurityData || missingSystems || missingDatabases);
        response.setAdmUnit(admUnitService.resolveByCode(application.getAdmUnitCode()));
        response.setDepartment(admUnitService.resolveDepartment(application.getAdmUnitCode()));
        return response;
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
        if (criteria != null && !StringUtils.isBlank(criteria.getQuickSearch())) {
            criteria.setQuickSearchAdmUnitCodes(admUnitService.findCodesByNameContaining(criteria.getQuickSearch()));
        }

        Boolean incompleteFilter = criteria != null ? criteria.getIncomplete() : null;
        if (incompleteFilter != null) {
            List<Long> incompleteIds = applicationRepository.findAll(null, Pageable.unpaged()).stream()
                    .map(Application::getId)
                    .filter(this::isIncomplete)
                    .toList();
            criteria.setIncompleteApplicationIds(incompleteIds);
        }

        Page<Application> domainPage = applicationRepository.findAll(criteria, pageable);
        return domainPage.map(application -> {
            ApplicationOutputDTO dto = applicationMapper.toResponse(application);
            dto.setAdmUnit(admUnitService.resolveByCode(application.getAdmUnitCode()));
            dto.setIncomplete(incompleteFilter != null ? incompleteFilter : isIncomplete(application.getId()));
            return dto;
        });
    }

    /**
     * Creates a new application and automatically instantiates and persists default
     * AppInformationSystemDb, Development, AppResponsibleAuthorized ("Responsables i Autoritzats" tab),
     * AppSecurity ("Seguretat" tab), and AppAccessibility ("Accessibilitat" tab) records bound to it.
     */
    @Override
    public ApplicationOutputDTO create(ApplicationInputDTO inputDTO) {
        log.info(Constants.LOG_FACADE_CREATE, inputDTO.getCode());

        Utils.sanitize(inputDTO);
        validateAdmUnitCode(inputDTO.getAdmUnitCode());

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

        AppSecurity appSecurityModel = new AppSecurity();
        appSecurityModel.setApplication(savedModel);
        AppSecurity savedAppSecurity = appSecurityRepository.create(appSecurityModel);

        AppAccessibility appAccessibilityModel = new AppAccessibility();
        appAccessibilityModel.setApplication(savedModel);
        AppAccessibility savedAppAccessibility = appAccessibilityRepository.create(appAccessibilityModel);

        ApplicationOutputDTO response = applicationMapper.toResponse(savedModel, savedInfoDb, savedDev, savedAppResponsibleAuthorized, savedAppSecurity, savedAppAccessibility);
        response.setAdmUnit(admUnitService.resolveByCode(savedModel.getAdmUnitCode()));
        return response;
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
        validateAdmUnitCode(inputDTO.getAdmUnitCode());

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

        AppSecurity appSecurity = fetchSingleAppSecurity(id);
        if (appSecurity == null) {
            appSecurity = new AppSecurity();
            appSecurity.setApplication(updatedModel);
            appSecurity = appSecurityRepository.create(appSecurity);
        }

        AppAccessibility appAccessibility = fetchSingleAppAccessibility(id);
        if (appAccessibility == null) {
            appAccessibility = new AppAccessibility();
            appAccessibility.setApplication(updatedModel);
            appAccessibility = appAccessibilityRepository.create(appAccessibility);
        }

        ApplicationOutputDTO response = applicationMapper.toResponse(updatedModel, infoDb, dev, appResponsibleAuthorized, appSecurity, appAccessibility);
        response.setAdmUnit(admUnitService.resolveByCode(updatedModel.getAdmUnitCode()));
        return response;
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

        AppSecurity appSecurity = fetchSingleAppSecurity(id);
        if (appSecurity != null && appSecurity.getDeletedAt() == null) {
            appSecurity.setDeletedAt(now);
            appSecurity.setDeletedBy(currentUser);
            appSecurityRepository.delete(appSecurity);
        }

        AppAccessibility appAccessibility = fetchSingleAppAccessibility(id);
        if (appAccessibility != null && appAccessibility.getDeletedAt() == null) {
            appAccessibility.setDeletedAt(now);
            appAccessibility.setDeletedBy(currentUser);
            appAccessibilityRepository.delete(appAccessibility);
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

        AppSecurity appSecurity = fetchSingleAppSecurity(id);
        if (appSecurity != null && appSecurity.getDeletedAt() != null) {
            appSecurity.setDeletedAt(null);
            appSecurity.setDeletedBy(null);
            appSecurity = appSecurityRepository.update(appSecurity, appSecurity.getId());
        }

        AppAccessibility appAccessibility = fetchSingleAppAccessibility(id);
        if (appAccessibility != null && appAccessibility.getDeletedAt() != null) {
            appAccessibility.setDeletedAt(null);
            appAccessibility.setDeletedBy(null);
            appAccessibility = appAccessibilityRepository.update(appAccessibility, appAccessibility.getId());
        }

        ApplicationOutputDTO response = applicationMapper.toResponse(updatedModel, infoDb, dev, appResponsibleAuthorized, appSecurity, appAccessibility);
        response.setAdmUnit(admUnitService.resolveByCode(updatedModel.getAdmUnitCode()));
        return response;
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

    /**
     * Fetches the {@code AppSecurity} anchor linked to the given application, if any.
     *
     * @param applicationId identifier of the owning application
     * @return the linked security anchor, or {@code null} if none exists
     */
    private AppSecurity fetchSingleAppSecurity(Long applicationId) {
        return appSecurityRepository.findByApplicationId(applicationId);
    }

    /**
     * Fetches the {@code AppAccessibility} anchor linked to the given application, if any. Like
     * {@code AppInformationSystemDb}/{@code AppDevelopment}/{@code AppResponsibleAuthorized}/
     * {@code AppSecurity}, this anchor is auto-created by {@link #create}, so it should only be
     * {@code null} for applications created before this anchor was introduced.
     *
     * @param applicationId identifier of the owning application
     * @return the linked accessibility anchor, or {@code null} if none exists
     */
    private AppAccessibility fetchSingleAppAccessibility(Long applicationId) {
        return appAccessibilityRepository.findByApplicationId(applicationId);
    }

    /**
     * Checks whether the given application has at least one incomplete tab, via the exact same
     * per-tab checks {@link #getById} computes ({@link #isDevelopmentIncomplete}, {@link
     * #hasUnfilledResponsibleType}, {@link #hasNoActiveAuthorized}, {@link #isAccessibilityIncomplete},
     * {@link #isSecurityIncomplete}, {@link #hasNoActiveSystem}, {@link #hasNoActiveDatabase}) — used
     * by {@link #getAll} to populate {@code incomplete} per row. Costs the same handful of lookups
     * per application as {@link #getById} does for one, so it is only called once per row actually
     * being returned (or, when filtering by {@code incomplete}, once per application in the system —
     * see the comment in {@link #getAll}).
     *
     * @param applicationId the application to check
     * @return {@code true} if any tab is missing or incomplete
     */
    private boolean isIncomplete(Long applicationId) {
        AppResponsibleAuthorized appResponsibleAuthorized = fetchSingleAppResponsibleAuthorized(applicationId);
        AppInformationSystemDb appInformationSystemDb = fetchSingleAppInformationSystemDb(applicationId);
        return isDevelopmentIncomplete(fetchSingleDevelopment(applicationId))
                || hasUnfilledResponsibleType(appResponsibleAuthorized)
                || hasNoActiveAuthorized(appResponsibleAuthorized)
                || isAccessibilityIncomplete(fetchSingleAppAccessibility(applicationId))
                || isSecurityIncomplete(fetchSingleAppSecurity(applicationId))
                || hasNoActiveSystem(appInformationSystemDb)
                || hasNoActiveDatabase(appInformationSystemDb);
    }

    /**
     * Checks whether the given development record is missing, or has any required field (every
     * field but {@code observation}) left blank.
     *
     * @param appDevelopment the development record to check, possibly {@code null}
     * @return {@code true} if the record is missing or incomplete
     */
    private boolean isDevelopmentIncomplete(AppDevelopment appDevelopment) {
        return appDevelopment == null
                || appDevelopment.getEnvironment() == null
                || appDevelopment.getModality() == null
                || appDevelopment.getCode() == null
                || appDevelopment.getStandardAdaption() == null
                || appDevelopment.getRevisionDate() == null;
    }

    /**
     * Checks whether the given anchor is missing, or any registered responsible type currently has
     * no active holder on it.
     *
     * @param appResponsibleAuthorized the anchor to check, possibly {@code null}
     * @return {@code true} if the anchor is missing, or at least one responsible type has no active holder
     */
    private boolean hasUnfilledResponsibleType(AppResponsibleAuthorized appResponsibleAuthorized) {
        if (appResponsibleAuthorized == null) {
            return true;
        }
        return responsibleTypeRepository.findAll().stream()
                .anyMatch(type -> appResponsibleRepository.findActiveByAppResponsibleAuthorizedAndResponsibleType(
                        appResponsibleAuthorized.getId(), type.getId()) == null);
    }

    /**
     * Checks whether the given anchor is missing, or has no active authorized person at all.
     *
     * @param appResponsibleAuthorized the anchor to check, possibly {@code null}
     * @return {@code true} if the anchor is missing, or no active authorized person exists on it
     */
    private boolean hasNoActiveAuthorized(AppResponsibleAuthorized appResponsibleAuthorized) {
        if (appResponsibleAuthorized == null) {
            return true;
        }
        return appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(appResponsibleAuthorized.getId()).isEmpty();
    }

    /**
     * Checks whether the given accessibility record is missing, or has any required field (every
     * field but {@code nonAccessibleContent} and {@code observations}) left blank.
     * {@code mobileApplicationName} is only required when {@code mobileApplication} is {@code true}.
     *
     * @param appAccessibility the accessibility record to check, possibly {@code null}
     * @return {@code true} if the record is missing or incomplete
     */
    private boolean isAccessibilityIncomplete(AppAccessibility appAccessibility) {
        return appAccessibility == null
                || appAccessibility.getClassificationSegment() == null
                || appAccessibility.getCompliance() == null
                || appAccessibility.getPublicUrl() == null
                || appAccessibility.getMobileApplication() == null
                || (Boolean.TRUE.equals(appAccessibility.getMobileApplication()) && appAccessibility.getMobileApplicationName() == null)
                || appAccessibility.getExpireDate() == null;
    }

    /**
     * Checks whether the given security anchor is missing, or has no active record in any of its
     * Web Context, ENS Classification, or Security Risk child tables.
     *
     * @param appSecurity the security anchor to check, possibly {@code null}
     * @return {@code true} if the anchor is missing, or at least one of the three child tables has
     * no active record for it
     */
    private boolean isSecurityIncomplete(AppSecurity appSecurity) {
        if (appSecurity == null) {
            return true;
        }
        Long appSecurityId = appSecurity.getId();
        boolean hasWebContext = !appWebContextService.getAll(appSecurityId,
                AppWebContextCriteria.builder().statusId(StatusEnum.ACTIVE.getId()).build(), FIRST_ROW).isEmpty();
        boolean hasEnsClassification = !appEnsClassificationService.getAll(appSecurityId,
                AppEnsClassificationCriteria.builder().statusId(StatusEnum.ACTIVE.getId()).build(), FIRST_ROW).isEmpty();
        boolean hasSecurityRisk = !appSecurityRiskService.getAll(appSecurityId,
                AppSecurityRiskCriteria.builder().statusId(StatusEnum.ACTIVE.getId()).build(), FIRST_ROW).isEmpty();
        return !hasWebContext || !hasEnsClassification || !hasSecurityRisk;
    }

    /**
     * Checks whether the given information system database anchor is missing, or has no active
     * system link at all.
     *
     * @param appInformationSystemDb the anchor to check, possibly {@code null}
     * @return {@code true} if the anchor is missing, or no active system link exists on it
     */
    private boolean hasNoActiveSystem(AppInformationSystemDb appInformationSystemDb) {
        if (appInformationSystemDb == null) {
            return true;
        }
        return appSystemService.getAll(appInformationSystemDb.getId(),
                AppSystemCriteria.builder().statusId(StatusEnum.ACTIVE.getId()).build(), FIRST_ROW).isEmpty();
    }

    /**
     * Checks whether the given information system database anchor is missing, or has no active
     * database link at all.
     *
     * @param appInformationSystemDb the anchor to check, possibly {@code null}
     * @return {@code true} if the anchor is missing, or no active database link exists on it
     */
    private boolean hasNoActiveDatabase(AppInformationSystemDb appInformationSystemDb) {
        if (appInformationSystemDb == null) {
            return true;
        }
        return appDatabaseService.getAll(appInformationSystemDb.getId(),
                AppDatabaseCriteria.builder().statusId(StatusEnum.ACTIVE.getId()).build(), FIRST_ROW).isEmpty();
    }

    /**
     * Validates that {@code admUnitCode}, when provided, matches a unit in the live DIR3CAIB tree
     * and sits strictly below department (Conselleria) level — an application must link to a
     * specific unit within a department, never to the department itself. No local record is
     * created or referenced — the code is simply stored on the application as given. Blank/
     * {@code null} is valid: the administrative unit is optional.
     *
     * @param admUnitCode the DIR3CAIB code supplied on the create/update payload, may be blank or {@code null}
     * @throws BusinessRuleException if {@code admUnitCode} is non-blank and has no match in
     * DIR3CAIB, matches a unit at or above department level, or DIR3CAIB cannot be reached (using
     * {@link AdmUnitService#resolveByCodeOrThrow}
     * rather than the null-swallowing {@code resolveByCode}, so an outage is reported as
     * "unavailable" instead of being misreported as "code not found")
     */
    private void validateAdmUnitCode(String admUnitCode) {
        if (StringUtils.isBlank(admUnitCode)) {
            return;
        }
        AdmUnitOutputDTO admUnit = admUnitService.resolveByCodeOrThrow(admUnitCode);
        if (admUnit == null) {
            throw new BusinessRuleException(Constants.ERR_APPLICATION_ADMUNIT_NOT_FOUND);
        }
        if (admUnit.getLevel() == null || admUnit.getLevel() <= admUnitService.getDepartmentHierarchyLevel()) {
            throw new BusinessRuleException(Constants.ERR_APPLICATION_ADMUNIT_MUST_BE_DEPARTMENT_CHILD);
        }
    }

}
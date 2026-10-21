package es.caib.invai.back.ejb.application.core;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.catalog.admUnit.DTO.AdmUnitOutputDTO;
import es.caib.invai.back.interna.application.core.DTO.ApplicationInputDTO;
import es.caib.invai.back.interna.application.core.DTO.ApplicationDetailOutputDTO;
import es.caib.invai.back.interna.application.core.DTO.ApplicationOutputDTO;
import es.caib.invai.back.interna.application.security.role.DTO.AppRoleOutputDTO;
import es.caib.invai.back.persistence.repository.application.accessibility.AppAccessibilityRepository;
import es.caib.invai.back.persistence.repository.application.data.AppDataRepository;
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
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.dir3.Dir3ValidationRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.responsible.AppResponsibleCriteria;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.responsible.AppResponsibleRepository;
import es.caib.invai.back.persistence.repository.application.integration.core.AppIntegrationRepository;
import es.caib.invai.back.persistence.repository.application.integration.connection.AppIntegrationConnectionCriteria;
import es.caib.invai.back.persistence.repository.application.integration.connection.AppIntegrationConnectionRepository;
import es.caib.invai.back.persistence.repository.application.security.core.AppSecurityRepository;
import es.caib.invai.back.persistence.repository.catalog.responsibleType.ResponsibleTypeRepository;
import es.caib.invai.back.service.facade.application.core.ApplicationService;
import es.caib.invai.back.service.facade.application.security.role.AppRoleService;
import es.caib.invai.back.service.facade.maintenance.responsible.person.PersonService;
import es.caib.invai.back.service.facade.application.security.ensClassification.AppEnsClassificationService;
import es.caib.invai.back.service.facade.application.security.risk.AppSecurityRiskService;
import es.caib.invai.back.service.facade.application.security.webContext.AppWebContextService;
import es.caib.invai.back.service.facade.application.system_database.database.AppDatabaseService;
import es.caib.invai.back.service.facade.application.system_database.system.AppSystemService;
import es.caib.invai.back.service.facade.catalog.admUnit.AdmUnitService;
import es.caib.invai.back.service.mapper.application.core.ApplicationMapper;
import es.caib.invai.back.service.model.application.integration.core.AppIntegration;
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
import java.util.Map;
import java.util.Objects;
import es.caib.invai.back.service.model.application.accessibility.AppAccessibility;
import es.caib.invai.back.service.model.application.data.AppData;
import es.caib.invai.back.service.model.application.development.core.AppDevelopment;
import es.caib.invai.back.service.model.application.system_database.core.AppInformationSystemDb;
import es.caib.invai.back.service.model.application.responsibleAuthorized.core.AppResponsibleAuthorized;
import es.caib.invai.back.service.model.application.responsibleAuthorized.dir3.Dir3Validation;
import es.caib.invai.back.service.model.application.responsibleAuthorized.responsible.AppResponsible;
import es.caib.invai.back.service.model.application.responsibleAuthorized.authorized.AppAuthorized;
import es.caib.invai.back.service.model.application.security.core.AppSecurity;
import es.caib.invai.back.service.model.application.core.Application;
import es.caib.invai.back.service.model.catalog.validationStatus.ValidationStatusEnum;
import es.caib.invai.back.interna.maintenance.responsible.person.DTO.PersonDir3CheckOutputDTO;

/**
 * Facade service implementation for managing system Application objects.
 * Handles status assignments, internal cascading operations, and dynamic criteria filtering.
 * <p>
 * {@link #getById} additionally computes completeness flags for the Responsables,
 * Desenvolupament, Accessibilitat, Seguretat, System Database (Sistemes/Bases de dades), and
 * Integracio tabs
 * ({@code missingResponsibleTypes}, {@code missingAuthorized}, {@code missingDevelopmentFields},
 * {@code missingAccessibilityFields}, {@code missingSecurityData}, {@code missingSystems},
 * {@code missingDatabases}, {@code missingIntegrationData} on {@link ApplicationOutputDTO}) — see
 * {@link #isDevelopmentIncomplete}, {@link #hasUnfilledResponsibleType}, {@link
 * #hasNoActiveAuthorized}, {@link #isAccessibilityIncomplete}, {@link #isSecurityIncomplete},
 * {@link #hasNoActiveSystem}, {@link #hasNoActiveDatabase} and {@link #hasNoActiveConnection}.
 * {@code create}/{@code update}/{@code reactivate} do not compute these flags.
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

    /** Repository handling persistence of the "Open Data" tab linked to an application. */
    @Autowired
    private AppDataRepository appDataRepository;

    /** Repository handling persistence of the "Integracio" tab linked to an application. */
    @Autowired
    private AppIntegrationRepository appIntegrationRepository;

    /**
     * Repository used to check for at least one active integration connection on an integration
     * anchor. Deliberately not the connection facade: that one resolves every row's required and
     * granted roles against Soffid, which a mere existence check must not depend on.
     */
    @Autowired
    private AppIntegrationConnectionRepository appIntegrationConnectionRepository;

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

    /** Facade used to check a person's DIR3 against an administrative unit code when recalculating DIR3 validation statuses. */
    @Autowired
    private PersonService personService;

    /** Repository used to update the DIR3 validation record linked to each responsible/authorized assignment. */
    @Autowired
    private Dir3ValidationRepository dir3ValidationRepository;

    /** Use case assigning to an application's security anchor the Soffid roles named after its prefix. */
    @Autowired
    private AppRoleService appRoleService;

    /**
     * Retrieves an application by its unique identifier, together with its linked
     * information system database, development, and responsible/authorized records. Every tab's
     * completeness flag is computed here and passed straight into
     * {@link ApplicationMapper#toDetailResponse(Application, AppInformationSystemDb, AppDevelopment,
     * AppResponsibleAuthorized, AppSecurity, AppAccessibility, Boolean, Boolean, Boolean, Boolean,
     * Boolean, Boolean, Boolean, Boolean)} so the response DTO comes back fully populated in one call
     * (see {@link #isDevelopmentIncomplete}, {@link #hasUnfilledResponsibleType}, {@link
     * #hasNoActiveAuthorized}, {@link #isAccessibilityIncomplete}, {@link #isSecurityIncomplete},
     * {@link #hasNoActiveSystem}, {@link #hasNoActiveDatabase} and {@link #hasNoActiveConnection}).
     *
     * @param id primary key of the application to fetch
     * @return the mapped {@link ApplicationDetailOutputDTO} representation
     * @throws BusinessRuleException if no application exists with the given ID
     */
    @Override
    @Transactional(readOnly = true)
    public ApplicationDetailOutputDTO getById(Long id) {
        log.info(Constants.LOG_FACADE_FETCH_BY_ID, id);
        Application application = applicationRepository.findById(id);

        if (Objects.isNull(application)) {
            throw new BusinessRuleException(Constants.ERR_APP_NOT_FOUND);
        }

        AppInformationSystemDb appInformationSystemDb = fetchSingleAppInformationSystemDb(id);
        AppDevelopment appDevelopment = fetchSingleDevelopment(id);
        AppResponsibleAuthorized appResponsibleAuthorized = fetchSingleAppResponsibleAuthorized(id);
        AppSecurity appSecurity = fetchSingleAppSecurity(id);
        AppAccessibility appAccessibility = fetchSingleAppAccessibility(id);
        AppData appData = fetchSingleAppData(id);
        AppIntegration appIntegration = fetchSingleAppIntegration(id);

        boolean missingDevelopmentFields = isDevelopmentIncomplete(appDevelopment);
        boolean missingResponsibleTypes = hasUnfilledResponsibleType(appResponsibleAuthorized);
        boolean missingAuthorized = hasNoActiveAuthorized(appResponsibleAuthorized);
        boolean missingAccessibilityFields = isAccessibilityIncomplete(appAccessibility);
        boolean missingSecurityData = isSecurityIncomplete(appSecurity);
        boolean missingSystems = hasNoActiveSystem(appInformationSystemDb);
        boolean missingDatabases = hasNoActiveDatabase(appInformationSystemDb);
        boolean missingIntegrationData = hasNoActiveConnection(appIntegration);

        ApplicationDetailOutputDTO response = applicationMapper.toDetailResponse(application, appInformationSystemDb, appDevelopment, appResponsibleAuthorized, appSecurity, appAccessibility,
                missingDevelopmentFields, missingResponsibleTypes, missingAuthorized,
                missingAccessibilityFields, missingSecurityData,
                missingSystems, missingDatabases, missingIntegrationData);
        response.setIncomplete(missingDevelopmentFields || missingResponsibleTypes || missingAuthorized
                || missingAccessibilityFields || missingSecurityData || missingSystems || missingDatabases
                || missingIntegrationData);
        response.setAdmUnit(admUnitService.resolveByCode(application.getAdmUnitCode()));
        response.setAppDataId(Objects.nonNull(appData) ? appData.getId() : null);
        response.setAppIntegrationId(Objects.nonNull(appIntegration) ? appIntegration.getId() : null);
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
        if (Objects.nonNull(criteria) && !StringUtils.isBlank(criteria.getQuickSearch())) {
            criteria.setQuickSearchAdmUnitCodes(admUnitService.findCodesByNameContaining(criteria.getQuickSearch()));
        }

        Boolean incompleteFilter = Objects.nonNull(criteria) ? criteria.getIncomplete() : null;
        if (Objects.nonNull(incompleteFilter)) {
            List<Long> incompleteIds = applicationRepository.findAll(null, Pageable.unpaged()).stream()
                    .map(Application::getId)
                    .filter(this::isIncomplete)
                    .toList();
            criteria.setIncompleteApplicationIds(incompleteIds);
        }

        Page<Application> domainPage = applicationRepository.findAll(criteria, pageable);
        Map<String, AdmUnitOutputDTO> admUnitsByCode = admUnitService.getAdmUnitsByCodes(
                domainPage.getContent().stream().map(Application::getAdmUnitCode).toList());
        return domainPage.map(application -> {
            ApplicationOutputDTO dto = applicationMapper.toResponse(application);
            dto.setAdmUnit(admUnitsByCode.get(application.getAdmUnitCode()));
            return dto;
        });
    }

    /**
     * Creates a new application and automatically instantiates and persists default
     * AppInformationSystemDb, Development, AppResponsibleAuthorized ("Responsables i Autoritzats" tab),
     * AppSecurity ("Seguretat" tab), AppAccessibility ("Accessibilitat" tab), AppData ("Open Data"
     * tab), and AppIntegration ("Integracio" tab) records bound to it.
     */
    @Override
    public ApplicationOutputDTO create(ApplicationInputDTO inputDTO) {
        log.info(Constants.LOG_FACADE_CREATE, inputDTO.getCode());

        Utils.sanitize(inputDTO);
        admUnitService.validateAdmUnitCode(inputDTO.getAdmUnitCode());

        if (applicationRepository.existsByCode(inputDTO.getCode())) {
            throw new BusinessRuleException(Constants.ERR_CODE_DUPLICATED);
        }
        if (applicationRepository.existsByPrefix(inputDTO.getPrefix())) {
            throw new BusinessRuleException(Constants.ERR_PREFIX_DUPLICATED);
        }

        Application model = applicationMapper.toModelFromInput(inputDTO);
        if (Objects.isNull(model.getStatus())) {
            model.setStatus(StatusEnum.ACTIVE);
        }
        Application savedModel = applicationRepository.create(model);

        AppInformationSystemDb infoDbModel = new AppInformationSystemDb();
        infoDbModel.setApplication(savedModel);
        appInformationSystemDbRepository.create(infoDbModel);

        AppDevelopment devModel = new AppDevelopment();
        devModel.setApplication(savedModel);
        appDevelopmentRepository.create(devModel);

        AppResponsibleAuthorized appResponsibleAuthorizedModel = new AppResponsibleAuthorized();
        appResponsibleAuthorizedModel.setApplication(savedModel);
        appResponsibleAuthorizedRepository.create(appResponsibleAuthorizedModel);

        AppSecurity appSecurityModel = new AppSecurity();
        appSecurityModel.setApplication(savedModel);
        appSecurityRepository.create(appSecurityModel);

        AppAccessibility appAccessibilityModel = new AppAccessibility();
        appAccessibilityModel.setApplication(savedModel);
        appAccessibilityRepository.create(appAccessibilityModel);

        AppData appDataModel = new AppData();
        appDataModel.setApplication(savedModel);
        appDataRepository.create(appDataModel);

        AppIntegration appIntegrationModel = new AppIntegration();
        appIntegrationModel.setApplication(savedModel);
        appIntegrationRepository.create(appIntegrationModel);

        ApplicationOutputDTO response = applicationMapper.toResponse(savedModel);
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
        if (Objects.isNull(existing)) {
            throw new BusinessRuleException(Constants.ERR_APP_NOT_FOUND);
        }

        if (Objects.nonNull(existing.getDeletedAt())) {
            throw new BusinessRuleException(Constants.ERR_APP_NOT_ACTIVE);
        }

        Utils.sanitize(inputDTO);
        admUnitService.validateAdmUnitCode(inputDTO.getAdmUnitCode());

        if (applicationRepository.existsByCodeAndIdNot(inputDTO.getCode(), id)) {
            throw new BusinessRuleException(Constants.ERR_CODE_OWNED_BY_OTHER);
        }
        if (applicationRepository.existsByPrefixAndIdNot(inputDTO.getPrefix(), id)) {
            throw new BusinessRuleException(Constants.ERR_PREFIX_OWNED_BY_OTHER);
        }

        String oldAdmUnitCode = existing.getAdmUnitCode();
        applicationMapper.updateModelFromInput(inputDTO, existing);
        Application updatedModel = applicationRepository.update(existing, id);

        AppInformationSystemDb infoDb = fetchSingleAppInformationSystemDb(id);
        if (Objects.isNull(infoDb)) {
            infoDb = new AppInformationSystemDb();
            infoDb.setApplication(updatedModel);
            appInformationSystemDbRepository.create(infoDb);
        }

        AppDevelopment dev = fetchSingleDevelopment(id);
        if (Objects.isNull(dev)) {
            dev = new AppDevelopment();
            dev.setApplication(updatedModel);
            appDevelopmentRepository.create(dev);
        }

        AppResponsibleAuthorized appResponsibleAuthorized = fetchSingleAppResponsibleAuthorized(id);
        if (Objects.isNull(appResponsibleAuthorized)) {
            appResponsibleAuthorized = new AppResponsibleAuthorized();
            appResponsibleAuthorized.setApplication(updatedModel);
            appResponsibleAuthorized = appResponsibleAuthorizedRepository.create(appResponsibleAuthorized);
        }

        if (!Objects.equals(oldAdmUnitCode, updatedModel.getAdmUnitCode())) {
            recalculateDir3Statuses(appResponsibleAuthorized.getId(), updatedModel.getAdmUnitCode());
        }

        AppSecurity appSecurity = fetchSingleAppSecurity(id);
        if (Objects.isNull(appSecurity)) {
            appSecurity = new AppSecurity();
            appSecurity.setApplication(updatedModel);
            appSecurityRepository.create(appSecurity);
        }

        AppAccessibility appAccessibility = fetchSingleAppAccessibility(id);
        if (Objects.isNull(appAccessibility)) {
            appAccessibility = new AppAccessibility();
            appAccessibility.setApplication(updatedModel);
            appAccessibilityRepository.create(appAccessibility);
        }

        AppData appData = fetchSingleAppData(id);
        if (Objects.isNull(appData)) {
            appData = new AppData();
            appData.setApplication(updatedModel);
            appDataRepository.create(appData);
        }

        AppIntegration appIntegration = fetchSingleAppIntegration(id);
        if (Objects.isNull(appIntegration)) {
            appIntegration = new AppIntegration();
            appIntegration.setApplication(updatedModel);
            appIntegrationRepository.create(appIntegration);
        }

        ApplicationOutputDTO response = applicationMapper.toResponse(updatedModel);
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

        if (Objects.isNull(existing)) {
            throw new BusinessRuleException(Constants.ERR_APP_NOT_FOUND);
        }

        if (Objects.nonNull(existing.getDeletedAt())) {
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
        if (Objects.nonNull(infoDb) && Objects.isNull(infoDb.getDeletedAt())) {
            infoDb.setDeletedAt(now);
            infoDb.setDeletedBy(currentUser);
            appInformationSystemDbRepository.delete(infoDb);
        }

        AppDevelopment dev = fetchSingleDevelopment(id);
        if (Objects.nonNull(dev) && Objects.isNull(dev.getDeletedAt())) {
            dev.setDeletedAt(now);
            dev.setDeletedBy(currentUser);
            appDevelopmentRepository.delete(dev);
        }

        AppResponsibleAuthorized appResponsibleAuthorized = fetchSingleAppResponsibleAuthorized(id);
        if (Objects.nonNull(appResponsibleAuthorized) && Objects.isNull(appResponsibleAuthorized.getDeletedAt())) {
            appResponsibleAuthorized.setDeletedAt(now);
            appResponsibleAuthorized.setDeletedBy(currentUser);
            appResponsibleAuthorizedRepository.delete(appResponsibleAuthorized);
        }

        AppSecurity appSecurity = fetchSingleAppSecurity(id);
        if (Objects.nonNull(appSecurity) && Objects.isNull(appSecurity.getDeletedAt())) {
            appSecurity.setDeletedAt(now);
            appSecurity.setDeletedBy(currentUser);
            appSecurityRepository.delete(appSecurity);
        }

        AppAccessibility appAccessibility = fetchSingleAppAccessibility(id);
        if (Objects.nonNull(appAccessibility) && Objects.isNull(appAccessibility.getDeletedAt())) {
            appAccessibility.setDeletedAt(now);
            appAccessibility.setDeletedBy(currentUser);
            appAccessibilityRepository.delete(appAccessibility);
        }

        AppData appData = fetchSingleAppData(id);
        if (Objects.nonNull(appData) && Objects.isNull(appData.getDeletedAt())) {
            appData.setDeletedAt(now);
            appData.setDeletedBy(currentUser);
            appDataRepository.delete(appData);
        }

        AppIntegration appIntegration = fetchSingleAppIntegration(id);
        if (Objects.nonNull(appIntegration) && Objects.isNull(appIntegration.getDeletedAt())) {
            appIntegration.setDeletedAt(now);
            appIntegration.setDeletedBy(currentUser);
            appIntegrationRepository.delete(appIntegration);
        }
    }

    /**
     * Reactivates the application and clears deletedAt timestamp across all child records.
     */
    @Override
    public ApplicationOutputDTO reactivate(Long id) {
        log.info("Facade: Reactivating application and associated records for ID: {}", id);

        Application existing = applicationRepository.findById(id);
        if (Objects.isNull(existing)) {
            throw new BusinessRuleException(Constants.ERR_APP_NOT_FOUND);
        }

        if (Objects.isNull(existing.getDeletedAt())) {
            throw new BusinessRuleException(Constants.ERR_APP_ACTIVE);
        }

        existing.setStatus(StatusEnum.ACTIVE);
        existing.setDeletedAt(null);
        existing.setExpirationDate(null);
        existing.setDeletedBy(null);
        Application updatedModel = applicationRepository.update(existing, id);

        AppInformationSystemDb infoDb = fetchSingleAppInformationSystemDb(id);
        if (Objects.nonNull(infoDb) && Objects.nonNull(infoDb.getDeletedAt())) {
            infoDb.setDeletedAt(null);
            infoDb.setDeletedBy(null);
            appInformationSystemDbRepository.update(infoDb, infoDb.getId());
        }

        AppDevelopment dev = fetchSingleDevelopment(id);
        if (Objects.nonNull(dev) && Objects.nonNull(dev.getDeletedAt())) {
            dev.setDeletedAt(null);
            dev.setDeletedBy(null);
            appDevelopmentRepository.update(dev, dev.getId());
        }

        AppResponsibleAuthorized appResponsibleAuthorized = fetchSingleAppResponsibleAuthorized(id);
        if (Objects.nonNull(appResponsibleAuthorized) && Objects.nonNull(appResponsibleAuthorized.getDeletedAt())) {
            appResponsibleAuthorized.setDeletedAt(null);
            appResponsibleAuthorized.setDeletedBy(null);
            appResponsibleAuthorizedRepository.update(appResponsibleAuthorized, appResponsibleAuthorized.getId());
        }

        AppSecurity appSecurity = fetchSingleAppSecurity(id);
        if (Objects.nonNull(appSecurity) && Objects.nonNull(appSecurity.getDeletedAt())) {
            appSecurity.setDeletedAt(null);
            appSecurity.setDeletedBy(null);
            appSecurityRepository.update(appSecurity, appSecurity.getId());
        }

        AppAccessibility appAccessibility = fetchSingleAppAccessibility(id);
        if (Objects.nonNull(appAccessibility) && Objects.nonNull(appAccessibility.getDeletedAt())) {
            appAccessibility.setDeletedAt(null);
            appAccessibility.setDeletedBy(null);
            appAccessibilityRepository.update(appAccessibility, appAccessibility.getId());
        }

        AppData appData = fetchSingleAppData(id);
        if (Objects.nonNull(appData) && Objects.nonNull(appData.getDeletedAt())) {
            appData.setDeletedAt(null);
            appData.setDeletedBy(null);
            appDataRepository.update(appData, appData.getId());
        }

        AppIntegration appIntegration = fetchSingleAppIntegration(id);
        if (Objects.nonNull(appIntegration) && Objects.nonNull(appIntegration.getDeletedAt())) {
            appIntegration.setDeletedAt(null);
            appIntegration.setDeletedBy(null);
            appIntegrationRepository.update(appIntegration, appIntegration.getId());
        }

        ApplicationOutputDTO response = applicationMapper.toResponse(updatedModel);
        response.setAdmUnit(admUnitService.resolveByCode(updatedModel.getAdmUnitCode()));
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Long> getAllApplicationIds() {
        return applicationRepository.findAll(new ApplicationCriteria(), Pageable.unpaged()).stream()
                .map(Application::getId)
                .toList();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Only checks that the application exists, is active, and has a security anchor; finding and
     * assigning the roles is {@link AppRoleService#assignByPrefix}'s job.
     * </p>
     */
    @Override
    public List<AppRoleOutputDTO> assignSoffidRoles(Long id) {
        log.info("Facade: Assigning the Soffid roles named after the prefix of application ID: {}", id);
        Application application = applicationRepository.findById(id);
        if (Objects.isNull(application)) {
            throw new BusinessRuleException(Constants.ERR_APP_NOT_FOUND);
        }
        if (Objects.nonNull(application.getDeletedAt())) {
            throw new BusinessRuleException(Constants.ERR_APP_NOT_ACTIVE);
        }
        AppSecurity appSecurity = fetchSingleAppSecurity(id);
        if (Objects.isNull(appSecurity)) {
            throw new BusinessRuleException(Constants.ERR_APP_SECURITY_NOT_FOUND);
        }

        return appRoleService.assignByPrefix(appSecurity.getId(), application.getPrefix());
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
     * Fetches the {@code AppData} anchor linked to the given application, if any. Like
     * {@code AppInformationSystemDb}/{@code AppDevelopment}/{@code AppResponsibleAuthorized}/
     * {@code AppSecurity}/{@code AppAccessibility}, this anchor is auto-created by {@link #create},
     * so it should only be {@code null} for applications created before this anchor was introduced.
     *
     * @param applicationId identifier of the owning application
     * @return the linked open data anchor, or {@code null} if none exists
     */
    private AppData fetchSingleAppData(Long applicationId) {
        return appDataRepository.findByApplicationId(applicationId);
    }

    /**
     * Fetches the {@code AppIntegration} anchor linked to the given application, if any.
     *
     * @param applicationId identifier of the owning application
     * @return the linked integration anchor, or {@code null} if none exists
     */
    private AppIntegration fetchSingleAppIntegration(Long applicationId) {
        return appIntegrationRepository.findByApplicationId(applicationId);
    }

    /**
     * Checks whether the given application has at least one incomplete tab, via the exact same
     * per-tab checks {@link #getById} computes ({@link #isDevelopmentIncomplete}, {@link
     * #hasUnfilledResponsibleType}, {@link #hasNoActiveAuthorized}, {@link #isAccessibilityIncomplete},
     * {@link #isSecurityIncomplete}, {@link #hasNoActiveSystem}, {@link #hasNoActiveDatabase} and
     * {@link #hasNoActiveConnection}) — used
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
                || hasNoActiveDatabase(appInformationSystemDb)
                || hasNoActiveConnection(fetchSingleAppIntegration(applicationId));
    }

    /**
     * Checks whether the given development record is missing, or has any required field (every
     * field but {@code observation}) left blank.
     *
     * @param appDevelopment the development record to check, possibly {@code null}
     * @return {@code true} if the record is missing or incomplete
     */
    private boolean isDevelopmentIncomplete(AppDevelopment appDevelopment) {
        return Objects.isNull(appDevelopment)
                || Objects.isNull(appDevelopment.getEnvironment())
                || Objects.isNull(appDevelopment.getModality())
                || Objects.isNull(appDevelopment.getCode())
                || Objects.isNull(appDevelopment.getStandardAdaption())
                || Objects.isNull(appDevelopment.getRevisionDate());
    }

    /**
     * Checks whether the given anchor is missing, or any registered responsible type currently has
     * no active holder on it.
     *
     * @param appResponsibleAuthorized the anchor to check, possibly {@code null}
     * @return {@code true} if the anchor is missing, or at least one responsible type has no active holder
     */
    private boolean hasUnfilledResponsibleType(AppResponsibleAuthorized appResponsibleAuthorized) {
        if (Objects.isNull(appResponsibleAuthorized)) {
            return true;
        }
        return responsibleTypeRepository.findAll().stream()
                .anyMatch(type -> Objects.isNull(appResponsibleRepository.findActiveByAppResponsibleAuthorizedAndResponsibleType(
                        appResponsibleAuthorized.getId(), type.getId())));
    }

    /**
     * Checks whether the given anchor is missing or has no active authorized person at all.
     *
     * @param appResponsibleAuthorized the anchor to check, possibly {@code null}
     * @return {@code true} if the anchor is missing, or no active authorized person exists on it
     */
    private boolean hasNoActiveAuthorized(AppResponsibleAuthorized appResponsibleAuthorized) {
        if (Objects.isNull(appResponsibleAuthorized)) {
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
        return Objects.isNull(appAccessibility)
                || Objects.isNull(appAccessibility.getClassificationSegment())
                || Objects.isNull(appAccessibility.getCompliance())
                || Objects.isNull(appAccessibility.getPublicUrl())
                || Objects.isNull(appAccessibility.getMobileApplication())
                || (appAccessibility.getMobileApplication() && Objects.isNull(appAccessibility.getMobileApplicationName()))
                || Objects.isNull(appAccessibility.getExpireDate());
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
        if (Objects.isNull(appSecurity)) {
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
     * Checks whether the given information system database anchor is missing or has no active
     * system link at all.
     *
     * @param appInformationSystemDb the anchor to check, possibly {@code null}
     * @return {@code true} if the anchor is missing, or no active system link exists on it
     */
    private boolean hasNoActiveSystem(AppInformationSystemDb appInformationSystemDb) {
        if (Objects.isNull(appInformationSystemDb)) {
            return true;
        }
        return appSystemService.getAll(appInformationSystemDb.getId(),
                AppSystemCriteria.builder().statusId(StatusEnum.ACTIVE.getId()).build(), FIRST_ROW).isEmpty();
    }

    /**
     * Checks whether the given information system database anchor is missing or has no active
     * database link at all.
     *
     * @param appInformationSystemDb the anchor to check, possibly {@code null}
     * @return {@code true} if the anchor is missing, or no active database link exists on it
     */
    private boolean hasNoActiveDatabase(AppInformationSystemDb appInformationSystemDb) {
        if (Objects.isNull(appInformationSystemDb)) {
            return true;
        }
        return appDatabaseService.getAll(appInformationSystemDb.getId(),
                AppDatabaseCriteria.builder().statusId(StatusEnum.ACTIVE.getId()).build(), FIRST_ROW).isEmpty();
    }

    /**
     * Checks whether the given integration anchor is missing or has no active integration
     * connection at all.
     *
     * @param appIntegration the anchor to check, possibly {@code null}
     * @return {@code true} if the anchor is missing, or no active connection exists on it
     */
    private boolean hasNoActiveConnection(AppIntegration appIntegration) {
        if (Objects.isNull(appIntegration)) {
            return true;
        }
        return appIntegrationConnectionRepository.findAll(appIntegration.getId(),
                AppIntegrationConnectionCriteria.builder().statusId(StatusEnum.ACTIVE.getId()).build(), FIRST_ROW).isEmpty();
    }

    /**
     * Recalculates the DIR3 validation status of every active responsible/authorized assignment on
     * the given anchor against the application's new administrative unit code, whenever that code
     * has just changed. Applied uniformly regardless of the assignment's current status - this can
     * revert a {@code MANUAL} override back to {@code VALIDATED} (if the new unit genuinely matches)
     * or to {@code NOT_VALIDATED} (if it still doesn't).
     *
     * @param anchorId the "Responsables i Autoritzats" anchor identifier scoping the assignments to recalculate
     * @param newAdmUnitCode the application's newly assigned administrative unit code, possibly blank
     */
    private void recalculateDir3Statuses(Long anchorId, String newAdmUnitCode) {
        AppResponsibleCriteria activeCriteria = AppResponsibleCriteria.builder().statusId(StatusEnum.ACTIVE.getId()).build();
        for (AppResponsible responsible : appResponsibleRepository.findAll(anchorId, activeCriteria, Pageable.unpaged())) {
            updateDir3Status(responsible, newAdmUnitCode);
        }
        for (AppAuthorized authorized : appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(anchorId)) {
            updateDir3Status(authorized, newAdmUnitCode);
        }
    }

    /**
     * Recalculates and, if changed, persists the DIR3 validation status of a single responsible
     * assignment against the given administrative unit code.
     */
    private void updateDir3Status(AppResponsible responsible, String newAdmUnitCode) {
        ValidationStatusEnum newStatus = checkAssignedPersonDir3Status(
                responsible.getPerson().isPersonalCaib(), newAdmUnitCode, responsible.getPerson().getEmail());
        Dir3Validation dir3Validation = responsible.getDir3Validation();
        if (dir3Validation.getDir3Status() != newStatus) {
            dir3Validation.setDir3Status(newStatus);
            dir3ValidationRepository.update(dir3Validation, dir3Validation.getId());
        }
    }

    /**
     * Recalculates and, if changed, persists the DIR3 validation status of a single authorized
     * assignment against the given administrative unit code.
     */
    private void updateDir3Status(AppAuthorized authorized, String newAdmUnitCode) {
        ValidationStatusEnum newStatus = checkAssignedPersonDir3Status(
                authorized.getPerson().isPersonalCaib(), newAdmUnitCode, authorized.getPerson().getEmail());
        Dir3Validation dir3Validation = authorized.getDir3Validation();
        if (dir3Validation.getDir3Status() != newStatus) {
            dir3Validation.setDir3Status(newStatus);
            dir3ValidationRepository.update(dir3Validation, dir3Validation.getId());
        }
    }

    /**
     * Resolves the DIR3 validation state for the cascade recalculation, catching any failure from
     * {@link PersonService#checkDir3} so a Soffid outage/timeout never blocks saving the
     * application itself - it's simply treated as inconclusive (see {@link ValidationStatusEnum#resolve}).
     */
    private ValidationStatusEnum checkAssignedPersonDir3Status(boolean personalCaib, String admUnitCode, String personEmail) {
        PersonDir3CheckOutputDTO checkResult = null;
        if (personalCaib && StringUtils.isNotBlank(admUnitCode)) {
            try {
                checkResult = personService.checkDir3(personEmail, admUnitCode);
            } catch (RuntimeException ex) {
                log.warn("Facade: Could not verify DIR3 while recalculating validation for person email: {}", personEmail);
            }
        }
        return ValidationStatusEnum.resolve(personalCaib, admUnitCode, checkResult);
    }

}
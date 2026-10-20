package es.caib.invai.back.ejb.application.core;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.core.DTO.ApplicationDir3MismatchOutputDTO;
import es.caib.invai.back.interna.application.core.DTO.ApplicationInputDTO;
import es.caib.invai.back.interna.application.core.DTO.ApplicationOutputDTO;
import es.caib.invai.back.persistence.repository.application.accessibility.AppAccessibilityRepository;
import es.caib.invai.back.persistence.repository.application.data.AppDataRepository;
import es.caib.invai.back.persistence.repository.application.core.ApplicationCriteria;
import es.caib.invai.back.persistence.repository.application.core.ApplicationRepository;
import es.caib.invai.back.interna.application.security.ensClassification.DTO.AppEnsClassificationOutputDTO;
import es.caib.invai.back.interna.application.security.risk.DTO.AppSecurityRiskOutputDTO;
import es.caib.invai.back.interna.application.development.webContext.DTO.AppWebContextOutputDTO;
import es.caib.invai.back.interna.application.system_database.database.DTO.AppDatabaseOutputDTO;
import es.caib.invai.back.interna.application.system_database.system.DTO.AppSystemOutputDTO;
import es.caib.invai.back.interna.application.integration.connection.DTO.AppIntegrationConnectionOutputDTO;
import es.caib.invai.back.persistence.repository.application.development.core.AppDevelopmentRepository;
import es.caib.invai.back.persistence.repository.application.system_database.core.AppInformationSystemDbRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.core.AppResponsibleAuthorizedRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.authorized.AppAuthorizedRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.dir3.Dir3ValidationRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.responsible.AppResponsibleRepository;
import es.caib.invai.back.persistence.repository.application.security.core.AppSecurityRepository;
import es.caib.invai.back.persistence.repository.application.integration.core.AppIntegrationRepository;
import es.caib.invai.back.persistence.repository.catalog.responsibleType.ResponsibleTypeRepository;
import es.caib.invai.back.interna.catalog.admUnit.DTO.AdmUnitOutputDTO;
import es.caib.invai.back.interna.maintenance.responsible.person.DTO.PersonDir3CheckOutputDTO;
import es.caib.invai.back.service.facade.application.security.ensClassification.AppEnsClassificationService;
import es.caib.invai.back.service.facade.application.security.risk.AppSecurityRiskService;
import es.caib.invai.back.service.facade.application.security.webContext.AppWebContextService;
import es.caib.invai.back.service.facade.application.system_database.database.AppDatabaseService;
import es.caib.invai.back.service.facade.application.system_database.system.AppSystemService;
import es.caib.invai.back.service.facade.application.integration.connection.AppIntegrationConnectionService;
import es.caib.invai.back.service.facade.catalog.admUnit.AdmUnitService;
import es.caib.invai.back.service.facade.maintenance.responsible.person.PersonService;
import es.caib.invai.back.service.mapper.application.core.ApplicationMapper;
import es.caib.invai.back.service.model.application.accessibility.AppAccessibility;
import es.caib.invai.back.service.model.application.system_database.core.AppInformationSystemDb;
import es.caib.invai.back.service.model.application.integration.core.AppIntegration;
import es.caib.invai.back.service.model.application.core.Application;
import es.caib.invai.back.service.model.application.development.core.AppDevelopment;
import es.caib.invai.back.service.model.application.responsibleAuthorized.core.AppResponsibleAuthorized;
import es.caib.invai.back.service.model.application.responsibleAuthorized.dir3.Dir3Validation;
import es.caib.invai.back.service.model.application.security.core.AppSecurity;
import es.caib.invai.back.service.model.application.responsibleAuthorized.responsible.AppResponsible;
import es.caib.invai.back.service.model.application.responsibleAuthorized.authorized.AppAuthorized;
import es.caib.invai.back.service.model.catalog.dir3Status.Dir3ValidationStatus;
import es.caib.invai.back.service.model.catalog.responsibleType.ResponsibleType;
import es.caib.invai.back.service.model.maintenance.responsible.person.Person;
import es.caib.invai.back.service.model.catalog.modality.Modality;
import es.caib.invai.back.service.model.catalog.standardAdaption.StandardAdaption;
import es.caib.invai.back.service.model.maintenance.classificationSegment.ClassificationSegment;
import es.caib.invai.back.service.model.maintenance.complianceSituation.ComplianceSituation;
import es.caib.invai.back.service.model.maintenance.systems.environment.Environment;
import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import es.caib.invai.back.utils.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ApplicationServiceFacadeBean}, exercising every branch of its business rules
 * with the {@link ApplicationRepository}, {@link ApplicationMapper}, {@link AppInformationSystemDbRepository},
 * and {@link AppDevelopmentRepository} collaborators fully mocked.
 */
@ExtendWith(MockitoExtension.class)
class ApplicationServiceFacadeBeanTest {

    @Mock
    private ApplicationMapper applicationMapper;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private AppInformationSystemDbRepository appInformationSystemDbRepository;

    @Mock
    private AppSystemService appSystemService;

    @Mock
    private AppDatabaseService appDatabaseService;

    @Mock
    private AppDevelopmentRepository appDevelopmentRepository;

    @Mock
    private AppResponsibleAuthorizedRepository appResponsibleAuthorizedRepository;

    @Mock
    private ResponsibleTypeRepository responsibleTypeRepository;

    @Mock
    private AppResponsibleRepository appResponsibleRepository;

    @Mock
    private AppAuthorizedRepository appAuthorizedRepository;

    @Mock
    private AppSecurityRepository appSecurityRepository;

    @Mock
    private AppAccessibilityRepository appAccessibilityRepository;

    @Mock
    private AppDataRepository appDataRepository;

    @Mock
    private AppIntegrationRepository appIntegrationRepository;

    @Mock
    private AppIntegrationConnectionService appIntegrationConnectionService;

    @Mock
    private AppWebContextService appWebContextService;

    @Mock
    private AppEnsClassificationService appEnsClassificationService;

    @Mock
    private AppSecurityRiskService appSecurityRiskService;

    @Mock
    private AdmUnitService admUnitService;

    @Mock
    private PersonService personService;

    @Mock
    private Dir3ValidationRepository dir3ValidationRepository;

    @InjectMocks
    private ApplicationServiceFacadeBean applicationServiceFacadeBean;

    private Application activeApplication;

    @BeforeEach
    void setUp() {
        activeApplication = new Application();
        activeApplication.setId(1L);
        activeApplication.setCode("APP01");
        activeApplication.setPrefix("AP1");
        activeApplication.setName("Application One");
        activeApplication.setStatus(StatusEnum.ACTIVE);
    }

    @Test
    void getById_found_returnsMappedResponseWithInfoDbAndDevelopment() {
        AppInformationSystemDb infoDb = new AppInformationSystemDb();
        infoDb.setId(5L);
        AppDevelopment development = new AppDevelopment();
        development.setId(7L);
        AppResponsibleAuthorized appResponsibleAuthorized = new AppResponsibleAuthorized();
        appResponsibleAuthorized.setId(9L);
        AppSecurity appSecurity = new AppSecurity();
        appSecurity.setId(11L);
        ApplicationOutputDTO expected = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(appInformationSystemDbRepository.findByApplicationId(1L)).thenReturn(infoDb);
        when(appDevelopmentRepository.findByApplicationId(1L)).thenReturn(development);
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(appResponsibleAuthorized);
        when(appSecurityRepository.findByApplicationId(1L)).thenReturn(appSecurity);
        when(appWebContextService.getAll(eq(11L), any(), any())).thenReturn(Page.empty());
        when(appEnsClassificationService.getAll(eq(11L), any(), any())).thenReturn(Page.empty());
        when(appSecurityRiskService.getAll(eq(11L), any(), any())).thenReturn(Page.empty());
        when(appSystemService.getAll(eq(5L), any(), any())).thenReturn(Page.empty());
        when(appDatabaseService.getAll(eq(5L), any(), any())).thenReturn(Page.empty());
        when(applicationMapper.toResponse(eq(activeApplication), eq(infoDb), eq(development), eq(appResponsibleAuthorized), eq(appSecurity), eq(null),
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean()))
                .thenReturn(expected);

        ApplicationOutputDTO result = applicationServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_found_noInfoDbOrDevelopment_returnsMappedResponseWithNulls() {
        ApplicationOutputDTO expected = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(appInformationSystemDbRepository.findByApplicationId(1L)).thenReturn(null);
        when(appDevelopmentRepository.findByApplicationId(1L)).thenReturn(null);
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(null);
        when(appSecurityRepository.findByApplicationId(1L)).thenReturn(null);
        when(applicationMapper.toResponse(eq(activeApplication), eq(null), eq(null), eq(null), eq(null), eq(null),
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean()))
                .thenReturn(expected);

        ApplicationOutputDTO result = applicationServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_everyAnchorMissing_setsIncompleteTrue() {
        ApplicationOutputDTO expected = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(appInformationSystemDbRepository.findByApplicationId(1L)).thenReturn(null);
        when(appDevelopmentRepository.findByApplicationId(1L)).thenReturn(null);
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(null);
        when(appSecurityRepository.findByApplicationId(1L)).thenReturn(null);
        when(applicationMapper.toResponse(eq(activeApplication), eq(null), eq(null), eq(null), eq(null), eq(null),
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean()))
                .thenReturn(expected);

        ApplicationOutputDTO result = applicationServiceFacadeBean.getById(1L);

        assertTrue(result.isIncomplete());
    }

    @Test
    void getById_everyTabComplete_setsIncompleteFalse() {
        AppInformationSystemDb infoDb = new AppInformationSystemDb();
        infoDb.setId(5L);
        AppDevelopment development = new AppDevelopment();
        development.setId(7L);
        development.setEnvironment(new Environment());
        development.setModality(new Modality());
        development.setCode("X");
        development.setStandardAdaption(new StandardAdaption());
        development.setRevisionDate(LocalDateTime.now());
        AppResponsibleAuthorized appResponsibleAuthorized = new AppResponsibleAuthorized();
        appResponsibleAuthorized.setId(9L);
        AppSecurity appSecurity = new AppSecurity();
        appSecurity.setId(11L);
        AppAccessibility accessibility = new AppAccessibility();
        accessibility.setClassificationSegment(new ClassificationSegment());
        accessibility.setCompliance(new ComplianceSituation());
        accessibility.setPublicUrl("http://x");
        accessibility.setMobileApplication(false);
        accessibility.setExpireDate(LocalDateTime.now());
        AppIntegration appIntegration = new AppIntegration();
        appIntegration.setId(13L);
        ApplicationOutputDTO expected = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(appInformationSystemDbRepository.findByApplicationId(1L)).thenReturn(infoDb);
        when(appDevelopmentRepository.findByApplicationId(1L)).thenReturn(development);
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(appResponsibleAuthorized);
        when(appSecurityRepository.findByApplicationId(1L)).thenReturn(appSecurity);
        when(appAccessibilityRepository.findByApplicationId(1L)).thenReturn(accessibility);
        when(appIntegrationRepository.findByApplicationId(1L)).thenReturn(appIntegration);
        when(responsibleTypeRepository.findAll()).thenReturn(List.of());
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(9L)).thenReturn(List.of(new AppAuthorized()));
        when(appWebContextService.getAll(eq(11L), any(), any())).thenReturn(new PageImpl<>(List.of(new AppWebContextOutputDTO())));
        when(appEnsClassificationService.getAll(eq(11L), any(), any())).thenReturn(new PageImpl<>(List.of(new AppEnsClassificationOutputDTO())));
        when(appSecurityRiskService.getAll(eq(11L), any(), any())).thenReturn(new PageImpl<>(List.of(new AppSecurityRiskOutputDTO())));
        when(appSystemService.getAll(eq(5L), any(), any())).thenReturn(new PageImpl<>(List.of(new AppSystemOutputDTO())));
        when(appIntegrationConnectionService.getAll(eq(13L), any(), any())).thenReturn(new PageImpl<>(List.of(new AppIntegrationConnectionOutputDTO())));
        when(appDatabaseService.getAll(eq(5L), any(), any())).thenReturn(new PageImpl<>(List.of(new AppDatabaseOutputDTO())));
        when(applicationMapper.toResponse(eq(activeApplication), eq(infoDb), eq(development), eq(appResponsibleAuthorized), eq(appSecurity), eq(accessibility),
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean()))
                .thenReturn(expected);

        ApplicationOutputDTO result = applicationServiceFacadeBean.getById(1L);

        assertFalse(result.isIncomplete());
    }

    @Test
    void getById_admUnitLinked_setsAdmUnitFromAdmUnitCode() {
        activeApplication.setAdmUnitCode("DGSAN");
        AdmUnitOutputDTO admUnit = new AdmUnitOutputDTO();
        admUnit.setCode("DGSAN");
        ApplicationOutputDTO expected = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(applicationMapper.toResponse(eq(activeApplication), any(), any(), any(), any(), any(),
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean()))
                .thenReturn(expected);
        when(admUnitService.resolveByCode("DGSAN")).thenReturn(admUnit);

        ApplicationOutputDTO result = applicationServiceFacadeBean.getById(1L);

        assertSame(admUnit, result.getAdmUnit());
    }

    @Test
    void getById_noAdmUnitLinked_leavesAdmUnitNull() {
        ApplicationOutputDTO expected = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(applicationMapper.toResponse(eq(activeApplication), any(), any(), any(), any(), any(),
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean()))
                .thenReturn(expected);

        ApplicationOutputDTO result = applicationServiceFacadeBean.getById(1L);

        assertNull(result.getAdmUnit());
    }

    @Test
    void getById_notFound_throwsBusinessRuleException() {
        when(applicationRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> applicationServiceFacadeBean.getById(99L));
        assertEquals(Constants.ERR_APP_NOT_FOUND, ex.getMessage());
    }

    private ResponsibleType responsibleType(long id) {
        ResponsibleType type = new ResponsibleType();
        type.setId(id);
        return type;
    }

    private AppDevelopment completeDevelopment() {
        AppDevelopment development = new AppDevelopment();
        development.setEnvironment(new Environment());
        development.setModality(new Modality());
        development.setCode("https://git.example/repo");
        development.setStandardAdaption(new StandardAdaption());
        development.setRevisionDate(LocalDateTime.now());
        return development;
    }

    private AppAccessibility completeAccessibility() {
        AppAccessibility accessibility = new AppAccessibility();
        accessibility.setClassificationSegment(new ClassificationSegment());
        accessibility.setCompliance(new ComplianceSituation());
        accessibility.setPublicUrl("https://example.org");
        accessibility.setMobileApplication(true);
        accessibility.setMobileApplicationName("Example App");
        accessibility.setExpireDate(LocalDateTime.now().plusYears(1));
        return accessibility;
    }

    /**
     * Note: {@code applicationMapper} is a mock, so the returned DTO's own boolean getters don't
     * reflect what the facade actually computed (they'd just be whatever {@code expected} was
     * pre-set to). These tests instead verify the computed flags via the exact arguments passed to
     * {@code toResponse}.
     */
    @Test
    void getById_everyTypeFilledAtLeastOneAuthorizedCompleteDevelopment_allFlagsFalse() {
        AppResponsibleAuthorized anchor = new AppResponsibleAuthorized();
        anchor.setId(9L);
        AppDevelopment development = completeDevelopment();
        AppSecurity appSecurity = new AppSecurity();
        appSecurity.setId(11L);
        AppAccessibility appAccessibility = completeAccessibility();
        AppInformationSystemDb infoDb = new AppInformationSystemDb();
        infoDb.setId(5L);
        AppIntegration appIntegration = new AppIntegration();
        appIntegration.setId(13L);
        ApplicationOutputDTO expected = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(appInformationSystemDbRepository.findByApplicationId(1L)).thenReturn(infoDb);
        when(appDevelopmentRepository.findByApplicationId(1L)).thenReturn(development);
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(anchor);
        when(responsibleTypeRepository.findAll()).thenReturn(List.of(responsibleType(1L), responsibleType(2L)));
        when(appResponsibleRepository.findActiveByAppResponsibleAuthorizedAndResponsibleType(9L, 1L)).thenReturn(new AppResponsible());
        when(appResponsibleRepository.findActiveByAppResponsibleAuthorizedAndResponsibleType(9L, 2L)).thenReturn(new AppResponsible());
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(9L)).thenReturn(List.of(new AppAuthorized()));
        when(appSecurityRepository.findByApplicationId(1L)).thenReturn(appSecurity);
        when(appWebContextService.getAll(eq(11L), any(), any())).thenReturn(new PageImpl<>(List.of(new AppWebContextOutputDTO())));
        when(appEnsClassificationService.getAll(eq(11L), any(), any())).thenReturn(new PageImpl<>(List.of(new AppEnsClassificationOutputDTO())));
        when(appSecurityRiskService.getAll(eq(11L), any(), any())).thenReturn(new PageImpl<>(List.of(new AppSecurityRiskOutputDTO())));
        when(appAccessibilityRepository.findByApplicationId(1L)).thenReturn(appAccessibility);
        when(appSystemService.getAll(eq(5L), any(), any())).thenReturn(new PageImpl<>(List.of(new AppSystemOutputDTO())));
        when(appDatabaseService.getAll(eq(5L), any(), any())).thenReturn(new PageImpl<>(List.of(new AppDatabaseOutputDTO())));
        when(appIntegrationRepository.findByApplicationId(1L)).thenReturn(appIntegration);
        when(appIntegrationConnectionService.getAll(eq(13L), any(), any())).thenReturn(new PageImpl<>(List.of(new AppIntegrationConnectionOutputDTO())));
        when(applicationMapper.toResponse(activeApplication, infoDb, development, anchor, appSecurity, appAccessibility, false, false, false, false, false, false, false, false)).thenReturn(expected);

        ApplicationOutputDTO result = applicationServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_oneResponsibleTypeUnfilled_missingResponsibleTypesTrue() {
        AppResponsibleAuthorized anchor = new AppResponsibleAuthorized();
        anchor.setId(9L);
        AppDevelopment development = completeDevelopment();
        ApplicationOutputDTO expected = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(appDevelopmentRepository.findByApplicationId(1L)).thenReturn(development);
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(anchor);
        when(responsibleTypeRepository.findAll()).thenReturn(List.of(responsibleType(1L), responsibleType(2L)));
        when(appResponsibleRepository.findActiveByAppResponsibleAuthorizedAndResponsibleType(9L, 1L)).thenReturn(new AppResponsible());
        when(appResponsibleRepository.findActiveByAppResponsibleAuthorizedAndResponsibleType(9L, 2L)).thenReturn(null);
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(9L)).thenReturn(List.of(new AppAuthorized()));
        when(applicationMapper.toResponse(eq(activeApplication), any(), eq(development), eq(anchor), eq(null), eq(null), eq(false), eq(true), eq(false), eq(true), eq(true), eq(true), eq(true), eq(true)))
                .thenReturn(expected);

        ApplicationOutputDTO result = applicationServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_noActiveAuthorized_missingAuthorizedTrue() {
        AppResponsibleAuthorized anchor = new AppResponsibleAuthorized();
        anchor.setId(9L);
        AppDevelopment development = completeDevelopment();
        ApplicationOutputDTO expected = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(appDevelopmentRepository.findByApplicationId(1L)).thenReturn(development);
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(anchor);
        when(responsibleTypeRepository.findAll()).thenReturn(List.of());
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(9L)).thenReturn(List.of());
        when(applicationMapper.toResponse(eq(activeApplication), any(), eq(development), eq(anchor), eq(null), eq(null), eq(false), eq(false), eq(true), eq(true), eq(true), eq(true), eq(true), eq(true)))
                .thenReturn(expected);

        ApplicationOutputDTO result = applicationServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_noAppResponsibleAuthorizedAnchor_bothResponsibleFlagsTrue() {
        AppDevelopment development = completeDevelopment();
        ApplicationOutputDTO expected = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(appDevelopmentRepository.findByApplicationId(1L)).thenReturn(development);
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(null);
        when(applicationMapper.toResponse(eq(activeApplication), any(), eq(development), eq(null), eq(null), eq(null), eq(false), eq(true), eq(true), eq(true), eq(true), eq(true), eq(true), eq(true)))
                .thenReturn(expected);

        ApplicationOutputDTO result = applicationServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
        verify(responsibleTypeRepository, never()).findAll();
        verify(appAuthorizedRepository, never()).findAllActiveByAppResponsibleAuthorized(any());
    }

    @Test
    void getById_missingDevelopmentRecord_missingDevelopmentFieldsTrue() {
        ApplicationOutputDTO expected = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(appDevelopmentRepository.findByApplicationId(1L)).thenReturn(null);
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(null);
        when(applicationMapper.toResponse(eq(activeApplication), any(), eq(null), eq(null), eq(null), eq(null), eq(true), eq(true), eq(true), eq(true), eq(true), eq(true), eq(true), eq(true)))
                .thenReturn(expected);

        ApplicationOutputDTO result = applicationServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_developmentMissingOneRequiredField_missingDevelopmentFieldsTrue() {
        AppDevelopment development = completeDevelopment();
        development.setCode(null);
        ApplicationOutputDTO expected = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(appDevelopmentRepository.findByApplicationId(1L)).thenReturn(development);
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(null);
        when(applicationMapper.toResponse(eq(activeApplication), any(), eq(development), eq(null), eq(null), eq(null), eq(true), eq(true), eq(true), eq(true), eq(true), eq(true), eq(true), eq(true)))
                .thenReturn(expected);

        ApplicationOutputDTO result = applicationServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_developmentObservationBlank_stillNotMissing() {
        AppDevelopment development = completeDevelopment();
        development.setObservation(null);
        AppResponsibleAuthorized anchor = new AppResponsibleAuthorized();
        anchor.setId(9L);
        ApplicationOutputDTO expected = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(appDevelopmentRepository.findByApplicationId(1L)).thenReturn(development);
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(anchor);
        when(responsibleTypeRepository.findAll()).thenReturn(List.of());
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(9L)).thenReturn(List.of(new AppAuthorized()));
        when(applicationMapper.toResponse(eq(activeApplication), any(), eq(development), eq(anchor), eq(null), eq(null), eq(false), eq(false), eq(false), eq(true), eq(true), eq(true), eq(true), eq(true)))
                .thenReturn(expected);

        ApplicationOutputDTO result = applicationServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_noAccessibilityRecord_missingAccessibilityFieldsTrue() {
        ApplicationOutputDTO expected = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(appAccessibilityRepository.findByApplicationId(1L)).thenReturn(null);
        when(applicationMapper.toResponse(eq(activeApplication), any(), any(), any(), any(), any(),
                anyBoolean(), anyBoolean(), anyBoolean(), eq(true), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean()))
                .thenReturn(expected);

        ApplicationOutputDTO result = applicationServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_accessibilityMissingOneRequiredField_missingAccessibilityFieldsTrue() {
        AppAccessibility accessibility = completeAccessibility();
        accessibility.setPublicUrl(null);
        ApplicationOutputDTO expected = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(appAccessibilityRepository.findByApplicationId(1L)).thenReturn(accessibility);
        when(applicationMapper.toResponse(eq(activeApplication), any(), any(), any(), any(), any(),
                anyBoolean(), anyBoolean(), anyBoolean(), eq(true), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean()))
                .thenReturn(expected);

        ApplicationOutputDTO result = applicationServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_accessibilityNoMobileApplicationAndNoName_stillNotMissing() {
        AppAccessibility accessibility = completeAccessibility();
        accessibility.setMobileApplication(false);
        accessibility.setMobileApplicationName(null);
        ApplicationOutputDTO expected = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(appAccessibilityRepository.findByApplicationId(1L)).thenReturn(accessibility);
        when(applicationMapper.toResponse(eq(activeApplication), any(), any(), any(), any(), any(),
                anyBoolean(), anyBoolean(), anyBoolean(), eq(false), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean()))
                .thenReturn(expected);

        ApplicationOutputDTO result = applicationServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_accessibilityMobileApplicationTrueButNoName_missingAccessibilityFieldsTrue() {
        AppAccessibility accessibility = completeAccessibility();
        accessibility.setMobileApplication(true);
        accessibility.setMobileApplicationName(null);
        ApplicationOutputDTO expected = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(appAccessibilityRepository.findByApplicationId(1L)).thenReturn(accessibility);
        when(applicationMapper.toResponse(eq(activeApplication), any(), any(), any(), any(), any(),
                anyBoolean(), anyBoolean(), anyBoolean(), eq(true), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean()))
                .thenReturn(expected);

        ApplicationOutputDTO result = applicationServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_accessibilityNonAccessibleContentAndObservationsBlank_stillNotMissing() {
        AppAccessibility accessibility = completeAccessibility();
        accessibility.setNonAccessibleContent(null);
        accessibility.setObservations(null);
        ApplicationOutputDTO expected = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(appAccessibilityRepository.findByApplicationId(1L)).thenReturn(accessibility);
        when(applicationMapper.toResponse(eq(activeApplication), any(), any(), any(), any(), any(),
                anyBoolean(), anyBoolean(), anyBoolean(), eq(false), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean()))
                .thenReturn(expected);

        ApplicationOutputDTO result = applicationServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_noSecurityAnchor_missingSecurityDataTrue() {
        ApplicationOutputDTO expected = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(appSecurityRepository.findByApplicationId(1L)).thenReturn(null);
        when(applicationMapper.toResponse(eq(activeApplication), any(), any(), any(), eq(null), any(),
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), eq(true), anyBoolean(), anyBoolean(), anyBoolean()))
                .thenReturn(expected);

        ApplicationOutputDTO result = applicationServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
        verify(appWebContextService, never()).getAll(any(), any(), any());
    }

    @Test
    void getById_securityMissingWebContext_missingSecurityDataTrue() {
        AppSecurity appSecurity = new AppSecurity();
        appSecurity.setId(11L);
        ApplicationOutputDTO expected = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(appSecurityRepository.findByApplicationId(1L)).thenReturn(appSecurity);
        when(appWebContextService.getAll(eq(11L), any(), any())).thenReturn(Page.empty());
        when(appEnsClassificationService.getAll(eq(11L), any(), any())).thenReturn(new PageImpl<>(List.of(new AppEnsClassificationOutputDTO())));
        when(appSecurityRiskService.getAll(eq(11L), any(), any())).thenReturn(new PageImpl<>(List.of(new AppSecurityRiskOutputDTO())));
        when(applicationMapper.toResponse(eq(activeApplication), any(), any(), any(), eq(appSecurity), any(),
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), eq(true), anyBoolean(), anyBoolean(), anyBoolean()))
                .thenReturn(expected);

        ApplicationOutputDTO result = applicationServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_securityAllThreeChildTablesPresent_missingSecurityDataFalse() {
        AppSecurity appSecurity = new AppSecurity();
        appSecurity.setId(11L);
        ApplicationOutputDTO expected = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(appSecurityRepository.findByApplicationId(1L)).thenReturn(appSecurity);
        when(appWebContextService.getAll(eq(11L), any(), any())).thenReturn(new PageImpl<>(List.of(new AppWebContextOutputDTO())));
        when(appEnsClassificationService.getAll(eq(11L), any(), any())).thenReturn(new PageImpl<>(List.of(new AppEnsClassificationOutputDTO())));
        when(appSecurityRiskService.getAll(eq(11L), any(), any())).thenReturn(new PageImpl<>(List.of(new AppSecurityRiskOutputDTO())));
        when(applicationMapper.toResponse(eq(activeApplication), any(), any(), any(), eq(appSecurity), any(),
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), eq(false), anyBoolean(), anyBoolean(), anyBoolean()))
                .thenReturn(expected);

        ApplicationOutputDTO result = applicationServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_noInformationSystemDbAnchor_missingSystemsAndDatabasesTrue() {
        ApplicationOutputDTO expected = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(appInformationSystemDbRepository.findByApplicationId(1L)).thenReturn(null);
        when(applicationMapper.toResponse(eq(activeApplication), eq(null), any(), any(), any(), any(),
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), eq(true), eq(true), anyBoolean()))
                .thenReturn(expected);

        ApplicationOutputDTO result = applicationServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
        verify(appSystemService, never()).getAll(any(), any(), any());
        verify(appDatabaseService, never()).getAll(any(), any(), any());
    }

    @Test
    void getById_noActiveSystemLink_missingSystemsTrueDatabasesFalse() {
        AppInformationSystemDb infoDb = new AppInformationSystemDb();
        infoDb.setId(5L);
        ApplicationOutputDTO expected = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(appInformationSystemDbRepository.findByApplicationId(1L)).thenReturn(infoDb);
        when(appSystemService.getAll(eq(5L), any(), any())).thenReturn(Page.empty());
        when(appDatabaseService.getAll(eq(5L), any(), any())).thenReturn(new PageImpl<>(List.of(new AppDatabaseOutputDTO())));
        when(applicationMapper.toResponse(eq(activeApplication), eq(infoDb), any(), any(), any(), any(),
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), eq(true), eq(false), anyBoolean()))
                .thenReturn(expected);

        ApplicationOutputDTO result = applicationServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_activeSystemAndDatabaseLinksPresent_bothFlagsFalse() {
        AppInformationSystemDb infoDb = new AppInformationSystemDb();
        infoDb.setId(5L);
        ApplicationOutputDTO expected = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(appInformationSystemDbRepository.findByApplicationId(1L)).thenReturn(infoDb);
        when(appSystemService.getAll(eq(5L), any(), any())).thenReturn(new PageImpl<>(List.of(new AppSystemOutputDTO())));
        when(appDatabaseService.getAll(eq(5L), any(), any())).thenReturn(new PageImpl<>(List.of(new AppDatabaseOutputDTO())));
        when(applicationMapper.toResponse(eq(activeApplication), eq(infoDb), any(), any(), any(), any(),
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), eq(false), eq(false), anyBoolean()))
                .thenReturn(expected);

        ApplicationOutputDTO result = applicationServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_noIntegrationAnchor_missingIntegrationDataTrue() {
        ApplicationOutputDTO expected = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(appIntegrationRepository.findByApplicationId(1L)).thenReturn(null);
        when(applicationMapper.toResponse(eq(activeApplication), any(), any(), any(), any(), any(),
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), eq(true)))
                .thenReturn(expected);

        ApplicationOutputDTO result = applicationServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
        verify(appIntegrationConnectionService, never()).getAll(any(), any(), any());
    }

    @Test
    void getById_integrationHasNoActiveConnection_missingIntegrationDataTrue() {
        AppIntegration appIntegration = new AppIntegration();
        appIntegration.setId(13L);
        ApplicationOutputDTO expected = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(appIntegrationRepository.findByApplicationId(1L)).thenReturn(appIntegration);
        when(appIntegrationConnectionService.getAll(eq(13L), any(), any())).thenReturn(Page.empty());
        when(applicationMapper.toResponse(eq(activeApplication), any(), any(), any(), any(), any(),
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), eq(true)))
                .thenReturn(expected);

        ApplicationOutputDTO result = applicationServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_integrationHasActiveConnection_missingIntegrationDataFalse() {
        AppIntegration appIntegration = new AppIntegration();
        appIntegration.setId(13L);
        ApplicationOutputDTO expected = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(appIntegrationRepository.findByApplicationId(1L)).thenReturn(appIntegration);
        when(appIntegrationConnectionService.getAll(eq(13L), any(), any())).thenReturn(new PageImpl<>(List.of(new AppIntegrationConnectionOutputDTO())));
        when(applicationMapper.toResponse(eq(activeApplication), any(), any(), any(), any(), any(),
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), eq(false)))
                .thenReturn(expected);

        ApplicationOutputDTO result = applicationServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_setsAppIntegrationIdFromIntegrationAnchor() {
        AppIntegration appIntegration = new AppIntegration();
        appIntegration.setId(13L);
        ApplicationOutputDTO expected = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(appIntegrationRepository.findByApplicationId(1L)).thenReturn(appIntegration);
        when(appIntegrationConnectionService.getAll(eq(13L), any(), any())).thenReturn(Page.empty());
        when(applicationMapper.toResponse(eq(activeApplication), any(), any(), any(), any(), any(),
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean()))
                .thenReturn(expected);

        ApplicationOutputDTO result = applicationServiceFacadeBean.getById(1L);

        assertEquals(13L, result.getAppIntegrationId());
    }

    @Test
    void getById_noIntegrationAnchor_leavesAppIntegrationIdNull() {
        ApplicationOutputDTO expected = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(appIntegrationRepository.findByApplicationId(1L)).thenReturn(null);
        when(applicationMapper.toResponse(eq(activeApplication), any(), any(), any(), any(), any(),
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean()))
                .thenReturn(expected);

        ApplicationOutputDTO result = applicationServiceFacadeBean.getById(1L);

        assertNull(result.getAppIntegrationId());
    }

    @Test
    void getAll_emptyPage_returnsEmptyPage() {
        ApplicationCriteria criteria = new ApplicationCriteria();
        Pageable pageable = Pageable.unpaged();
        when(applicationRepository.findAll(criteria, pageable)).thenReturn(Page.empty(pageable));

        Page<ApplicationOutputDTO> result = applicationServiceFacadeBean.getAll(criteria, pageable);

        assertTrue(result.isEmpty());
        verify(applicationMapper, never()).toResponse(any(Application.class));
    }

    @Test
    void getAll_delegatesAndMapsApplications() {
        ApplicationCriteria criteria = new ApplicationCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<Application> domainPage = new PageImpl<>(List.of(activeApplication));

        ApplicationOutputDTO mapped = new ApplicationOutputDTO();

        when(applicationRepository.findAll(criteria, pageable)).thenReturn(domainPage);
        when(applicationMapper.toResponse(activeApplication)).thenReturn(mapped);
        when(admUnitService.getAdmUnitsByCodes(any())).thenReturn(Collections.emptyMap());

        Page<ApplicationOutputDTO> result = applicationServiceFacadeBean.getAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(mapped, result.getContent().get(0));
    }

    @Test
    void getAll_withQuickSearch_resolvesMatchingAdmUnitCodesOntoCriteria() {
        ApplicationCriteria criteria = new ApplicationCriteria();
        criteria.setQuickSearch("Sanidad");
        Pageable pageable = Pageable.unpaged();
        when(admUnitService.findCodesByNameContaining("Sanidad")).thenReturn(List.of("A04026919"));
        when(applicationRepository.findAll(criteria, pageable)).thenReturn(Page.empty(pageable));

        applicationServiceFacadeBean.getAll(criteria, pageable);

        assertEquals(List.of("A04026919"), criteria.getQuickSearchAdmUnitCodes());
    }

    @Test
    void getAll_blankQuickSearch_doesNotQueryAdmUnitService() {
        ApplicationCriteria criteria = new ApplicationCriteria();
        Pageable pageable = Pageable.unpaged();
        when(applicationRepository.findAll(criteria, pageable)).thenReturn(Page.empty(pageable));

        applicationServiceFacadeBean.getAll(criteria, pageable);

        verify(admUnitService, never()).findCodesByNameContaining(any());
    }

    @Test
    void getAll_filteredByIncomplete_checksEveryApplicationAndPopulatesCriteriaIds() {
        ApplicationCriteria criteria = new ApplicationCriteria();
        criteria.setIncomplete(true);
        Pageable pageable = PageRequest.of(0, 10);
        // Anchor lookups for application 1 are left unstubbed (return null), so
        // isIncomplete(1L) short-circuits to true on isDevelopmentIncomplete(null) alone.
        Page<Application> allApplications = new PageImpl<>(List.of(activeApplication));
        Page<Application> domainPage = new PageImpl<>(List.of(activeApplication));
        ApplicationOutputDTO mapped = new ApplicationOutputDTO();

        when(applicationRepository.findAll(null, Pageable.unpaged())).thenReturn(allApplications);
        when(applicationRepository.findAll(criteria, pageable)).thenReturn(domainPage);
        when(applicationMapper.toResponse(activeApplication)).thenReturn(mapped);
        when(admUnitService.getAdmUnitsByCodes(any())).thenReturn(Collections.emptyMap());

        Page<ApplicationOutputDTO> result = applicationServiceFacadeBean.getAll(criteria, pageable);

        assertEquals(List.of(1L), criteria.getIncompleteApplicationIds());
        assertTrue(result.getContent().get(0).isIncomplete());
    }

    @Test
    void getAll_noIncompleteFilter_checksOnlyTheReturnedPageNotEveryApplication() {
        ApplicationCriteria criteria = new ApplicationCriteria();
        Pageable pageable = PageRequest.of(0, 10);
        Application other = new Application();
        other.setId(2L);
        // Neither application's anchors are stubbed, so both come back incomplete - the point of
        // this test is that only the 2 returned rows are checked, not the whole table.
        Page<Application> domainPage = new PageImpl<>(List.of(activeApplication, other));
        ApplicationOutputDTO mappedFirst = new ApplicationOutputDTO();
        ApplicationOutputDTO mappedSecond = new ApplicationOutputDTO();

        when(applicationRepository.findAll(criteria, pageable)).thenReturn(domainPage);
        when(applicationMapper.toResponse(activeApplication)).thenReturn(mappedFirst);
        when(applicationMapper.toResponse(other)).thenReturn(mappedSecond);
        when(admUnitService.getAdmUnitsByCodes(any())).thenReturn(Collections.emptyMap());

        Page<ApplicationOutputDTO> result = applicationServiceFacadeBean.getAll(criteria, pageable);

        assertNull(criteria.getIncompleteApplicationIds());
        List<ApplicationOutputDTO> content = result.getContent();
        assertTrue(content.get(0).isIncomplete());
        assertTrue(content.get(1).isIncomplete());
        verify(applicationRepository, never()).findAll(isNull(), any());
    }

    private ApplicationInputDTO buildInputDTO() {
        ApplicationInputDTO inputDTO = new ApplicationInputDTO();
        inputDTO.setCode("APP02");
        inputDTO.setPrefix("AP2");
        inputDTO.setName("Application Two");
        return inputDTO;
    }

    @Test
    void create_duplicateCode_throwsBusinessRuleException() {
        ApplicationInputDTO inputDTO = buildInputDTO();
        when(applicationRepository.existsByCode("APP02")).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> applicationServiceFacadeBean.create(inputDTO));
        assertEquals(Constants.ERR_CODE_DUPLICATED, ex.getMessage());
        verify(applicationRepository, never()).create(any());
    }

    @Test
    void create_duplicatePrefix_throwsBusinessRuleException() {
        ApplicationInputDTO inputDTO = buildInputDTO();
        when(applicationRepository.existsByCode("APP02")).thenReturn(false);
        when(applicationRepository.existsByPrefix("AP2")).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> applicationServiceFacadeBean.create(inputDTO));
        assertEquals(Constants.ERR_PREFIX_DUPLICATED, ex.getMessage());
        verify(applicationRepository, never()).create(any());
    }

    @Test
    void create_statusNull_defaultsToActiveAndCreatesChildRecords() {
        ApplicationInputDTO inputDTO = buildInputDTO();
        Application model = new Application();
        model.setStatus(null);
        Application saved = new Application();
        saved.setId(2L);
        AppInformationSystemDb savedInfoDb = new AppInformationSystemDb();
        AppDevelopment savedDev = new AppDevelopment();
        AppResponsibleAuthorized savedAppResponsibleAuthorized = new AppResponsibleAuthorized();
        AppSecurity savedAppSecurity = new AppSecurity();
        AppAccessibility savedAppAccessibility = new AppAccessibility();
        ApplicationOutputDTO response = new ApplicationOutputDTO();

        when(applicationRepository.existsByCode("APP02")).thenReturn(false);
        when(applicationRepository.existsByPrefix("AP2")).thenReturn(false);
        when(applicationMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(applicationRepository.create(model)).thenReturn(saved);
        when(appInformationSystemDbRepository.create(any(AppInformationSystemDb.class))).thenReturn(savedInfoDb);
        when(appDevelopmentRepository.create(any(AppDevelopment.class))).thenReturn(savedDev);
        when(appResponsibleAuthorizedRepository.create(any(AppResponsibleAuthorized.class))).thenReturn(savedAppResponsibleAuthorized);
        when(appSecurityRepository.create(any(AppSecurity.class))).thenReturn(savedAppSecurity);
        when(appAccessibilityRepository.create(any(AppAccessibility.class))).thenReturn(savedAppAccessibility);
        AppIntegration savedAppIntegration = new AppIntegration();
        savedAppIntegration.setId(15L);
        when(appIntegrationRepository.create(any(AppIntegration.class))).thenReturn(savedAppIntegration);
        when(applicationMapper.toResponse(saved, savedInfoDb, savedDev, savedAppResponsibleAuthorized, savedAppSecurity, savedAppAccessibility)).thenReturn(response);

        ApplicationOutputDTO result = applicationServiceFacadeBean.create(inputDTO);

        assertEquals(StatusEnum.ACTIVE, model.getStatus());
        assertEquals(response, result);

        ArgumentCaptor<AppInformationSystemDb> infoDbCaptor = ArgumentCaptor.forClass(AppInformationSystemDb.class);
        verify(appInformationSystemDbRepository).create(infoDbCaptor.capture());
        assertEquals(saved, infoDbCaptor.getValue().getApplication());

        ArgumentCaptor<AppDevelopment> devCaptor = ArgumentCaptor.forClass(AppDevelopment.class);
        verify(appDevelopmentRepository).create(devCaptor.capture());
        assertEquals(saved, devCaptor.getValue().getApplication());

        ArgumentCaptor<AppResponsibleAuthorized> appResponsibleAuthorizedCaptor = ArgumentCaptor.forClass(AppResponsibleAuthorized.class);
        verify(appResponsibleAuthorizedRepository).create(appResponsibleAuthorizedCaptor.capture());
        assertEquals(saved, appResponsibleAuthorizedCaptor.getValue().getApplication());

        ArgumentCaptor<AppSecurity> appSecurityCaptor = ArgumentCaptor.forClass(AppSecurity.class);
        verify(appSecurityRepository).create(appSecurityCaptor.capture());
        assertEquals(saved, appSecurityCaptor.getValue().getApplication());

        ArgumentCaptor<AppAccessibility> appAccessibilityCaptor = ArgumentCaptor.forClass(AppAccessibility.class);
        verify(appAccessibilityRepository).create(appAccessibilityCaptor.capture());
        assertEquals(saved, appAccessibilityCaptor.getValue().getApplication());

        ArgumentCaptor<AppIntegration> appIntegrationCaptor = ArgumentCaptor.forClass(AppIntegration.class);
        verify(appIntegrationRepository).create(appIntegrationCaptor.capture());
        assertEquals(saved, appIntegrationCaptor.getValue().getApplication());
        assertEquals(15L, result.getAppIntegrationId());
    }

    @Test
    void create_statusProvided_keepsProvidedStatus() {
        ApplicationInputDTO inputDTO = buildInputDTO();
        Application model = new Application();
        model.setStatus(StatusEnum.INACTIVE);
        Application saved = new Application();
        when(applicationRepository.existsByCode("APP02")).thenReturn(false);
        when(applicationRepository.existsByPrefix("AP2")).thenReturn(false);
        when(applicationMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(applicationRepository.create(model)).thenReturn(saved);
        when(appInformationSystemDbRepository.create(any(AppInformationSystemDb.class))).thenReturn(new AppInformationSystemDb());
        when(appDevelopmentRepository.create(any(AppDevelopment.class))).thenReturn(new AppDevelopment());
        when(appResponsibleAuthorizedRepository.create(any(AppResponsibleAuthorized.class))).thenReturn(new AppResponsibleAuthorized());
        when(appSecurityRepository.create(any(AppSecurity.class))).thenReturn(new AppSecurity());
        when(appAccessibilityRepository.create(any(AppAccessibility.class))).thenReturn(new AppAccessibility());
        when(applicationMapper.toResponse(any(), any(), any(), any(), any(), any())).thenReturn(new ApplicationOutputDTO());

        applicationServiceFacadeBean.create(inputDTO);

        assertEquals(StatusEnum.INACTIVE, model.getStatus());
    }

    @Test
    void create_admUnitCodeNotFoundInDir3Caib_throwsBusinessRuleException() {
        ApplicationInputDTO inputDTO = buildInputDTO();
        inputDTO.setAdmUnitCode("UNKNOWN");
        doThrow(new BusinessRuleException(Constants.ERR_ADMUNIT_NOT_FOUND)).when(admUnitService).validateAdmUnitCode("UNKNOWN");

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> applicationServiceFacadeBean.create(inputDTO));

        assertEquals(Constants.ERR_ADMUNIT_NOT_FOUND, ex.getMessage());
        verify(applicationRepository, never()).create(any());
    }

    @Test
    void create_admUnitCodeAboveDepartmentLevel_throwsBusinessRuleException() {
        ApplicationInputDTO inputDTO = buildInputDTO();
        inputDTO.setAdmUnitCode("A04003003");
        doThrow(new BusinessRuleException(Constants.ERR_ADMUNIT_ABOVE_DEPARTMENT_LEVEL)).when(admUnitService).validateAdmUnitCode("A04003003");

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> applicationServiceFacadeBean.create(inputDTO));

        assertEquals(Constants.ERR_ADMUNIT_ABOVE_DEPARTMENT_LEVEL, ex.getMessage());
        verify(applicationRepository, never()).create(any());
    }

    @Test
    void create_admUnitCodeIsDepartmentItself_isValidAndSetsAdmUnitOnResponse() {
        ApplicationInputDTO inputDTO = buildInputDTO();
        inputDTO.setAdmUnitCode("A04026919");
        AdmUnitOutputDTO admUnit = new AdmUnitOutputDTO();
        admUnit.setCode("A04026919");
        admUnit.setLevel(2);
        when(admUnitService.resolveByCode("A04026919")).thenReturn(admUnit);
        Application model = new Application();
        Application saved = new Application();
        saved.setAdmUnitCode("A04026919");
        when(applicationRepository.existsByCode("APP02")).thenReturn(false);
        when(applicationRepository.existsByPrefix("AP2")).thenReturn(false);
        when(applicationMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(applicationRepository.create(model)).thenReturn(saved);
        when(appInformationSystemDbRepository.create(any(AppInformationSystemDb.class))).thenReturn(new AppInformationSystemDb());
        when(appDevelopmentRepository.create(any(AppDevelopment.class))).thenReturn(new AppDevelopment());
        when(appResponsibleAuthorizedRepository.create(any(AppResponsibleAuthorized.class))).thenReturn(new AppResponsibleAuthorized());
        when(appSecurityRepository.create(any(AppSecurity.class))).thenReturn(new AppSecurity());
        when(appAccessibilityRepository.create(any(AppAccessibility.class))).thenReturn(new AppAccessibility());
        when(applicationMapper.toResponse(any(), any(), any(), any(), any(), any())).thenReturn(new ApplicationOutputDTO());

        ApplicationOutputDTO result = applicationServiceFacadeBean.create(inputDTO);

        assertSame(admUnit, result.getAdmUnit());
        verify(applicationRepository).create(model);
    }

    @Test
    void create_admUnitCodeFoundInDir3Caib_setsAdmUnitOnResponse() {
        ApplicationInputDTO inputDTO = buildInputDTO();
        inputDTO.setAdmUnitCode("A04026919");
        AdmUnitOutputDTO admUnit = new AdmUnitOutputDTO();
        admUnit.setCode("A04026919");
        admUnit.setLevel(3);
        when(admUnitService.resolveByCode("A04026919")).thenReturn(admUnit);
        Application model = new Application();
        Application saved = new Application();
        saved.setAdmUnitCode("A04026919");
        when(applicationRepository.existsByCode("APP02")).thenReturn(false);
        when(applicationRepository.existsByPrefix("AP2")).thenReturn(false);
        when(applicationMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(applicationRepository.create(model)).thenReturn(saved);
        when(appInformationSystemDbRepository.create(any(AppInformationSystemDb.class))).thenReturn(new AppInformationSystemDb());
        when(appDevelopmentRepository.create(any(AppDevelopment.class))).thenReturn(new AppDevelopment());
        when(appResponsibleAuthorizedRepository.create(any(AppResponsibleAuthorized.class))).thenReturn(new AppResponsibleAuthorized());
        when(appSecurityRepository.create(any(AppSecurity.class))).thenReturn(new AppSecurity());
        when(appAccessibilityRepository.create(any(AppAccessibility.class))).thenReturn(new AppAccessibility());
        when(applicationMapper.toResponse(any(), any(), any(), any(), any(), any())).thenReturn(new ApplicationOutputDTO());

        ApplicationOutputDTO result = applicationServiceFacadeBean.create(inputDTO);

        assertSame(admUnit, result.getAdmUnit());
    }

    @Test
    void create_blankAdmUnitCode_isValidAndSkipsDir3CaibValidation() {
        ApplicationInputDTO inputDTO = buildInputDTO();
        Application model = new Application();
        Application saved = new Application();
        when(applicationRepository.existsByCode("APP02")).thenReturn(false);
        when(applicationRepository.existsByPrefix("AP2")).thenReturn(false);
        when(applicationMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(applicationRepository.create(model)).thenReturn(saved);
        when(appInformationSystemDbRepository.create(any(AppInformationSystemDb.class))).thenReturn(new AppInformationSystemDb());
        when(appDevelopmentRepository.create(any(AppDevelopment.class))).thenReturn(new AppDevelopment());
        when(appResponsibleAuthorizedRepository.create(any(AppResponsibleAuthorized.class))).thenReturn(new AppResponsibleAuthorized());
        when(appSecurityRepository.create(any(AppSecurity.class))).thenReturn(new AppSecurity());
        when(appAccessibilityRepository.create(any(AppAccessibility.class))).thenReturn(new AppAccessibility());
        when(applicationMapper.toResponse(any(), any(), any(), any(), any(), any())).thenReturn(new ApplicationOutputDTO());

        ApplicationOutputDTO result = applicationServiceFacadeBean.create(inputDTO);

        assertNotNull(result);
    }

    @Test
    void update_notFound_throwsBusinessRuleException() {
        ApplicationInputDTO inputDTO = buildInputDTO();
        when(applicationRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> applicationServiceFacadeBean.update(99L, inputDTO));
        assertEquals(Constants.ERR_APP_NOT_FOUND, ex.getMessage());
    }

    @Test
    void update_notActive_throwsBusinessRuleException() {
        ApplicationInputDTO inputDTO = buildInputDTO();
        activeApplication.setDeletedAt(LocalDateTime.now());
        when(applicationRepository.findById(1L)).thenReturn(activeApplication);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> applicationServiceFacadeBean.update(1L, inputDTO));
        assertEquals(Constants.ERR_APP_NOT_ACTIVE, ex.getMessage());
    }

    @Test
    void update_admUnitCodeNotFoundInDir3Caib_throwsBusinessRuleException() {
        ApplicationInputDTO inputDTO = buildInputDTO();
        inputDTO.setAdmUnitCode("UNKNOWN");
        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        doThrow(new BusinessRuleException(Constants.ERR_ADMUNIT_NOT_FOUND)).when(admUnitService).validateAdmUnitCode("UNKNOWN");

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> applicationServiceFacadeBean.update(1L, inputDTO));
        assertEquals(Constants.ERR_ADMUNIT_NOT_FOUND, ex.getMessage());
        verify(applicationMapper, never()).updateModelFromInput(any(), any());
    }

    @Test
    void update_admUnitCodeAboveDepartmentLevel_throwsBusinessRuleException() {
        ApplicationInputDTO inputDTO = buildInputDTO();
        inputDTO.setAdmUnitCode("A04003003");
        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        doThrow(new BusinessRuleException(Constants.ERR_ADMUNIT_ABOVE_DEPARTMENT_LEVEL)).when(admUnitService).validateAdmUnitCode("A04003003");

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> applicationServiceFacadeBean.update(1L, inputDTO));
        assertEquals(Constants.ERR_ADMUNIT_ABOVE_DEPARTMENT_LEVEL, ex.getMessage());
        verify(applicationMapper, never()).updateModelFromInput(any(), any());
    }

    @Test
    void update_admUnitCodeIsDepartmentItself_isValid() {
        ApplicationInputDTO inputDTO = buildInputDTO();
        inputDTO.setAdmUnitCode("A04026919");
        AdmUnitOutputDTO admUnit = new AdmUnitOutputDTO();
        admUnit.setCode("A04026919");
        admUnit.setLevel(2);
        Application updated = new Application();
        updated.setId(1L);
        updated.setAdmUnitCode("A04026919");
        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(admUnitService.resolveByCode("A04026919")).thenReturn(admUnit);
        when(applicationRepository.existsByCodeAndIdNot("APP02", 1L)).thenReturn(false);
        when(applicationRepository.existsByPrefixAndIdNot("AP2", 1L)).thenReturn(false);
        when(applicationRepository.update(activeApplication, 1L)).thenReturn(updated);
        when(appInformationSystemDbRepository.create(any(AppInformationSystemDb.class))).thenReturn(new AppInformationSystemDb());
        when(appDevelopmentRepository.create(any(AppDevelopment.class))).thenReturn(new AppDevelopment());
        when(appResponsibleAuthorizedRepository.create(any(AppResponsibleAuthorized.class))).thenReturn(new AppResponsibleAuthorized());
        when(appSecurityRepository.create(any(AppSecurity.class))).thenReturn(new AppSecurity());
        when(appAccessibilityRepository.create(any(AppAccessibility.class))).thenReturn(new AppAccessibility());
        when(applicationMapper.toResponse(any(), any(), any(), any(), any(), any())).thenReturn(new ApplicationOutputDTO());
        // admUnitCode actually changes here, triggering the DIR3 status recalculation cascade.
        when(appResponsibleRepository.findAll(any(), any(), any())).thenReturn(Page.empty());
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(any())).thenReturn(List.of());

        ApplicationOutputDTO result = applicationServiceFacadeBean.update(1L, inputDTO);

        assertSame(admUnit, result.getAdmUnit());
        verify(applicationMapper).updateModelFromInput(inputDTO, activeApplication);
    }

    // ------------------------------------------------------------------
    // update - DIR3 validation status recalculation cascade
    // ------------------------------------------------------------------

    /**
     * Stubs the common collaborators for an {@code update()} call that changes {@code admUnitCode}
     * from {@code oldCode} to {@code newCode}, with an existing "Responsables i Autoritzats" anchor
     * (id 9L) - so the recalculation cascade has a real, stable anchor id to key off.
     */
    private void stubUpdateWithAdmUnitCodeChange(String oldCode, String newCode) {
        activeApplication.setAdmUnitCode(oldCode);
        AdmUnitOutputDTO admUnit = new AdmUnitOutputDTO();
        admUnit.setCode(newCode);
        admUnit.setLevel(2);
        Application updated = new Application();
        updated.setId(1L);
        updated.setAdmUnitCode(newCode);
        AppResponsibleAuthorized anchor = new AppResponsibleAuthorized();
        anchor.setId(9L);

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(admUnitService.resolveByCode(newCode)).thenReturn(admUnit);
        when(applicationRepository.existsByCodeAndIdNot("APP02", 1L)).thenReturn(false);
        when(applicationRepository.existsByPrefixAndIdNot("AP2", 1L)).thenReturn(false);
        when(applicationRepository.update(activeApplication, 1L)).thenReturn(updated);
        when(appInformationSystemDbRepository.create(any(AppInformationSystemDb.class))).thenReturn(new AppInformationSystemDb());
        when(appDevelopmentRepository.create(any(AppDevelopment.class))).thenReturn(new AppDevelopment());
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(anchor);
        when(appSecurityRepository.create(any(AppSecurity.class))).thenReturn(new AppSecurity());
        when(appAccessibilityRepository.create(any(AppAccessibility.class))).thenReturn(new AppAccessibility());
        when(applicationMapper.toResponse(any(), any(), any(), any(), any(), any())).thenReturn(new ApplicationOutputDTO());
    }

    private AppResponsible buildAppResponsibleWithDir3Validation(Long personId, boolean personalCaib, Dir3ValidationStatus currentStatus) {
        Person person = new Person();
        person.setId(personId);
        person.setEmail("person" + personId + "@caib.es");
        person.setPersonalCaib(personalCaib);
        AppResponsible responsible = new AppResponsible();
        responsible.setId(1L);
        responsible.setPerson(person);
        responsible.setDir3Validation(Dir3Validation.builder().id(700L).dir3Status(currentStatus).build());
        return responsible;
    }

    @Test
    void update_admUnitCodeUnchanged_doesNotRecalculateDir3Statuses() {
        ApplicationInputDTO inputDTO = buildInputDTO();
        inputDTO.setAdmUnitCode("A04026919");
        stubUpdateWithAdmUnitCodeChange("A04026919", "A04026919");

        applicationServiceFacadeBean.update(1L, inputDTO);

        verify(appResponsibleRepository, never()).findAll(any(), any(), any());
        verify(appAuthorizedRepository, never()).findAllActiveByAppResponsibleAuthorized(any());
        verify(personService, never()).checkDir3(any(), any());
    }

    @Test
    void update_admUnitCodeChanges_mismatchedPersonSetToNoValidado() {
        ApplicationInputDTO inputDTO = buildInputDTO();
        inputDTO.setAdmUnitCode("NEW_UNIT");
        stubUpdateWithAdmUnitCodeChange("OLD_UNIT", "NEW_UNIT");
        AppResponsible responsible = buildAppResponsibleWithDir3Validation(50L, true, Dir3ValidationStatus.VALIDATED);
        when(appResponsibleRepository.findAll(eq(9L), any(), any())).thenReturn(new PageImpl<>(List.of(responsible)));
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(9L)).thenReturn(List.of());
        when(personService.checkDir3("person50@caib.es", "NEW_UNIT")).thenReturn(new PersonDir3CheckOutputDTO(null, "SOME_OTHER_CODE", "NEW_UNIT", false));

        applicationServiceFacadeBean.update(1L, inputDTO);

        assertEquals(Dir3ValidationStatus.NOT_VALIDATED, responsible.getDir3Validation().getDir3Status());
        verify(dir3ValidationRepository).update(responsible.getDir3Validation(), 700L);
    }

    @Test
    void update_admUnitCodeChanges_matchingPersonRevertsManualToValidado() {
        ApplicationInputDTO inputDTO = buildInputDTO();
        inputDTO.setAdmUnitCode("NEW_UNIT");
        stubUpdateWithAdmUnitCodeChange("OLD_UNIT", "NEW_UNIT");
        AppResponsible responsible = buildAppResponsibleWithDir3Validation(50L, true, Dir3ValidationStatus.MANUAL);
        when(appResponsibleRepository.findAll(eq(9L), any(), any())).thenReturn(new PageImpl<>(List.of(responsible)));
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(9L)).thenReturn(List.of());
        when(personService.checkDir3("person50@caib.es", "NEW_UNIT")).thenReturn(new PersonDir3CheckOutputDTO(null, "NEW_UNIT", "NEW_UNIT", true));

        applicationServiceFacadeBean.update(1L, inputDTO);

        assertEquals(Dir3ValidationStatus.VALIDATED, responsible.getDir3Validation().getDir3Status());
        verify(dir3ValidationRepository).update(responsible.getDir3Validation(), 700L);
    }

    @Test
    void update_admUnitCodeChanges_externalPersonAlreadyNoAplica_isNotPersistedAgain() {
        ApplicationInputDTO inputDTO = buildInputDTO();
        inputDTO.setAdmUnitCode("NEW_UNIT");
        stubUpdateWithAdmUnitCodeChange("OLD_UNIT", "NEW_UNIT");
        AppResponsible responsible = buildAppResponsibleWithDir3Validation(60L, false, Dir3ValidationStatus.NOT_APPLY);
        when(appResponsibleRepository.findAll(eq(9L), any(), any())).thenReturn(new PageImpl<>(List.of(responsible)));
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(9L)).thenReturn(List.of());

        applicationServiceFacadeBean.update(1L, inputDTO);

        assertEquals(Dir3ValidationStatus.NOT_APPLY, responsible.getDir3Validation().getDir3Status());
        verify(personService, never()).checkDir3(any(), any());
        verify(dir3ValidationRepository, never()).update(any(), any());
    }

    @Test
    void update_duplicateCode_throwsBusinessRuleException() {
        ApplicationInputDTO inputDTO = buildInputDTO();
        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(applicationRepository.existsByCodeAndIdNot("APP02", 1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> applicationServiceFacadeBean.update(1L, inputDTO));
        assertEquals(Constants.ERR_CODE_OWNED_BY_OTHER, ex.getMessage());
        verify(applicationMapper, never()).updateModelFromInput(any(), any());
    }

    @Test
    void update_duplicatePrefix_throwsBusinessRuleException() {
        ApplicationInputDTO inputDTO = buildInputDTO();
        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(applicationRepository.existsByCodeAndIdNot("APP02", 1L)).thenReturn(false);
        when(applicationRepository.existsByPrefixAndIdNot("AP2", 1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> applicationServiceFacadeBean.update(1L, inputDTO));
        assertEquals(Constants.ERR_PREFIX_OWNED_BY_OTHER, ex.getMessage());
        verify(applicationMapper, never()).updateModelFromInput(any(), any());
    }

    @Test
    void update_valid_createsMissingInfoDbAndDevelopment() {
        ApplicationInputDTO inputDTO = buildInputDTO();
        Application updated = new Application();
        updated.setId(1L);
        AppInformationSystemDb createdInfoDb = new AppInformationSystemDb();
        AppDevelopment createdDev = new AppDevelopment();
        AppResponsibleAuthorized createdAppResponsibleAuthorized = new AppResponsibleAuthorized();
        AppSecurity createdAppSecurity = new AppSecurity();
        AppAccessibility createdAppAccessibility = new AppAccessibility();
        ApplicationOutputDTO response = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(applicationRepository.existsByCodeAndIdNot("APP02", 1L)).thenReturn(false);
        when(applicationRepository.existsByPrefixAndIdNot("AP2", 1L)).thenReturn(false);
        when(applicationRepository.update(activeApplication, 1L)).thenReturn(updated);
        when(appInformationSystemDbRepository.findByApplicationId(1L)).thenReturn(null);
        when(appDevelopmentRepository.findByApplicationId(1L)).thenReturn(null);
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(null);
        when(appSecurityRepository.findByApplicationId(1L)).thenReturn(null);
        when(appAccessibilityRepository.findByApplicationId(1L)).thenReturn(null);
        when(appInformationSystemDbRepository.create(any(AppInformationSystemDb.class))).thenReturn(createdInfoDb);
        when(appDevelopmentRepository.create(any(AppDevelopment.class))).thenReturn(createdDev);
        when(appResponsibleAuthorizedRepository.create(any(AppResponsibleAuthorized.class))).thenReturn(createdAppResponsibleAuthorized);
        when(appSecurityRepository.create(any(AppSecurity.class))).thenReturn(createdAppSecurity);
        when(appAccessibilityRepository.create(any(AppAccessibility.class))).thenReturn(createdAppAccessibility);
        when(applicationMapper.toResponse(updated, createdInfoDb, createdDev, createdAppResponsibleAuthorized, createdAppSecurity, createdAppAccessibility)).thenReturn(response);

        ApplicationOutputDTO result = applicationServiceFacadeBean.update(1L, inputDTO);

        verify(applicationMapper).updateModelFromInput(inputDTO, activeApplication);
        verify(appInformationSystemDbRepository).create(any(AppInformationSystemDb.class));
        verify(appDevelopmentRepository).create(any(AppDevelopment.class));
        verify(appResponsibleAuthorizedRepository).create(any(AppResponsibleAuthorized.class));
        verify(appSecurityRepository).create(any(AppSecurity.class));
        verify(appAccessibilityRepository).create(any(AppAccessibility.class));
        assertEquals(response, result);
    }

    @Test
    void update_valid_reusesExistingInfoDbAndDevelopment() {
        ApplicationInputDTO inputDTO = buildInputDTO();
        Application updated = new Application();
        updated.setId(1L);
        AppInformationSystemDb existingInfoDb = new AppInformationSystemDb();
        existingInfoDb.setId(5L);
        AppDevelopment existingDev = new AppDevelopment();
        existingDev.setId(7L);
        AppResponsibleAuthorized existingAppResponsibleAuthorized = new AppResponsibleAuthorized();
        existingAppResponsibleAuthorized.setId(9L);
        AppSecurity existingAppSecurity = new AppSecurity();
        existingAppSecurity.setId(11L);
        AppAccessibility existingAppAccessibility = new AppAccessibility();
        existingAppAccessibility.setId(13L);
        ApplicationOutputDTO response = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(applicationRepository.existsByCodeAndIdNot("APP02", 1L)).thenReturn(false);
        when(applicationRepository.existsByPrefixAndIdNot("AP2", 1L)).thenReturn(false);
        when(applicationRepository.update(activeApplication, 1L)).thenReturn(updated);
        when(appInformationSystemDbRepository.findByApplicationId(1L)).thenReturn(existingInfoDb);
        when(appDevelopmentRepository.findByApplicationId(1L)).thenReturn(existingDev);
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(existingAppResponsibleAuthorized);
        when(appSecurityRepository.findByApplicationId(1L)).thenReturn(existingAppSecurity);
        when(appAccessibilityRepository.findByApplicationId(1L)).thenReturn(existingAppAccessibility);
        when(applicationMapper.toResponse(updated, existingInfoDb, existingDev, existingAppResponsibleAuthorized, existingAppSecurity, existingAppAccessibility)).thenReturn(response);

        ApplicationOutputDTO result = applicationServiceFacadeBean.update(1L, inputDTO);

        verify(appInformationSystemDbRepository, never()).create(any());
        verify(appDevelopmentRepository, never()).create(any());
        verify(appResponsibleAuthorizedRepository, never()).create(any());
        verify(appSecurityRepository, never()).create(any());
        verify(appAccessibilityRepository, never()).create(any());
        assertEquals(response, result);
    }

    @Test
    void update_valid_createsMissingAppIntegrationAnchor() {
        ApplicationInputDTO inputDTO = buildInputDTO();
        Application updated = new Application();
        updated.setId(1L);
        AppIntegration createdAppIntegration = new AppIntegration();
        createdAppIntegration.setId(15L);
        ApplicationOutputDTO response = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(applicationRepository.existsByCodeAndIdNot("APP02", 1L)).thenReturn(false);
        when(applicationRepository.existsByPrefixAndIdNot("AP2", 1L)).thenReturn(false);
        when(applicationRepository.update(activeApplication, 1L)).thenReturn(updated);
        when(appIntegrationRepository.findByApplicationId(1L)).thenReturn(null);
        when(appIntegrationRepository.create(any(AppIntegration.class))).thenReturn(createdAppIntegration);
        when(applicationMapper.toResponse(any(), any(), any(), any(), any(), any())).thenReturn(response);

        ApplicationOutputDTO result = applicationServiceFacadeBean.update(1L, inputDTO);

        ArgumentCaptor<AppIntegration> appIntegrationCaptor = ArgumentCaptor.forClass(AppIntegration.class);
        verify(appIntegrationRepository).create(appIntegrationCaptor.capture());
        assertEquals(updated, appIntegrationCaptor.getValue().getApplication());
        assertEquals(15L, result.getAppIntegrationId());
    }

    @Test
    void update_valid_reusesExistingAppIntegrationAnchor() {
        ApplicationInputDTO inputDTO = buildInputDTO();
        Application updated = new Application();
        updated.setId(1L);
        AppIntegration existingAppIntegration = new AppIntegration();
        existingAppIntegration.setId(15L);
        ApplicationOutputDTO response = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(applicationRepository.existsByCodeAndIdNot("APP02", 1L)).thenReturn(false);
        when(applicationRepository.existsByPrefixAndIdNot("AP2", 1L)).thenReturn(false);
        when(applicationRepository.update(activeApplication, 1L)).thenReturn(updated);
        when(appIntegrationRepository.findByApplicationId(1L)).thenReturn(existingAppIntegration);
        when(applicationMapper.toResponse(any(), any(), any(), any(), any(), any())).thenReturn(response);

        ApplicationOutputDTO result = applicationServiceFacadeBean.update(1L, inputDTO);

        verify(appIntegrationRepository, never()).create(any());
        assertEquals(15L, result.getAppIntegrationId());
    }

    @Test
    void delete_notFound_throwsBusinessRuleException() {
        when(applicationRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> applicationServiceFacadeBean.delete(99L));
        assertEquals(Constants.ERR_APP_NOT_FOUND, ex.getMessage());
    }

    @Test
    void delete_notActive_throwsBusinessRuleException() {
        activeApplication.setDeletedAt(LocalDateTime.now());
        when(applicationRepository.findById(1L)).thenReturn(activeApplication);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> applicationServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_APP_NOT_ACTIVE, ex.getMessage());
    }

    @Test
    void delete_valid_softDeletesApplicationAndChildRecords() {
        AppInformationSystemDb infoDb = new AppInformationSystemDb();
        AppDevelopment dev = new AppDevelopment();
        AppResponsibleAuthorized appResponsibleAuthorized = new AppResponsibleAuthorized();
        AppSecurity appSecurity = new AppSecurity();
        AppAccessibility appAccessibility = new AppAccessibility();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(appInformationSystemDbRepository.findByApplicationId(1L)).thenReturn(infoDb);
        when(appDevelopmentRepository.findByApplicationId(1L)).thenReturn(dev);
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(appResponsibleAuthorized);
        when(appSecurityRepository.findByApplicationId(1L)).thenReturn(appSecurity);
        when(appAccessibilityRepository.findByApplicationId(1L)).thenReturn(appAccessibility);

        applicationServiceFacadeBean.delete(1L);

        assertEquals(StatusEnum.INACTIVE, activeApplication.getStatus());
        assertNotNull(activeApplication.getDeletedAt());
        assertNotNull(activeApplication.getExpirationDate());
        verify(applicationRepository, times(1)).delete(activeApplication);
        assertNotNull(infoDb.getDeletedAt());
        verify(appInformationSystemDbRepository, times(1)).delete(infoDb);
        assertNotNull(dev.getDeletedAt());
        verify(appDevelopmentRepository, times(1)).delete(dev);
        assertNotNull(appResponsibleAuthorized.getDeletedAt());
        verify(appResponsibleAuthorizedRepository, times(1)).delete(appResponsibleAuthorized);
        assertNotNull(appSecurity.getDeletedAt());
        verify(appSecurityRepository, times(1)).delete(appSecurity);
        assertNotNull(appAccessibility.getDeletedAt());
        verify(appAccessibilityRepository, times(1)).delete(appAccessibility);
    }

    @Test
    void delete_valid_childRecordsAlreadyDeleted_skipsReDeletion() {
        AppInformationSystemDb infoDb = new AppInformationSystemDb();
        infoDb.setDeletedAt(LocalDateTime.now().minusDays(1));
        AppDevelopment dev = new AppDevelopment();
        dev.setDeletedAt(LocalDateTime.now().minusDays(1));
        AppResponsibleAuthorized appResponsibleAuthorized = new AppResponsibleAuthorized();
        appResponsibleAuthorized.setDeletedAt(LocalDateTime.now().minusDays(1));
        AppSecurity appSecurity = new AppSecurity();
        appSecurity.setDeletedAt(LocalDateTime.now().minusDays(1));
        AppAccessibility appAccessibility = new AppAccessibility();
        appAccessibility.setDeletedAt(LocalDateTime.now().minusDays(1));

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(appInformationSystemDbRepository.findByApplicationId(1L)).thenReturn(infoDb);
        when(appDevelopmentRepository.findByApplicationId(1L)).thenReturn(dev);
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(appResponsibleAuthorized);
        when(appSecurityRepository.findByApplicationId(1L)).thenReturn(appSecurity);
        when(appAccessibilityRepository.findByApplicationId(1L)).thenReturn(appAccessibility);

        applicationServiceFacadeBean.delete(1L);

        verify(appInformationSystemDbRepository, never()).delete(any());
        verify(appDevelopmentRepository, never()).delete(any());
        verify(appResponsibleAuthorizedRepository, never()).delete(any());
        verify(appSecurityRepository, never()).delete(any());
        verify(appAccessibilityRepository, never()).delete(any());
    }

    @Test
    void reactivate_notFound_throwsBusinessRuleException() {
        when(applicationRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> applicationServiceFacadeBean.reactivate(99L));
        assertEquals(Constants.ERR_APP_NOT_FOUND, ex.getMessage());
    }

    @Test
    void reactivate_active_throwsBusinessRuleException() {
        when(applicationRepository.findById(1L)).thenReturn(activeApplication);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> applicationServiceFacadeBean.reactivate(1L));
        assertEquals(Constants.ERR_APP_ACTIVE, ex.getMessage());
    }

    @Test
    void reactivate_valid_reactivatesApplicationAndChildRecords() {
        activeApplication.setDeletedAt(LocalDateTime.now());
        activeApplication.setDeletedBy("someone");
        activeApplication.setStatus(StatusEnum.INACTIVE);

        Application updated = new Application();
        updated.setId(1L);
        AppInformationSystemDb infoDb = new AppInformationSystemDb();
        infoDb.setId(5L);
        infoDb.setDeletedAt(LocalDateTime.now());
        AppInformationSystemDb updatedInfoDb = new AppInformationSystemDb();
        AppDevelopment dev = new AppDevelopment();
        dev.setId(7L);
        dev.setDeletedAt(LocalDateTime.now());
        AppDevelopment updatedDev = new AppDevelopment();
        AppResponsibleAuthorized appResponsibleAuthorized = new AppResponsibleAuthorized();
        appResponsibleAuthorized.setId(9L);
        appResponsibleAuthorized.setDeletedAt(LocalDateTime.now());
        AppResponsibleAuthorized updatedAppResponsibleAuthorized = new AppResponsibleAuthorized();
        AppSecurity appSecurity = new AppSecurity();
        appSecurity.setId(11L);
        appSecurity.setDeletedAt(LocalDateTime.now());
        AppSecurity updatedAppSecurity = new AppSecurity();
        AppAccessibility appAccessibility = new AppAccessibility();
        appAccessibility.setId(13L);
        appAccessibility.setDeletedAt(LocalDateTime.now());
        AppAccessibility updatedAppAccessibility = new AppAccessibility();
        ApplicationOutputDTO response = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(applicationRepository.update(activeApplication, 1L)).thenReturn(updated);
        when(appInformationSystemDbRepository.findByApplicationId(1L)).thenReturn(infoDb);
        when(appDevelopmentRepository.findByApplicationId(1L)).thenReturn(dev);
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(appResponsibleAuthorized);
        when(appSecurityRepository.findByApplicationId(1L)).thenReturn(appSecurity);
        when(appAccessibilityRepository.findByApplicationId(1L)).thenReturn(appAccessibility);
        when(appInformationSystemDbRepository.update(infoDb, 5L)).thenReturn(updatedInfoDb);
        when(appDevelopmentRepository.update(dev, 7L)).thenReturn(updatedDev);
        when(appResponsibleAuthorizedRepository.update(appResponsibleAuthorized, 9L)).thenReturn(updatedAppResponsibleAuthorized);
        when(appSecurityRepository.update(appSecurity, 11L)).thenReturn(updatedAppSecurity);
        when(appAccessibilityRepository.update(appAccessibility, 13L)).thenReturn(updatedAppAccessibility);
        when(applicationMapper.toResponse(updated, updatedInfoDb, updatedDev, updatedAppResponsibleAuthorized, updatedAppSecurity, updatedAppAccessibility)).thenReturn(response);

        ApplicationOutputDTO result = applicationServiceFacadeBean.reactivate(1L);

        assertEquals(StatusEnum.ACTIVE, activeApplication.getStatus());
        assertNull(activeApplication.getDeletedAt());
        assertNull(activeApplication.getExpirationDate());
        assertNull(activeApplication.getDeletedBy());
        assertEquals(response, result);
    }

    @Test
    void reactivate_valid_infoDbAndDevAlreadyActive_skipsUpdate() {
        activeApplication.setDeletedAt(LocalDateTime.now());

        Application updated = new Application();
        updated.setId(1L);
        AppInformationSystemDb infoDb = new AppInformationSystemDb();
        infoDb.setId(5L);
        infoDb.setDeletedAt(null);
        AppDevelopment dev = new AppDevelopment();
        dev.setId(7L);
        dev.setDeletedAt(null);
        AppResponsibleAuthorized appResponsibleAuthorized = new AppResponsibleAuthorized();
        appResponsibleAuthorized.setId(9L);
        appResponsibleAuthorized.setDeletedAt(null);
        AppSecurity appSecurity = new AppSecurity();
        appSecurity.setId(11L);
        appSecurity.setDeletedAt(null);
        AppAccessibility appAccessibility = new AppAccessibility();
        appAccessibility.setId(13L);
        appAccessibility.setDeletedAt(null);
        ApplicationOutputDTO response = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(applicationRepository.update(activeApplication, 1L)).thenReturn(updated);
        when(appInformationSystemDbRepository.findByApplicationId(1L)).thenReturn(infoDb);
        when(appDevelopmentRepository.findByApplicationId(1L)).thenReturn(dev);
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(appResponsibleAuthorized);
        when(appSecurityRepository.findByApplicationId(1L)).thenReturn(appSecurity);
        when(appAccessibilityRepository.findByApplicationId(1L)).thenReturn(appAccessibility);
        when(applicationMapper.toResponse(updated, infoDb, dev, appResponsibleAuthorized, appSecurity, appAccessibility)).thenReturn(response);

        ApplicationOutputDTO result = applicationServiceFacadeBean.reactivate(1L);

        verify(appInformationSystemDbRepository, never()).update(any(), any());
        verify(appDevelopmentRepository, never()).update(any(), any());
        verify(appResponsibleAuthorizedRepository, never()).update(any(), any());
        verify(appSecurityRepository, never()).update(any(), any());
        verify(appAccessibilityRepository, never()).update(any(), any());
        assertEquals(response, result);
    }

    @Test
    void delete_valid_softDeletesAppIntegrationAnchor() {
        AppIntegration appIntegration = new AppIntegration();
        appIntegration.setId(15L);

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(appIntegrationRepository.findByApplicationId(1L)).thenReturn(appIntegration);

        applicationServiceFacadeBean.delete(1L);

        assertNotNull(appIntegration.getDeletedAt());
        assertNotNull(appIntegration.getDeletedBy());
        verify(appIntegrationRepository, times(1)).delete(appIntegration);
    }

    @Test
    void delete_valid_appIntegrationAlreadyDeleted_skipsReDeletion() {
        AppIntegration appIntegration = new AppIntegration();
        appIntegration.setDeletedAt(LocalDateTime.now().minusDays(1));

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(appIntegrationRepository.findByApplicationId(1L)).thenReturn(appIntegration);

        applicationServiceFacadeBean.delete(1L);

        verify(appIntegrationRepository, never()).delete(any());
    }

    @Test
    void delete_valid_noAppIntegrationAnchor_skipsDeletion() {
        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(appIntegrationRepository.findByApplicationId(1L)).thenReturn(null);

        applicationServiceFacadeBean.delete(1L);

        verify(appIntegrationRepository, never()).delete(any());
    }

    @Test
    void reactivate_valid_reactivatesAppIntegrationAnchor() {
        activeApplication.setDeletedAt(LocalDateTime.now());

        Application updated = new Application();
        updated.setId(1L);
        AppIntegration appIntegration = new AppIntegration();
        appIntegration.setId(15L);
        appIntegration.setDeletedAt(LocalDateTime.now());
        AppIntegration updatedAppIntegration = new AppIntegration();
        updatedAppIntegration.setId(15L);
        ApplicationOutputDTO response = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(applicationRepository.update(activeApplication, 1L)).thenReturn(updated);
        when(appIntegrationRepository.findByApplicationId(1L)).thenReturn(appIntegration);
        when(appIntegrationRepository.update(appIntegration, 15L)).thenReturn(updatedAppIntegration);
        when(applicationMapper.toResponse(any(), any(), any(), any(), any(), any())).thenReturn(response);

        ApplicationOutputDTO result = applicationServiceFacadeBean.reactivate(1L);

        assertNull(appIntegration.getDeletedAt());
        assertNull(appIntegration.getDeletedBy());
        verify(appIntegrationRepository, times(1)).update(appIntegration, 15L);
        assertEquals(15L, result.getAppIntegrationId());
    }

    @Test
    void reactivate_valid_appIntegrationAlreadyActive_skipsUpdate() {
        activeApplication.setDeletedAt(LocalDateTime.now());

        Application updated = new Application();
        updated.setId(1L);
        AppIntegration appIntegration = new AppIntegration();
        appIntegration.setId(15L);
        appIntegration.setDeletedAt(null);
        ApplicationOutputDTO response = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(applicationRepository.update(activeApplication, 1L)).thenReturn(updated);
        when(appIntegrationRepository.findByApplicationId(1L)).thenReturn(appIntegration);
        when(applicationMapper.toResponse(any(), any(), any(), any(), any(), any())).thenReturn(response);

        ApplicationOutputDTO result = applicationServiceFacadeBean.reactivate(1L);

        verify(appIntegrationRepository, never()).update(any(), any());
        assertEquals(15L, result.getAppIntegrationId());
    }

    // ------------------------------------------------------------------
    // checkDir3MismatchForAllApplications
    // ------------------------------------------------------------------

    private Application applicationWithAdmUnitCode(Long id, String name, String admUnitCode) {
        Application application = new Application();
        application.setId(id);
        application.setName(name);
        application.setAdmUnitCode(admUnitCode);
        return application;
    }

    private void stubAllApplications(Application... applications) {
        when(applicationRepository.findAll(any(ApplicationCriteria.class), eq(Pageable.unpaged())))
                .thenReturn(new PageImpl<>(List.of(applications)));
    }

    @Test
    void checkDir3MismatchForAllApplications_blankAdmUnitCode_isSkippedEntirely() {
        stubAllApplications(applicationWithAdmUnitCode(1L, "App One", null));

        List<ApplicationDir3MismatchOutputDTO> result = applicationServiceFacadeBean.checkDir3MismatchForAllApplications();

        assertEquals(0, result.size());
        verify(appResponsibleAuthorizedRepository, never()).findByApplicationId(any());
    }

    @Test
    void checkDir3MismatchForAllApplications_noAnchor_isSkipped() {
        stubAllApplications(applicationWithAdmUnitCode(1L, "App One", "A04026919"));
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(null);

        List<ApplicationDir3MismatchOutputDTO> result = applicationServiceFacadeBean.checkDir3MismatchForAllApplications();

        assertEquals(0, result.size());
        verify(appResponsibleRepository, never()).findAll(any(), any(), any());
    }

    @Test
    void checkDir3MismatchForAllApplications_nonPersonalCaibPerson_neverChecksSoffidAndNeverMismatches() {
        stubAllApplications(applicationWithAdmUnitCode(1L, "App One", "A04026919"));
        AppResponsibleAuthorized anchor = AppResponsibleAuthorized.builder().id(9L).build();
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(anchor);
        AppResponsible responsible = buildAppResponsibleWithDir3Validation(60L, false, Dir3ValidationStatus.NOT_APPLY);
        when(appResponsibleRepository.findAll(eq(9L), any(), any())).thenReturn(new PageImpl<>(List.of(responsible)));
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(9L)).thenReturn(List.of());

        List<ApplicationDir3MismatchOutputDTO> result = applicationServiceFacadeBean.checkDir3MismatchForAllApplications();

        assertEquals(0, result.size());
        verify(personService, never()).checkDir3(any(), any());
        verify(dir3ValidationRepository, never()).update(any(), any());
    }

    @Test
    void checkDir3MismatchForAllApplications_matchingResponsible_isNotFlaggedOrTouched() {
        stubAllApplications(applicationWithAdmUnitCode(1L, "App One", "A04026919"));
        AppResponsibleAuthorized anchor = AppResponsibleAuthorized.builder().id(9L).build();
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(anchor);
        AppResponsible responsible = buildAppResponsibleWithDir3Validation(50L, true, Dir3ValidationStatus.VALIDATED);
        when(appResponsibleRepository.findAll(eq(9L), any(), any())).thenReturn(new PageImpl<>(List.of(responsible)));
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(9L)).thenReturn(List.of());
        when(personService.checkDir3("person50@caib.es", "A04026919")).thenReturn(new PersonDir3CheckOutputDTO(null, "A04026919", "A04026919", true));

        List<ApplicationDir3MismatchOutputDTO> result = applicationServiceFacadeBean.checkDir3MismatchForAllApplications();

        assertEquals(0, result.size());
        assertEquals(Dir3ValidationStatus.VALIDATED, responsible.getDir3Validation().getDir3Status());
        verify(dir3ValidationRepository, never()).update(any(), any());
    }

    @Test
    void checkDir3MismatchForAllApplications_mismatchedResponsible_flagsApplicationAndDowngradesToNoValidado() {
        stubAllApplications(applicationWithAdmUnitCode(1L, "App One", "A04026919"));
        AppResponsibleAuthorized anchor = AppResponsibleAuthorized.builder().id(9L).build();
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(anchor);
        AppResponsible responsible = buildAppResponsibleWithDir3Validation(50L, true, Dir3ValidationStatus.VALIDATED);
        when(appResponsibleRepository.findAll(eq(9L), any(), any())).thenReturn(new PageImpl<>(List.of(responsible)));
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(9L)).thenReturn(List.of());
        when(personService.checkDir3("person50@caib.es", "A04026919")).thenReturn(new PersonDir3CheckOutputDTO(null, "OTHER_UNIT", "A04026919", false));

        List<ApplicationDir3MismatchOutputDTO> result = applicationServiceFacadeBean.checkDir3MismatchForAllApplications();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("App One", result.get(0).getName());
        assertTrue(result.get(0).isDir3Mismatch());
        assertEquals(Dir3ValidationStatus.NOT_VALIDATED, responsible.getDir3Validation().getDir3Status());
        verify(dir3ValidationRepository).update(responsible.getDir3Validation(), 700L);
    }

    @Test
    void checkDir3MismatchForAllApplications_manualAssignmentStillMismatching_isCheckedButProtectedFromDowngrade() {
        stubAllApplications(applicationWithAdmUnitCode(1L, "App One", "A04026919"));
        AppResponsibleAuthorized anchor = AppResponsibleAuthorized.builder().id(9L).build();
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(anchor);
        AppResponsible responsible = buildAppResponsibleWithDir3Validation(50L, true, Dir3ValidationStatus.MANUAL);
        when(appResponsibleRepository.findAll(eq(9L), any(), any())).thenReturn(new PageImpl<>(List.of(responsible)));
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(9L)).thenReturn(List.of());
        when(personService.checkDir3("person50@caib.es", "A04026919")).thenReturn(new PersonDir3CheckOutputDTO(null, "OTHER_UNIT", "A04026919", false));

        List<ApplicationDir3MismatchOutputDTO> result = applicationServiceFacadeBean.checkDir3MismatchForAllApplications();

        assertEquals(0, result.size());
        verify(personService).checkDir3("person50@caib.es", "A04026919");
        assertEquals(Dir3ValidationStatus.MANUAL, responsible.getDir3Validation().getDir3Status());
        verify(dir3ValidationRepository, never()).update(any(), any());
    }

    @Test
    void checkDir3MismatchForAllApplications_manualAssignmentNowMatching_isPromotedToValidadoButNotFlagged() {
        stubAllApplications(applicationWithAdmUnitCode(1L, "App One", "A04026919"));
        AppResponsibleAuthorized anchor = AppResponsibleAuthorized.builder().id(9L).build();
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(anchor);
        AppResponsible responsible = buildAppResponsibleWithDir3Validation(50L, true, Dir3ValidationStatus.MANUAL);
        when(appResponsibleRepository.findAll(eq(9L), any(), any())).thenReturn(new PageImpl<>(List.of(responsible)));
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(9L)).thenReturn(List.of());
        when(personService.checkDir3("person50@caib.es", "A04026919")).thenReturn(new PersonDir3CheckOutputDTO(null, "A04026919", "A04026919", true));

        List<ApplicationDir3MismatchOutputDTO> result = applicationServiceFacadeBean.checkDir3MismatchForAllApplications();

        assertEquals(0, result.size());
        verify(personService).checkDir3("person50@caib.es", "A04026919");
        assertEquals(Dir3ValidationStatus.VALIDATED, responsible.getDir3Validation().getDir3Status());
        verify(dir3ValidationRepository).update(responsible.getDir3Validation(), 700L);
    }

    @Test
    void checkDir3MismatchForAllApplications_noAplicaAssignment_isNeverCheckedOrChanged() {
        stubAllApplications(applicationWithAdmUnitCode(1L, "App One", "A04026919"));
        AppResponsibleAuthorized anchor = AppResponsibleAuthorized.builder().id(9L).build();
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(anchor);
        AppResponsible responsible = buildAppResponsibleWithDir3Validation(50L, true, Dir3ValidationStatus.NOT_APPLY);
        when(appResponsibleRepository.findAll(eq(9L), any(), any())).thenReturn(new PageImpl<>(List.of(responsible)));
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(9L)).thenReturn(List.of());

        List<ApplicationDir3MismatchOutputDTO> result = applicationServiceFacadeBean.checkDir3MismatchForAllApplications();

        assertEquals(0, result.size());
        verify(personService, never()).checkDir3(any(), any());
        assertEquals(Dir3ValidationStatus.NOT_APPLY, responsible.getDir3Validation().getDir3Status());
        verify(dir3ValidationRepository, never()).update(any(), any());
    }

    @Test
    void checkDir3MismatchForAllApplications_alreadyNoValidadoStillMismatching_isIncludedButDir3ValidationNotRewritten() {
        stubAllApplications(applicationWithAdmUnitCode(1L, "App One", "A04026919"));
        AppResponsibleAuthorized anchor = AppResponsibleAuthorized.builder().id(9L).build();
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(anchor);
        AppResponsible responsible = buildAppResponsibleWithDir3Validation(50L, true, Dir3ValidationStatus.NOT_VALIDATED);
        when(appResponsibleRepository.findAll(eq(9L), any(), any())).thenReturn(new PageImpl<>(List.of(responsible)));
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(9L)).thenReturn(List.of());
        when(personService.checkDir3("person50@caib.es", "A04026919")).thenReturn(new PersonDir3CheckOutputDTO(null, "OTHER_UNIT", "A04026919", false));

        List<ApplicationDir3MismatchOutputDTO> result = applicationServiceFacadeBean.checkDir3MismatchForAllApplications();

        // Unlike mismatchFound in the old design, the returned list now reports every application
        // currently mismatching - not just newly-discovered downgrades - so a person who was
        // already NOT_VALIDATED before this run, and still doesn't match, is included here too.
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(personService).checkDir3("person50@caib.es", "A04026919");
        assertEquals(Dir3ValidationStatus.NOT_VALIDATED, responsible.getDir3Validation().getDir3Status());
        // The Dir3Validation status itself didn't change (it was already NOT_VALIDATED), so no need
        // to write it again - only the resulting status matters for the list/application flag.
        verify(dir3ValidationRepository, never()).update(any(), any());
    }

    @Test
    void checkDir3MismatchForAllApplications_alreadyNoValidadoNowMatching_isPromotedToValidadoButNotFlagged() {
        stubAllApplications(applicationWithAdmUnitCode(1L, "App One", "A04026919"));
        AppResponsibleAuthorized anchor = AppResponsibleAuthorized.builder().id(9L).build();
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(anchor);
        AppResponsible responsible = buildAppResponsibleWithDir3Validation(50L, true, Dir3ValidationStatus.NOT_VALIDATED);
        when(appResponsibleRepository.findAll(eq(9L), any(), any())).thenReturn(new PageImpl<>(List.of(responsible)));
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(9L)).thenReturn(List.of());
        when(personService.checkDir3("person50@caib.es", "A04026919")).thenReturn(new PersonDir3CheckOutputDTO(null, "A04026919", "A04026919", true));

        List<ApplicationDir3MismatchOutputDTO> result = applicationServiceFacadeBean.checkDir3MismatchForAllApplications();

        assertEquals(0, result.size());
        verify(personService).checkDir3("person50@caib.es", "A04026919");
        assertEquals(Dir3ValidationStatus.VALIDATED, responsible.getDir3Validation().getDir3Status());
        verify(dir3ValidationRepository).update(responsible.getDir3Validation(), 700L);
    }

    @Test
    void checkDir3MismatchForAllApplications_mismatchedAuthorized_flagsApplicationAndDowngradesToNoValidado() {
        stubAllApplications(applicationWithAdmUnitCode(1L, "App One", "A04026919"));
        AppResponsibleAuthorized anchor = AppResponsibleAuthorized.builder().id(9L).build();
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(anchor);
        when(appResponsibleRepository.findAll(eq(9L), any(), any())).thenReturn(new PageImpl<>(List.of()));
        Person person = new Person();
        person.setId(55L);
        person.setEmail("person55@caib.es");
        person.setPersonalCaib(true);
        AppAuthorized authorized = AppAuthorized.builder()
                .id(2L)
                .person(person)
                .dir3Validation(Dir3Validation.builder().id(701L).dir3Status(Dir3ValidationStatus.VALIDATED).build())
                .build();
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(9L)).thenReturn(List.of(authorized));
        when(personService.checkDir3("person55@caib.es", "A04026919")).thenReturn(new PersonDir3CheckOutputDTO(null, "OTHER_UNIT", "A04026919", false));

        List<ApplicationDir3MismatchOutputDTO> result = applicationServiceFacadeBean.checkDir3MismatchForAllApplications();

        assertEquals(1, result.size());
        assertEquals(Dir3ValidationStatus.NOT_VALIDATED, authorized.getDir3Validation().getDir3Status());
        verify(dir3ValidationRepository).update(authorized.getDir3Validation(), 701L);
    }

    @Test
    void checkDir3MismatchForAllApplications_onlyMismatchedApplicationsAreReturned() {
        Application cleanApp = applicationWithAdmUnitCode(1L, "Clean App", "A04026919");
        Application mismatchedApp = applicationWithAdmUnitCode(2L, "Mismatched App", "A04026919");
        stubAllApplications(cleanApp, mismatchedApp);
        AppResponsibleAuthorized anchor1 = AppResponsibleAuthorized.builder().id(9L).build();
        AppResponsibleAuthorized anchor2 = AppResponsibleAuthorized.builder().id(10L).build();
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(anchor1);
        when(appResponsibleAuthorizedRepository.findByApplicationId(2L)).thenReturn(anchor2);
        AppResponsible matching = buildAppResponsibleWithDir3Validation(50L, true, Dir3ValidationStatus.VALIDATED);
        AppResponsible mismatched = buildAppResponsibleWithDir3Validation(51L, true, Dir3ValidationStatus.VALIDATED);
        when(appResponsibleRepository.findAll(eq(9L), any(), any())).thenReturn(new PageImpl<>(List.of(matching)));
        when(appResponsibleRepository.findAll(eq(10L), any(), any())).thenReturn(new PageImpl<>(List.of(mismatched)));
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(any())).thenReturn(List.of());
        when(personService.checkDir3("person50@caib.es", "A04026919")).thenReturn(new PersonDir3CheckOutputDTO(null, "A04026919", "A04026919", true));
        when(personService.checkDir3("person51@caib.es", "A04026919")).thenReturn(new PersonDir3CheckOutputDTO(null, "OTHER_UNIT", "A04026919", false));

        List<ApplicationDir3MismatchOutputDTO> result = applicationServiceFacadeBean.checkDir3MismatchForAllApplications();

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getId());
        assertEquals("Mismatched App", result.get(0).getName());
    }

    @Test
    void checkDir3MismatchForAllApplications_currentMismatch_persistsTrueOnApplication() {
        Application application = applicationWithAdmUnitCode(1L, "App One", "A04026919");
        stubAllApplications(application);
        AppResponsibleAuthorized anchor = AppResponsibleAuthorized.builder().id(9L).build();
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(anchor);
        AppResponsible responsible = buildAppResponsibleWithDir3Validation(50L, true, Dir3ValidationStatus.VALIDATED);
        when(appResponsibleRepository.findAll(eq(9L), any(), any())).thenReturn(new PageImpl<>(List.of(responsible)));
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(9L)).thenReturn(List.of());
        when(personService.checkDir3("person50@caib.es", "A04026919")).thenReturn(new PersonDir3CheckOutputDTO(null, "OTHER_UNIT", "A04026919", false));

        applicationServiceFacadeBean.checkDir3MismatchForAllApplications();

        assertTrue(application.isDir3Mismatch());
        verify(applicationRepository).update(application, 1L);
    }

    @Test
    void checkDir3MismatchForAllApplications_onlyManualMismatching_doesNotPersistDir3MismatchTrue() {
        Application application = applicationWithAdmUnitCode(1L, "App One", "A04026919");
        stubAllApplications(application);
        AppResponsibleAuthorized anchor = AppResponsibleAuthorized.builder().id(9L).build();
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(anchor);
        AppResponsible responsible = buildAppResponsibleWithDir3Validation(50L, true, Dir3ValidationStatus.MANUAL);
        when(appResponsibleRepository.findAll(eq(9L), any(), any())).thenReturn(new PageImpl<>(List.of(responsible)));
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(9L)).thenReturn(List.of());
        when(personService.checkDir3("person50@caib.es", "A04026919")).thenReturn(new PersonDir3CheckOutputDTO(null, "OTHER_UNIT", "A04026919", false));

        applicationServiceFacadeBean.checkDir3MismatchForAllApplications();

        assertFalse(application.isDir3Mismatch());
        verify(applicationRepository, never()).update(any(), any());
    }

    @Test
    void checkDir3MismatchForAllApplications_flagAlreadyCurrent_doesNotCallUpdateAgain() {
        Application application = applicationWithAdmUnitCode(1L, "App One", "A04026919");
        application.setDir3Mismatch(true);
        stubAllApplications(application);
        AppResponsibleAuthorized anchor = AppResponsibleAuthorized.builder().id(9L).build();
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(anchor);
        AppResponsible responsible = buildAppResponsibleWithDir3Validation(50L, true, Dir3ValidationStatus.NOT_VALIDATED);
        when(appResponsibleRepository.findAll(eq(9L), any(), any())).thenReturn(new PageImpl<>(List.of(responsible)));
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(9L)).thenReturn(List.of());
        when(personService.checkDir3("person50@caib.es", "A04026919")).thenReturn(new PersonDir3CheckOutputDTO(null, "OTHER_UNIT", "A04026919", false));

        applicationServiceFacadeBean.checkDir3MismatchForAllApplications();

        assertTrue(application.isDir3Mismatch());
        verify(applicationRepository, never()).update(any(), any());
    }

    @Test
    void checkDir3MismatchForAllApplications_blankAdmUnitCode_clearsStaleMismatchFlag() {
        Application application = applicationWithAdmUnitCode(1L, "App One", null);
        application.setDir3Mismatch(true);
        stubAllApplications(application);

        applicationServiceFacadeBean.checkDir3MismatchForAllApplications();

        assertFalse(application.isDir3Mismatch());
        verify(applicationRepository).update(application, 1L);
    }

    @Test
    void checkDir3MismatchForAllApplications_noAnchor_clearsStaleMismatchFlag() {
        Application application = applicationWithAdmUnitCode(1L, "App One", "A04026919");
        application.setDir3Mismatch(true);
        stubAllApplications(application);
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(null);

        applicationServiceFacadeBean.checkDir3MismatchForAllApplications();

        assertFalse(application.isDir3Mismatch());
        verify(applicationRepository).update(application, 1L);
    }
}

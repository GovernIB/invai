package es.caib.invai.back.ejb.application.core;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.core.DTO.ApplicationInputDTO;
import es.caib.invai.back.interna.application.core.DTO.ApplicationOutputDTO;
import es.caib.invai.back.persistence.repository.application.accessibility.AppAccessibilityRepository;
import es.caib.invai.back.persistence.repository.application.core.ApplicationCriteria;
import es.caib.invai.back.persistence.repository.application.core.ApplicationRepository;
import es.caib.invai.back.interna.application.security.ensClassification.DTO.AppEnsClassificationOutputDTO;
import es.caib.invai.back.interna.application.security.risk.DTO.AppSecurityRiskOutputDTO;
import es.caib.invai.back.interna.application.security.webContext.DTO.AppWebContextOutputDTO;
import es.caib.invai.back.interna.application.system_database.database.DTO.AppDatabaseOutputDTO;
import es.caib.invai.back.interna.application.system_database.system.DTO.AppSystemOutputDTO;
import es.caib.invai.back.persistence.repository.application.development.core.AppDevelopmentRepository;
import es.caib.invai.back.persistence.repository.application.system_database.core.AppInformationSystemDbRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.core.AppResponsibleAuthorizedRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.authorized.AppAuthorizedRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.responsible.AppResponsibleRepository;
import es.caib.invai.back.persistence.repository.application.security.core.AppSecurityRepository;
import es.caib.invai.back.persistence.repository.catalog.responsibleType.ResponsibleTypeRepository;
import es.caib.invai.back.interna.integrations.dir3.admUnit.DTO.AdmUnitOutputDTO;
import es.caib.invai.back.service.facade.application.security.ensClassification.AppEnsClassificationService;
import es.caib.invai.back.service.facade.application.security.risk.AppSecurityRiskService;
import es.caib.invai.back.service.facade.application.security.webContext.AppWebContextService;
import es.caib.invai.back.service.facade.application.system_database.database.AppDatabaseService;
import es.caib.invai.back.service.facade.application.system_database.system.AppSystemService;
import es.caib.invai.back.service.facade.integrations.dir3.admUnit.AdmUnitService;
import es.caib.invai.back.service.mapper.application.core.ApplicationMapper;
import es.caib.invai.back.service.model.application.accessibility.AppAccessibility;
import es.caib.invai.back.service.model.application.system_database.core.AppInformationSystemDb;
import es.caib.invai.back.service.model.application.core.Application;
import es.caib.invai.back.service.model.application.development.core.AppDevelopment;
import es.caib.invai.back.service.model.application.responsibleAuthorized.core.AppResponsibleAuthorized;
import es.caib.invai.back.service.model.application.security.core.AppSecurity;
import es.caib.invai.back.service.model.application.responsibleAuthorized.responsible.AppResponsible;
import es.caib.invai.back.service.model.application.responsibleAuthorized.authorized.AppAuthorized;
import es.caib.invai.back.service.model.catalog.responsibleType.ResponsibleType;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
    private AppWebContextService appWebContextService;

    @Mock
    private AppEnsClassificationService appEnsClassificationService;

    @Mock
    private AppSecurityRiskService appSecurityRiskService;

    @Mock
    private AdmUnitService admUnitService;

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
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean()))
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
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean()))
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
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean()))
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
        ApplicationOutputDTO expected = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(appInformationSystemDbRepository.findByApplicationId(1L)).thenReturn(infoDb);
        when(appDevelopmentRepository.findByApplicationId(1L)).thenReturn(development);
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(appResponsibleAuthorized);
        when(appSecurityRepository.findByApplicationId(1L)).thenReturn(appSecurity);
        when(appAccessibilityRepository.findByApplicationId(1L)).thenReturn(accessibility);
        when(responsibleTypeRepository.findAll()).thenReturn(List.of());
        when(appAuthorizedRepository.findAllActiveByAppResponsibleAuthorized(9L)).thenReturn(List.of(new AppAuthorized()));
        when(appWebContextService.getAll(eq(11L), any(), any())).thenReturn(new PageImpl<>(List.of(new AppWebContextOutputDTO())));
        when(appEnsClassificationService.getAll(eq(11L), any(), any())).thenReturn(new PageImpl<>(List.of(new AppEnsClassificationOutputDTO())));
        when(appSecurityRiskService.getAll(eq(11L), any(), any())).thenReturn(new PageImpl<>(List.of(new AppSecurityRiskOutputDTO())));
        when(appSystemService.getAll(eq(5L), any(), any())).thenReturn(new PageImpl<>(List.of(new AppSystemOutputDTO())));
        when(appDatabaseService.getAll(eq(5L), any(), any())).thenReturn(new PageImpl<>(List.of(new AppDatabaseOutputDTO())));
        when(applicationMapper.toResponse(eq(activeApplication), eq(infoDb), eq(development), eq(appResponsibleAuthorized), eq(appSecurity), eq(accessibility),
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean()))
                .thenReturn(expected);

        ApplicationOutputDTO result = applicationServiceFacadeBean.getById(1L);

        assertFalse(result.isIncomplete());
    }

    @Test
    void getById_admUnitLinked_setsAdmUnitAndDepartmentFromAdmUnitCode() {
        activeApplication.setAdmUnitCode("DGSAN");
        AdmUnitOutputDTO admUnit = new AdmUnitOutputDTO();
        admUnit.setCode("DGSAN");
        AdmUnitOutputDTO department = new AdmUnitOutputDTO();
        department.setCode("A04026919");
        ApplicationOutputDTO expected = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(applicationMapper.toResponse(eq(activeApplication), any(), any(), any(), any(), any(),
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean()))
                .thenReturn(expected);
        when(admUnitService.resolveByCode("DGSAN")).thenReturn(admUnit);
        when(admUnitService.resolveDepartment("DGSAN")).thenReturn(department);

        ApplicationOutputDTO result = applicationServiceFacadeBean.getById(1L);

        assertSame(admUnit, result.getAdmUnit());
        assertSame(department, result.getDepartment());
    }

    @Test
    void getById_noAdmUnitLinked_leavesAdmUnitAndDepartmentNull() {
        ApplicationOutputDTO expected = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(applicationMapper.toResponse(eq(activeApplication), any(), any(), any(), any(), any(),
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean()))
                .thenReturn(expected);

        ApplicationOutputDTO result = applicationServiceFacadeBean.getById(1L);

        assertNull(result.getAdmUnit());
        assertNull(result.getDepartment());
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
        when(applicationMapper.toResponse(activeApplication, infoDb, development, anchor, appSecurity, appAccessibility, false, false, false, false, false, false, false)).thenReturn(expected);

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
        when(applicationMapper.toResponse(eq(activeApplication), any(), eq(development), eq(anchor), eq(null), eq(null), eq(false), eq(true), eq(false), eq(true), eq(true), eq(true), eq(true)))
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
        when(applicationMapper.toResponse(eq(activeApplication), any(), eq(development), eq(anchor), eq(null), eq(null), eq(false), eq(false), eq(true), eq(true), eq(true), eq(true), eq(true)))
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
        when(applicationMapper.toResponse(eq(activeApplication), any(), eq(development), eq(null), eq(null), eq(null), eq(false), eq(true), eq(true), eq(true), eq(true), eq(true), eq(true)))
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
        when(applicationMapper.toResponse(eq(activeApplication), any(), eq(null), eq(null), eq(null), eq(null), eq(true), eq(true), eq(true), eq(true), eq(true), eq(true), eq(true)))
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
        when(applicationMapper.toResponse(eq(activeApplication), any(), eq(development), eq(null), eq(null), eq(null), eq(true), eq(true), eq(true), eq(true), eq(true), eq(true), eq(true)))
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
        when(applicationMapper.toResponse(eq(activeApplication), any(), eq(development), eq(anchor), eq(null), eq(null), eq(false), eq(false), eq(false), eq(true), eq(true), eq(true), eq(true)))
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
                anyBoolean(), anyBoolean(), anyBoolean(), eq(true), anyBoolean(), anyBoolean(), anyBoolean()))
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
                anyBoolean(), anyBoolean(), anyBoolean(), eq(true), anyBoolean(), anyBoolean(), anyBoolean()))
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
                anyBoolean(), anyBoolean(), anyBoolean(), eq(false), anyBoolean(), anyBoolean(), anyBoolean()))
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
                anyBoolean(), anyBoolean(), anyBoolean(), eq(true), anyBoolean(), anyBoolean(), anyBoolean()))
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
                anyBoolean(), anyBoolean(), anyBoolean(), eq(false), anyBoolean(), anyBoolean(), anyBoolean()))
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
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), eq(true), anyBoolean(), anyBoolean()))
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
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), eq(true), anyBoolean(), anyBoolean()))
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
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), eq(false), anyBoolean(), anyBoolean()))
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
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), eq(true), eq(true)))
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
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), eq(true), eq(false)))
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
                anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), eq(false), eq(false)))
                .thenReturn(expected);

        ApplicationOutputDTO result = applicationServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
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
        when(admUnitService.resolveByCodeOrThrow("UNKNOWN")).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> applicationServiceFacadeBean.create(inputDTO));

        assertEquals(Constants.ERR_APPLICATION_ADMUNIT_NOT_FOUND, ex.getMessage());
        verify(applicationRepository, never()).create(any());
    }

    @Test
    void create_dir3CaibUnavailable_propagatesBusinessRuleExceptionRatherThanNotFound() {
        ApplicationInputDTO inputDTO = buildInputDTO();
        inputDTO.setAdmUnitCode("DGSAN");
        when(admUnitService.resolveByCodeOrThrow("DGSAN")).thenThrow(new BusinessRuleException(Constants.ERR_ADMUNIT_DIR3_UNAVAILABLE));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> applicationServiceFacadeBean.create(inputDTO));

        assertEquals(Constants.ERR_ADMUNIT_DIR3_UNAVAILABLE, ex.getMessage());
        verify(applicationRepository, never()).create(any());
    }

    @Test
    void create_admUnitCodeIsDepartmentItself_throwsBusinessRuleException() {
        ApplicationInputDTO inputDTO = buildInputDTO();
        inputDTO.setAdmUnitCode("A04026919");
        AdmUnitOutputDTO admUnit = new AdmUnitOutputDTO();
        admUnit.setCode("A04026919");
        admUnit.setLevel(2);
        when(admUnitService.resolveByCodeOrThrow("A04026919")).thenReturn(admUnit);
        when(admUnitService.getDepartmentHierarchyLevel()).thenReturn(2);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> applicationServiceFacadeBean.create(inputDTO));

        assertEquals(Constants.ERR_APPLICATION_ADMUNIT_MUST_BE_DEPARTMENT_CHILD, ex.getMessage());
        verify(applicationRepository, never()).create(any());
    }

    @Test
    void create_admUnitCodeFoundInDir3Caib_setsAdmUnitOnResponse() {
        ApplicationInputDTO inputDTO = buildInputDTO();
        inputDTO.setAdmUnitCode("A04026919");
        AdmUnitOutputDTO admUnit = new AdmUnitOutputDTO();
        admUnit.setCode("A04026919");
        admUnit.setLevel(3);
        when(admUnitService.resolveByCodeOrThrow("A04026919")).thenReturn(admUnit);
        when(admUnitService.resolveByCode("A04026919")).thenReturn(admUnit);
        when(admUnitService.getDepartmentHierarchyLevel()).thenReturn(2);
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
        when(admUnitService.resolveByCodeOrThrow("UNKNOWN")).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> applicationServiceFacadeBean.update(1L, inputDTO));
        assertEquals(Constants.ERR_APPLICATION_ADMUNIT_NOT_FOUND, ex.getMessage());
        verify(applicationMapper, never()).updateModelFromInput(any(), any());
    }

    @Test
    void update_admUnitCodeIsDepartmentItself_throwsBusinessRuleException() {
        ApplicationInputDTO inputDTO = buildInputDTO();
        inputDTO.setAdmUnitCode("A04026919");
        AdmUnitOutputDTO admUnit = new AdmUnitOutputDTO();
        admUnit.setCode("A04026919");
        admUnit.setLevel(2);
        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(admUnitService.resolveByCodeOrThrow("A04026919")).thenReturn(admUnit);
        when(admUnitService.getDepartmentHierarchyLevel()).thenReturn(2);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> applicationServiceFacadeBean.update(1L, inputDTO));
        assertEquals(Constants.ERR_APPLICATION_ADMUNIT_MUST_BE_DEPARTMENT_CHILD, ex.getMessage());
        verify(applicationMapper, never()).updateModelFromInput(any(), any());
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
}

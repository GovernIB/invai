package es.caib.invai.back.ejb.application.core;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.core.DTO.ApplicationInputDTO;
import es.caib.invai.back.interna.application.core.DTO.ApplicationOutputDTO;
import es.caib.invai.back.persistence.repository.application.core.ApplicationCriteria;
import es.caib.invai.back.persistence.repository.application.core.ApplicationRepository;
import es.caib.invai.back.persistence.repository.application.development.core.AppDevelopmentRepository;
import es.caib.invai.back.persistence.repository.application.system_database.core.AppInformationSystemDbRepository;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.core.AppResponsibleAuthorizedRepository;
import es.caib.invai.back.service.mapper.application.core.ApplicationMapper;
import es.caib.invai.back.service.model.application.system_database.core.AppInformationSystemDb;
import es.caib.invai.back.service.model.application.core.Application;
import es.caib.invai.back.service.model.application.development.core.AppDevelopment;
import es.caib.invai.back.service.model.application.responsibleAuthorized.core.AppResponsibleAuthorized;
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
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private AppDevelopmentRepository appDevelopmentRepository;

    @Mock
    private AppResponsibleAuthorizedRepository appResponsibleAuthorizedRepository;

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
        ApplicationOutputDTO expected = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(appInformationSystemDbRepository.findByApplicationId(1L)).thenReturn(infoDb);
        when(appDevelopmentRepository.findByApplicationId(1L)).thenReturn(development);
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(appResponsibleAuthorized);
        when(applicationMapper.toResponse(activeApplication, infoDb, development, appResponsibleAuthorized)).thenReturn(expected);

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
        when(applicationMapper.toResponse(activeApplication, null, null, null)).thenReturn(expected);

        ApplicationOutputDTO result = applicationServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_notFound_throwsBusinessRuleException() {
        when(applicationRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> applicationServiceFacadeBean.getById(99L));
        assertEquals(Constants.ERR_APP_NOT_FOUND, ex.getMessage());
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
        ApplicationOutputDTO response = new ApplicationOutputDTO();

        when(applicationRepository.existsByCode("APP02")).thenReturn(false);
        when(applicationRepository.existsByPrefix("AP2")).thenReturn(false);
        when(applicationMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(applicationRepository.create(model)).thenReturn(saved);
        when(appInformationSystemDbRepository.create(any(AppInformationSystemDb.class))).thenReturn(savedInfoDb);
        when(appDevelopmentRepository.create(any(AppDevelopment.class))).thenReturn(savedDev);
        when(appResponsibleAuthorizedRepository.create(any(AppResponsibleAuthorized.class))).thenReturn(savedAppResponsibleAuthorized);
        when(applicationMapper.toResponse(saved, savedInfoDb, savedDev, savedAppResponsibleAuthorized)).thenReturn(response);

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
        when(applicationMapper.toResponse(any(), any(), any(), any())).thenReturn(new ApplicationOutputDTO());

        applicationServiceFacadeBean.create(inputDTO);

        assertEquals(StatusEnum.INACTIVE, model.getStatus());
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
        ApplicationOutputDTO response = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(applicationRepository.existsByCodeAndIdNot("APP02", 1L)).thenReturn(false);
        when(applicationRepository.existsByPrefixAndIdNot("AP2", 1L)).thenReturn(false);
        when(applicationRepository.update(activeApplication, 1L)).thenReturn(updated);
        when(appInformationSystemDbRepository.findByApplicationId(1L)).thenReturn(null);
        when(appDevelopmentRepository.findByApplicationId(1L)).thenReturn(null);
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(null);
        when(appInformationSystemDbRepository.create(any(AppInformationSystemDb.class))).thenReturn(createdInfoDb);
        when(appDevelopmentRepository.create(any(AppDevelopment.class))).thenReturn(createdDev);
        when(appResponsibleAuthorizedRepository.create(any(AppResponsibleAuthorized.class))).thenReturn(createdAppResponsibleAuthorized);
        when(applicationMapper.toResponse(updated, createdInfoDb, createdDev, createdAppResponsibleAuthorized)).thenReturn(response);

        ApplicationOutputDTO result = applicationServiceFacadeBean.update(1L, inputDTO);

        verify(applicationMapper).updateModelFromInput(inputDTO, activeApplication);
        verify(appInformationSystemDbRepository).create(any(AppInformationSystemDb.class));
        verify(appDevelopmentRepository).create(any(AppDevelopment.class));
        verify(appResponsibleAuthorizedRepository).create(any(AppResponsibleAuthorized.class));
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
        ApplicationOutputDTO response = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(applicationRepository.existsByCodeAndIdNot("APP02", 1L)).thenReturn(false);
        when(applicationRepository.existsByPrefixAndIdNot("AP2", 1L)).thenReturn(false);
        when(applicationRepository.update(activeApplication, 1L)).thenReturn(updated);
        when(appInformationSystemDbRepository.findByApplicationId(1L)).thenReturn(existingInfoDb);
        when(appDevelopmentRepository.findByApplicationId(1L)).thenReturn(existingDev);
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(existingAppResponsibleAuthorized);
        when(applicationMapper.toResponse(updated, existingInfoDb, existingDev, existingAppResponsibleAuthorized)).thenReturn(response);

        ApplicationOutputDTO result = applicationServiceFacadeBean.update(1L, inputDTO);

        verify(appInformationSystemDbRepository, never()).create(any());
        verify(appDevelopmentRepository, never()).create(any());
        verify(appResponsibleAuthorizedRepository, never()).create(any());
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

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(appInformationSystemDbRepository.findByApplicationId(1L)).thenReturn(infoDb);
        when(appDevelopmentRepository.findByApplicationId(1L)).thenReturn(dev);
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(appResponsibleAuthorized);

        applicationServiceFacadeBean.delete(1L);

        assertEquals(StatusEnum.INACTIVE, activeApplication.getStatus());
        assertTrue(activeApplication.getDeletedAt() != null);
        assertTrue(activeApplication.getExpirationDate() != null);
        verify(applicationRepository, times(1)).delete(activeApplication);
        assertTrue(infoDb.getDeletedAt() != null);
        verify(appInformationSystemDbRepository, times(1)).delete(infoDb);
        assertTrue(dev.getDeletedAt() != null);
        verify(appDevelopmentRepository, times(1)).delete(dev);
        assertTrue(appResponsibleAuthorized.getDeletedAt() != null);
        verify(appResponsibleAuthorizedRepository, times(1)).delete(appResponsibleAuthorized);
    }

    @Test
    void delete_valid_childRecordsAlreadyDeleted_skipsReDeletion() {
        AppInformationSystemDb infoDb = new AppInformationSystemDb();
        infoDb.setDeletedAt(LocalDateTime.now().minusDays(1));
        AppDevelopment dev = new AppDevelopment();
        dev.setDeletedAt(LocalDateTime.now().minusDays(1));
        AppResponsibleAuthorized appResponsibleAuthorized = new AppResponsibleAuthorized();
        appResponsibleAuthorized.setDeletedAt(LocalDateTime.now().minusDays(1));

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(appInformationSystemDbRepository.findByApplicationId(1L)).thenReturn(infoDb);
        when(appDevelopmentRepository.findByApplicationId(1L)).thenReturn(dev);
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(appResponsibleAuthorized);

        applicationServiceFacadeBean.delete(1L);

        verify(appInformationSystemDbRepository, never()).delete(any());
        verify(appDevelopmentRepository, never()).delete(any());
        verify(appResponsibleAuthorizedRepository, never()).delete(any());
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
        ApplicationOutputDTO response = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(applicationRepository.update(activeApplication, 1L)).thenReturn(updated);
        when(appInformationSystemDbRepository.findByApplicationId(1L)).thenReturn(infoDb);
        when(appDevelopmentRepository.findByApplicationId(1L)).thenReturn(dev);
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(appResponsibleAuthorized);
        when(appInformationSystemDbRepository.update(infoDb, 5L)).thenReturn(updatedInfoDb);
        when(appDevelopmentRepository.update(dev, 7L)).thenReturn(updatedDev);
        when(appResponsibleAuthorizedRepository.update(appResponsibleAuthorized, 9L)).thenReturn(updatedAppResponsibleAuthorized);
        when(applicationMapper.toResponse(updated, updatedInfoDb, updatedDev, updatedAppResponsibleAuthorized)).thenReturn(response);

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
        ApplicationOutputDTO response = new ApplicationOutputDTO();

        when(applicationRepository.findById(1L)).thenReturn(activeApplication);
        when(applicationRepository.update(activeApplication, 1L)).thenReturn(updated);
        when(appInformationSystemDbRepository.findByApplicationId(1L)).thenReturn(infoDb);
        when(appDevelopmentRepository.findByApplicationId(1L)).thenReturn(dev);
        when(appResponsibleAuthorizedRepository.findByApplicationId(1L)).thenReturn(appResponsibleAuthorized);
        when(applicationMapper.toResponse(updated, infoDb, dev, appResponsibleAuthorized)).thenReturn(response);

        ApplicationOutputDTO result = applicationServiceFacadeBean.reactivate(1L);

        verify(appInformationSystemDbRepository, never()).update(any(), any());
        verify(appDevelopmentRepository, never()).update(any(), any());
        verify(appResponsibleAuthorizedRepository, never()).update(any(), any());
        assertEquals(response, result);
    }
}

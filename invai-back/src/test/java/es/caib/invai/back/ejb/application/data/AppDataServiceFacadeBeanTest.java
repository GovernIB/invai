package es.caib.invai.back.ejb.application.data;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.exception.DataTimeoutException;
import es.caib.invai.back.interna.application.data.DTO.AppDataInputDTO;
import es.caib.invai.back.interna.application.data.DTO.AppDataOutputDTO;
import es.caib.invai.back.interna.application.data.DTO.AppDataUrlInputDTO;
import es.caib.invai.back.interna.application.data.DTO.AppDataUrlOutputDTO;
import es.caib.invai.back.persistence.repository.application.core.ApplicationRepository;
import es.caib.invai.back.persistence.repository.application.data.AppDataRepository;
import es.caib.invai.back.rest.data.DataApiClient;
import es.caib.invai.back.rest.data.UrlCheckResult;
import es.caib.invai.back.service.mapper.application.data.AppDataMapper;
import es.caib.invai.back.service.model.application.core.Application;
import es.caib.invai.back.service.model.application.data.AppData;
import es.caib.invai.back.utils.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AppDataServiceFacadeBean}, with every collaborator mocked. {@code
 * @Value}-injected fields are only resolved inside a Spring context, so they're set explicitly
 * here, mirroring what the corresponding properties resolve to in production.
 */
@ExtendWith(MockitoExtension.class)
class AppDataServiceFacadeBeanTest {

    private static final String DEFAULT_OPEN_DATA_URL = "https://intranet.caib.es/sedeibapi/externa";
    private static final String DEFAULT_REUSE_URL = "https://intranet.caib.es/sedeibapi/interna";
    private static final String TYPED_OPEN_DATA_URL = "https://swagger.example.test/open";
    private static final String TYPED_REUSE_URL = "https://swagger.example.test/reuse";

    @Mock
    private AppDataMapper appDataMapper;

    @Mock
    private AppDataRepository appDataRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private DataApiClient dataApiClient;

    @InjectMocks
    private AppDataServiceFacadeBean appDataServiceFacadeBean;

    private Application application;
    private AppData activeModel;

    @BeforeEach
    void setUp() {
        application = new Application();
        application.setId(10L);
        application.setCode("SEDEIB");

        activeModel = new AppData();
        activeModel.setId(1L);
        activeModel.setApplication(application);

        ReflectionTestUtils.setField(appDataServiceFacadeBean, "baseUrl", "https://intranet.caib.es");
        ReflectionTestUtils.setField(appDataServiceFacadeBean, "openDataPathPrefix", "api/externa");
        ReflectionTestUtils.setField(appDataServiceFacadeBean, "reusePathPrefix", "api/interna");
    }

    private static UrlCheckResult answer(String url, int statusCode) {
        return new UrlCheckResult(url, statusCode >= 200 && statusCode < 300, statusCode);
    }

    private static AppDataInputDTO input() {
        return new AppDataInputDTO(10L, "obs");
    }

    // ------------------------------------------------------------------
    // getById
    // ------------------------------------------------------------------

    @Test
    void getById_notFound_returnsNullAndChecksNothing() {
        when(appDataRepository.findById(99L)).thenReturn(null);

        assertNull(appDataServiceFacadeBean.getById(99L));

        verify(dataApiClient, never()).checkUrl(any());
    }

    @Test
    void getById_noTypedUrls_returnsTheDefaultOnesBuiltFromTheCode() {
        AppDataOutputDTO mapped = new AppDataOutputDTO();
        when(appDataRepository.findById(1L)).thenReturn(activeModel);
        when(appDataMapper.toResponse(activeModel)).thenReturn(mapped);

        AppDataOutputDTO result = appDataServiceFacadeBean.getById(1L);

        assertSame(mapped, result);
        assertEquals(DEFAULT_OPEN_DATA_URL, result.getOpenDataUrl());
        assertEquals(DEFAULT_REUSE_URL, result.getReuseUrl());
    }

    @Test
    void getById_typedUrls_areReturnedInsteadOfTheDefaultOnes() {
        activeModel.setOpenDataUrl(TYPED_OPEN_DATA_URL);
        activeModel.setReuseUrl(TYPED_REUSE_URL);
        when(appDataRepository.findById(1L)).thenReturn(activeModel);
        when(appDataMapper.toResponse(activeModel)).thenReturn(new AppDataOutputDTO());

        AppDataOutputDTO result = appDataServiceFacadeBean.getById(1L);

        assertEquals(TYPED_OPEN_DATA_URL, result.getOpenDataUrl());
        assertEquals(TYPED_REUSE_URL, result.getReuseUrl());
    }

    @Test
    void getById_aTypedUrlOnOneTabOnly_leavesTheOtherOneOnItsDefault() {
        activeModel.setOpenDataUrl(TYPED_OPEN_DATA_URL);
        when(appDataRepository.findById(1L)).thenReturn(activeModel);
        when(appDataMapper.toResponse(activeModel)).thenReturn(new AppDataOutputDTO());

        AppDataOutputDTO result = appDataServiceFacadeBean.getById(1L);

        assertEquals(TYPED_OPEN_DATA_URL, result.getOpenDataUrl());
        assertEquals(DEFAULT_REUSE_URL, result.getReuseUrl());
    }

    @Test
    void getById_aBlankTypedUrl_isTreatedAsTheDefaultOne() {
        activeModel.setOpenDataUrl("   ");
        when(appDataRepository.findById(1L)).thenReturn(activeModel);
        when(appDataMapper.toResponse(activeModel)).thenReturn(new AppDataOutputDTO());

        assertEquals(DEFAULT_OPEN_DATA_URL, appDataServiceFacadeBean.getById(1L).getOpenDataUrl());
    }

    @Test
    void getById_theDefaultUrlIsTheHostThenTheLowerCasedCodeDirectlyFollowedByThePathSegment() {
        application.setCode("APP_02");
        when(appDataRepository.findById(1L)).thenReturn(activeModel);
        when(appDataMapper.toResponse(activeModel)).thenReturn(new AppDataOutputDTO());

        assertEquals("https://intranet.caib.es/app_02api/externa", appDataServiceFacadeBean.getById(1L).getOpenDataUrl());
    }

    @Test
    void getById_theDefaultUrlIgnoresATrailingSlashOnTheHostOrOnThePathSegment() {
        ReflectionTestUtils.setField(appDataServiceFacadeBean, "baseUrl", "https://intranet.caib.es/");
        ReflectionTestUtils.setField(appDataServiceFacadeBean, "openDataPathPrefix", "api/externa/");
        ReflectionTestUtils.setField(appDataServiceFacadeBean, "reusePathPrefix", "api/interna/");
        when(appDataRepository.findById(1L)).thenReturn(activeModel);
        when(appDataMapper.toResponse(activeModel)).thenReturn(new AppDataOutputDTO());

        AppDataOutputDTO result = appDataServiceFacadeBean.getById(1L);

        assertEquals(DEFAULT_OPEN_DATA_URL, result.getOpenDataUrl());
        assertEquals(DEFAULT_REUSE_URL, result.getReuseUrl());
    }

    @Test
    void getById_queriesNoUrl_andStoresNothing() {
        when(appDataRepository.findById(1L)).thenReturn(activeModel);
        when(appDataMapper.toResponse(activeModel)).thenReturn(new AppDataOutputDTO());

        appDataServiceFacadeBean.getById(1L);

        verifyNoInteractions(dataApiClient);
        verify(appDataRepository, never()).update(any(), any());
        verify(appDataRepository, never()).create(any());
    }

    // ------------------------------------------------------------------
    // checkUrl
    // ------------------------------------------------------------------

    @Test
    void checkUrl_aUrlThatWorks_isReportedValid() {
        when(dataApiClient.checkUrl(DEFAULT_OPEN_DATA_URL)).thenReturn(answer(DEFAULT_OPEN_DATA_URL, 200));

        AppDataUrlOutputDTO result = appDataServiceFacadeBean.checkUrl(DEFAULT_OPEN_DATA_URL);

        assertEquals(DEFAULT_OPEN_DATA_URL, result.getUrl());
        assertTrue(result.isValidUrl());
    }

    @Test
    void checkUrl_aUrlAnsweringAnError_isReportedNotValid_notRaisedAsAnError() {
        when(dataApiClient.checkUrl(DEFAULT_REUSE_URL)).thenReturn(answer(DEFAULT_REUSE_URL, 404));

        AppDataUrlOutputDTO result = appDataServiceFacadeBean.checkUrl(DEFAULT_REUSE_URL);

        assertEquals(DEFAULT_REUSE_URL, result.getUrl());
        assertFalse(result.isValidUrl());
    }

    @Test
    void checkUrl_aUrlThatCouldNotBeQueried_isReportedNotValid() {
        when(dataApiClient.checkUrl(DEFAULT_REUSE_URL)).thenReturn(new UrlCheckResult(DEFAULT_REUSE_URL, false, null));

        assertFalse(appDataServiceFacadeBean.checkUrl(DEFAULT_REUSE_URL).isValidUrl());
    }

    @Test
    void checkUrl_alwaysQueriesTheUrl_nothingIsRememberedFromTheLastCall() {
        when(dataApiClient.checkUrl(DEFAULT_OPEN_DATA_URL)).thenReturn(answer(DEFAULT_OPEN_DATA_URL, 200));

        appDataServiceFacadeBean.checkUrl(DEFAULT_OPEN_DATA_URL);
        appDataServiceFacadeBean.checkUrl(DEFAULT_OPEN_DATA_URL);

        verify(dataApiClient, times(2)).checkUrl(DEFAULT_OPEN_DATA_URL);
        verifyNoInteractions(appDataRepository);
    }

    @Test
    void checkUrl_theCheckTimingOut_propagates() {
        when(dataApiClient.checkUrl(DEFAULT_OPEN_DATA_URL)).thenThrow(new DataTimeoutException());

        assertThrows(DataTimeoutException.class, () -> appDataServiceFacadeBean.checkUrl(DEFAULT_OPEN_DATA_URL));
    }

    @Test
    void checkUrl_theCheckBeingUnavailable_propagates() {
        when(dataApiClient.checkUrl(DEFAULT_OPEN_DATA_URL)).thenThrow(new BusinessRuleException(Constants.ERR_DATA_URL_UNAVAILABLE));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appDataServiceFacadeBean.checkUrl(DEFAULT_OPEN_DATA_URL));

        assertEquals(Constants.ERR_DATA_URL_UNAVAILABLE, ex.getMessage());
    }

    // ------------------------------------------------------------------
    // create
    // ------------------------------------------------------------------

    @Test
    void create_applicationNotFound_throwsBusinessRuleException() {
        when(applicationRepository.findById(10L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appDataServiceFacadeBean.create(input()));

        assertEquals(Constants.ERR_APP_NOT_FOUND, ex.getMessage());
        verify(appDataRepository, never()).create(any());
    }

    @Test
    void create_activeAnchorAlreadyExists_throwsBusinessRuleException() {
        when(applicationRepository.findById(10L)).thenReturn(application);
        when(appDataRepository.findByApplicationId(10L)).thenReturn(activeModel);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appDataServiceFacadeBean.create(input()));

        assertEquals(Constants.ERR_APP_DATA_ALREADY_EXISTS, ex.getMessage());
        verify(appDataRepository, never()).create(any());
    }

    @Test
    void create_previousAnchorSoftDeleted_doesNotBlockANewOne() {
        activeModel.setDeletedAt(LocalDateTime.now());
        AppDataInputDTO inputDTO = input();
        AppData toSave = new AppData();
        AppData saved = new AppData();
        when(applicationRepository.findById(10L)).thenReturn(application);
        when(appDataRepository.findByApplicationId(10L)).thenReturn(activeModel);
        when(appDataMapper.toModelFromInput(inputDTO)).thenReturn(toSave);
        when(appDataRepository.create(toSave)).thenReturn(saved);
        when(appDataMapper.toResponse(saved)).thenReturn(new AppDataOutputDTO());

        appDataServiceFacadeBean.create(inputDTO);

        verify(appDataRepository).create(toSave);
    }

    @Test
    void create_checksNothing_andLeavesTheTabsOutOfTheResponse() {
        AppDataInputDTO inputDTO = input();
        AppData toSave = new AppData();
        AppData saved = new AppData();
        AppDataOutputDTO mapped = new AppDataOutputDTO();
        when(applicationRepository.findById(10L)).thenReturn(application);
        when(appDataRepository.findByApplicationId(10L)).thenReturn(null);
        when(appDataMapper.toModelFromInput(inputDTO)).thenReturn(toSave);
        when(appDataRepository.create(toSave)).thenReturn(saved);
        when(appDataMapper.toResponse(saved)).thenReturn(mapped);

        AppDataOutputDTO result = appDataServiceFacadeBean.create(inputDTO);

        assertSame(mapped, result);
        assertNull(result.getOpenDataUrl());
        assertNull(result.getReuseUrl());
        verifyNoInteractions(dataApiClient);
    }

    // ------------------------------------------------------------------
    // update (observation only)
    // ------------------------------------------------------------------

    @Test
    void update_notFound_throwsBusinessRuleException() {
        when(appDataRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appDataServiceFacadeBean.update(99L, input()));

        assertEquals(Constants.ERR_APP_DATA_NOT_FOUND, ex.getMessage());
        verify(appDataRepository, never()).update(any(), any());
    }

    @Test
    void update_appliesTheInput_checksNothing_andLeavesTheTabsOutOfTheResponse() {
        activeModel.setOpenDataUrl(TYPED_OPEN_DATA_URL);
        AppDataInputDTO inputDTO = input();
        AppDataOutputDTO mapped = new AppDataOutputDTO();
        when(appDataRepository.findById(1L)).thenReturn(activeModel);
        when(appDataRepository.update(activeModel, 1L)).thenReturn(activeModel);
        when(appDataMapper.toResponse(activeModel)).thenReturn(mapped);

        AppDataOutputDTO result = appDataServiceFacadeBean.update(1L, inputDTO);

        verify(appDataMapper).updateModelFromInput(inputDTO, activeModel);
        assertEquals(TYPED_OPEN_DATA_URL, activeModel.getOpenDataUrl());
        assertNull(result.getOpenDataUrl());
        assertNull(result.getReuseUrl());
        verifyNoInteractions(dataApiClient);
    }

    // ------------------------------------------------------------------
    // updateOpenDataUrl
    // ------------------------------------------------------------------

    @Test
    void updateOpenDataUrl_notFound_throwsBusinessRuleException() {
        when(appDataRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appDataServiceFacadeBean.updateOpenDataUrl(99L, new AppDataUrlInputDTO(TYPED_OPEN_DATA_URL)));

        assertEquals(Constants.ERR_APP_DATA_NOT_FOUND, ex.getMessage());
        verify(appDataRepository, never()).update(any(), any());
    }

    @Test
    void updateOpenDataUrl_aTypedUrl_isCheckedSavedAndReturnedAsTheIntegrationReportedIt() {
        when(appDataRepository.findById(1L)).thenReturn(activeModel);
        when(dataApiClient.checkUrl(TYPED_OPEN_DATA_URL)).thenReturn(answer(TYPED_OPEN_DATA_URL, 200));

        AppDataUrlOutputDTO result = appDataServiceFacadeBean.updateOpenDataUrl(1L, new AppDataUrlInputDTO(TYPED_OPEN_DATA_URL));

        assertEquals(TYPED_OPEN_DATA_URL, result.getUrl());
        assertTrue(result.isValidUrl());
        assertEquals(TYPED_OPEN_DATA_URL, activeModel.getOpenDataUrl());
        verify(appDataRepository).update(activeModel, 1L);
        verify(dataApiClient, never()).checkUrl(DEFAULT_REUSE_URL);
    }

    @Test
    void updateOpenDataUrl_aTypedUrlThatDoesNotWork_isSavedAnywayAndReportedNotValid() {
        when(appDataRepository.findById(1L)).thenReturn(activeModel);
        when(dataApiClient.checkUrl("https://swagger.example.test/broken"))
                .thenReturn(answer("https://swagger.example.test/broken", 404));

        AppDataUrlOutputDTO result = appDataServiceFacadeBean.updateOpenDataUrl(1L, new AppDataUrlInputDTO("https://swagger.example.test/broken"));

        assertEquals("https://swagger.example.test/broken", result.getUrl());
        assertFalse(result.isValidUrl());
        assertEquals("https://swagger.example.test/broken", activeModel.getOpenDataUrl());
        verify(appDataRepository).update(activeModel, 1L);
    }

    @Test
    void updateOpenDataUrl_theSameUrlAsBefore_isCheckedAgainAnyway() {
        activeModel.setOpenDataUrl(TYPED_OPEN_DATA_URL);
        when(appDataRepository.findById(1L)).thenReturn(activeModel);
        when(dataApiClient.checkUrl(TYPED_OPEN_DATA_URL)).thenReturn(answer(TYPED_OPEN_DATA_URL, 200));

        AppDataUrlOutputDTO result = appDataServiceFacadeBean.updateOpenDataUrl(1L, new AppDataUrlInputDTO(TYPED_OPEN_DATA_URL));

        verify(dataApiClient).checkUrl(TYPED_OPEN_DATA_URL);
        assertTrue(result.isValidUrl());
    }

    @Test
    void updateOpenDataUrl_aNullUrl_goesBackToTheDefaultOneAndReturnsIt() {
        activeModel.setOpenDataUrl(TYPED_OPEN_DATA_URL);
        when(appDataRepository.findById(1L)).thenReturn(activeModel);
        when(dataApiClient.checkUrl(DEFAULT_OPEN_DATA_URL)).thenReturn(answer(DEFAULT_OPEN_DATA_URL, 200));

        AppDataUrlOutputDTO result = appDataServiceFacadeBean.updateOpenDataUrl(1L, new AppDataUrlInputDTO(null));

        assertEquals(DEFAULT_OPEN_DATA_URL, result.getUrl());
        assertNull(activeModel.getOpenDataUrl());
        assertTrue(result.isValidUrl());
    }

    @Test
    void updateOpenDataUrl_aBlankUrl_isTreatedAsNull() {
        activeModel.setOpenDataUrl(TYPED_OPEN_DATA_URL);
        when(appDataRepository.findById(1L)).thenReturn(activeModel);
        when(dataApiClient.checkUrl(DEFAULT_OPEN_DATA_URL)).thenReturn(answer(DEFAULT_OPEN_DATA_URL, 200));

        AppDataUrlOutputDTO result = appDataServiceFacadeBean.updateOpenDataUrl(1L, new AppDataUrlInputDTO("   "));

        assertEquals(DEFAULT_OPEN_DATA_URL, result.getUrl());
        assertNull(activeModel.getOpenDataUrl());
    }

    @Test
    void updateOpenDataUrl_aTypedUrlWithSurroundingBlanks_isTrimmedBeforeItIsStoredAndChecked() {
        when(appDataRepository.findById(1L)).thenReturn(activeModel);
        when(dataApiClient.checkUrl(TYPED_OPEN_DATA_URL)).thenReturn(answer(TYPED_OPEN_DATA_URL, 200));

        AppDataUrlOutputDTO result = appDataServiceFacadeBean.updateOpenDataUrl(1L, new AppDataUrlInputDTO("  " + TYPED_OPEN_DATA_URL + "  "));

        assertEquals(TYPED_OPEN_DATA_URL, result.getUrl());
        assertEquals(TYPED_OPEN_DATA_URL, activeModel.getOpenDataUrl());
    }

    @Test
    void updateOpenDataUrl_theCheckTimingOut_propagatesAndSavesNothing() {
        when(appDataRepository.findById(1L)).thenReturn(activeModel);
        when(dataApiClient.checkUrl(TYPED_OPEN_DATA_URL)).thenThrow(new DataTimeoutException());

        assertThrows(DataTimeoutException.class,
                () -> appDataServiceFacadeBean.updateOpenDataUrl(1L, new AppDataUrlInputDTO(TYPED_OPEN_DATA_URL)));

        verify(appDataRepository, never()).update(any(), any());
    }

    // ------------------------------------------------------------------
    // updateReuseUrl
    // ------------------------------------------------------------------

    @Test
    void updateReuseUrl_notFound_throwsBusinessRuleException() {
        when(appDataRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appDataServiceFacadeBean.updateReuseUrl(99L, new AppDataUrlInputDTO(TYPED_REUSE_URL)));

        assertEquals(Constants.ERR_APP_DATA_NOT_FOUND, ex.getMessage());
    }

    @Test
    void updateReuseUrl_aTypedUrl_isCheckedSavedAndReturned_withoutTouchingTheOpenDataTab() {
        activeModel.setOpenDataUrl(TYPED_OPEN_DATA_URL);
        when(appDataRepository.findById(1L)).thenReturn(activeModel);
        when(dataApiClient.checkUrl(TYPED_REUSE_URL)).thenReturn(answer(TYPED_REUSE_URL, 200));

        AppDataUrlOutputDTO result = appDataServiceFacadeBean.updateReuseUrl(1L, new AppDataUrlInputDTO(TYPED_REUSE_URL));

        assertEquals(TYPED_REUSE_URL, result.getUrl());
        assertTrue(result.isValidUrl());
        assertEquals(TYPED_REUSE_URL, activeModel.getReuseUrl());
        assertEquals(TYPED_OPEN_DATA_URL, activeModel.getOpenDataUrl());
        verify(dataApiClient, never()).checkUrl(DEFAULT_OPEN_DATA_URL);
    }

    @Test
    void updateReuseUrl_aTypedUrlThatDoesNotWork_isSavedAnywayAndReportedNotValid() {
        when(appDataRepository.findById(1L)).thenReturn(activeModel);
        when(dataApiClient.checkUrl(TYPED_REUSE_URL)).thenReturn(answer(TYPED_REUSE_URL, 500));

        AppDataUrlOutputDTO result = appDataServiceFacadeBean.updateReuseUrl(1L, new AppDataUrlInputDTO(TYPED_REUSE_URL));

        assertFalse(result.isValidUrl());
        assertEquals(TYPED_REUSE_URL, activeModel.getReuseUrl());
    }

    @Test
    void updateReuseUrl_aNullUrl_goesBackToTheDefaultOneAndReturnsIt() {
        activeModel.setReuseUrl(TYPED_REUSE_URL);
        when(appDataRepository.findById(1L)).thenReturn(activeModel);
        when(dataApiClient.checkUrl(DEFAULT_REUSE_URL)).thenReturn(answer(DEFAULT_REUSE_URL, 200));

        AppDataUrlOutputDTO result = appDataServiceFacadeBean.updateReuseUrl(1L, new AppDataUrlInputDTO(null));

        assertEquals(DEFAULT_REUSE_URL, result.getUrl());
        assertNull(activeModel.getReuseUrl());
    }

    @Test
    void updateReuseUrl_theSameUrlAsBefore_isCheckedAgainAnyway() {
        activeModel.setReuseUrl(TYPED_REUSE_URL);
        when(appDataRepository.findById(1L)).thenReturn(activeModel);
        when(dataApiClient.checkUrl(TYPED_REUSE_URL)).thenReturn(answer(TYPED_REUSE_URL, 200));

        appDataServiceFacadeBean.updateReuseUrl(1L, new AppDataUrlInputDTO(TYPED_REUSE_URL));

        verify(dataApiClient).checkUrl(TYPED_REUSE_URL);
    }

    // ------------------------------------------------------------------
    // delete
    // ------------------------------------------------------------------

    @Test
    void delete_valid_softDeletes() {
        when(appDataRepository.findById(1L)).thenReturn(activeModel);

        appDataServiceFacadeBean.delete(1L);

        assertNotNull(activeModel.getDeletedAt());
        assertNotNull(activeModel.getDeletedBy());
        verify(appDataRepository).delete(activeModel);
    }

    @Test
    void delete_notFound_throwsBusinessRuleException() {
        when(appDataRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appDataServiceFacadeBean.delete(99L));

        assertEquals(Constants.ERR_APP_DATA_NOT_FOUND, ex.getMessage());
    }

    @Test
    void delete_alreadyDeleted_throwsBusinessRuleException() {
        activeModel.setDeletedAt(LocalDateTime.now());
        when(appDataRepository.findById(1L)).thenReturn(activeModel);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> appDataServiceFacadeBean.delete(1L));

        assertEquals(Constants.ERR_APP_DATA_NOT_ACTIVE, ex.getMessage());
        verify(appDataRepository, never()).delete(any());
    }
}

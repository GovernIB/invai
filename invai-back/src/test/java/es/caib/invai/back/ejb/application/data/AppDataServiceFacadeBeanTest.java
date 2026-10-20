package es.caib.invai.back.ejb.application.data;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.exception.DataTimeoutException;
import es.caib.invai.back.interna.application.data.DTO.AppDataInputDTO;
import es.caib.invai.back.interna.application.data.DTO.AppDataOutputDTO;
import es.caib.invai.back.interna.application.data.DTO.OpenDataEndpointOutputDTO;
import es.caib.invai.back.interna.application.data.DTO.OpenDataOperationOutputDTO;
import es.caib.invai.back.persistence.repository.application.data.AppDataRepository;
import es.caib.invai.back.rest.data.DataApiClient;
import es.caib.invai.back.rest.data.OpenApiDocumentResult;
import es.caib.invai.back.rest.openapi.OpenApiDocument;
import es.caib.invai.back.rest.openapi.OpenApiOperation;
import es.caib.invai.back.rest.openapi.OpenApiPathItem;
import es.caib.invai.back.service.mapper.application.data.AppDataMapper;
import es.caib.invai.back.service.mapper.application.data.OpenDataOperationMapper;
import es.caib.invai.back.service.model.application.core.Application;
import es.caib.invai.back.service.model.application.data.AppData;
import es.caib.invai.back.utils.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.Map;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AppDataServiceFacadeBean}, exercising every branch of its CRUD
 * business rules plus the GET-only filtering of the live OpenAPI document resolution.
 */
@ExtendWith(MockitoExtension.class)
class AppDataServiceFacadeBeanTest {

    @Mock
    private AppDataMapper appDataMapper;

    @Mock
    private AppDataRepository appDataRepository;

    @Mock
    private DataApiClient dataApiClient;

    @Mock
    private OpenDataOperationMapper openDataOperationMapper;

    @Mock
    private AppDataUrlCacheService appDataUrlCacheService;

    @InjectMocks
    private AppDataServiceFacadeBean appDataServiceFacadeBean;

    private AppData activeModel;

    /**
     * Every {@code getById} test resolves both tabs; tests focused only on the "Open Data" side
     * don't care about "Reutilització", so this default keeps the reuse fetch harmless (empty
     * document) unless a test overrides it. {@code lenient()} since not every test reaches
     * {@code getById} at all.
     */
    @BeforeEach
    void setUp() {
        Application application = new Application();
        application.setId(10L);
        application.setCode("sedeib");

        activeModel = new AppData();
        activeModel.setId(1L);
        activeModel.setApplication(application);

        lenient().when(dataApiClient.fetchOpenApiDocument(any(), anyBoolean(), any()))
                .thenReturn(new OpenApiDocumentResult(new OpenApiDocument(), null));
        lenient().when(dataApiClient.fetchReuseDocument(any(), anyBoolean(), any()))
                .thenReturn(new OpenApiDocumentResult(new OpenApiDocument(), null));
    }

    private static OpenApiPathItem pathItemWithGet(String operationId) {
        OpenApiOperation operation = new OpenApiOperation();
        operation.setOperationId(operationId);
        OpenApiPathItem pathItem = new OpenApiPathItem();
        pathItem.setGet(operation);
        return pathItem;
    }

    private static OpenDataOperationOutputDTO operationOutputFor(String operationId) {
        return OpenDataOperationOutputDTO.builder().operationId(operationId).build();
    }

    @Test
    void getById_notFound_returnsNull() {
        when(appDataRepository.findById(99L)).thenReturn(null);

        AppDataOutputDTO result = appDataServiceFacadeBean.getById(99L);

        assertNull(result);
        verify(dataApiClient, never()).fetchOpenApiDocument(any(), anyBoolean(), any());
        verify(dataApiClient, never()).fetchReuseDocument(any(), anyBoolean(), any());
    }

    @Test
    void getById_found_resolvesOnlyGetEndpointsFromLiveDocument() {
        AppDataOutputDTO mapped = new AppDataOutputDTO();
        when(appDataRepository.findById(1L)).thenReturn(activeModel);
        when(appDataMapper.toResponse(activeModel)).thenReturn(mapped);

        OpenApiPathItem centrePathItem = pathItemWithGet("centrePerEtapa");
        OpenApiPathItem alumnePathItem = pathItemWithGet("groupByPerIlla");
        OpenApiPathItem noVerbsPathItem = new OpenApiPathItem();

        Map<String, OpenApiPathItem> paths = new LinkedHashMap<>();
        paths.put("/services/centre", centrePathItem);
        paths.put("/services/alumne-matricula", alumnePathItem);
        paths.put("/services/no-verbs", noVerbsPathItem);
        OpenApiDocument document = new OpenApiDocument();
        document.setPaths(paths);

        when(dataApiClient.fetchOpenApiDocument("sedeib", false, null))
                .thenReturn(new OpenApiDocumentResult(document, "https://intranet.caib.es/sedeibapi/externa/openapi.json"));
        when(openDataOperationMapper.toResponse(centrePathItem.getGet())).thenReturn(operationOutputFor("centrePerEtapa"));
        when(openDataOperationMapper.toResponse(alumnePathItem.getGet())).thenReturn(operationOutputFor("groupByPerIlla"));

        AppDataOutputDTO result = appDataServiceFacadeBean.getById(1L);

        assertEquals(mapped, result);
        List<OpenDataEndpointOutputDTO> endpoints = result.getOpenData();
        assertEquals(2, endpoints.size());
        OpenDataEndpointOutputDTO centre = endpoints.stream()
                .filter(e -> e.getPath().equals("/services/centre")).findFirst().orElseThrow();
        assertEquals("GET", centre.getMethod());
        assertEquals("centrePerEtapa", centre.getOperation().getOperationId());
        assertEquals("groupByPerIlla", endpoints.stream()
                .filter(e -> e.getPath().equals("/services/alumne-matricula")).findFirst().orElseThrow()
                .getOperation().getOperationId());
        verify(appDataUrlCacheService).cacheOpenDataUrl(activeModel, "https://intranet.caib.es/sedeibapi/externa/openapi.json");
    }

    @Test
    void getById_applicationCodeStoredInMixedCase_lowercasesItBeforeFetching() {
        activeModel.getApplication().setCode("SeDeIb");
        AppDataOutputDTO mapped = new AppDataOutputDTO();
        when(appDataRepository.findById(1L)).thenReturn(activeModel);
        when(appDataMapper.toResponse(activeModel)).thenReturn(mapped);

        appDataServiceFacadeBean.getById(1L);

        verify(dataApiClient).fetchOpenApiDocument("sedeib", false, null);
        verify(dataApiClient).fetchReuseDocument("sedeib", false, null);
    }

    @Test
    void getById_applicationCodeWithUnderscoresAndDigits_onlyLowercasesWithoutRemovingAnyCharacter() {
        activeModel.getApplication().setCode("SEDE_IB-2");
        AppDataOutputDTO mapped = new AppDataOutputDTO();
        when(appDataRepository.findById(1L)).thenReturn(activeModel);
        when(appDataMapper.toResponse(activeModel)).thenReturn(mapped);

        appDataServiceFacadeBean.getById(1L);

        verify(dataApiClient).fetchOpenApiDocument("sede_ib-2", false, null);
        verify(dataApiClient).fetchReuseDocument("sede_ib-2", false, null);
    }

    @Test
    void getById_documentWithNoPaths_returnsEmptyOpenDataList() {
        AppDataOutputDTO mapped = new AppDataOutputDTO();
        OpenApiDocument document = new OpenApiDocument();
        when(appDataRepository.findById(1L)).thenReturn(activeModel);
        when(appDataMapper.toResponse(activeModel)).thenReturn(mapped);
        when(dataApiClient.fetchOpenApiDocument("sedeib", false, null))
                .thenReturn(new OpenApiDocumentResult(document, "https://intranet.caib.es/sedeibapi/externa/openapi.json"));

        AppDataOutputDTO result = appDataServiceFacadeBean.getById(1L);

        assertEquals(List.of(), result.getOpenData());
    }

    @Test
    void getById_useOpenDataUrlTrue_fetchesFromExplicitUrlInsteadOfApplicationName() {
        activeModel.setUseOpenDataUrl(true);
        activeModel.setOpenDataUrl("https://example.test/custom/openapi.json");
        AppDataOutputDTO mapped = new AppDataOutputDTO();

        OpenApiPathItem customPathItem = pathItemWithGet("customGet");
        OpenApiDocument document = new OpenApiDocument();
        document.setPaths(Map.of("/custom/endpoint", customPathItem));

        when(appDataRepository.findById(1L)).thenReturn(activeModel);
        when(appDataMapper.toResponse(activeModel)).thenReturn(mapped);
        when(dataApiClient.fetchOpenApiDocument("sedeib", true, "https://example.test/custom/openapi.json"))
                .thenReturn(new OpenApiDocumentResult(document, "https://example.test/custom/openapi.json"));
        when(openDataOperationMapper.toResponse(customPathItem.getGet())).thenReturn(operationOutputFor("customGet"));

        AppDataOutputDTO result = appDataServiceFacadeBean.getById(1L);

        assertEquals(1, result.getOpenData().size());
        assertEquals("customGet", result.getOpenData().get(0).getOperation().getOperationId());
        verify(dataApiClient).fetchOpenApiDocument("sedeib", true, "https://example.test/custom/openapi.json");
    }

    @Test
    void getById_openDataFetchFails_returnsNullOpenDataButValidReuse() {
        when(appDataRepository.findById(1L)).thenReturn(activeModel);
        when(appDataMapper.toResponse(activeModel)).thenReturn(new AppDataOutputDTO());
        when(dataApiClient.fetchOpenApiDocument("sedeib", false, null))
                .thenThrow(new BusinessRuleException(Constants.ERR_OPENDATA_UNAVAILABLE));

        AppDataOutputDTO result = appDataServiceFacadeBean.getById(1L);

        assertNull(result.getOpenData());
        assertEquals(List.of(), result.getReuse());
    }

    @Test
    void getById_bothFetchesFail_throwsBusinessRuleException() {
        when(appDataRepository.findById(1L)).thenReturn(activeModel);
        when(appDataMapper.toResponse(activeModel)).thenReturn(new AppDataOutputDTO());
        when(dataApiClient.fetchOpenApiDocument("sedeib", false, null))
                .thenThrow(new BusinessRuleException(Constants.ERR_OPENDATA_UNAVAILABLE));
        when(dataApiClient.fetchReuseDocument("sedeib", false, null))
                .thenThrow(new BusinessRuleException(Constants.ERR_REUSE_UNAVAILABLE));

        assertThrows(BusinessRuleException.class, () -> appDataServiceFacadeBean.getById(1L));
    }

    @Test
    void getById_bothFetchesFail_onlyOpenDataTimesOut_throwsDataTimeoutException() {
        when(appDataRepository.findById(1L)).thenReturn(activeModel);
        when(appDataMapper.toResponse(activeModel)).thenReturn(new AppDataOutputDTO());
        when(dataApiClient.fetchOpenApiDocument("sedeib", false, null))
                .thenThrow(new DataTimeoutException());
        when(dataApiClient.fetchReuseDocument("sedeib", false, null))
                .thenThrow(new BusinessRuleException(Constants.ERR_REUSE_UNAVAILABLE));

        assertThrows(DataTimeoutException.class, () -> appDataServiceFacadeBean.getById(1L));
    }

    @Test
    void getById_found_resolvesOnlyGetEndpointsFromLiveReuseDocument() {
        AppDataOutputDTO mapped = new AppDataOutputDTO();
        when(appDataRepository.findById(1L)).thenReturn(activeModel);
        when(appDataMapper.toResponse(activeModel)).thenReturn(mapped);

        OpenApiPathItem centrePathItem = pathItemWithGet("centrePerEtapa");
        OpenApiPathItem noVerbsPathItem = new OpenApiPathItem();

        Map<String, OpenApiPathItem> paths = new LinkedHashMap<>();
        paths.put("/services/centre", centrePathItem);
        paths.put("/services/no-verbs", noVerbsPathItem);
        OpenApiDocument document = new OpenApiDocument();
        document.setPaths(paths);

        when(dataApiClient.fetchReuseDocument("sedeib", false, null))
                .thenReturn(new OpenApiDocumentResult(document, "https://intranet.caib.es/sedeibapi/externa/swagger.json"));
        when(openDataOperationMapper.toResponse(centrePathItem.getGet())).thenReturn(operationOutputFor("centrePerEtapa"));

        AppDataOutputDTO result = appDataServiceFacadeBean.getById(1L);

        assertEquals(mapped, result);
        List<OpenDataEndpointOutputDTO> endpoints = result.getReuse();
        assertEquals(1, endpoints.size());
        assertEquals("centrePerEtapa", endpoints.get(0).getOperation().getOperationId());
        verify(appDataUrlCacheService).cacheReuseUrl(activeModel, "https://intranet.caib.es/sedeibapi/externa/swagger.json");
    }

    @Test
    void getById_reuseDocumentWithNoPaths_returnsEmptyReuseList() {
        AppDataOutputDTO mapped = new AppDataOutputDTO();
        when(appDataRepository.findById(1L)).thenReturn(activeModel);
        when(appDataMapper.toResponse(activeModel)).thenReturn(mapped);

        AppDataOutputDTO result = appDataServiceFacadeBean.getById(1L);

        assertEquals(List.of(), result.getReuse());
    }

    @Test
    void getById_useReuseUrlTrue_fetchesFromExplicitUrlInsteadOfApplicationName() {
        activeModel.setUseReuseUrl(true);
        activeModel.setReuseUrl("https://example.test/custom/back");
        AppDataOutputDTO mapped = new AppDataOutputDTO();

        OpenApiPathItem customPathItem = pathItemWithGet("customGet");
        OpenApiDocument document = new OpenApiDocument();
        document.setPaths(Map.of("/custom/endpoint", customPathItem));

        when(appDataRepository.findById(1L)).thenReturn(activeModel);
        when(appDataMapper.toResponse(activeModel)).thenReturn(mapped);
        when(dataApiClient.fetchReuseDocument("sedeib", true, "https://example.test/custom/back"))
                .thenReturn(new OpenApiDocumentResult(document, "https://example.test/custom/back"));
        when(openDataOperationMapper.toResponse(customPathItem.getGet())).thenReturn(operationOutputFor("customGet"));

        AppDataOutputDTO result = appDataServiceFacadeBean.getById(1L);

        assertEquals(1, result.getReuse().size());
        assertEquals("customGet", result.getReuse().get(0).getOperation().getOperationId());
        verify(dataApiClient).fetchReuseDocument("sedeib", true, "https://example.test/custom/back");
    }

    @Test
    void getById_reuseFetchFails_returnsNullReuseButValidOpenData() {
        when(appDataRepository.findById(1L)).thenReturn(activeModel);
        when(appDataMapper.toResponse(activeModel)).thenReturn(new AppDataOutputDTO());
        when(dataApiClient.fetchReuseDocument("sedeib", false, null))
                .thenThrow(new BusinessRuleException(Constants.ERR_REUSE_UNAVAILABLE));

        AppDataOutputDTO result = appDataServiceFacadeBean.getById(1L);

        assertNull(result.getReuse());
        assertEquals(List.of(), result.getOpenData());
    }

    /**
     * Regression test for the "everything ends up cached as reuse" bug: {@code
     * resolveOpenDataEndpoints}/{@code resolveReuseEndpoints} used to run concurrently and both
     * close over the very same shared {@code AppData} instance, so two concurrent full-entity
     * saves could race over that shared instance and clobber one column with the other's stale
     * snapshot. Both tabs are now resolved sequentially, each caching its own URL right after its
     * own fetch, so this proves each tab's resolved URL lands in its own cache call rather than
     * both ending up cached under the same one.
     */
    @Test
    void getById_resolvesBothTabs_cachesEachUrlUnderItsOwnTab() {
        when(appDataRepository.findById(1L)).thenReturn(activeModel);
        when(appDataMapper.toResponse(activeModel)).thenReturn(new AppDataOutputDTO());
        when(dataApiClient.fetchOpenApiDocument("sedeib", false, null))
                .thenReturn(new OpenApiDocumentResult(new OpenApiDocument(), "https://intranet.caib.es/sedeibapi/externa/openapi.json"));
        when(dataApiClient.fetchReuseDocument("sedeib", false, null))
                .thenReturn(new OpenApiDocumentResult(new OpenApiDocument(), "https://intranet.caib.es/sedeibapi/externa/swagger.json"));

        appDataServiceFacadeBean.getById(1L);

        verify(appDataUrlCacheService).cacheOpenDataUrl(activeModel, "https://intranet.caib.es/sedeibapi/externa/openapi.json");
        verify(appDataUrlCacheService).cacheReuseUrl(activeModel, "https://intranet.caib.es/sedeibapi/externa/swagger.json");
    }

    @Test
    void create_noActiveAnchorForApplication_persistsAndReturnsResponse() {
        AppDataInputDTO inputDTO = new AppDataInputDTO(10L, "Observacio", null, false, null, false);
        AppData model = new AppData();
        AppData saved = new AppData();
        AppDataOutputDTO response = new AppDataOutputDTO();
        when(appDataRepository.findByApplicationId(10L)).thenReturn(null);
        when(appDataMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(appDataRepository.create(model)).thenReturn(saved);
        when(appDataMapper.toResponse(saved)).thenReturn(response);

        AppDataOutputDTO result = appDataServiceFacadeBean.create(inputDTO);

        assertEquals(response, result);
    }

    @Test
    void create_applicationAlreadyHasActiveAnchor_throwsBusinessRuleException() {
        AppDataInputDTO inputDTO = new AppDataInputDTO(10L, null, null, false, null, false);
        AppData existing = new AppData();
        existing.setDeletedAt(null);
        when(appDataRepository.findByApplicationId(10L)).thenReturn(existing);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appDataServiceFacadeBean.create(inputDTO));

        assertEquals(Constants.ERR_APP_DATA_ALREADY_EXISTS, ex.getMessage());
        verify(appDataRepository, never()).create(any());
    }

    @Test
    void create_applicationHasOnlySoftDeletedAnchor_allowsCreate() {
        AppDataInputDTO inputDTO = new AppDataInputDTO(10L, null, null, false, null, false);
        AppData softDeleted = new AppData();
        softDeleted.setDeletedAt(LocalDateTime.now());
        when(appDataRepository.findByApplicationId(10L)).thenReturn(softDeleted);
        when(appDataMapper.toModelFromInput(inputDTO)).thenReturn(new AppData());
        when(appDataRepository.create(any())).thenReturn(new AppData());
        when(appDataMapper.toResponse(any())).thenReturn(new AppDataOutputDTO());

        appDataServiceFacadeBean.create(inputDTO);

        verify(appDataRepository).create(any());
    }

    @Test
    void create_useOpenDataUrlTrueWithBlankUrl_throwsBusinessRuleException() {
        AppDataInputDTO inputDTO = new AppDataInputDTO(10L, null, "  ", true, null, false);
        when(appDataRepository.findByApplicationId(10L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appDataServiceFacadeBean.create(inputDTO));

        assertEquals(Constants.ERR_APP_DATA_URL_REQUIRED, ex.getMessage());
        verify(appDataRepository, never()).create(any());
    }

    @Test
    void create_useOpenDataUrlTrueWithUrl_persistsAndReturnsResponse() {
        AppDataInputDTO inputDTO = new AppDataInputDTO(10L, null, "https://example.test/openapi.json", true, null, false);
        when(appDataRepository.findByApplicationId(10L)).thenReturn(null);
        when(appDataMapper.toModelFromInput(inputDTO)).thenReturn(new AppData());
        when(appDataRepository.create(any())).thenReturn(new AppData());
        when(appDataMapper.toResponse(any())).thenReturn(new AppDataOutputDTO());

        appDataServiceFacadeBean.create(inputDTO);

        verify(appDataRepository).create(any());
    }

    @Test
    void create_useReuseUrlTrueWithBlankUrl_throwsBusinessRuleException() {
        AppDataInputDTO inputDTO = new AppDataInputDTO(10L, null, null, false, "  ", true);
        when(appDataRepository.findByApplicationId(10L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appDataServiceFacadeBean.create(inputDTO));

        assertEquals(Constants.ERR_APP_DATA_URL_REQUIRED, ex.getMessage());
        verify(appDataRepository, never()).create(any());
    }

    @Test
    void create_useReuseUrlTrueWithUrl_persistsAndReturnsResponse() {
        AppDataInputDTO inputDTO = new AppDataInputDTO(10L, null, null, false, "https://example.test/back", true);
        when(appDataRepository.findByApplicationId(10L)).thenReturn(null);
        when(appDataMapper.toModelFromInput(inputDTO)).thenReturn(new AppData());
        when(appDataRepository.create(any())).thenReturn(new AppData());
        when(appDataMapper.toResponse(any())).thenReturn(new AppDataOutputDTO());

        appDataServiceFacadeBean.create(inputDTO);

        verify(appDataRepository).create(any());
    }

    @Test
    void update_notFound_throwsBusinessRuleException() {
        when(appDataRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appDataServiceFacadeBean.update(99L, new AppDataInputDTO(10L, null, null, false, null, false)));

        assertEquals(Constants.ERR_APP_DATA_NOT_FOUND, ex.getMessage());
    }

    @Test
    void update_found_updatesAndReturnsResponse() {
        AppDataInputDTO inputDTO = new AppDataInputDTO(10L, "Nova observacio", null, false, null, false);
        AppData updated = new AppData();
        AppDataOutputDTO response = new AppDataOutputDTO();
        when(appDataRepository.findById(1L)).thenReturn(activeModel);
        when(appDataRepository.update(activeModel, 1L)).thenReturn(updated);
        when(appDataMapper.toResponse(updated)).thenReturn(response);

        AppDataOutputDTO result = appDataServiceFacadeBean.update(1L, inputDTO);

        assertEquals(response, result);
        verify(appDataMapper).updateModelFromInput(inputDTO, activeModel);
    }

    @Test
    void update_useOpenDataUrlTrueWithBlankUrl_throwsBusinessRuleException() {
        AppDataInputDTO inputDTO = new AppDataInputDTO(10L, null, null, true, null, false);
        when(appDataRepository.findById(1L)).thenReturn(activeModel);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appDataServiceFacadeBean.update(1L, inputDTO));

        assertEquals(Constants.ERR_APP_DATA_URL_REQUIRED, ex.getMessage());
        verify(appDataMapper, never()).updateModelFromInput(any(), any());
    }

    @Test
    void update_useReuseUrlTrueWithBlankUrl_throwsBusinessRuleException() {
        AppDataInputDTO inputDTO = new AppDataInputDTO(10L, null, null, false, null, true);
        when(appDataRepository.findById(1L)).thenReturn(activeModel);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appDataServiceFacadeBean.update(1L, inputDTO));

        assertEquals(Constants.ERR_APP_DATA_URL_REQUIRED, ex.getMessage());
        verify(appDataMapper, never()).updateModelFromInput(any(), any());
    }

    @Test
    void delete_notFound_throwsBusinessRuleException() {
        when(appDataRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appDataServiceFacadeBean.delete(99L));

        assertEquals(Constants.ERR_APP_DATA_NOT_FOUND, ex.getMessage());
    }

    @Test
    void delete_alreadyInactive_throwsBusinessRuleException() {
        activeModel.setDeletedAt(LocalDateTime.now());
        when(appDataRepository.findById(1L)).thenReturn(activeModel);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> appDataServiceFacadeBean.delete(1L));

        assertEquals(Constants.ERR_APP_DATA_NOT_ACTIVE, ex.getMessage());
        verify(appDataRepository, never()).delete(any());
    }

    @Test
    void delete_active_softDeletes() {
        when(appDataRepository.findById(1L)).thenReturn(activeModel);

        appDataServiceFacadeBean.delete(1L);

        assertNotNull(activeModel.getDeletedAt());
        verify(appDataRepository).delete(activeModel);
    }
}

package es.caib.invai.back.ejb.application.data;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.exception.DataTimeoutException;
import es.caib.invai.back.interna.application.data.DTO.AppDataInputDTO;
import es.caib.invai.back.interna.application.data.DTO.AppDataOutputDTO;
import es.caib.invai.back.interna.application.data.DTO.OpenDataEndpointOutputDTO;
import es.caib.invai.back.persistence.repository.application.data.AppDataRepository;
import es.caib.invai.back.rest.data.DataApiClient;
import es.caib.invai.back.rest.data.OpenApiDocumentResult;
import es.caib.invai.back.rest.openapi.OpenApiDocument;
import es.caib.invai.back.rest.openapi.OpenApiOperation;
import es.caib.invai.back.rest.openapi.OpenApiPathItem;
import es.caib.invai.back.service.facade.application.data.AppDataService;
import es.caib.invai.back.service.mapper.application.data.AppDataMapper;
import es.caib.invai.back.service.mapper.application.data.OpenDataOperationMapper;
import es.caib.invai.back.service.model.application.data.AppData;
import es.caib.invai.back.utils.Constants;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Facade service implementing CRUD for the "data" anchor, plus the live resolution of the owning
 * application's published GET endpoints for both its "Open Data" tab (see {@link
 * #resolveOpenDataEndpoints}) and its "Reutilització" tab (see {@link #resolveReuseEndpoints}),
 * both surfaced by {@link #getById} - resolved independently of one another, so a failure on one
 * tab still returns the other tab's valid result; only a failure on both raises an exception. Like its
 * AppDevelopment/AppSecurity/AppAccessibility/AppInformationSystemDb/AppResponsibleAuthorized
 * counterparts, this anchor is auto-created when the owning {@code Application} is created (see
 * {@code ApplicationServiceFacadeBean}); {@link #create} exists mainly to backfill applications
 * created before this anchor existed, and rejects the request if the target application already
 * has an active anchor.
 *
 * @since 1.0.5
 */
@Service
@Slf4j
@Transactional
public class AppDataServiceFacadeBean implements AppDataService {

    /** Mapper converting between AppData domain models, entities, and DTOs. */
    @Autowired
    private AppDataMapper appDataMapper;

    /** Repository handling persistence of the "data" anchor linked to an application. */
    @Autowired
    private AppDataRepository appDataRepository;

    /** Client used to fetch a live document for the owning application's "Open Data"/"Reutilització" tabs. */
    @Autowired
    private DataApiClient dataApiClient;

    /** Caches whichever URL the live fetch's candidate race resolved, for the next call. */
    @Autowired
    private AppDataUrlCacheService appDataUrlCacheService;

    /** Maps the raw {@code rest.openapi} wire model into the outbound "data" tab DTOs. */
    @Autowired
    private OpenDataOperationMapper openDataOperationMapper;

    /**
     * Fetches the data anchor by its own primary key. Unlike {@link #update} and {@link
     * #delete}, this does not throw when no such anchor exists: the MapStruct mapper simply
     * returns {@code null} for a {@code null} input model, so the caller gets back a {@code null}
     * DTO rather than a "not found" error. When an anchor is found, its owning application's
     * currently published GET endpoints are additionally resolved live for both tabs (see {@link
     * #resolveOpenDataEndpoints} and {@link #resolveReuseEndpoints}) and attached under {@code
     * openData}/{@code reuse} respectively - both fetches are launched concurrently (see {@link
     * Utils#join}) rather than one after the other, so the wall-clock cost of the pair is the
     * slower of the two instead of their sum, and are resolved independently: if only one fails
     * (timeout, unreachable, malformed document), the other tab's valid result is still returned
     * and the failed one comes back {@code null}; an error is only raised when both fail.
     *
     * @param id primary key of the data anchor
     * @return the matching anchor, or {@code null} if no anchor exists with this ID
     * @throws BusinessRuleException if the anchor exists, but neither live document can be
     * fetched nor parsed
     * @throws DataTimeoutException if neither live document can be fetched, and at least one of
     * the two failures was itself a timeout
     */
    @Override
    @Transactional(readOnly = true)
    public AppDataOutputDTO getById(Long id) {
        log.debug("Facade: Fetching application data anchor for ID: {}", id);
        AppData domain = appDataRepository.findById(id);
        if (domain == null) {
            return null;
        }
        AppDataOutputDTO response = appDataMapper.toResponse(domain);

        SecurityContext securityContext = SecurityContextHolder.getContext();
        CompletableFuture<OpenApiDocumentResult> openDataFuture = CompletableFuture.supplyAsync(() -> {
            SecurityContextHolder.setContext(securityContext);
            try {
                return resolveOpenDataEndpoints(domain);
            } finally {
                SecurityContextHolder.clearContext();
            }
        });
        CompletableFuture<OpenApiDocumentResult> reuseFuture = CompletableFuture.supplyAsync(() -> {
            SecurityContextHolder.setContext(securityContext);
            try {
                return resolveReuseEndpoints(domain);
            } finally {
                SecurityContextHolder.clearContext();
            }
        });

        RuntimeException openDataError = null;
        OpenApiDocumentResult openDataResult = null;
        try {
            openDataResult = Utils.join(openDataFuture);
            response.setOpenData(extractGetEndpoints(openDataResult.getDocument()));
        } catch (BusinessRuleException | DataTimeoutException e) {
            log.warn("Facade: Could not resolve Open Data endpoints for data anchor ID: {}", id, e);
            openDataError = e;
        }

        RuntimeException reuseError = null;
        OpenApiDocumentResult reuseResult = null;
        try {
            reuseResult = Utils.join(reuseFuture);
            response.setReuse(extractGetEndpoints(reuseResult.getDocument()));
        } catch (BusinessRuleException | DataTimeoutException e) {
            log.warn("Facade: Could not resolve Reutilitzacio endpoints for data anchor ID: {}", id, e);
            reuseError = e;
        }

        if (openDataResult != null) {
            appDataUrlCacheService.cacheOpenDataUrl(domain, openDataResult.getResolvedUrl());
        }
        if (reuseResult != null) {
            appDataUrlCacheService.cacheReuseUrl(domain, reuseResult.getResolvedUrl());
        }

        if (openDataError != null && reuseError != null) {
            if (openDataError instanceof DataTimeoutException) {
                throw openDataError;
            }
            throw reuseError instanceof DataTimeoutException ? reuseError : openDataError;
        }
        return response;
    }

    /**
     * Creates a new data anchor for the application referenced by {@code
     * inputDTO.getApplicationId()}. Rejects the request if that application already has an active
     * (non-soft-deleted) anchor; a previously soft-deleted anchor for the same application does
     * not block creating a new one.
     *
     * @param inputDTO creation payload; {@code applicationId} identifies the owning application
     * @return the newly created anchor, mapped to its outbound representation
     * @throws BusinessRuleException if the target application already has an active data anchor
     */
    @Override
    public AppDataOutputDTO create(AppDataInputDTO inputDTO) {
        log.info("Facade: Persisting new application data anchor for application ID: {}", inputDTO.getApplicationId());

        AppData existing = appDataRepository.findByApplicationId(inputDTO.getApplicationId());
        if (existing != null && existing.getDeletedAt() == null) {
            throw new BusinessRuleException(Constants.ERR_APP_DATA_ALREADY_EXISTS);
        }

        Utils.sanitize(inputDTO);
        requireOpenDataUrlIfInUse(inputDTO);
        requireReuseUrlIfInUse(inputDTO);

        AppData domainModel = appDataMapper.toModelFromInput(inputDTO);
        AppData savedModel = appDataRepository.create(domainModel);
        return appDataMapper.toResponse(savedModel);
    }

    /**
     * Applies the given payload onto the data anchor identified by {@code id}. Does not
     * check whether the anchor is currently soft-deleted — unlike {@link #delete}, a
     * previously-deleted anchor can still be updated through this method.
     *
     * @param id       primary key of the data anchor to update
     * @param inputDTO payload with the new field values
     * @return the updated anchor, mapped to its outbound representation
     * @throws BusinessRuleException if no anchor exists with the given ID
     */
    @Override
    public AppDataOutputDTO update(Long id, AppDataInputDTO inputDTO) {
        log.info("Facade: Updating application data anchor ID: {}", id);

        AppData existingModel = appDataRepository.findById(id);
        if (existingModel == null) {
            throw new BusinessRuleException(Constants.ERR_APP_DATA_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);
        requireOpenDataUrlIfInUse(inputDTO);
        requireReuseUrlIfInUse(inputDTO);

        appDataMapper.updateModelFromInput(inputDTO, existingModel);
        return appDataMapper.toResponse(appDataRepository.update(existingModel, id));
    }

    /**
     * Soft-deletes the data anchor identified by {@code id}, stamping {@code deletedAt}/
     * {@code deletedBy} rather than removing the row.
     *
     * @param id primary key of the data anchor to delete
     * @throws BusinessRuleException if no anchor exists with the given ID, or it is already soft-deleted
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Logically deleting application data anchor ID: {}", id);

        AppData existingModel = appDataRepository.findById(id);
        if (existingModel == null) {
            throw new BusinessRuleException(Constants.ERR_APP_DATA_NOT_FOUND);
        }

        if (existingModel.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_APP_DATA_NOT_ACTIVE);
        }

        existingModel.setDeletedAt(LocalDateTime.now());
        existingModel.setDeletedBy(Utils.resolveCurrentUsername());

        appDataRepository.delete(existingModel);
    }

    /**
     * Requires {@code openDataUrl} to be non-blank when {@code useOpenDataUrl} is submitted as
     * {@code true} - there would otherwise be nothing to query once the application-name-derived
     * URL is overridden.
     *
     * @param inputDTO the (already sanitized) payload to check
     * @throws BusinessRuleException if {@code useOpenDataUrl} is {@code true} and {@code
     * openDataUrl} is blank
     */
    private void requireOpenDataUrlIfInUse(AppDataInputDTO inputDTO) {
        if (inputDTO.isUseOpenDataUrl() && StringUtils.isBlank(inputDTO.getOpenDataUrl())) {
            throw new BusinessRuleException(Constants.ERR_APP_DATA_URL_REQUIRED);
        }
    }

    /**
     * Requires {@code reuseUrl} to be non-blank when {@code useReuseUrl} is submitted as
     * {@code true} - there would otherwise be nothing to query once the application-name-derived
     * URL is overridden.
     *
     * @param inputDTO the (already sanitized) payload to check
     * @throws BusinessRuleException if {@code useReuseUrl} is {@code true} and {@code
     * reuseUrl} is blank
     */
    private void requireReuseUrlIfInUse(AppDataInputDTO inputDTO) {
        if (inputDTO.isUseReuseUrl() && StringUtils.isBlank(inputDTO.getReuseUrl())) {
            throw new BusinessRuleException(Constants.ERR_APP_DATA_URL_REQUIRED);
        }
    }

    /**
     * Fetches the live OpenAPI 3 document to query for the given anchor's "Open Data" tab - either
     * the application's own external REST API, derived from its {@code code} (lower-cased - the
     * URL convention is always lowercase regardless of how the code is actually stored; any
     * underscore, digit or other character it carries is kept as-is), or, when {@code
     * useOpenDataUrl} is {@code true}, {@code openDataUrl} itself, used exactly as stored - and
     * resolves every declared GET operation into an {@link OpenDataEndpointOutputDTO}.
     *
     * @param appData the anchor whose published GET endpoints to resolve
     * @return the fetched document paired with the URL that resolved it - the caller extracts the
     * GET endpoints and caches the URL once both tabs have been resolved (see {@link #getById})
     * @throws BusinessRuleException if the live document can't be fetched or parsed
     */
    private OpenApiDocumentResult resolveOpenDataEndpoints(AppData appData) {
        return dataApiClient.fetchOpenApiDocument(
                appData.getApplication().getCode().toLowerCase(), appData.isUseOpenDataUrl(), appData.getOpenDataUrl());
    }

    /**
     * Fetches the live reuse document to query for the given anchor's "Reutilització" tab - either
     * the application's own backend, derived from its {@code code} (lower-cased; any underscore,
     * digit, or other character it carries is kept as-is), or, when {@code useReuseUrl} is {@code
     * true}, {@code reuseUrl} itself, used exactly as stored - and resolves every declared GET
     * operation into an {@link OpenDataEndpointOutputDTO}.
     *
     * @param appData the anchor whose published GET endpoints to resolve
     * @return the fetched document paired with the URL that resolved it - the caller extracts the
     * GET endpoints and caches the URL once both tabs have been resolved (see {@link #getById})
     * @throws BusinessRuleException if the live document can't be fetched or parsed
     */
    private OpenApiDocumentResult resolveReuseEndpoints(AppData appData) {
        return dataApiClient.fetchReuseDocument(
                appData.getApplication().getCode().toLowerCase(), appData.isUseReuseUrl(), appData.getReuseUrl());
    }

    /**
     * Resolves every declared GET operation in the given OpenAPI 3 document into an {@link
     * OpenDataEndpointOutputDTO}, shared by both {@link #resolveOpenDataEndpoints} and {@link
     * #resolveReuseEndpoints} since both tabs query the same document shape. Paths declaring no
     * GET operation are skipped entirely.
     *
     * @param document the parsed document to extract GET endpoints from
     * @return every published GET endpoint, or an empty list if the document declares none
     */
    private List<OpenDataEndpointOutputDTO> extractGetEndpoints(OpenApiDocument document) {
        Map<String, OpenApiPathItem> paths = document.getPaths();

        List<OpenDataEndpointOutputDTO> endpoints = new ArrayList<>();
        if (paths == null) {
            return endpoints;
        }
        for (Map.Entry<String, OpenApiPathItem> pathEntry : paths.entrySet()) {
            OpenApiOperation get = pathEntry.getValue() != null ? pathEntry.getValue().getGet() : null;
            if (get == null) {
                continue;
            }
            endpoints.add(new OpenDataEndpointOutputDTO(pathEntry.getKey(), "GET", openDataOperationMapper.toResponse(get)));
        }
        return endpoints;
    }
}

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
import es.caib.invai.back.service.facade.application.data.AppDataService;
import es.caib.invai.back.service.mapper.application.data.AppDataMapper;
import es.caib.invai.back.service.model.application.data.AppData;
import es.caib.invai.back.utils.Constants;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;

/**
 * Facade service implementing CRUD for the "data" anchor, plus the check of the URL of the swagger
 * of both its "Open Data" and its "Reutilització" tabs.
 * <p>
 * Each tab uses a URL: by default, the one derived from the application's {@code code} (see {@link
 * #getTabUrl}), or, when a person typed one in, that one - a {@code null} URL on the anchor means
 * "the default one". Whether a URL works is never stored nor asked by {@link #getById}: the front
 * end asks it, URL by URL, with {@link #checkUrl}, which queries it only to find out whether it
 * answers with a 2xx status code; anything else (4xx, 5xx, unreachable) means the URL doesn't
 * work, and the front end asks the user for another one. That URL is set tab by tab, with {@link
 * #updateOpenDataUrl} and {@link #updateReuseUrl}, which query it too.
 * </p>
 * <p>
 * Like its AppDevelopment/AppSecurity/AppAccessibility/AppInformationSystemDb/
 * AppResponsibleAuthorized counterparts, this anchor is auto-created when the owning {@code
 * Application} is created (see {@code ApplicationServiceFacadeBean}); {@link #create} exists mainly
 * to backfill applications created before this anchor existed, and rejects the request if the
 * target application already has an active anchor.
 * </p>
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

    /** Repository used to check that the application of a new anchor exists. */
    @Autowired
    private ApplicationRepository applicationRepository;

    /** Client used to query the URL of the swagger of each tab. */
    @Autowired
    private DataApiClient dataApiClient;

    /** Host serving every application's external REST API and its own backend. */
    @Value("${es.caib.invai.data.base-url}")
    private String baseUrl;

    /** Path segment that follows the application's code in the default "Open Data" URL, e.g. {@code api/externa}. */
    @Value("${es.caib.invai.data.opendata.path-prefix}")
    private String openDataPathPrefix;

    /** Path segment that follows the application's code in the default "Reutilització" URL, e.g. {@code api/interna}. */
    @Value("${es.caib.invai.data.reuse.path-prefix}")
    private String reusePathPrefix;

    /**
     * Fetches the data anchor by its own primary key. Unlike {@link #update} and {@link
     * #delete}, this does not throw when no such anchor exists: it returns {@code null}.
     * When an anchor is found, the URL each tab uses (the one typed in by hand, or the default one)
     * is returned. No URL is queried: see {@link #checkUrl}.
     *
     * @param id primary key of the data anchor
     * @return the matching anchor, or {@code null} if no anchor exists with this ID
     */
    @Override
    @Transactional(readOnly = true)
    public AppDataOutputDTO getById(Long id) {
        log.debug("Facade: Fetching application data anchor for ID: {}", id);
        AppData domain = appDataRepository.findById(id);
        if (Objects.isNull(domain)) {
            return null;
        }
        String code = domain.getApplication().getCode();

        AppDataOutputDTO response = appDataMapper.toResponse(domain);
        response.setOpenDataUrl(getTabUrl(domain.getOpenDataUrl(), code, openDataPathPrefix));
        response.setReuseUrl(getTabUrl(domain.getReuseUrl(), code, reusePathPrefix));
        return response;
    }

    /**
     * Queries a URL to find out whether it answers with a 2xx status code. It touches no data, so
     * it runs outside any transaction rather than holding a database connection while it waits for
     * the URL to answer.
     *
     * @param url the URL to check
     * @return the URL queried, and whether it works
     * @throws BusinessRuleException if the URL can't be checked at all (the integration module is unreachable)
     * @throws DataTimeoutException  if the check timed out
     */
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public AppDataUrlOutputDTO checkUrl(String url) {
        log.debug("Facade: Checking the data URL {}", url);
        UrlCheckResult result = dataApiClient.checkUrl(url);
        return new AppDataUrlOutputDTO(result.getUrl(), result.isValid());
    }

    /**
     * Creates a new data anchor for the application referenced by {@code
     * inputDTO.getApplicationId()}. Rejects the request if that application doesn't exist or
     * already has an active (non-soft-deleted) anchor; a previously soft-deleted anchor for the
     * same application does not block creating a new one. Both tabs start on their default URL.
     * The response carries no {@code openData}/{@code reuse}: this call queries no URL.
     *
     * @param inputDTO creation payload; {@code applicationId} identifies the owning application
     * @return the newly created anchor, mapped to its outbound representation
     * @throws BusinessRuleException if the application doesn't exist, or already has an active data anchor
     */
    @Override
    public AppDataOutputDTO create(AppDataInputDTO inputDTO) {
        log.info("Facade: Persisting new application data anchor for application ID: {}", inputDTO.getApplicationId());

        if (Objects.isNull(applicationRepository.findById(inputDTO.getApplicationId()))) {
            throw new BusinessRuleException(Constants.ERR_APP_NOT_FOUND);
        }
        AppData existing = appDataRepository.findByApplicationId(inputDTO.getApplicationId());
        if (Objects.nonNull(existing) && Objects.isNull(existing.getDeletedAt())) {
            throw new BusinessRuleException(Constants.ERR_APP_DATA_ALREADY_EXISTS);
        }

        Utils.sanitize(inputDTO);

        AppData domainModel = appDataMapper.toModelFromInput(inputDTO);
        return appDataMapper.toResponse(appDataRepository.create(domainModel));
    }

    /**
     * Applies the given payload onto the data anchor identified by {@code id}: only the free-text
     * observation, since the URL of each tab is set on its own (see {@link #updateOpenDataUrl} and
     * {@link #updateReuseUrl}) and is left untouched here. The response carries no {@code
     * openData}/{@code reuse}: this call queries no URL. Does not check whether the anchor is
     * currently soft-deleted — unlike {@link #delete}, a previously deleted anchor can still be
     * updated through this method.
     *
     * @param id       primary key of the data anchor to update
     * @param inputDTO payload with the new field values
     * @return the updated anchor, mapped to its outbound representation
     * @throws BusinessRuleException if no anchor exists with the given ID
     */
    @Override
    public AppDataOutputDTO update(Long id, AppDataInputDTO inputDTO) {
        log.info("Facade: Updating application data anchor ID: {}", id);

        AppData existingModel = findById(id);

        Utils.sanitize(inputDTO);

        appDataMapper.updateModelFromInput(inputDTO, existingModel);
        return appDataMapper.toResponse(appDataRepository.update(existingModel, id));
    }

    /**
     * Sets the URL of the swagger of the "Open Data" tab - or, with a {@code null}/blank one, makes
     * the tab go back to the default URL - after querying the URL the tab will use (the typed one,
     * or else the default one) to find out whether it answers with a 2xx status code. The URL is
     * saved whether it works or not, and the answer is returned, not stored.
     *
     * @param id       primary key of the data anchor
     * @param inputDTO payload carrying the URL
     * @return the URL the tab uses (the one typed in, or else the default one), and whether it works
     * @throws BusinessRuleException if no anchor exists with the given ID, or the URL can't be queried at all
     * @throws DataTimeoutException  if the check timed out
     */
    @Override
    public AppDataUrlOutputDTO updateOpenDataUrl(Long id, AppDataUrlInputDTO inputDTO) {
        log.info("Facade: Setting the Open Data URL of application data anchor ID: {}", id);
        AppData existingModel = findById(id);
        String url = StringUtils.trimToNull(inputDTO.getUrl());

        UrlCheckResult result = dataApiClient.checkUrl(getTabUrl(url, existingModel.getApplication().getCode(), openDataPathPrefix));
        existingModel.setOpenDataUrl(url);
        appDataRepository.update(existingModel, id);
        return new AppDataUrlOutputDTO(result.getUrl(), result.isValid());
    }

    /**
     * Sets the URL of the swagger of the "Reutilització" tab - or, with a {@code null}/blank one,
     * makes the tab go back to the default URL - after querying the URL the tab will use (the typed
     * one, or else the default one) to find out whether it answers with a 2xx status code. The URL
     * is saved whether it works or not, and the answer is returned, not stored.
     *
     * @param id       primary key of the data anchor
     * @param inputDTO payload carrying the URL
     * @return the URL the tab uses (the one typed in, or else the default one), and whether it works
     * @throws BusinessRuleException if no anchor exists with the given ID, or the URL can't be queried at all
     * @throws DataTimeoutException  if the check timed out
     */
    @Override
    public AppDataUrlOutputDTO updateReuseUrl(Long id, AppDataUrlInputDTO inputDTO) {
        log.info("Facade: Setting the Reutilitzacio URL of application data anchor ID: {}", id);
        AppData existingModel = findById(id);
        String url = StringUtils.trimToNull(inputDTO.getUrl());

        UrlCheckResult result = dataApiClient.checkUrl(getTabUrl(url, existingModel.getApplication().getCode(), reusePathPrefix));
        existingModel.setReuseUrl(url);
        appDataRepository.update(existingModel, id);
        return new AppDataUrlOutputDTO(result.getUrl(), result.isValid());
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

        AppData existingModel = findById(id);

        if (Objects.nonNull(existingModel.getDeletedAt())) {
            throw new BusinessRuleException(Constants.ERR_APP_DATA_NOT_ACTIVE);
        }

        existingModel.setDeletedAt(LocalDateTime.now());
        existingModel.setDeletedBy(Utils.resolveCurrentUsername());

        appDataRepository.delete(existingModel);
    }

    /**
     * Finds the anchor whose tab URL is about to be set.
     *
     * @param id primary key of the data anchor
     * @return the anchor
     * @throws BusinessRuleException if no anchor exists with the given ID
     */
    private AppData findById(Long id) {
        AppData existingModel = appDataRepository.findById(id);
        if (Objects.isNull(existingModel)) {
            throw new BusinessRuleException(Constants.ERR_APP_DATA_NOT_FOUND);
        }
        return existingModel;
    }

    /**
     * Gives the URL a tab uses: the one typed in by hand, or else the default one, which is the
     * host, the lower-cased application code and the path segment of the tab, the code directly
     * followed by the path segment with no separator in between, ignoring any trailing slash the
     * configured host or segment may carry. For an application whose code is {@code SEDEIB}, with
     * the host {@code https://intranet.caib.es} and the segment {@code api/externa}, it is {@code
     * https://intranet.caib.es/sedeibapi/externa}.
     *
     * @param typedUrl        the URL typed in by hand, or {@code null} if the tab uses the default one
     * @param applicationCode the application's code
     * @param pathPrefix      the path segment that follows the code in the default URL of the tab
     * @return the URL the tab uses
     */
    private String getTabUrl(String typedUrl, String applicationCode, String pathPrefix) {
        if (StringUtils.isNotBlank(typedUrl)) {
            return typedUrl;
        }
        return StringUtils.stripEnd(baseUrl, "/") + "/" + applicationCode.toLowerCase(Locale.ROOT) + StringUtils.stripEnd(pathPrefix, "/");
    }
}

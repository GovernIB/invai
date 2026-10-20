package es.caib.invai.back.rest.data;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.exception.DataTimeoutException;
import es.caib.invai.back.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Optional;

/**
 * Calls the {@code invai-api-interna} module's Open Data and Reuse endpoints on behalf of
 * {@code AppDataServiceFacadeBean}, forwarding the current session's Bearer token via
 * {@code IntegracionsClientConfig}. The module itself resolves which URL to query - either the
 * application-name-derived one, or the caller's own explicit override - so this client only
 * forwards the raw parameters needed to make that decision. Both tabs publish the same document
 * shape, so a single {@link #fetchDocument} helper backs both {@link #fetchOpenApiDocument} and
 * {@link #fetchReuseDocument}.
 * <p>
 * Built on {@code WebClient}, but blocks for a synchronous result, like every other integration in
 * this codebase (see {@code Dir3CaibClient}).
 * </p>
 *
 * @since 1.0.5
 */
@Component
@Slf4j
public class DataApiClient {

    /** Pre-configured {@link WebClient} pointing at {@code invai-api-interna}, see {@code IntegracionsClientConfig}. */
    @Autowired
    @Qualifier("integracionsWebClient")
    private WebClient integracionsWebClient;

    /**
     * Fetches and parses the OpenAPI 3 document published by the given application, or, when
     * {@code useOpenDataUrl} is {@code true}, at {@code openDataUrl} itself instead.
     *
     * @param applicationName the {@code name} of the application whose OpenAPI document to fetch
     * @param useOpenDataUrl  whether to query {@code openDataUrl} as a hard override instead of racing candidates
     * @param openDataUrl     an explicit override URL (when {@code useOpenDataUrl} is {@code true}), or a
     *                        previously cached candidate to try first (when {@code false})
     * @return the parsed OpenAPI document paired with the URL that resolved it (see {@link
     * OpenApiDocumentResult#getDocument()}{@code .getPaths()} for every declared path, of every
     * HTTP verb - filtering to GET-only is the caller's job)
     * @throws DataTimeoutException  if the fetch timed out
     * @throws BusinessRuleException if the document can't be fetched or parsed for any other reason
     * (network failure, non-2xx response, malformed JSON), or every candidate fails
     */
    public OpenApiDocumentResult fetchOpenApiDocument(String applicationName, boolean useOpenDataUrl, String openDataUrl) {
        return fetchDocument("/opendata/document", applicationName, "useOpenDataUrl", useOpenDataUrl,
                "openDataUrl", openDataUrl, Constants.ERR_OPENDATA_UNAVAILABLE);
    }

    /**
     * Fetches and parses the OpenAPI 3 document published by the given application's own backend,
     * or, when {@code useReuseUrl} is {@code true}, at {@code reuseUrl} itself instead.
     *
     * @param applicationName the {@code name} of the application whose reuse document to fetch
     * @param useReuseUrl     whether to query {@code reuseUrl} as a hard override instead of racing candidates
     * @param reuseUrl        an explicit override URL (when {@code useReuseUrl} is {@code true}), or a
     *                        previously cached candidate to try first (when {@code false})
     * @return the parsed OpenAPI document paired with the URL that resolved it (see {@link
     * OpenApiDocumentResult#getDocument()}{@code .getPaths()} for every declared path, of every
     * HTTP verb - filtering to GET-only is the caller's job)
     * @throws DataTimeoutException  if the fetch timed out
     * @throws BusinessRuleException if the document can't be fetched or parsed for any other reason
     * (network failure, non-2xx response, malformed JSON), or every candidate fails
     */
    public OpenApiDocumentResult fetchReuseDocument(String applicationName, boolean useReuseUrl, String reuseUrl) {
        return fetchDocument("/reuse/document", applicationName, "useReuseUrl", useReuseUrl,
                "reuseUrl", reuseUrl, Constants.ERR_REUSE_UNAVAILABLE);
    }

    /**
     * Shared GET call backing both {@link #fetchOpenApiDocument} and {@link #fetchReuseDocument} -
     * both hit {@code invai-api-interna} the same way, differing only in the target path, the
     * override flag/URL query parameter names, and which error to raise on failure.
     *
     * @param path             the {@code invai-api-interna} endpoint path to call
     * @param applicationName  the {@code name} of the application whose document to fetch
     * @param useUrlParam      query parameter name carrying the override flag ({@code useOpenDataUrl}/{@code useReuseUrl})
     * @param useUrl           the override flag's value
     * @param urlParam         query parameter name carrying the override URL ({@code openDataUrl}/{@code reuseUrl})
     * @param explicitUrl      the override URL, sent only when present
     * @param unavailableError the {@link Constants} error key to raise if the document can't be fetched or parsed
     * @return the parsed OpenAPI document paired with the URL that resolved it
     * @throws DataTimeoutException  if {@code invai-api-interna} reports the fetch timed out (a 504)
     * @throws BusinessRuleException if the document can't be fetched or parsed for any other reason
     */
    private OpenApiDocumentResult fetchDocument(String path, String applicationName, String useUrlParam, boolean useUrl,
            String urlParam, String explicitUrl, String unavailableError) {
        log.debug("Client: Fetching document from {} for application name: {}", path, applicationName);
        try {
            OpenApiDocumentResult result = integracionsWebClient.get()
                    .uri(uriBuilder -> uriBuilder.path(path)
                            .queryParam("applicationName", applicationName)
                            .queryParam(useUrlParam, useUrl)
                            .queryParamIfPresent(urlParam, Optional.ofNullable(explicitUrl))
                            .build())
                    .retrieve()
                    .bodyToMono(OpenApiDocumentResult.class)
                    .block();

            if (result == null || result.getDocument() == null) {
                throw new BusinessRuleException(unavailableError);
            }
            return result;
        } catch (WebClientResponseException e) {
            log.error("Client error: document fetch failed for application name: {}", applicationName, e);
            if (e.getStatusCode() == HttpStatus.GATEWAY_TIMEOUT) {
                throw new DataTimeoutException();
            }
            throw new BusinessRuleException(unavailableError);
        } catch (WebClientException | IllegalArgumentException e) {
            log.error("Client error: document fetch failed for application name: {}", applicationName, e);
            throw new BusinessRuleException(unavailableError);
        }
    }
}

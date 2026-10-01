package es.caib.invai.api.interna.rest.data;

import es.caib.invai.api.interna.config.DataApiConfig;
import es.caib.invai.api.interna.exception.IntegrationTimeoutException;
import es.caib.invai.api.interna.exception.IntegrationUnavailableException;
import es.caib.invai.api.interna.rest.openapi.OpenApiDocument;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;

/**
 * Fetches a live OpenAPI 3 ("swagger") document straight off an application's own external REST
 * API (for the "Open Data" tab) or its own backend (for the "Reutilització" tab), on behalf of
 * {@code es.caib.invai.api.interna.controller.data.DataController}. Both tabs publish the same
 * document shape, so a single {@link #getOpenApiDocument}/{@link #getFirstRespondingCandidate} pair of helpers backs
 * both {@link #getOpenApiDocumentForOpenData} and {@link #getOpenApiDocumentForReuse}; each resolves which URL to
 * query itself:
 * <ul>
 *     <li>the caller's own explicit override ({@code openDataUrl}/{@code reuseUrl}), used exactly
 *     as given, when it asks to use it;</li>
 *     <li>otherwise, when the caller passes a previously cached candidate (not marked as an
 *     explicit override), that single URL is tried directly first - see {@link #getWithCachedHint};</li>
 *     <li>otherwise (or if that cached candidate stopped working), every known candidate URL (see
 *     {@link DataApiConfig#buildOpenApiUrlCandidates}/{@link DataApiConfig#buildReuseUrlCandidates})
 *     is raced in parallel and whichever responds first wins - real CAIB applications don't all
 *     publish their OpenAPI document under the same path convention.</li>
 * </ul>
 * Every fetch reports back which URL actually resolved (see {@link OpenApiDocumentResult}), so
 * {@code invai-back} can cache it on {@code AppData} and skip the race on the next call.
 * <p>
 * Built on {@code WebClient}, but blocks for a synchronous result, like every other integration in
 * this module (see {@code Dir3CaibClient}).
 * </p>
 *
 * @since 1.0.5
 */
@Component
@Slf4j
public class DataApiClient {

    /**
     * Timeout applied only to the cached-candidate fast path (see {@link #getWithCachedHint}) -
     * deliberately shorter than the {@code WebClient}'s own connect/response timeouts, since a
     * cached candidate that has gone from a clean failure to genuinely hanging shouldn't stall the
     * fallback race for as long as a normal, one-shot call would be allowed to take.
     */
    private static final Duration CACHED_CANDIDATE_TIMEOUT = Duration.ofSeconds(3);

    /** Resolves the application-name-derived candidate URLs when no explicit override is requested. */
    @Autowired
    private DataApiConfig dataApiConfig;

    /** Pre-configured {@link WebClient} shared by both tabs, see {@link DataApiConfig}. */
    @Autowired
    @Qualifier("dataWebClient")
    private WebClient dataWebClient;

    /**
     * Fetches and parses the OpenAPI 3 document published by the given application, or, when
     * {@code useOpenDataUrl} is {@code true}, at {@code openDataUrl} itself instead.
     *
     * @param applicationName the {@code name} of the application whose OpenAPI document to fetch
     * @param useOpenDataUrl  whether to query {@code openDataUrl} as a hard override instead of racing candidates
     * @param openDataUrl     an explicit override URL (when {@code useOpenDataUrl} is {@code true}), or a
     *                        previously cached candidate to try first (when {@code false})
     * @return the fetched document paired with the URL that resolved it ({@link
     * OpenApiDocument#getPaths()} carries every declared path, of every HTTP verb - filtering to
     * GET-only is the caller's job)
     * @throws IntegrationTimeoutException     if an explicit override times out (a cached candidate
     * that times out instead falls back to racing every known candidate, see {@link
     * #getWithCachedHint})
     * @throws IntegrationUnavailableException if the document can't be fetched or parsed for any
     * other reason (network failure, non-2xx response, malformed JSON), or every candidate fails
     */
    public OpenApiDocumentResult getOpenApiDocumentForOpenData(String applicationName, boolean useOpenDataUrl, String openDataUrl) {
        if (useOpenDataUrl) {
            return new OpenApiDocumentResult(getOpenApiDocument(openDataUrl, "Open Data document unavailable", null), openDataUrl);
        }
        return getWithCachedHint(openDataUrl, dataApiConfig.buildOpenApiUrlCandidates(applicationName),
                "Open Data document unavailable");
    }

    /**
     * Fetches and parses the OpenAPI 3 document published by the given application's own backend,
     * or, when {@code useReuseUrl} is {@code true}, at {@code reuseUrl} itself instead.
     *
     * @param applicationName the {@code name} of the application whose reuse document to fetch
     * @param useReuseUrl     whether to query {@code reuseUrl} as a hard override instead of racing candidates
     * @param reuseUrl        an explicit override URL (when {@code useReuseUrl} is {@code true}), or a
     *                        previously cached candidate to try first (when {@code false})
     * @return the fetched document paired with the URL that resolved it ({@link
     * OpenApiDocument#getPaths()} carries every declared path, of every HTTP verb - filtering to
     * GET-only is the caller's job)
     * @throws IntegrationTimeoutException     if an explicit override times out (a cached candidate
     * that times out instead falls back to racing every known candidate, see {@link
     * #getWithCachedHint})
     * @throws IntegrationUnavailableException if the document can't be fetched or parsed for any
     * other reason (network failure, non-2xx response, malformed JSON), or every candidate fails
     */
    public OpenApiDocumentResult getOpenApiDocumentForReuse(String applicationName, boolean useReuseUrl, String reuseUrl) {
        if (useReuseUrl) {
            return new OpenApiDocumentResult(getOpenApiDocument(reuseUrl, "Reuse document unavailable", null), reuseUrl);
        }
        return getWithCachedHint(reuseUrl, dataApiConfig.buildReuseUrlCandidates(applicationName),
                "Reuse document unavailable");
    }

    /**
     * Tries {@code cachedUrl} alone first, when present - the fast path once a previous call has
     * already resolved which candidate works for this application. Falls back to racing every
     * known candidate when there's nothing cached yet, or the cached candidate stopped working
     * (e.g., the application changed its documentation stack).
     *
     * @param cachedUrl        a previously resolved candidate to try first, or {@code null}/blank if none
     * @param candidateUrls    every known candidate URL to race if {@code cachedUrl} is absent or fails
     * @param unavailableMessage the message to raise if every attempt fails
     * @return the fetched document paired with the URL that resolved it
     * @throws IntegrationUnavailableException if {@code cachedUrl} fails and every candidate also fails
     */
    private OpenApiDocumentResult getWithCachedHint(String cachedUrl, List<String> candidateUrls, String unavailableMessage) {
        if (StringUtils.isNotBlank(cachedUrl)) {
            try {
                OpenApiDocument document = getOpenApiDocument(cachedUrl, unavailableMessage, CACHED_CANDIDATE_TIMEOUT);
                return new OpenApiDocumentResult(document, cachedUrl);
            } catch (IntegrationUnavailableException | IntegrationTimeoutException e) {
                log.debug("Cached candidate no longer works, re-resolving: {}", cachedUrl);
            }
        }
        return getFirstRespondingCandidate(candidateUrls, unavailableMessage);
    }

    /**
     * Races every candidate URL in parallel and keeps whichever responds first with a valid
     * document, ignoring failures from the others - only raises {@link
     * IntegrationUnavailableException} if every candidate fails.
     *
     * @param candidateUrls      every candidate URL to race
     * @param unavailableMessage the message to raise if every candidate fails
     * @return the fetched document paired with the candidate URL that won the race
     * @throws IntegrationUnavailableException if every candidate fails
     */
    private OpenApiDocumentResult getFirstRespondingCandidate(List<String> candidateUrls, String unavailableMessage) {
        List<Mono<OpenApiDocumentResult>> attempts = candidateUrls.stream()
                .map(url -> getOpenApiDocumentMono(url)
                        .map(document -> new OpenApiDocumentResult(document, url))
                        .onErrorResume(e -> Mono.empty()))
                .toList();

        try {
            OpenApiDocumentResult result = Mono.firstWithValue(attempts).block();
            if (result == null) {
                throw new IntegrationUnavailableException(unavailableMessage, null);
            }
            return result;
        } catch (NoSuchElementException e) {
            throw new IntegrationUnavailableException(unavailableMessage, e);
        }
    }

    /**
     * Fetches and parses the document at {@code url}, optionally capped by {@code timeout} - used
     * to fail fast on the cached-candidate path (see {@link #CACHED_CANDIDATE_TIMEOUT}) without
     * shortening the timeout for an explicit override or a race candidate. Classifies any failure
     * exactly once (see {@link #isTimeout}) instead of juggling a Reactor-level timeout and a
     * {@code WebClient}-level one through separate mechanisms - both surface here as a plain
     * {@link RuntimeException} either way, so one check covers both.
     *
     * @param url                the URL to fetch
     * @param unavailableMessage the message to raise if the fetch fails for a reason other than a
     *                           timeout
     * @param timeout            an additional cap on top of the {@code WebClient}'s own connect/response
     *                           timeouts, or {@code null} to rely on those alone
     * @return the fetched document
     * @throws IntegrationTimeoutException     if {@code timeout} elapses, or the underlying
     * {@code WebClient}'s own connect/response timeout fires first
     * @throws IntegrationUnavailableException if the fetch fails for any other reason
     */
    private OpenApiDocument getOpenApiDocument(String url, String unavailableMessage, Duration timeout) {
        log.debug("Fetching document from URL: {}", url);
        OpenApiDocument document;
        try {
            Mono<OpenApiDocument> mono = getOpenApiDocumentMono(url);
            document = (timeout != null ? mono.timeout(timeout) : mono).block();
        } catch (RuntimeException e) {
            log.error("Document fetch failed for URL: {}", url, e);
            if (isTimeout(e)) {
                throw new IntegrationTimeoutException(unavailableMessage, e);
            }
            throw new IntegrationUnavailableException(unavailableMessage, e);
        }
        if (document == null) {
            throw new IntegrationUnavailableException(unavailableMessage, null);
        }
        return document;
    }

    /**
     * Walks {@code ex}'s cause chain looking for a timeout-flavored exception. Nothing here raises
     * a single dedicated "this was a timeout" type: Reactor's own {@code .timeout(Duration)}
     * operator raises {@link TimeoutException}, while the underlying {@code WebClient}'s
     * connect/response timeout surfaces one of several Netty exceptions ({@code
     * ReadTimeoutException}, {@code ConnectTimeoutException}, ...) - every one of them names itself
     * accordingly, so matching on the class name covers all of them with one check.
     *
     * @param ex the exception to inspect
     * @return {@code true} if {@code ex} or any of its causes looks like a timeout
     */
    private static boolean isTimeout(Throwable ex) {
        for (Throwable current = ex; current != null; current = current.getCause()) {
            if (current.getClass().getSimpleName().contains("Timeout")) {
                return true;
            }
        }
        return false;
    }

    private Mono<OpenApiDocument> getOpenApiDocumentMono(String url) {
        return Mono.defer(() -> dataWebClient.get()
                .uri(URI.create(url))
                .retrieve()
                .bodyToMono(OpenApiDocument.class));
    }
}

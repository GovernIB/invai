package es.caib.invai.api.interna.rest.data;

import es.caib.invai.api.interna.config.DataApiConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.util.Objects;

/**
 * Queries a URL an application publishes for its "Open Data" (external REST API) or
 * "Reutilització" (own backend) tab, on behalf of {@code
 * es.caib.invai.api.interna.controller.data.DataController}, and reports only whether it answered
 * with a 2xx status code. The document itself is never read: what to show for a working URL is the
 * front end's business. Which URL to query is the caller's business too: the default URL of each
 * tab is derived by {@code invai-back}, which stores what it takes to build it.
 * <p>
 * Any outcome other than a 2xx - a 4xx or 5xx status code, a malformed or unsupported (non-HTTP)
 * URL, a network failure, or a timeout - is reported as "not valid" rather than raised as an
 * exception, so the caller can ask the user for a different URL. Redirects are followed (see
 * {@link DataApiConfig}), so what counts is the final status code.
 * </p>
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

    /** Pre-configured {@link WebClient}, see {@link DataApiConfig}. */
    @Autowired
    @Qualifier("dataWebClient")
    private WebClient dataWebClient;

    /**
     * Queries the given URL and reports whether it answered with a 2xx status code.
     *
     * @param url the absolute {@code http}/{@code https} URL to query; anything else is not queried
     * @return the URL together with whether it is valid and the status code it answered with, if any
     */
    public UrlCheckResult checkUrl(String url) {
        if (!isHttpUrl(url)) {
            log.debug("Not an absolute http(s) URL, not queried: {}", url);
            return new UrlCheckResult(url, false, null);
        }
        try {
            Integer statusCode = dataWebClient.get()
                    .uri(URI.create(url))
                    .exchangeToMono(response -> response.releaseBody().thenReturn(response.statusCode().value()))
                    .block();
            boolean valid = Objects.nonNull(statusCode) && statusCode >= 200 && statusCode < 300;
            log.debug("URL {} answered {}", url, statusCode);
            return new UrlCheckResult(url, valid, statusCode);
        } catch (RuntimeException e) {
            log.warn("URL could not be queried: {}", url, e);
            return new UrlCheckResult(url, false, null);
        }
    }

    /**
     * Tells whether the given text is an absolute {@code http}/{@code https} URL. Anything else
     * (blank, relative, or another scheme such as {@code file:}) is never queried.
     *
     * @param url the text to check
     * @return {@code true} if it is an absolute http or https URL
     */
    private static boolean isHttpUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return false;
        }
        try {
            URI uri = URI.create(url);
            return StringUtils.equalsAny(uri.getScheme(), "http", "https") && StringUtils.isNotBlank(uri.getHost());
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}

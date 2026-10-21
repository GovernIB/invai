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

import java.util.Objects;

/**
 * Calls the {@code invai-api-interna} module's URL check on behalf of {@code
 * AppDataServiceFacadeBean}, forwarding the current session's Bearer token via {@code
 * IntegracionsClientConfig}. The module queries the given URL - the one of the "Open Data" tab (the
 * application's external REST API) or of the "Reutilització" tab (its own backend), which are
 * checked the same way - and reports whether it answered with a 2xx status code.
 * <p>
 * A URL that answers with an error (4xx, 5xx, unreachable) is <em>not</em> a failure of this call:
 * it is a result with {@code valid} set to {@code false}. Only a failure to reach {@code
 * invai-api-interna} itself is raised as an exception.
 * </p>
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
     * Checks a URL.
     *
     * @param url the URL to check
     * @return the URL that was queried and whether it answered with a 2xx status code
     * @throws DataTimeoutException  if {@code invai-api-interna} reports that the check timed out (a 504)
     * @throws BusinessRuleException if the check can't be carried out for any other reason
     */
    public UrlCheckResult checkUrl(String url) {
        log.debug("Client: Checking the data URL {}", url);
        try {
            UrlCheckResult result = integracionsWebClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/data/check").queryParam("url", "{url}").build(url))
                    .retrieve()
                    .bodyToMono(UrlCheckResult.class)
                    .block();
            if (Objects.isNull(result)) {
                throw new BusinessRuleException(Constants.ERR_DATA_URL_UNAVAILABLE);
            }
            return result;
        } catch (WebClientResponseException e) {
            log.error("Client error: URL check failed for {}", url, e);
            if (e.getStatusCode() == HttpStatus.GATEWAY_TIMEOUT) {
                throw new DataTimeoutException();
            }
            throw new BusinessRuleException(Constants.ERR_DATA_URL_UNAVAILABLE);
        } catch (WebClientException | IllegalArgumentException e) {
            log.error("Client error: URL check failed for {}", url, e);
            throw new BusinessRuleException(Constants.ERR_DATA_URL_UNAVAILABLE);
        }
    }
}

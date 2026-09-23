package es.caib.invai.back.rest.dir3;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import java.util.List;

/**
 * Calls the {@code invai-api-interna} module's DIR3CAIB endpoint on behalf of
 * {@code AdmUnitServiceFacadeBean}, forwarding the current session's Bearer token via
 * {@code IntegracionsClientConfig}.
 * <p>
 * Built on {@code WebClient}, but blocks for a synchronous result: every other layer of this
 * codebase (controller, facade) is standard blocking Spring MVC, so this client stays synchronous
 * rather than exposing a reactive {@code Mono}/{@code Flux} type this codebase has no other
 * machinery to consume yet.
 * </p>
 *
 * @since 1.0.4
 */
@Component
@Slf4j
public class Dir3CaibClient {

    /** Pre-configured {@link WebClient} pointing at {@code invai-api-interna}, see {@code IntegracionsClientConfig}. */
    @Autowired
    @Qualifier("integracionsWebClient")
    private WebClient integracionsWebClient;

    /**
     * Fetches the full, flattened tree of organizational units under the given root code.
     *
     * @param rootCode   the root unit's code
     * @param coOfficial whether to prefer the co-official denomination when one exists
     * @return every descendant unit (root included), or an empty list if the root itself is not found
     */
    public List<UnidadRest> getTree(String rootCode, boolean coOfficial) {
        log.debug("Client: Fetching DIR3CAIB organizational unit tree rooted at: {}", rootCode);
        try {
            ResponseEntity<List<UnidadRest>> response = integracionsWebClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/dir3/tree")
                            .queryParam("rootCode", rootCode)
                            .queryParam("coOfficial", coOfficial)
                            .build())
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<List<UnidadRest>>() {
                    })
                    .block();

            if (response == null || response.getStatusCode() == HttpStatus.NO_CONTENT || response.getBody() == null) {
                return List.of();
            }
            return response.getBody();
        } catch (WebClientException e) {
            log.error("Client error: DIR3CAIB tree fetch failed for root code: {}", rootCode, e);
            throw new BusinessRuleException(Constants.ERR_ADMUNIT_DIR3_UNAVAILABLE);
        }
    }
}

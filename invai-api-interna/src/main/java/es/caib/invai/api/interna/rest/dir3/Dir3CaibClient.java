package es.caib.invai.api.interna.rest.dir3;

import es.caib.invai.api.interna.exception.IntegrationUnavailableException;
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
 * Calls DIR3CAIB's real {@code obtenerArbolUnidades} REST operation on behalf of
 * {@link Dir3CaibController}.
 * <p>
 * Built on {@code WebClient}, but blocks for a synchronous result: this module's controller layer
 * is standard blocking Spring MVC, so this client stays synchronous rather than exposing a
 * reactive {@code Mono}/{@code Flux} type it has no other machinery to consume yet.
 * </p>
 *
 * @since 1.0.4
 */
@Component
@Slf4j
public class Dir3CaibClient {

    /** Pre-configured DIR3CAIB {@link WebClient}, see {@link es.caib.invai.api.interna.config.Dir3CaibConfig}. */
    @Autowired
    @Qualifier("dir3CaibWebClient")
    private WebClient dir3CaibWebClient;

    /**
     * Fetches the full, flattened tree of organizational units under the given root code.
     *
     * @param rootCode   the root unit's code
     * @param coOfficial whether to prefer the co-official denomination when one exists
     * @return every descendant unit (root included), or an empty list if the root itself is not found
     */
    public List<UnidadRest> getTree(String rootCode, boolean coOfficial) {
        log.debug("Fetching DIR3CAIB organizational unit tree rooted at: {}", rootCode);
        try {
            ResponseEntity<List<UnidadRest>> response = dir3CaibWebClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/rest/unidades/obtenerArbolUnidades")
                            .queryParam("codigo", rootCode)
                            .queryParam("denominacionCooficial", coOfficial)
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
            log.error("DIR3CAIB tree fetch failed for root code: {}", rootCode, e);
            throw new IntegrationUnavailableException("DIR3CAIB unavailable", e);
        }
    }
}

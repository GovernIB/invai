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
 * Infrastructure adapter implementing the outbound port boundary {@link Dir3CaibClient}, calling
 * DIR3CAIB's {@code obtenerArbolUnidades} REST operation.
 * <p>
 * Built on {@code WebClient}, but blocks for a synchronous result: every other layer of this
 * codebase (controller, facade) is standard blocking Spring MVC, so the port interface stays
 * synchronous rather than exposing a reactive {@code Mono}/{@code Flux} type this codebase has no
 * other machinery to consume yet.
 * </p>
 *
 * @since 1.0.4
 */
@Component
@Slf4j
public class Dir3CaibClientAdapter implements Dir3CaibClient {

    /** Pre-configured DIR3CAIB {@link WebClient}, see {@code Dir3CaibConfig}. */
    @Autowired
    @Qualifier("dir3CaibWebClient")
    private WebClient dir3CaibWebClient;

    @Override
    public List<UnidadRest> getTree(String rootCode, boolean coOfficial) {
        log.debug("Client: Fetching DIR3CAIB organizational unit tree rooted at: {}", rootCode);
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
            log.error("Client error: DIR3CAIB tree fetch failed for root code: {}", rootCode, e);
            throw new BusinessRuleException(Constants.ERR_ADMUNIT_DIR3_UNAVAILABLE);
        }
    }
}

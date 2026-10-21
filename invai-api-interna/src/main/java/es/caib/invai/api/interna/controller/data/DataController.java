package es.caib.invai.api.interna.controller.data;

import es.caib.invai.api.interna.rest.data.DataApiClient;
import es.caib.invai.api.interna.rest.data.UrlCheckResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes to {@code invai-back} the check of a URL an application publishes for its "Open Data"
 * (external REST API) or "Reutilització" (own backend) tab.
 *
 * @since 1.0.5
 */
@RestController
@RequiredArgsConstructor
public class DataController {

    /** Client querying the URLs. */
    private final DataApiClient dataApiClient;

    /**
     * Queries the given URL and reports whether it answered with a 2xx status code.
     *
     * @param url the URL to query
     * @return the URL queried whether it is valid and the status code it answered with, if any
     */
    @Tag(name = "Dades", description = "Comprovació de les URL publicades per una aplicació per a Dades obertes i Reutilització.")
    @GetMapping("/data/check")
    public UrlCheckResult checkUrl(@RequestParam String url) {
        return dataApiClient.checkUrl(url);
    }
}

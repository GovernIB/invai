package es.caib.invai.api.interna.controller.data;

import es.caib.invai.api.interna.rest.data.DataApiClient;
import es.caib.invai.api.interna.rest.data.OpenApiDocumentResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes an application's live document for both the "Open Data" tab (its external REST API) and
 * the "Reutilització" tab (its own backend) to {@code invai-back}.
 *
 * @since 1.0.5
 */
@RestController
@RequiredArgsConstructor
public class DataController {

    /** Client fetching the real documents for both tabs. */
    private final DataApiClient dataApiClient;

    /**
     * Fetches and parses the OpenAPI 3 document published by the given application, or, when
     * {@code useOpenDataUrl} is {@code true}, at {@code openDataUrl} itself instead.
     *
     * @param applicationName the {@code name} of the application whose OpenAPI document to fetch
     * @param useOpenDataUrl  whether to query {@code openDataUrl} as a hard override instead of racing candidates
     * @param openDataUrl     an explicit override URL (when {@code useOpenDataUrl} is {@code true}), or a
     *                        previously cached candidate to try first (when {@code false})
     * @return the parsed OpenAPI document paired with the URL that resolved it
     */
    @Tag(name = "Open Data", description = "Consulta en viu del document OpenAPI publicat per una aplicació.")
    @GetMapping("/opendata/document")
    public OpenApiDocumentResult getOpenDataDocument(@RequestParam String applicationName,
                                                      @RequestParam(defaultValue = "false") boolean useOpenDataUrl,
                                                      @RequestParam(required = false) String openDataUrl) {
        return dataApiClient.getOpenApiDocumentForOpenData(applicationName, useOpenDataUrl, openDataUrl);
    }

    /**
     * Fetches and parses the OpenAPI 3 document published by the given application's own backend,
     * or, when {@code useReuseUrl} is {@code true}, at {@code reuseUrl} itself instead.
     *
     * @param applicationName the {@code name} of the application whose reuse document to fetch
     * @param useReuseUrl     whether to query {@code reuseUrl} as a hard override instead of racing candidates
     * @param reuseUrl        an explicit override URL (when {@code useReuseUrl} is {@code true}), or a
     *                        previously cached candidate to try first (when {@code false})
     * @return the parsed OpenAPI document paired with the URL that resolved it
     */
    @Tag(name = "Reutilització", description = "Consulta en viu del document de reutilització publicat per una aplicació.")
    @GetMapping("/reuse/document")
    public OpenApiDocumentResult getReuseDocument(@RequestParam String applicationName,
                                                   @RequestParam(defaultValue = "false") boolean useReuseUrl,
                                                   @RequestParam(required = false) String reuseUrl) {
        return dataApiClient.getOpenApiDocumentForReuse(applicationName, useReuseUrl, reuseUrl);
    }
}

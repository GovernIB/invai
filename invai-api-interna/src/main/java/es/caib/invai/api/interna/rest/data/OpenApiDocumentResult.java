package es.caib.invai.api.interna.rest.data;

import es.caib.invai.api.interna.rest.openapi.OpenApiDocument;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Pairs a fetched {@link OpenApiDocument} with the URL that actually returned it, so a caller that
 * raced several candidate URLs (see {@link DataApiClient}) can tell which one won - {@code
 * invai-back} persists it as a cache hint on {@code AppData} to skip the race on the next call.
 *
 * @since 1.0.5
 */
@Getter
@AllArgsConstructor
public class OpenApiDocumentResult {

    /** The fetched and parsed document. */
    private final OpenApiDocument document;

    /** The URL that was actually queried to obtain {@link #document}. */
    private final String resolvedUrl;
}

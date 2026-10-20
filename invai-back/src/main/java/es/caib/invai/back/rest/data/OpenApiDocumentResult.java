package es.caib.invai.back.rest.data;

import es.caib.invai.back.rest.openapi.OpenApiDocument;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Pairs a fetched {@link OpenApiDocument} with the URL that actually returned it, as reported by
 * {@code invai-api-interna}'s candidate race - see {@code
 * es.caib.invai.back.ejb.application.data.AppDataServiceFacadeBean}, which caches it on {@code
 * AppData} to skip the race on the next call.
 *
 * @since 1.0.5
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OpenApiDocumentResult {

    /** The fetched and parsed document. */
    private OpenApiDocument document;

    /** The URL that was actually queried to obtain {@link #document}. */
    private String resolvedUrl;
}

package es.caib.invai.back.interna.application.data.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A single GET endpoint resolved from an application's live OpenAPI 3 document (see {@code
 * es.caib.invai.back.rest.openapi.OpenApiClient}), carrying its documentation and everything
 * needed to actually invoke it.
 *
 * @since 1.0.5
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OpenDataEndpointOutputDTO {

    /** The OpenAPI path template this endpoint is registered under (e.g. {@code /services/centre}). */
    private String path;

    /** The HTTP verb of this endpoint - always {@code GET}, since only GET endpoints are surfaced. */
    private String method;

    /** The parsed OpenAPI Operation Object for this path/verb. */
    private OpenDataOperationOutputDTO operation;
}

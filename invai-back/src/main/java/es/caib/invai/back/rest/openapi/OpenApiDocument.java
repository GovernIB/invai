package es.caib.invai.back.rest.openapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * Raw wire DTO mapping the root of an OpenAPI 3 ("swagger") document, as published by any
 * application's own external REST API. Only {@link #paths} is modeled - every other top-level
 * field ({@code openapi}, {@code info}, {@code components}, {@code servers}, ...) is intentionally
 * ignored, since the "Open Data" tab only ever surfaces published GET endpoints.
 *
 * @since 1.0.5
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenApiDocument {

    /** Every path template declared by the document (e.g. {@code "/services/centre"}), keyed by that template. */
    private Map<String, OpenApiPathItem> paths;
}

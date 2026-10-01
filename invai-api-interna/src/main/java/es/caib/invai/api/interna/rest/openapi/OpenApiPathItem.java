package es.caib.invai.api.interna.rest.openapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Raw wire DTO mapping an OpenAPI 3 Path Item Object. Only {@link #get} is modeled - every other
 * HTTP verb the path might declare ({@code post}, {@code put}, {@code delete}, ...) is intentionally
 * ignored, since the "Open Data" tab only ever surfaces published GET endpoints.
 *
 * @since 1.0.5
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenApiPathItem {

    /** The GET operation declared for this path, or {@code null} if this path declares no GET. */
    private OpenApiOperation get;
}

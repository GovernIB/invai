package es.caib.invai.api.interna.rest.openapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Raw wire DTO mapping an OpenAPI 3 Parameter Object - one input a GET operation accepts, and
 * everything needed to actually supply it when invoking that operation.
 *
 * @since 1.0.5
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenApiParameter {

    /** Name of the parameter, as it must be supplied (path placeholder name, or query/header/cookie key). */
    private String name;

    /** Where this parameter is supplied - {@code path}, {@code query}, {@code header}, or {@code cookie}. */
    private String in;

    /** Whether the operation can't be invoked without this parameter. Always {@code true} when {@link #in} is {@code path}. */
    private boolean required;

    /** Human-readable explanation of this parameter, when the API declares one. */
    private String description;

    /** The parameter's value type/format/constraints, when the API declares a schema for it. */
    private OpenApiSchema schema;
}

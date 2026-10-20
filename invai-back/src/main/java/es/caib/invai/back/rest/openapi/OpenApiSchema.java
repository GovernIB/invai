package es.caib.invai.back.rest.openapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Raw wire DTO mapping the (JSON Schema-derived) OpenAPI 3 Schema Object attached to a parameter -
 * only the fields relevant to building an input for it are modeled.
 *
 * @since 1.0.5
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenApiSchema {

    /** JSON Schema primitive type ({@code string}, {@code integer}, {@code boolean}, {@code array}, ...), when declared. */
    private String type;

    /** Format refining {@link #type} (e.g. {@code date}, {@code date-time}, {@code int64}), when declared. */
    private String format;

    /** Default value to pre-fill when the caller doesn't supply one, when declared. Named {@code defaultValue} since {@code default} is a Java keyword. */
    @JsonProperty("default")
    private Object defaultValue;

    /** Closed set of accepted values, when the schema restricts the parameter to one. Named {@code enumValues} since {@code enum} is a Java keyword. */
    @JsonProperty("enum")
    private List<String> enumValues;
}

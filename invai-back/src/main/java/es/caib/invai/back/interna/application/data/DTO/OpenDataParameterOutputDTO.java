package es.caib.invai.back.interna.application.data.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * A single input a GET endpoint accepts, resolved from its OpenAPI 3 Parameter Object - everything
 * needed to actually supply it when invoking that endpoint.
 *
 * @since 1.0.5
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenDataParameterOutputDTO {

    /** Name of the parameter, as it must be supplied (path placeholder name, or query/header/cookie key). */
    private String name;

    /** Where this parameter is supplied - {@code path}, {@code query}, {@code header}, or {@code cookie}. */
    private String in;

    /** Whether the endpoint can't be invoked without this parameter. Always {@code true} when {@link #in} is {@code path}. */
    private boolean required;

    /** Human-readable explanation of this parameter, or {@code null} if the API declares none. */
    private String description;

    /** JSON Schema primitive type ({@code string}, {@code integer}, {@code boolean}, {@code array}, ...), or {@code null} if undeclared. */
    private String type;

    /** Format refining {@link #type} (e.g. {@code date}, {@code date-time}, {@code int64}), or {@code null} if undeclared. */
    private String format;

    /** Default value to pre-fill when the caller doesn't supply one, or {@code null} if undeclared. */
    private Object defaultValue;

    /** Closed set of accepted values, or {@code null} if the schema doesn't restrict this parameter to one. */
    private List<String> enumValues;
}

package es.caib.invai.back.rest.openapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Raw wire DTO mapping an OpenAPI 3 Operation Object for a GET endpoint. Only the fields needed to
 * describe the endpoint and let a caller actually invoke it are modeled ({@link #operationId},
 * {@link #summary}, {@link #description}, {@link #parameters}); {@code responses}, {@code tags},
 * {@code security}, and every other field are intentionally ignored.
 *
 * @since 1.0.5
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenApiOperation {

    /** Unique identifier of this operation within the document, when the API declares one. */
    private String operationId;

    /** Short, human-readable summary of what this operation does, when the API declares one. */
    private String summary;

    /** Longer, human-readable description of this operation, when the API declares one. */
    private String description;

    /**
     * Every path, query, header, and cookie parameter this operation accepts - the information
     * needed to actually invoke it. {@code null} (rather than an empty list) when the operation
     * declares no parameters at all.
     */
    private List<OpenApiParameter> parameters;
}

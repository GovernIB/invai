package es.caib.invai.back.interna.application.data.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Description of a single GET endpoint's OpenAPI 3 Operation Object, resolved from an
 * application's live document (see {@code es.caib.invai.back.rest.openapi.OpenApiClient}) -
 * everything needed to actually invoke it, alongside its human-readable documentation.
 *
 * @since 1.0.5
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenDataOperationOutputDTO {

    /** Unique identifier of this operation within the document, or {@code null} if the API declares none. */
    private String operationId;

    /** Short, human-readable summary of what this operation does, or {@code null} if the API declares none. */
    private String summary;

    /** Longer, human-readable description of this operation, or {@code null} if the API declares none. */
    private String description;

    /**
     * Every path, query, header, and cookie parameter this operation accepts - the information
     * needed to actually invoke it. Empty when the operation declares no parameters at all.
     */
    private List<OpenDataParameterOutputDTO> parameters;
}

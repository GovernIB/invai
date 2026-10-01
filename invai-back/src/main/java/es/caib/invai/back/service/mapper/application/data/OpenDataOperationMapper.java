package es.caib.invai.back.service.mapper.application.data;

import es.caib.invai.back.interna.application.data.DTO.OpenDataOperationOutputDTO;
import es.caib.invai.back.interna.application.data.DTO.OpenDataParameterOutputDTO;
import es.caib.invai.back.rest.openapi.OpenApiOperation;
import es.caib.invai.back.rest.openapi.OpenApiParameter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper flattening the raw {@code rest.openapi} wire model (see {@link
 * es.caib.invai.back.rest.openapi.OpenApiClient}) into the outbound "Open Data" tab DTOs.
 *
 * @since 1.0.5
 */
@Mapper(componentModel = "spring")
public interface OpenDataOperationMapper {

    /**
     * Maps a parsed {@link OpenApiOperation} to its outbound {@link OpenDataOperationOutputDTO}.
     *
     * @param operation the parsed OpenAPI Operation Object to convert
     * @return the mapped outbound DTO
     */
    OpenDataOperationOutputDTO toResponse(OpenApiOperation operation);

    /**
     * Maps a parsed {@link OpenApiParameter} to its outbound {@link OpenDataParameterOutputDTO},
     * flattening its nested {@code schema} fields directly onto the output.
     *
     * @param parameter the parsed OpenAPI Parameter Object to convert
     * @return the mapped outbound DTO
     */
    @Mapping(target = "type", source = "schema.type")
    @Mapping(target = "format", source = "schema.format")
    @Mapping(target = "defaultValue", source = "schema.defaultValue")
    @Mapping(target = "enumValues", source = "schema.enumValues")
    OpenDataParameterOutputDTO toResponse(OpenApiParameter parameter);
}

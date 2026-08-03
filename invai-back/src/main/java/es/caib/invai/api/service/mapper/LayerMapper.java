package es.caib.invai.api.service.mapper;

import es.caib.invai.api.interna.maintenance.layer.DTO.LayerInputDTO;
import es.caib.invai.api.interna.maintenance.layer.DTO.LayerOutputDTO;
import es.caib.invai.api.persistence.model.LayerEntity;
import es.caib.invai.api.service.model.Layer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct data mapping abstraction interface providing structural state conversions across
 * Layer database entities, business domain models, and API transfer schemas.
 *
 * @since 1.0.2
 */
@Mapper(componentModel = "spring")
public interface LayerMapper {

    Layer toModel(LayerEntity entity);

    LayerEntity toEntity(Layer model);

    LayerOutputDTO toResponse(Layer model);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    Layer toModelFromInput(LayerInputDTO inputDTO);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateModelFromInput(LayerInputDTO inputDTO, @MappingTarget Layer model);

    default LayerOutputDTO map(Long value) {
        if (value == null) {
            return null;
        }
        LayerOutputDTO layer = new LayerOutputDTO();
        layer.setId(value);
        return layer;
    }
}

package es.caib.invai.back.service.mapper.maintenance.development.layer;

import es.caib.invai.back.interna.maintenance.development.layer.DTO.LayerInputDTO;
import es.caib.invai.back.interna.maintenance.development.layer.DTO.LayerOutputDTO;
import es.caib.invai.back.persistence.model.maintenance.development.layer.LayerEntity;
import es.caib.invai.back.service.model.maintenance.development.layer.Layer;
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

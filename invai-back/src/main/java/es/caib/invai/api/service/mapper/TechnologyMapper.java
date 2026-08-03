package es.caib.invai.api.service.mapper;

import es.caib.invai.api.interna.maintenance.technology.DTO.TechnologyInputDTO;
import es.caib.invai.api.interna.maintenance.technology.DTO.TechnologyOutputDTO;
import es.caib.invai.api.persistence.model.TechnologyEntity;
import es.caib.invai.api.service.model.Technology;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct data mapping abstraction interface providing structural state conversions across
 * Technology catalog database entities, business domain models, and API transfer schemas.
 *
 * @since 1.0.2
 */
@Mapper(componentModel = "spring", uses = {LayerMapper.class})
public interface TechnologyMapper {

    Technology toModel(TechnologyEntity entity);

    TechnologyEntity toEntity(Technology model);

    TechnologyOutputDTO toResponse(Technology model);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "layer.id", source = "layerId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    Technology toModelFromInput(TechnologyInputDTO inputDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "layer.id", source = "layerId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateModelFromInput(TechnologyInputDTO inputDTO, @MappingTarget Technology model);

    default TechnologyOutputDTO map(Long value) {
        if (value == null) {
            return null;
        }
        TechnologyOutputDTO technology = new TechnologyOutputDTO();
        technology.setId(value);
        return technology;
    }
}

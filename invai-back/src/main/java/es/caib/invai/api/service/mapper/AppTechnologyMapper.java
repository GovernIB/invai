package es.caib.invai.api.service.mapper;

import es.caib.invai.api.interna.application.development.technology.DTO.AppTechnologyInputDTO;
import es.caib.invai.api.interna.application.development.technology.DTO.AppTechnologyOutputDTO;
import es.caib.invai.api.persistence.model.AppTechnologyEntity;
import es.caib.invai.api.service.model.AppTechnology;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct data mapping abstraction interface providing structural state conversions across
 * Technology relation entities, business domain models, and API transfer schemas.
 *
 * @since 1.0.2
 */
@Mapper(componentModel = "spring", uses = {DevelopmentMapper.class, LayerMapper.class, TechnologyMapper.class})
public interface AppTechnologyMapper {

    /**
     * Materializes a persistent relation entity into a business domain aggregate.
     *
     * @param entity the persistent relation entity source node
     * @return a clean domain business layout graph
     */
    AppTechnology toModel(AppTechnologyEntity entity);

    /**
     * Maps business domain representations down to persistent relation database entities.
     *
     * @param model the active composite business domain model node
     * @return a mapped relational database entity layout
     */
    AppTechnologyEntity toEntity(AppTechnology model);

    /**
     * Flattens and structuralizes domain graphs into outbound API presentation response layers.
     *
     * @param model source domain business state schema instance
     * @return the outbound presentation API data carrier DTO
     */
    AppTechnologyOutputDTO toResponse(AppTechnology model);

    /**
     * Maps incoming flat reference fields into a decoupled domain state instance,
     * resolving the parent development relationship ID to its respective nested model ID,
     * while ignoring audit fields.
     *
     * @param inputDTO inbound client creation payload containing mapping configuration
     * @return a decoupled domain state instance ready for orchestration processing pipelines
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "appDevelopment.id", source = "appDevelopmentId")
    @Mapping(target = "layer.id", source = "layerId")
    @Mapping(target = "technology.id", source = "technologyId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    AppTechnology toModelFromInput(AppTechnologyInputDTO inputDTO);

    /**
     * Integrates update parameters from flat payload tracking definitions directly over an active
     * business entity, avoiding logical soft-delete and primary key attribute modifications.
     *
     * @param inputDTO delta parameter updates tracking values payload DTO
     * @param model    the active operational business graph container targeted for modifier updates
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "appDevelopment.id", source = "appDevelopmentId")
    @Mapping(target = "layer.id", source = "layerId")
    @Mapping(target = "technology.id", source = "technologyId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateModelFromInput(AppTechnologyInputDTO inputDTO, @MappingTarget AppTechnology model);
}

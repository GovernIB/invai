package es.caib.invai.back.service.mapper.application.development.technology;

import es.caib.invai.back.interna.application.development.technology.DTO.AppTechnologyInputDTO;
import es.caib.invai.back.interna.application.development.technology.DTO.AppTechnologyOutputDTO;
import es.caib.invai.back.persistence.model.application.development.technology.AppTechnologyEntity;
import es.caib.invai.back.service.model.application.development.technology.AppTechnology;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import es.caib.invai.back.service.mapper.application.development.core.AppDevelopmentMapper;
import es.caib.invai.back.service.mapper.maintenance.development.layer.LayerMapper;
import es.caib.invai.back.service.mapper.maintenance.development.technology.TechnologyMapper;

/**
 * MapStruct mapper converting between {@link AppTechnology} domain models, {@link AppTechnologyEntity}
 * persistence entities, and the technology input/output DTOs, resolving the parent development,
 * layer, and technology lookup references along the way.
 *
 * @since 1.0.2
 */
@Mapper(componentModel = "spring", uses = {AppDevelopmentMapper.class, LayerMapper.class, TechnologyMapper.class})
public interface AppTechnologyMapper {

    /**
     * Maps a persistence {@link AppTechnologyEntity} to its corresponding {@link AppTechnology} domain model.
     *
     * @param entity the JPA entity to convert
     * @return the mapped domain model
     */
    AppTechnology toModel(AppTechnologyEntity entity);

    /**
     * Maps an {@link AppTechnology} domain model to its corresponding {@link AppTechnologyEntity} persistence entity.
     *
     * @param model the domain model to convert
     * @return the mapped JPA entity
     */
    AppTechnologyEntity toEntity(AppTechnology model);

    /**
     * Maps an {@link AppTechnology} domain model to an outbound {@link AppTechnologyOutputDTO}.
     *
     * @param model the domain model to convert
     * @return the mapped outbound DTO
     */
    AppTechnologyOutputDTO toResponse(AppTechnology model);

    /**
     * Maps an inbound {@link AppTechnologyInputDTO} to a new {@link AppTechnology} domain model,
     * resolving the parent development, layer, and technology references, and leaving the id and
     * audit metadata fields unset.
     *
     * @param inputDTO the inbound DTO containing technology data
     * @return the mapped domain model
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
     * Applies the values of an inbound {@link AppTechnologyInputDTO} onto an existing {@link AppTechnology}
     * domain model in place, resolving the parent development, layer, and technology references,
     * and leaving the id and audit metadata fields untouched.
     *
     * @param inputDTO the inbound DTO containing updated technology data
     * @param model    the existing domain model to update
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

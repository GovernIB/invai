package es.caib.invai.back.service.mapper.maintenance.development.technology;

import es.caib.invai.back.interna.maintenance.development.technology.DTO.TechnologyInputDTO;
import es.caib.invai.back.interna.maintenance.development.technology.DTO.TechnologyOutputDTO;
import es.caib.invai.back.persistence.model.maintenance.development.technology.TechnologyEntity;
import es.caib.invai.back.service.model.maintenance.development.technology.Technology;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import es.caib.invai.back.service.mapper.maintenance.development.layer.LayerMapper;

/**
 * MapStruct data mapping abstraction interface providing structural state conversions across
 * Technology catalog database entities, business domain models, and API transfer schemas.
 *
 * @since 1.0.2
 */
@Mapper(componentModel = "spring", uses = {LayerMapper.class})
public interface TechnologyMapper {

    /**
     * Converts a persistence entity into a business domain model.
     *
     * @param entity the entity to convert
     * @return the corresponding domain model
     */
    Technology toModel(TechnologyEntity entity);

    /**
     * Converts a business domain model into a persistence entity.
     *
     * @param model the domain model to convert
     * @return the corresponding entity
     */
    TechnologyEntity toEntity(Technology model);

    /**
     * Converts a business domain model into an outbound API response DTO.
     *
     * @param model the domain model to convert
     * @return the corresponding output DTO
     */
    TechnologyOutputDTO toResponse(Technology model);

    /**
     * Converts an inbound API request DTO into a new business domain model, leaving
     * audit and identifier fields unset.
     *
     * @param inputDTO the request payload to convert
     * @return the corresponding new domain model
     */
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
    /**
     * Applies the changes from an inbound API request DTO onto an existing business domain model,
     * leaving audit and identifier fields untouched.
     *
     * @param inputDTO the request payload with the new values
     * @param model    the existing domain model to update in place
     */
    void updateModelFromInput(TechnologyInputDTO inputDTO, @MappingTarget Technology model);

    /**
     * Wraps a raw technology identifier into a minimal output DTO carrying only the ID.
     *
     * @param value the technology identifier, may be {@code null}
     * @return an output DTO with only the ID set, or {@code null} if the value is {@code null}
     */
    default TechnologyOutputDTO map(Long value) {
        if (value == null) {
            return null;
        }
        TechnologyOutputDTO technology = new TechnologyOutputDTO();
        technology.setId(value);
        return technology;
    }
}

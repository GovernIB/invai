package es.caib.invai.api.service.mapper;

import es.caib.invai.api.interna.maintenance.server.DTO.ServerInputDTO;
import es.caib.invai.api.interna.maintenance.server.DTO.ServerOutputDTO;
import es.caib.invai.api.persistence.model.ServerEntity;
import es.caib.invai.api.service.model.Server;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct data mapping abstraction interface providing structural state conversions across
 * Server database entities, business domain models, and API transfer schemas.
 *
 * @since 1.0.2
 */
@Mapper(componentModel = "spring", uses = {EnvironmentMapper.class, ServerTypeMapper.class})
public interface ServerMapper {

    /**
     * Converts a database relational layer entity into a plain business domain model.
     */
    Server toModel(ServerEntity entity);

    /**
     * Converts a domain business layer model into a relational database entity representation.
     */
    ServerEntity toEntity(Server model);

    /**
     * Converts a domain model configuration into an outbound presentation layer REST DTO.
     */
    ServerOutputDTO toResponse(Server model);

    /**
     * Constructs a pure domain business structure from incoming input payload parameter DTOs.
     */
    @Mapping(target = "environment.id", source = "environmentId")
    @Mapping(target = "serverType.id", source = "serverTypeId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    Server toModelFromInput(ServerInputDTO inputDTO);

    /**
     * Merges update parameters from an API input payload DTO into an existing active domain business model.
     */
    @Mapping(target = "environment.id", source = "environmentId")
    @Mapping(target = "serverType.id", source = "serverTypeId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateModelFromInput(ServerInputDTO inputDTO, @MappingTarget Server model);
}

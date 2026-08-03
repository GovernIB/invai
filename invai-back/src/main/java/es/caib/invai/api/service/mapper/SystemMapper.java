package es.caib.invai.api.service.mapper;

import es.caib.invai.api.interna.maintenance.system.DTO.SystemInputDTO;
import es.caib.invai.api.interna.maintenance.system.DTO.SystemOutputDTO;
import es.caib.invai.api.persistence.model.SystemEntity;
import es.caib.invai.api.service.model.System;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct data mapping abstraction interface providing structural state conversions across
 * Enterprise Systems database entities, business domain models, and API transfer schemas.
 *
 * @since 1.0.2
 */
@Mapper(componentModel = "spring", uses = {ServerMapper.class})
public interface SystemMapper {

    /**
     * Converts a database relational layer entity into a plain business domain model.
     *
     * @param entity the source persistent database state representation
     * @return the mapped business domain model instance
     */
    System toModel(SystemEntity entity);

    /**
     * Converts a domain business layer model into a relational database entity representation.
     *
     * @param model the source domain data state profile
     * @return the ready-to-persist relational database entity
     */
    SystemEntity toEntity(System model);

    /**
     * Converts a domain model configuration into an outbound presentation layer REST DTO.
     *
     * @param model the source domain layer data model
     * @return the outbound presentation API data carrier DTO
     */
    SystemOutputDTO toResponse(System model);

    /**
     * Constructs a pure domain business structure from incoming input payload parameter DTOs,
     * forcing logical deactivation timelines to safely bypass user-land overwrites.
     *
     * @param inputDTO the inbound presentation payload containing parameters
     * @return a clean business domain instance with isolated metadata parameters
     */
    @Mapping(target = "server.id", source = "serverId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    System toModelFromInput(SystemInputDTO inputDTO);

    /**
     * Merges update parameters from an API input payload DTO into an existing active
     * domain business model, preserving its logical deactivation tracking signatures.
     *
     * @param inputDTO incoming operational variables delta payload
     * @param model    the active target business domain model instance to update inline
     */
    @Mapping(target = "server.id", source = "serverId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateModelFromInput(SystemInputDTO inputDTO, @MappingTarget System model);

    /**
     * Convenience reference lookup utility initializing a detached shallow domain reference.
     */
    default SystemOutputDTO map(Long value) {
        if (value == null) {
            return null;
        }
        SystemOutputDTO system = new SystemOutputDTO();
        system.setId(value);
        return system;
    }

    /**
     * Direct pass-through mapping helper. Resolves deep-cloning constraints
     * when MapStruct encounters identical 'System' types on both source and target.
     *
     * @param value the active domain system instance
     * @return the identical instance mapped directly
     */
    default System map(System value) {
        return value;
    }
}
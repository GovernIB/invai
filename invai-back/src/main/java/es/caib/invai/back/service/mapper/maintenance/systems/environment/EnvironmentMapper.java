package es.caib.invai.back.service.mapper.maintenance.systems.environment;

import es.caib.invai.back.interna.maintenance.systems.environment.DTO.EnvironmentInputDTO;
import es.caib.invai.back.interna.maintenance.systems.environment.DTO.EnvironmentOutputDTO;
import es.caib.invai.back.persistence.model.maintenance.systems.environment.EnvironmentEntity;
import es.caib.invai.back.service.model.maintenance.systems.environment.Environment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct data mapping abstraction interface providing structural state conversions across
 * database entities, business domain models, and API transfer schemas.
 *
 * @since 1.0.2
 */
@Mapper(componentModel = "spring")
public interface EnvironmentMapper {

    /**
     * Converts a database relational layer entity into a plain business domain model.
     */
    Environment toModel(EnvironmentEntity entity);

    /**
     * Converts a domain business layer model into a relational database entity representation.
     */
    EnvironmentEntity toEntity(Environment model);

    /**
     * Converts a domain model configuration into an outbound presentation layer REST DTO.
     */
    EnvironmentOutputDTO toResponse(Environment model);

    /**
     * Constructs a pure domain business structure from incoming input payload parameter DTOs.
     */
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    Environment toModelFromInput(EnvironmentInputDTO inputDTO);

    /**
     * Merges update parameters from an API input payload DTO into an existing active domain business model.
     */
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateModelFromInput(EnvironmentInputDTO inputDTO, @MappingTarget Environment model);

    /**
     * Convenience reference lookup utility initializing a detached shallow domain reference.
     */
    default EnvironmentOutputDTO map(Long value) {
        if (value == null) {
            return null;
        }
        EnvironmentOutputDTO dto = new EnvironmentOutputDTO();
        dto.setId(value);
        return dto;
    }

    /**
     * Direct pass-through mapping helper to avoid recursion or duplication conflicts.
     */
    default Environment map(Environment value) {
        return value;
    }
}
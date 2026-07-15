package es.caib.invai.api.service.mapper;

import es.caib.invai.api.interna.systemType.DTO.SystemTypeInputDTO;
import es.caib.invai.api.interna.systemType.DTO.SystemTypeOutputDTO;
import es.caib.invai.api.persistence.model.SystemTypeEntity;
import es.caib.invai.api.service.model.SystemType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct data mapping abstraction interface providing structural state conversions across
 * System Architecture Classification database entities, business domain models, and API transfer schemas.
 *
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface SystemTypeMapper {

    /**
     * Converts a database relational layer entity into a plain business domain model.
     *
     * @param entity the source persistent database state representation
     * @return the mapped business domain model instance
     */
    SystemType toModel(SystemTypeEntity entity);

    /**
     * Converts a domain business layer model into a relational database entity representation.
     *
     * @param model the source domain data state profile
     * @return the ready-to-persist relational database entity
     */
    SystemTypeEntity toEntity(SystemType model);

    /**
     * Converts a domain model configuration into an outbound presentation layer REST DTO.
     *
     * @param model the source domain layer data model
     * @return the outbound presentation API data carrier DTO
     */
    SystemTypeOutputDTO toResponse(SystemType model);

    /**
     * Constructs a pure domain business structure from incoming input payload parameter DTOs,
     * forcing logical deactivation timelines to safely bypass user-land overwrites.
     *
     * @param inputDTO the inbound presentation payload containing parameters
     * @return a clean business domain instance with isolated metadata parameters
     */
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    SystemType toModelFromInput(SystemTypeInputDTO inputDTO);

    /**
     * Merges update parameters from an API input payload DTO into an existing active
     * domain business model, preserving its logical deactivation tracking signatures.
     *
     * @param inputDTO incoming operational variables delta payload
     * @param model    the active target business domain model instance to update inline
     */
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateModelFromInput(SystemTypeInputDTO inputDTO, @MappingTarget SystemType model);

    /**
     * Convenience reference lookup utility initializing a detached shallow domain reference
     * using a target primary index key sequence.
     *
     * @param value primary tracking index key reference identity
     * @return a stub domain model tracking the target index, or {@code null} if input is null
     */
    default SystemType map(Long value) {
        if (value == null) {
            return null;
        }
        SystemType systemType = new SystemType();
        systemType.setId(value);
        return systemType;
    }
}
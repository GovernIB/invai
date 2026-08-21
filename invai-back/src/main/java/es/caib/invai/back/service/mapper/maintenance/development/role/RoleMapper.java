package es.caib.invai.back.service.mapper.maintenance.development.role;

import es.caib.invai.back.interna.maintenance.development.role.DTO.RoleInputDTO;
import es.caib.invai.back.interna.maintenance.development.role.DTO.RoleOutputDTO;
import es.caib.invai.back.persistence.model.maintenance.development.role.RoleEntity;
import es.caib.invai.back.service.model.maintenance.development.role.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct data mapping abstraction interface providing structural state conversions across
 * Role database entities, business domain models, and API transfer schemas.
 *
 * @since 1.0.2
 */
@Mapper(componentModel = "spring")
public interface RoleMapper {

    Role toModel(RoleEntity entity);

    RoleEntity toEntity(Role model);

    RoleOutputDTO toResponse(Role model);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    Role toModelFromInput(RoleInputDTO inputDTO);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateModelFromInput(RoleInputDTO inputDTO, @MappingTarget Role model);

    /**
     * Convenience reference lookup utility initializing a detached shallow domain reference
     * using a target primary index key sequence.
     *
     * @param value primary tracking index key reference identity
     * @return a stub domain model tracking the target index, or {@code null} if input is null
     */
    default RoleOutputDTO map(Long value) {
        if (value == null) {
            return null;
        }
        RoleOutputDTO role = new RoleOutputDTO();
        role.setId(value);
        return role;
    }
}

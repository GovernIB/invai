package es.caib.invai.back.service.mapper.maintenance.development.providerRole;

import es.caib.invai.back.interna.maintenance.development.providerRole.DTO.ProviderRoleInputDTO;
import es.caib.invai.back.interna.maintenance.development.providerRole.DTO.ProviderRoleOutputDTO;
import es.caib.invai.back.persistence.model.maintenance.development.providerRole.ProviderRoleEntity;
import es.caib.invai.back.service.model.maintenance.development.providerRole.ProviderRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Objects;

/**
 * MapStruct data mapping abstraction interface providing structural state conversions across
 * Role database entities, business domain models, and API transfer schemas.
 *
 * @since 1.0.2
 */
@Mapper(componentModel = "spring")
public interface ProviderRoleMapper {

    ProviderRole toModel(ProviderRoleEntity entity);

    ProviderRoleEntity toEntity(ProviderRole model);

    ProviderRoleOutputDTO toResponse(ProviderRole model);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    ProviderRole toModelFromInput(ProviderRoleInputDTO inputDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateModelFromInput(ProviderRoleInputDTO inputDTO, @MappingTarget ProviderRole model);

    /**
     * Convenience reference lookup utility initializing a detached shallow domain reference
     * using a target primary index key sequence.
     *
     * @param value primary tracking index key reference identity
     * @return a stub domain model tracking the target index, or {@code null} if input is null
     */
    default ProviderRoleOutputDTO map(Long value) {
        if (Objects.isNull(value)) {
            return null;
        }
        ProviderRoleOutputDTO role = new ProviderRoleOutputDTO();
        role.setId(value);
        return role;
    }
}

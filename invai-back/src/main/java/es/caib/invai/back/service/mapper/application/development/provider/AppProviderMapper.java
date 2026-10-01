package es.caib.invai.back.service.mapper.application.development.provider;

import es.caib.invai.back.interna.application.development.provider.DTO.AppProviderInputDTO;
import es.caib.invai.back.interna.application.development.provider.DTO.AppProviderOutputDTO;
import es.caib.invai.back.persistence.model.application.development.provider.AppProviderEntity;
import es.caib.invai.back.service.model.application.development.provider.AppProvider;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import es.caib.invai.back.service.mapper.application.development.core.AppDevelopmentMapper;
import es.caib.invai.back.service.mapper.maintenance.development.role.RoleMapper;

/**
 * MapStruct mapper converting between {@link AppProvider} domain models, {@link AppProviderEntity}
 * persistence entities, and the provider input/output DTOs, resolving the parent development and
 * role lookup references along the way.
 *
 * @since 1.0.2
 */
@Mapper(componentModel = "spring", uses = {AppDevelopmentMapper.class, RoleMapper.class})
public interface AppProviderMapper {

    /**
     * Maps a persistence {@link AppProviderEntity} to its corresponding {@link AppProvider} domain model.
     *
     * @param entity the JPA entity to convert
     * @return the mapped domain model
     */
    AppProvider toModel(AppProviderEntity entity);

    /**
     * Maps an {@link AppProvider} domain model to its corresponding {@link AppProviderEntity} persistence entity.
     *
     * @param model the domain model to convert
     * @return the mapped JPA entity
     */
    AppProviderEntity toEntity(AppProvider model);

    /**
     * Maps an {@link AppProvider} domain model to an outbound {@link AppProviderOutputDTO}.
     *
     * @param model the domain model to convert
     * @return the mapped outbound DTO
     */
    AppProviderOutputDTO toResponse(AppProvider model);

    /**
     * Maps an inbound {@link AppProviderInputDTO} to a new {@link AppProvider} domain model,
     * resolving the parent development reference, and leaving the id and audit metadata fields unset.
     * See {@link #nullifyRoleWhenIdMissing} for how the optional role reference is handled.
     *
     * @param inputDTO the inbound DTO containing provider data
     * @return the mapped domain model
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "appDevelopment.id", source = "appDevelopmentId")
    @Mapping(target = "role.id", source = "roleId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    AppProvider toModelFromInput(AppProviderInputDTO inputDTO);

    /**
     * Applies the values of an inbound {@link AppProviderInputDTO} onto an existing {@link AppProvider}
     * domain model in place, resolving the parent development reference, and leaving the id and
     * audit metadata fields untouched. See {@link #nullifyRoleWhenIdMissing} for how the optional
     * role reference is handled.
     *
     * @param inputDTO the inbound DTO containing updated provider data
     * @param model    the existing domain model to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "appDevelopment.id", source = "appDevelopmentId")
    @Mapping(target = "role.id", source = "roleId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateModelFromInput(AppProviderInputDTO inputDTO, @MappingTarget AppProvider model);

    /**
     * MapStruct's generated {@code role.id ← roleId} mapping always builds a nested
     * {@code Role} instance, even when {@code roleId} is null (yielding {@code Role{id=null}}
     * instead of a null reference). Since {@code roleId} is optional, force the reference back
     * to {@code null} whenever no role was submitted, so a transient/unsaved reference is never
     * handed to JPA.
     *
     * @param inputDTO inbound payload carrying the optional role identifier
     * @param model    the domain model instance being populated by this mapping round
     */
    @AfterMapping
    default void nullifyRoleWhenIdMissing(AppProviderInputDTO inputDTO, @MappingTarget AppProvider model) {
        if (inputDTO.getRoleId() == null) {
            model.setRole(null);
        }
    }
}

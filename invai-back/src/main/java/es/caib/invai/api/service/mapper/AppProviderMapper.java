package es.caib.invai.api.service.mapper;

import es.caib.invai.api.interna.application.development.provider.DTO.AppProviderInputDTO;
import es.caib.invai.api.interna.application.development.provider.DTO.AppProviderOutputDTO;
import es.caib.invai.api.persistence.model.AppProviderEntity;
import es.caib.invai.api.service.model.AppProvider;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct data mapping abstraction interface providing structural state conversions across
 * Provider relation entities, business domain models, and API transfer schemas.
 *
 * @since 1.0.2
 */
@Mapper(componentModel = "spring", uses = {DevelopmentMapper.class, RoleMapper.class})
public interface AppProviderMapper {

    /**
     * Materializes a persistent relation entity into a business domain aggregate.
     *
     * @param entity the persistent relation entity source node
     * @return a clean domain business layout graph
     */
    AppProvider toModel(AppProviderEntity entity);

    /**
     * Maps business domain representations down to persistent relation database entities.
     *
     * @param model the active composite business domain model node
     * @return a mapped relational database entity layout
     */
    AppProviderEntity toEntity(AppProvider model);

    /**
     * Flattens and structuralizes domain graphs into outbound API presentation response layers.
     *
     * @param model source domain business state schema instance
     * @return the outbound presentation API data carrier DTO
     */
    AppProviderOutputDTO toResponse(AppProvider model);

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
    @Mapping(target = "role.id", source = "roleId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    AppProvider toModelFromInput(AppProviderInputDTO inputDTO);

    /**
     * Integrates update parameters from flat payload tracking definitions directly over an active
     * business entity, avoiding logical soft-delete and primary key attribute modifications.
     *
     * @param inputDTO delta parameter updates tracking values payload DTO
     * @param model    the active operational business graph container targeted for modifier updates
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

package es.caib.invai.back.service.mapper.application.security.role;

import es.caib.invai.back.interna.application.security.role.DTO.AppRoleInputDTO;
import es.caib.invai.back.interna.application.security.role.DTO.AppRoleOutputDTO;
import es.caib.invai.back.persistence.model.application.security.role.AppRoleEntity;
import es.caib.invai.back.service.model.application.security.role.AppRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import es.caib.invai.back.service.mapper.application.security.core.AppSecurityMapper;
import es.caib.invai.back.service.mapper.catalog.validationStatus.ValidationStatusMapper;
import es.caib.invai.back.service.mapper.maintenance.security.securityRole.SecurityRoleMapper;

/**
 * MapStruct data mapping abstraction interface providing structural state conversions across
 * AppRole relation entities, business domain models, and API transfer schemas.
 *
 * @since 1.0.4
 */
@Mapper(componentModel = "spring", uses = {AppSecurityMapper.class, SecurityRoleMapper.class, ValidationStatusMapper.class})
public interface AppRoleMapper {

    /**
     * Materializes a persistent relation entity into a business domain aggregate.
     *
     * @param entity the persistent relation entity source node
     * @return a clean domain business layout graph
     */
    AppRole toModel(AppRoleEntity entity);

    /**
     * Maps business domain representations down to persistent relation database entities.
     *
     * @param model the active composite business domain model node
     * @return a mapped relational database entity layout
     */
    @Mapping(target = "validationStatus", source = "validationStatus", qualifiedByName = "mapValidationStatusEnumToEntity")
    AppRoleEntity toEntity(AppRole model);

    /**
     * Flattens and structuralizes domain graphs into outbound API presentation response layers.
     *
     * @param model source domain business state schema instance
     * @return the outbound presentation API data carrier DTO
     */
    AppRoleOutputDTO toResponse(AppRole model);

    /**
     * Maps incoming flat reference fields into a decoupled domain state instance,
     * resolving the security anchor ID to its nested model ID and ignoring audit fields. The security
     * role is not mapped: the payload carries Soffid's role id, which the caller resolves to the local
     * copy of the role. Nor are the validation fields: they are only ever set by the facade.
     *
     * @param inputDTO inbound client creation payload containing mapping configuration
     * @return a decoupled domain state instance ready for orchestration processing pipelines
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "appSecurity.id", source = "appSecurityId")
    @Mapping(target = "securityRole", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "validationStatus", ignore = true)
    @Mapping(target = "manualValidatedAt", ignore = true)
    @Mapping(target = "manualValidatedBy", ignore = true)
    AppRole toModelFromInput(AppRoleInputDTO inputDTO);

    /**
     * Integrates update parameters from flat payload tracking definitions directly over an active
     * business entity, avoiding logical soft-delete and primary key attribute modifications.
     *
     * @param inputDTO delta parameter updates tracking values payload DTO
     * @param model    the active operational business graph container targeted for modifier updates
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "appSecurity.id", source = "appSecurityId")
    @Mapping(target = "securityRole", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "validationStatus", ignore = true)
    @Mapping(target = "manualValidatedAt", ignore = true)
    @Mapping(target = "manualValidatedBy", ignore = true)
    void updateModelFromInput(AppRoleInputDTO inputDTO, @MappingTarget AppRole model);
}

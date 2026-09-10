package es.caib.invai.back.service.mapper.application.security.risk;

import es.caib.invai.back.interna.application.security.risk.DTO.AppSecurityRiskInputDTO;
import es.caib.invai.back.interna.application.security.risk.DTO.AppSecurityRiskOutputDTO;
import es.caib.invai.back.persistence.model.application.security.risk.AppSecurityRiskEntity;
import es.caib.invai.back.service.model.application.security.risk.AppSecurityRisk;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import es.caib.invai.back.service.mapper.application.security.core.AppSecurityMapper;
import es.caib.invai.back.service.mapper.catalog.securityLevel.SecurityLevelMapper;
import es.caib.invai.back.service.mapper.maintenance.general.field.FieldMapper;

/**
 * MapStruct data mapping abstraction interface providing structural state conversions across
 * AppSecurityRisk relation entities, business domain models, and API transfer schemas.
 *
 * @since 1.0.4
 */
@Mapper(componentModel = "spring", uses = {AppSecurityMapper.class, SecurityLevelMapper.class, FieldMapper.class})
public interface AppSecurityRiskMapper {

    /**
     * Materializes a persistent relation entity into a business domain aggregate.
     *
     * @param entity the persistent relation entity source node
     * @return a clean domain business layout graph
     */
    AppSecurityRisk toModel(AppSecurityRiskEntity entity);

    /**
     * Maps business domain representations down to persistent relation database entities.
     *
     * @param model the active composite business domain model node
     * @return a mapped relational database entity layout
     */
    AppSecurityRiskEntity toEntity(AppSecurityRisk model);

    /**
     * Flattens and structuralizes domain graphs into outbound API presentation response layers.
     *
     * @param model source domain business state schema instance
     * @return the outbound presentation API data carrier DTO
     */
    AppSecurityRiskOutputDTO toResponse(AppSecurityRisk model);

    /**
     * Maps incoming flat reference fields into a decoupled domain state instance,
     * resolving relationship IDs (security anchor, security level, field) to their respective
     * nested model IDs, while ignoring audit fields.
     *
     * @param inputDTO inbound client creation payload containing mapping configuration
     * @return a decoupled domain state instance ready for orchestration processing pipelines
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "appSecurity.id", source = "appSecurityId")
    @Mapping(target = "level.id", source = "levelId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "field.id", source = "fieldId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    AppSecurityRisk toModelFromInput(AppSecurityRiskInputDTO inputDTO);

    /**
     * Integrates update parameters from flat payload tracking definitions directly over an active
     * business entity, avoiding logical soft-delete and primary key attribute modifications.
     *
     * @param inputDTO delta parameter updates tracking values payload DTO
     * @param model    the active operational business graph container targeted for modifier updates
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "appSecurity.id", source = "appSecurityId")
    @Mapping(target = "level.id", source = "levelId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "field.id", source = "fieldId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateModelFromInput(AppSecurityRiskInputDTO inputDTO, @MappingTarget AppSecurityRisk model);
}

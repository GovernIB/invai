package es.caib.invai.back.service.mapper.application.security.measure;

import es.caib.invai.back.interna.application.security.measure.DTO.AppSecurityMeasureInputDTO;
import es.caib.invai.back.interna.application.security.measure.DTO.AppSecurityMeasureOutputDTO;
import es.caib.invai.back.persistence.model.application.security.measure.AppSecurityMeasureEntity;
import es.caib.invai.back.service.model.application.security.measure.AppSecurityMeasure;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import es.caib.invai.back.service.mapper.application.security.core.AppSecurityMapper;
import es.caib.invai.back.service.mapper.maintenance.security.securityMeasureType.SecurityMeasureTypeMapper;
import es.caib.invai.back.service.mapper.maintenance.security.ensRequirement.EnsRequirementMapper;

/**
 * MapStruct data mapping abstraction interface providing structural state conversions across
 * AppSecurityMeasure relation entities, business domain models, and API transfer schemas.
 *
 * @since 1.0.4
 */
@Mapper(componentModel = "spring", uses = {AppSecurityMapper.class, SecurityMeasureTypeMapper.class, EnsRequirementMapper.class})
public interface AppSecurityMeasureMapper {

    /**
     * Materializes a persistent relation entity into a business domain aggregate.
     *
     * @param entity the persistent relation entity source node
     * @return a clean domain business layout graph
     */
    AppSecurityMeasure toModel(AppSecurityMeasureEntity entity);

    /**
     * Maps business domain representations down to persistent relation database entities.
     *
     * @param model the active composite business domain model node
     * @return a mapped relational database entity layout
     */
    AppSecurityMeasureEntity toEntity(AppSecurityMeasure model);

    /**
     * Flattens and structuralizes domain graphs into outbound API presentation response layers.
     *
     * @param model source domain business state schema instance
     * @return the outbound presentation API data carrier DTO
     */
    AppSecurityMeasureOutputDTO toResponse(AppSecurityMeasure model);

    /**
     * Maps incoming flat reference fields into a decoupled domain state instance,
     * resolving relationship IDs (security anchor, measure type, ENS requirement) to their
     * respective nested model IDs, while ignoring audit fields.
     *
     * @param inputDTO inbound client creation payload containing mapping configuration
     * @return a decoupled domain state instance ready for orchestration processing pipelines
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "appSecurity.id", source = "appSecurityId")
    @Mapping(target = "type.id", source = "typeId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "ensRequirement.id", source = "ensRequirementId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    AppSecurityMeasure toModelFromInput(AppSecurityMeasureInputDTO inputDTO);

    /**
     * Integrates update parameters from flat payload tracking definitions directly over an active
     * business entity, avoiding logical soft-delete and primary key attribute modifications.
     *
     * @param inputDTO delta parameter updates tracking values payload DTO
     * @param model    the active operational business graph container targeted for modifier updates
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "appSecurity.id", source = "appSecurityId")
    @Mapping(target = "type.id", source = "typeId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "ensRequirement.id", source = "ensRequirementId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateModelFromInput(AppSecurityMeasureInputDTO inputDTO, @MappingTarget AppSecurityMeasure model);
}

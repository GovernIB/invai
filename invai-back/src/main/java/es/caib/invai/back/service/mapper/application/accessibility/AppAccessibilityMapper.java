package es.caib.invai.back.service.mapper.application.accessibility;

import es.caib.invai.back.interna.application.accessibility.DTO.AppAccessibilityInputDTO;
import es.caib.invai.back.interna.application.accessibility.DTO.AppAccessibilityOutputDTO;
import es.caib.invai.back.persistence.model.application.accessibility.AppAccessibilityEntity;
import es.caib.invai.back.service.model.application.accessibility.AppAccessibility;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import es.caib.invai.back.service.mapper.application.core.ApplicationMapper;
import es.caib.invai.back.service.mapper.maintenance.complianceSituation.ComplianceSituationMapper;
import es.caib.invai.back.service.mapper.maintenance.classificationSegment.ClassificationSegmentMapper;

/**
 * MapStruct mapper converting between {@link AppAccessibility} domain models, {@link
 * AppAccessibilityEntity} persistence entities, and the inbound/outbound AppAccessibility DTOs,
 * resolving the application, compliance situation, and classification segment references along
 * the way.
 *
 * @since 1.0.4
 */
@Mapper(componentModel = "spring", uses = {ApplicationMapper.class, ComplianceSituationMapper.class, ClassificationSegmentMapper.class})
public interface AppAccessibilityMapper {

    /**
     * Maps a persistence {@link AppAccessibilityEntity} to its corresponding {@link AppAccessibility} domain model.
     *
     * @param entity the JPA entity to convert
     * @return the mapped domain model
     */
    AppAccessibility toModel(AppAccessibilityEntity entity);

    /**
     * Maps an {@link AppAccessibility} domain model to its corresponding {@link AppAccessibilityEntity} persistence entity.
     *
     * @param model the domain model to convert
     * @return the mapped JPA entity
     */
    AppAccessibilityEntity toEntity(AppAccessibility model);

    /**
     * Maps an {@link AppAccessibility} domain model to its outbound {@link AppAccessibilityOutputDTO}.
     *
     * @param model the domain model to convert
     * @return the mapped outbound DTO
     */
    AppAccessibilityOutputDTO toResponse(AppAccessibility model);

    /**
     * Maps an inbound {@link AppAccessibilityInputDTO} to a new {@link AppAccessibility} domain
     * model, resolving {@code applicationId}/{@code complianceId}/{@code classificationSegmentId}
     * to their nested reference IDs and leaving audit metadata fields unset.
     *
     * @param inputDTO the inbound DTO containing accessibility anchor data
     * @return the mapped domain model
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "application.id", source = "applicationId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "compliance.id", source = "complianceId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "classificationSegment.id", source = "classificationSegmentId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    AppAccessibility toModelFromInput(AppAccessibilityInputDTO inputDTO);

    /**
     * Applies the values of an inbound {@link AppAccessibilityInputDTO} onto an existing {@link
     * AppAccessibility} domain model in place, leaving audit metadata fields untouched. Because the
     * {@code applicationId}/{@code complianceId}/{@code classificationSegmentId} mappings use
     * {@link NullValuePropertyMappingStrategy#IGNORE}, sending {@code null} for one of these on an
     * update leaves the existing reference on {@code model} untouched rather than clearing it —
     * there is no way to unlink an already-set compliance/classification/application through this method.
     *
     * @param inputDTO the inbound DTO containing updated accessibility anchor data
     * @param model    the existing domain model to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "application.id", source = "applicationId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "compliance.id", source = "complianceId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "classificationSegment.id", source = "classificationSegmentId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateModelFromInput(AppAccessibilityInputDTO inputDTO, @MappingTarget AppAccessibility model);
}

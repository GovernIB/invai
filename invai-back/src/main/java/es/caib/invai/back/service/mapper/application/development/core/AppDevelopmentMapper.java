package es.caib.invai.back.service.mapper.application.development.core;

import es.caib.invai.back.interna.application.development.core.DTO.DevelopmentInputDTO;
import es.caib.invai.back.interna.application.development.core.DTO.DevelopmentOutputDTO;
import es.caib.invai.back.persistence.model.application.development.core.AppDevelopmentEntity;
import es.caib.invai.back.service.model.application.development.core.AppDevelopment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import es.caib.invai.back.service.mapper.catalog.modality.ModalityMapper;
import es.caib.invai.back.service.mapper.catalog.standardAdaption.StandardAdaptionMapper;
import es.caib.invai.back.service.mapper.catalog.status.StatusMapper;

/**
 * MapStruct data mapping abstraction interface providing structural state conversions across
 * Development relation entities, business domain models, and API transfer schemas.
 *
 * @since 1.0.2
 */
@Mapper(componentModel = "spring", uses = {ModalityMapper.class, StandardAdaptionMapper.class, StatusMapper.class})
public interface AppDevelopmentMapper {

    /**
     * Materializes a persistent relation entity into a business domain aggregate.
     *
     * @param entity the persistent relation entity source node
     * @return a clean domain business layout graph
     */
    AppDevelopment toModel(AppDevelopmentEntity entity);

    /**
     * Maps business domain representations down to persistent relation database entities.
     *
     * @param model the active composite business domain model node
     * @return a mapped relational database entity layout
     */
    AppDevelopmentEntity toEntity(AppDevelopment model);

    /**
     * Flattens and structuralizes domain graphs into outbound API presentation response layers.
     *
     * @param model source domain business state schema instance
     * @return the outbound presentation API data carrier DTO
     */
    DevelopmentOutputDTO toResponse(AppDevelopment model);

    /**
     * Maps incoming flat reference fields into a decoupled domain state instance,
     * resolving relationship IDs (application, environment, modality, standard adaption) to their
     * respective nested model IDs, while ignoring audit fields.
     *
     * @param inputDTO inbound client creation payload containing mapping configuration
     * @return a decoupled domain state instance ready for orchestration processing pipelines
     */
    @Mapping(target = "application.id", source = "applicationId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "environment.id", source = "environmentId")
    @Mapping(target = "modality", source = "modalityId")
    @Mapping(target = "standardAdaption", source = "standardAdaptionId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    AppDevelopment toModelFromInput(DevelopmentInputDTO inputDTO);

    /**
     * Integrates update parameters from flat payload tracking definitions directly over an active
     * business entity, avoiding logical soft-delete and primary key attribute modifications.
     *
     * @param inputDTO delta parameter updates tracking values payload DTO
     * @param model    the active operational business graph container targeted for modifier updates
     */
    @Mapping(target = "application.id", source = "applicationId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "environment.id", source = "environmentId")
    @Mapping(target = "modality", source = "modalityId")
    @Mapping(target = "standardAdaption", source = "standardAdaptionId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateModelFromInput(DevelopmentInputDTO inputDTO, @MappingTarget AppDevelopment model);
}

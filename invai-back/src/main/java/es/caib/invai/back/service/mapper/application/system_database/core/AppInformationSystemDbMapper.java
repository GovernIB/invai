package es.caib.invai.back.service.mapper.application.system_database.core;

import es.caib.invai.back.interna.application.system_database.core.DTO.AppInformationSystemDbInputDTO;
import es.caib.invai.back.interna.application.system_database.core.DTO.AppInformationSystemDbOutputDTO;
import es.caib.invai.back.persistence.model.application.system_database.core.AppInformationSystemDbEntity;
import es.caib.invai.back.service.model.application.system_database.core.AppInformationSystemDb;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import es.caib.invai.back.service.mapper.application.core.ApplicationMapper;
import es.caib.invai.back.service.mapper.catalog.status.StatusMapper;

/**
 * MapStruct data mapping abstraction interface providing structural state conversions across
 * AppInformationSystemDb relation entities, business domain models, and API transfer schemas.
 *
 * @since 1.0.2
 */
@Mapper(componentModel = "spring", uses = {StatusMapper.class, ApplicationMapper.class})
public interface AppInformationSystemDbMapper {

    /**
     * Materializes a persistent relation entity into a business domain aggregate.
     *
     * @param entity the persistent relation entity source node
     * @return a clean domain business layout graph
     */
    AppInformationSystemDb toModel(AppInformationSystemDbEntity entity);

    /**
     * Maps business domain representations down to persistent relation database entities.
     *
     * @param model the active composite business domain model node
     * @return a mapped relational database entity layout
     */
    AppInformationSystemDbEntity toEntity(AppInformationSystemDb model);

    /**
     * Flattens and structuralizes domain graphs into outbound API presentation response layers.
     *
     * @param model source domain business state schema instance
     * @return the outbound presentation API data carrier DTO
     */
    AppInformationSystemDbOutputDTO toResponse(AppInformationSystemDb model);

    /**
     * Maps incoming flat reference fields into a decoupled domain state instance,
     * resolving the application relationship ID to its respective nested model ID,
     * while ignoring audit fields.
     *
     * @param inputDTO inbound client creation payload containing mapping configuration
     * @return a decoupled domain state instance ready for orchestration processing pipelines
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "application.id", source = "applicationId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    AppInformationSystemDb toModelFromInput(AppInformationSystemDbInputDTO inputDTO);

    /**
     * Integrates update parameters from flat payload tracking definitions directly over an active
     * business entity, avoiding logical soft-delete and primary key attribute modifications.
     *
     * @param inputDTO delta parameter updates tracking values payload DTO
     * @param model    the active operational business graph container targeted for modifier updates
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "application.id", source = "applicationId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateModelFromInput(AppInformationSystemDbInputDTO inputDTO, @MappingTarget AppInformationSystemDb model);
}

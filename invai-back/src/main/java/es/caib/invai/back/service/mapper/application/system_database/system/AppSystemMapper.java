package es.caib.invai.back.service.mapper.application.system_database.system;

import es.caib.invai.back.interna.application.system_database.system.DTO.AppSystemInputDTO;
import es.caib.invai.back.interna.application.system_database.system.DTO.AppSystemOutputDTO;
import es.caib.invai.back.persistence.model.application.system_database.system.AppSystemEntity;
import es.caib.invai.back.service.model.application.system_database.system.AppSystem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import es.caib.invai.back.service.mapper.application.core.ApplicationMapper;
import es.caib.invai.back.service.mapper.catalog.status.StatusMapper;

/**
 * MapStruct data mapping abstraction interface providing structural state conversions across
 * AppSystem relation entities, business domain models, and API transfer schemas.
 *
 * @since 1.0.2
 */
@Mapper(
        componentModel = "spring", uses = {StatusMapper.class, ApplicationMapper.class})
public interface AppSystemMapper {

    /**
     * Materializes a persistent relation entity into a business domain aggregate.
     *
     * @param entity the persistent relation entity source node
     * @return a clean domain business layout graph
     */
    AppSystem toModel(AppSystemEntity entity);

    /**
     * Maps business domain representations down to persistent relation database entities.
     *
     * @param model the active composite business domain model node
     * @return a mapped relational database entity layout
     */
    AppSystemEntity toEntity(AppSystem model);

    /**
     * Flattens and structuralizes domain graphs into outbound API presentation response layers.
     *
     * @param model source domain business state schema instance
     * @return the outbound presentation API data carrier DTO
     */
    AppSystemOutputDTO toResponse(AppSystem model);

    /**
     * Maps incoming flat reference fields into a decoupled domain state instance,
     * resolving relationship IDs (information system database, system) to their respective
     * nested model IDs, while ignoring audit fields.
     *
     * @param inputDTO inbound client creation payload containing mapping configuration
     * @return a decoupled domain state instance ready for orchestration processing pipelines
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "informationSystemDb.id", source = "informationSystemDbId")
    @Mapping(target = "system.id", source = "systemId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    AppSystem toModelFromInput(AppSystemInputDTO inputDTO);

    /**
     * Integrates update parameters from flat payload tracking definitions directly over an active
     * business entity, avoiding logical soft-delete and primary key attribute modifications.
     *
     * @param inputDTO delta parameter updates tracking values payload DTO
     * @param model    the active operational business graph container targeted for modifier updates
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "informationSystemDb.id", source = "informationSystemDbId")
    @Mapping(target = "system.id", source = "systemId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateModelFromInput(AppSystemInputDTO inputDTO, @MappingTarget AppSystem model);
}

package es.caib.invai.back.service.mapper.maintenance.systems.database;

import es.caib.invai.back.persistence.model.maintenance.systems.database.DatabaseEntity;
import es.caib.invai.back.service.model.maintenance.systems.database.Database;
import es.caib.invai.back.interna.maintenance.systems.database.DTO.DatabaseInputDTO;
import es.caib.invai.back.interna.maintenance.systems.database.DTO.DatabaseOutputDTO;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import es.caib.invai.back.service.mapper.maintenance.systems.databaseVendor.DatabaseVendorMapper;
import es.caib.invai.back.service.mapper.maintenance.systems.server.ServerMapper;

/**
 * MapStruct data mapping abstraction interface providing structural state conversions across
 * Database relational database entities, business domain models, and API transfer schemas.
 * Includes referencing capabilities to delegate Server relation resolution to {@link ServerMapper}.
 *
 * @since 1.0.2
 */
@Mapper(componentModel = "spring", uses = {ServerMapper.class, DatabaseVendorMapper.class})
public interface DatabaseMapper {

    /**
     * Materializes a database entity graph into a plain business domain aggregate.
     *
     * @param entity the complex database persistent layout tree root
     * @return a clean domain business layout graph
     */
    Database toModel(DatabaseEntity entity);

    /**
     * Resolves composite relationships and maps business domain representations down to
     * persistent database relational entities.
     *
     * @param model the active composite business domain model node
     * @return a mapped relational database entity layout
     */
    DatabaseEntity toEntity(Database model);

    /**
     * Flattens and structuralizes domain graphs into outbound API presentation response layers.
     *
     * @param model source domain business state schema instance
     * @return the outbound presentation API data carrier DTO
     */
    DatabaseOutputDTO toResponse(Database model);

    /**
     * Maps incoming flat reference fields directly into a decoupled domain state instance
     * ready for orchestration processing pipelines.
     *
     * @param inputDTO inbound client creation payload containing design data
     * @return a decoupled domain state instance ready for orchestration processing pipelines
     */
    @Mapping(target = "server.id", source = "serverId")
    @Mapping(target = "databaseType.id", source = "databaseTypeId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    Database toModelFromInput(DatabaseInputDTO inputDTO);

    /**
     * Integrates update parameters from flat payload tracking definitions directly over an active
     * business entity, avoiding logical soft-delete attribute modifications.
     *
     * @param inputDTO delta parameter updates tracking values payload DTO
     * @param model    the active operational business graph container targeted for modifier updates
     */
    @Mapping(target = "server.id", source = "serverId")
    @Mapping(target = "databaseType.id", source = "databaseTypeId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateModelFromInput(DatabaseInputDTO inputDTO, @MappingTarget Database model);

    /**
     * MapStruct's generated {@code databaseType.id ← databaseTypeId} mapping always builds a
     * nested {@code DatabaseVendor} instance, even when {@code databaseTypeId} is null (yielding
     * {@code DatabaseVendor{id=null}} instead of a null reference). The DDL leaves {@code DATABASE_TYPE}
     * nullable, so force the reference back to {@code null} whenever no vendor was submitted, guarding
     * against a transient/unsaved reference ever reaching JPA regardless of the DTO's current validation strictness.
     *
     * @param inputDTO inbound payload carrying the database vendor identifier
     * @param model    the domain model instance being populated by this mapping round
     */
    @AfterMapping
    default void nullifyDatabaseTypeWhenIdMissing(DatabaseInputDTO inputDTO, @MappingTarget Database model) {
        if (inputDTO.getDatabaseTypeId() == null) {
            model.setDatabaseType(null);
        }
    }
}
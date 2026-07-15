package es.caib.invai.api.service.mapper;

import es.caib.invai.api.persistence.model.ApplicationEntity;
import es.caib.invai.api.service.model.Application;
import es.caib.invai.api.interna.application.DTO.ApplicationInputDTO;
import es.caib.invai.api.interna.application.DTO.ApplicationOutputDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Complex MapStruct orchestration mapper compiling nested inventory metadata variables into
 * unified corporate Application asset graphs, linking related lookup entities.
 *
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", uses = {
        CategoryMapper.class,
        SystemTypeMapper.class,
        FieldMapper.class,
        AdmUnitMapper.class,
        CommissionMapper.class,
        StatusMapper.class
})
public interface ApplicationMapper {

    /**
     * Materializes a database application graph entity into a plain business domain aggregate.
     *
     * @param entity the complex database persistent layout tree root
     * @return a clean domain business layout graph
     */
    Application toModel(ApplicationEntity entity);

    /**
     * Resolves composite relationships and normalizes state enums to output ready-to-persist
     * relational application entities.
     *
     * @param model the active composite business domain model node
     * @return a mapped relational database entity layout
     */
    @Mapping(target = "status", source = "status", qualifiedByName = "mapStatusEnumToStatusEntity")
    ApplicationEntity toEntity(Application model);

    /**
     * Flattens and structuralizes domain graphs into outbound API presentation response layers.
     *
     * @param model source domain business state schema instance
     * @return the outbound presentation API data carrier DTO
     */
    ApplicationOutputDTO toResponse(Application model);

    /**
     * Maps incoming flat reference identity keys into target shallow complex entity stubs
     * during fresh entity mapping transformations.
     *
     * @param inputDTO inbound client creation payload containing design data
     * @return a decoupled domain state instance ready for orchestration processing pipelines
     */
    @Mapping(target = "category.id", source = "categoryId")
    @Mapping(target = "systemType.id", source = "systemTypeId")
    @Mapping(target = "field.id", source = "fieldId")
    @Mapping(target = "admUnit.id", source = "admUnitId")
    @Mapping(target = "csCommission.id", source = "commissionId")
    @Mapping(target = "status", source = "statusId", qualifiedByName = "mapLongToStatusEnum")
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    Application toModelFromInput(ApplicationInputDTO inputDTO);

    /**
     * Integrates update parameters from flat payload tracking definitions directly over an active
     * business entity, avoiding logical soft-delete attribute modifications.
     *
     * @param inputDTO delta parameter updates tracking values payload DTO
     * @param model    the active operational business graph container targeted for modifier updates
     */
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "category.id", source = "categoryId")
    @Mapping(target = "systemType.id", source = "systemTypeId")
    @Mapping(target = "field.id", source = "fieldId")
    @Mapping(target = "admUnit.id", source = "admUnitId")
    @Mapping(target = "csCommission.id", source = "commissionId")
    @Mapping(target = "status", source = "statusId", qualifiedByName = "mapLongToStatusEnum")
    void updateModelFromInput(ApplicationInputDTO inputDTO, @MappingTarget Application model);
}
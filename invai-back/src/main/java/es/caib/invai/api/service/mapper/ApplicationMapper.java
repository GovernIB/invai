package es.caib.invai.api.service.mapper;

import es.caib.invai.api.interna.application.core.DTO.ApplicationInputDTO;
import es.caib.invai.api.interna.application.core.DTO.ApplicationOutputDTO;
import es.caib.invai.api.persistence.model.ApplicationEntity;
import es.caib.invai.api.service.model.AppInformationSystemDb;
import es.caib.invai.api.service.model.Application;
import es.caib.invai.api.service.model.AppDevelopment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Complex MapStruct orchestration mapper compiling nested inventory metadata variables into
 * unified corporate Application asset graphs, linking related lookup entities.
 *
 * @since 1.0.1
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

    Application toModel(ApplicationEntity entity);

    @Mapping(target = "status", source = "status", qualifiedByName = "mapStatusEnumToStatusEntity")
    ApplicationEntity toEntity(Application model);

    @Mapping(target = "appInformationSystemDbId", ignore = true)
    @Mapping(target = "appDevelopmentId", ignore = true)
    ApplicationOutputDTO toResponse(Application application);

    @Mapping(target = "id", source = "application.id")
    @Mapping(target = "name", source = "application.name")
    @Mapping(target = "prefix", source = "application.prefix")
    @Mapping(target = "code", source = "application.code")
    @Mapping(target = "description", source = "application.description")
    @Mapping(target = "status", source = "application.status")
    @Mapping(target = "category", source = "application.category")
    @Mapping(target = "systemType", source = "application.systemType")
    @Mapping(target = "field", source = "application.field")
    @Mapping(target = "admUnit", source = "application.admUnit")
    @Mapping(target = "csCommission", source = "application.csCommission")
    @Mapping(target = "createdAt", source = "application.createdAt")
    @Mapping(target = "createdBy", source = "application.createdBy")
    @Mapping(target = "updatedAt", source = "application.updatedAt")
    @Mapping(target = "updatedBy", source = "application.updatedBy")
    @Mapping(target = "appInformationSystemDbId", source = "appInformationSystemDb.id")
    @Mapping(target = "appDevelopmentId", source = "appDevelopment.id")
    ApplicationOutputDTO toResponse(
            Application application,
            AppInformationSystemDb appInformationSystemDb,
            AppDevelopment appDevelopment
    );

    @Mapping(target = "category.id", source = "categoryId")
    @Mapping(target = "systemType.id", source = "systemTypeId")
    @Mapping(target = "field.id", source = "fieldId")
    @Mapping(target = "admUnit.id", source = "admUnitId")
    @Mapping(target = "csCommission.id", source = "commissionId")
    @Mapping(target = "status", source = "statusId", qualifiedByName = "mapLongToStatusEnum")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    Application toModelFromInput(ApplicationInputDTO inputDTO);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
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
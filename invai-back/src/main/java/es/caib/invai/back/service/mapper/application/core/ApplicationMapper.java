package es.caib.invai.back.service.mapper.application.core;

import es.caib.invai.back.interna.application.core.DTO.ApplicationInputDTO;
import es.caib.invai.back.interna.application.core.DTO.ApplicationOutputDTO;
import es.caib.invai.back.persistence.model.application.core.ApplicationEntity;
import es.caib.invai.back.service.model.application.system_database.core.AppInformationSystemDb;
import es.caib.invai.back.service.model.application.core.Application;
import es.caib.invai.back.service.model.application.development.core.AppDevelopment;
import es.caib.invai.back.service.model.application.responsibleAuthorized.core.AppResponsibleAuthorized;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import es.caib.invai.back.service.mapper.maintenance.admUnit.AdmUnitMapper;
import es.caib.invai.back.service.mapper.maintenance.general.category.CategoryMapper;
import es.caib.invai.back.service.mapper.maintenance.general.commission.CommissionMapper;
import es.caib.invai.back.service.mapper.maintenance.general.field.FieldMapper;
import es.caib.invai.back.service.mapper.catalog.status.StatusMapper;
import es.caib.invai.back.service.mapper.maintenance.general.systemType.SystemTypeMapper;

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

    /**
     * Maps a persistence {@link ApplicationEntity} to its corresponding {@link Application} domain model.
     *
     * @param entity the JPA entity to convert
     * @return the mapped domain model
     */
    Application toModel(ApplicationEntity entity);

    /**
     * Maps an {@link Application} domain model to its corresponding {@link ApplicationEntity} persistence entity.
     *
     * @param model the domain model to convert
     * @return the mapped JPA entity
     */
    @Mapping(target = "status", source = "status", qualifiedByName = "mapStatusEnumToStatusEntity")
    ApplicationEntity toEntity(Application model);

    /**
     * Maps an {@link Application} domain model to an outbound {@link ApplicationOutputDTO},
     * leaving the linked tab identifiers unset.
     *
     * @param application the domain model to convert
     * @return the mapped outbound DTO
     */
    @Mapping(target = "appInformationSystemDbId", ignore = true)
    @Mapping(target = "appDevelopmentId", ignore = true)
    @Mapping(target = "appResponsibleAuthorizedId", ignore = true)
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
    /**
     * Maps an {@link Application} domain model, together with its linked information system database,
     * development, and responsible/authorized records, to a fully populated outbound {@link ApplicationOutputDTO}.
     *
     * @param application               the domain model to convert
     * @param appInformationSystemDb    the linked "AppInformationSystemDb" tab record, may be {@code null}
     * @param appDevelopment            the linked "AppDevelopment" tab record, may be {@code null}
     * @param appResponsibleAuthorized  the linked "Responsables i Autoritzats" tab record, may be {@code null}
     * @return the mapped outbound DTO
     */
    @Mapping(target = "appResponsibleAuthorizedId", source = "appResponsibleAuthorized.id")
    ApplicationOutputDTO toResponse(
            Application application,
            AppInformationSystemDb appInformationSystemDb,
            AppDevelopment appDevelopment,
            AppResponsibleAuthorized appResponsibleAuthorized
    );

    /**
     * Maps an inbound {@link ApplicationInputDTO} to a new {@link Application} domain model,
     * resolving foreign key references and leaving audit metadata fields unset.
     *
     * @param inputDTO the inbound DTO containing application data
     * @return the mapped domain model
     */
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

    /**
     * Applies the values of an inbound {@link ApplicationInputDTO} onto an existing {@link Application}
     * domain model in place, resolving foreign key references and leaving audit metadata fields untouched.
     *
     * @param inputDTO the inbound DTO containing updated application data
     * @param model    the existing domain model to update
     */
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
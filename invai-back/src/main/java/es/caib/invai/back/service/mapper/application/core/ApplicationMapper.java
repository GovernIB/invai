package es.caib.invai.back.service.mapper.application.core;

import es.caib.invai.back.interna.application.core.DTO.ApplicationInputDTO;
import es.caib.invai.back.interna.application.core.DTO.ApplicationOutputDTO;
import es.caib.invai.back.persistence.model.application.core.ApplicationEntity;
import es.caib.invai.back.service.model.application.accessibility.AppAccessibility;
import es.caib.invai.back.service.model.application.system_database.core.AppInformationSystemDb;
import es.caib.invai.back.service.model.application.core.Application;
import es.caib.invai.back.service.model.application.development.core.AppDevelopment;
import es.caib.invai.back.service.model.application.responsibleAuthorized.core.AppResponsibleAuthorized;
import es.caib.invai.back.service.model.application.security.core.AppSecurity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import es.caib.invai.back.service.mapper.maintenance.general.category.CategoryMapper;
import es.caib.invai.back.service.mapper.maintenance.general.commission.CommissionMapper;
import es.caib.invai.back.service.mapper.maintenance.general.field.FieldMapper;
import es.caib.invai.back.service.mapper.catalog.status.StatusMapper;
import es.caib.invai.back.service.mapper.maintenance.general.systemType.SystemTypeMapper;

/**
 * MapStruct mapper converting between {@link Application} domain models, {@link ApplicationEntity}
 * persistence entities, and the inbound/outbound Application DTOs, resolving the category, system
 * type, field, commission, and status lookup references along the way.
 * <p>
 * Declares three {@code toResponse} overloads, each backing a different
 * {@code ApplicationServiceFacadeBean} call site: the 1-argument form for {@code getAll} (no
 * anchors resolved), the 5-argument form for {@code create}/{@code update}/{@code reactivate}
 * (anchors resolved, no completeness flags), and the 12-argument form for {@code getById} (anchors
 * plus the Responsables/Desenvolupament/Seguretat/Accessibilitat/Sistemes/Base de dades
 * completeness flags).
 * </p>
 *
 * @since 1.0.1
 */
@Mapper(componentModel = "spring", uses = {
        CategoryMapper.class,
        SystemTypeMapper.class,
        FieldMapper.class,
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
    @Mapping(target = "appSecurityId", ignore = true)
    @Mapping(target = "appAccessibilityId", ignore = true)
    ApplicationOutputDTO toResponse(Application application);

    /**
     * Maps an {@link Application} domain model, together with its linked information system database,
     * development, responsible/authorized, security, and accessibility records, to a fully populated
     * outbound {@link ApplicationOutputDTO} — used by {@code create}/{@code update}/{@code reactivate}
     * in {@code ApplicationServiceFacadeBean}, none of which compute the per-tab completeness flags.
     * Since those 7 {@code missingXxx} fields are {@link Boolean} (not {@code boolean}), leaving
     * them unmapped here means they come back {@code null} rather than a misleading {@code false}.
     * Contrast with the 13-argument overload below, used by {@code getById}, which takes those
     * flags explicitly instead of leaving them unset.
     *
     * @param application               the domain model to convert
     * @param appInformationSystemDb    the linked "AppInformationSystemDb" tab record, may be {@code null}
     * @param appDevelopment            the linked "AppDevelopment" tab record, may be {@code null}
     * @param appResponsibleAuthorized  the linked "Responsables i Autoritzats" tab record, may be {@code null}
     * @param appSecurity               the linked "Seguretat" tab record, may be {@code null}
     * @param appAccessibility          the linked "Accessibilitat" tab record, may be {@code null}
     * @return the mapped outbound DTO
     */
    @Mapping(target = "id", source = "application.id")
    @Mapping(target = "name", source = "application.name")
    @Mapping(target = "prefix", source = "application.prefix")
    @Mapping(target = "code", source = "application.code")
    @Mapping(target = "description", source = "application.description")
    @Mapping(target = "status", source = "application.status")
    @Mapping(target = "category", source = "application.category")
    @Mapping(target = "systemType", source = "application.systemType")
    @Mapping(target = "field", source = "application.field")
    @Mapping(target = "csCommission", source = "application.csCommission")
    @Mapping(target = "createdAt", source = "application.createdAt")
    @Mapping(target = "createdBy", source = "application.createdBy")
    @Mapping(target = "updatedAt", source = "application.updatedAt")
    @Mapping(target = "updatedBy", source = "application.updatedBy")
    @Mapping(target = "appInformationSystemDbId", source = "appInformationSystemDb.id")
    @Mapping(target = "appDevelopmentId", source = "appDevelopment.id")
    @Mapping(target = "appResponsibleAuthorizedId", source = "appResponsibleAuthorized.id")
    @Mapping(target = "appSecurityId", source = "appSecurity.id")
    @Mapping(target = "appAccessibilityId", source = "appAccessibility.id")
    ApplicationOutputDTO toResponse(
            Application application,
            AppInformationSystemDb appInformationSystemDb,
            AppDevelopment appDevelopment,
            AppResponsibleAuthorized appResponsibleAuthorized,
            AppSecurity appSecurity,
            AppAccessibility appAccessibility
    );

    /**
     * Maps an {@link Application} domain model, together with its linked information system database,
     * development, responsible/authorized, security, and accessibility records, plus every tab's
     * completeness flag computed by the caller, to a fully populated outbound
     * {@link ApplicationOutputDTO} — used only by {@code getById} in {@code ApplicationServiceFacadeBean}.
     * See the 6-argument overload above for the variant used by {@code create}/{@code update}/
     * {@code reactivate}, which don't compute these flags.
     *
     * @param application               the domain model to convert
     * @param appInformationSystemDb    the linked "AppInformationSystemDb" tab record, may be {@code null}
     * @param appDevelopment            the linked "AppDevelopment" tab record, may be {@code null}
     * @param appResponsibleAuthorized  the linked "Responsables i Autoritzats" tab record, may be {@code null}
     * @param appSecurity               the linked "Seguretat" tab record, may be {@code null}
     * @param appAccessibility          the linked "Accessibilitat" tab record, may be {@code null}
     * @param missingDevelopmentFields  {@code true} if the "AppDevelopment" tab is missing or incomplete
     * @param missingResponsibleTypes   {@code true} if any responsible type has no active holder
     * @param missingAuthorized         {@code true} if no active authorized person exists
     * @param missingAccessibilityFields {@code true} if the "AppAccessibility" tab is missing or incomplete
     * @param missingSecurityData       {@code true} if the "Seguretat" anchor is missing, or has no
     * active record in any of its Web Context, ENS Classification, or Security Risk child tables
     * @param missingSystems            {@code true} if the "AppInformationSystemDb" anchor is missing,
     * or has no active system link at all
     * @param missingDatabases          {@code true} if the "AppInformationSystemDb" anchor is missing,
     * or has no active database link at all
     * @return the mapped outbound DTO
     */
    @Mapping(target = "id", source = "application.id")
    @Mapping(target = "name", source = "application.name")
    @Mapping(target = "prefix", source = "application.prefix")
    @Mapping(target = "code", source = "application.code")
    @Mapping(target = "description", source = "application.description")
    @Mapping(target = "status", source = "application.status")
    @Mapping(target = "category", source = "application.category")
    @Mapping(target = "systemType", source = "application.systemType")
    @Mapping(target = "field", source = "application.field")
    @Mapping(target = "csCommission", source = "application.csCommission")
    @Mapping(target = "createdAt", source = "application.createdAt")
    @Mapping(target = "createdBy", source = "application.createdBy")
    @Mapping(target = "updatedAt", source = "application.updatedAt")
    @Mapping(target = "updatedBy", source = "application.updatedBy")
    @Mapping(target = "appInformationSystemDbId", source = "appInformationSystemDb.id")
    @Mapping(target = "appDevelopmentId", source = "appDevelopment.id")
    @Mapping(target = "appResponsibleAuthorizedId", source = "appResponsibleAuthorized.id")
    @Mapping(target = "appSecurityId", source = "appSecurity.id")
    @Mapping(target = "appAccessibilityId", source = "appAccessibility.id")
    @Mapping(target = "missingDevelopmentFields", source = "missingDevelopmentFields")
    @Mapping(target = "missingResponsibleTypes", source = "missingResponsibleTypes")
    @Mapping(target = "missingAuthorized", source = "missingAuthorized")
    @Mapping(target = "missingAccessibilityFields", source = "missingAccessibilityFields")
    @Mapping(target = "missingSecurityData", source = "missingSecurityData")
    @Mapping(target = "missingSystems", source = "missingSystems")
    @Mapping(target = "missingDatabases", source = "missingDatabases")
    ApplicationOutputDTO toResponse(
            Application application,
            AppInformationSystemDb appInformationSystemDb,
            AppDevelopment appDevelopment,
            AppResponsibleAuthorized appResponsibleAuthorized,
            AppSecurity appSecurity,
            AppAccessibility appAccessibility,
            Boolean missingDevelopmentFields,
            Boolean missingResponsibleTypes,
            Boolean missingAuthorized,
            Boolean missingAccessibilityFields,
            Boolean missingSecurityData,
            Boolean missingSystems,
            Boolean missingDatabases
    );

    /**
     * Maps an inbound {@link ApplicationInputDTO} to a new {@link Application} domain model,
     * resolving foreign key references and leaving audit metadata fields unset.
     *
     * @param inputDTO the inbound DTO containing application data
     * @return the mapped domain model
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "expirationDate", ignore = true)
    @Mapping(target = "category.id", source = "categoryId")
    @Mapping(target = "systemType.id", source = "systemTypeId")
    @Mapping(target = "field.id", source = "fieldId")
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
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "expirationDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "category.id", source = "categoryId")
    @Mapping(target = "systemType.id", source = "systemTypeId")
    @Mapping(target = "field.id", source = "fieldId")
    @Mapping(target = "csCommission.id", source = "commissionId")
    @Mapping(target = "status", source = "statusId", qualifiedByName = "mapLongToStatusEnum")
    void updateModelFromInput(ApplicationInputDTO inputDTO, @MappingTarget Application model);
}
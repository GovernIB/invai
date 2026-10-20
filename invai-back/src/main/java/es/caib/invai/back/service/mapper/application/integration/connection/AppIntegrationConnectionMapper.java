package es.caib.invai.back.service.mapper.application.integration.connection;

import es.caib.invai.back.interna.application.integration.connection.DTO.AppIntegrationConnectionInputDTO;
import es.caib.invai.back.interna.application.integration.connection.DTO.AppIntegrationConnectionOutputDTO;
import es.caib.invai.back.persistence.model.application.integration.connection.AppIntegrationConnectionEntity;
import es.caib.invai.back.service.mapper.application.core.ApplicationMapper;
import es.caib.invai.back.service.mapper.maintenance.development.technology.TechnologyMapper;
import es.caib.invai.back.service.mapper.maintenance.integration.externalSystem.ExternalSystemMapper;
import es.caib.invai.back.service.model.application.core.Application;
import es.caib.invai.back.service.model.application.integration.connection.AppIntegrationConnection;
import es.caib.invai.back.service.model.maintenance.integration.externalSystem.ExternalSystem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * MapStruct mapper converting between {@link AppIntegrationConnection} domain models, {@link
 * AppIntegrationConnectionEntity} persistence entities, and the inbound/outbound DTOs. The mutual
 * exclusion between {@code applicationId}/{@code externalSystemId} is validated by the facade, not
 * here - the mapper just maps whichever of the two is present.
 *
 * @since 1.0.5
 */
@Mapper(componentModel = "spring", uses = {ApplicationMapper.class, ExternalSystemMapper.class, TechnologyMapper.class})
public interface AppIntegrationConnectionMapper {

    AppIntegrationConnection toModel(AppIntegrationConnectionEntity entity);

    AppIntegrationConnectionEntity toEntity(AppIntegrationConnection model);

    @Mapping(target = "appIntegrationId", source = "appIntegration.id")
    @Mapping(target = "requiredRoles", ignore = true)
    @Mapping(target = "grantedRoles", ignore = true)
    @Mapping(target = "rolesMismatch", ignore = true)
    AppIntegrationConnectionOutputDTO toResponse(AppIntegrationConnection model);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "appIntegration.id", source = "appIntegrationId")
    @Mapping(target = "application", source = "applicationId")
    @Mapping(target = "externalSystem", source = "externalSystemId")
    @Mapping(target = "technology.id", source = "technologyId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    AppIntegrationConnection toModelFromInput(AppIntegrationConnectionInputDTO inputDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "appIntegration.id", source = "appIntegrationId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "application.id", source = "applicationId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "externalSystem.id", source = "externalSystemId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "technology.id", source = "technologyId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateModelFromInput(AppIntegrationConnectionInputDTO inputDTO, @MappingTarget AppIntegrationConnection model);

    /**
     * Resolves an {@link Application} stub from its identifier, or {@code null} if the identifier
     * itself is {@code null} - this field is optional (and mutually exclusive with {@code
     * externalSystem}) on {@link AppIntegrationConnection}.
     *
     * @param applicationId the application identifier, possibly {@code null}
     * @return a stub carrying only the given id, or {@code null}
     */
    default Application mapApplicationId(Long applicationId) {
        if (applicationId == null) {
            return null;
        }
        Application application = new Application();
        application.setId(applicationId);
        return application;
    }

    /**
     * Resolves an {@link ExternalSystem} stub from its identifier, or {@code null} if the
     * identifier itself is {@code null} - this field is optional (and mutually exclusive with
     * {@code application}) on {@link AppIntegrationConnection}.
     *
     * @param externalSystemId the external system identifier, possibly {@code null}
     * @return a stub carrying only the given id, or {@code null}
     */
    default ExternalSystem mapExternalSystemId(Long externalSystemId) {
        if (externalSystemId == null) {
            return null;
        }
        ExternalSystem externalSystem = new ExternalSystem();
        externalSystem.setId(externalSystemId);
        return externalSystem;
    }
}

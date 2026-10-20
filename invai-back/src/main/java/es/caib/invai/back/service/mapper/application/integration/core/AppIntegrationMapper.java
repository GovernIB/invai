package es.caib.invai.back.service.mapper.application.integration.core;

import es.caib.invai.back.interna.application.integration.core.DTO.AppIntegrationInputDTO;
import es.caib.invai.back.interna.application.integration.core.DTO.AppIntegrationOutputDTO;
import es.caib.invai.back.persistence.model.application.integration.core.AppIntegrationEntity;
import es.caib.invai.back.service.mapper.application.core.ApplicationMapper;
import es.caib.invai.back.service.model.application.integration.core.AppIntegration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * MapStruct mapper converting between {@link AppIntegration} domain models, {@link
 * AppIntegrationEntity} persistence entities, and the inbound/outbound AppIntegration DTOs.
 *
 * @since 1.0.5
 */
@Mapper(componentModel = "spring", uses = {ApplicationMapper.class})
public interface AppIntegrationMapper {

    AppIntegration toModel(AppIntegrationEntity entity);

    AppIntegrationEntity toEntity(AppIntegration model);

    @Mapping(target = "applicationId", source = "application.id")
    AppIntegrationOutputDTO toResponse(AppIntegration model);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "application.id", source = "applicationId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    AppIntegration toModelFromInput(AppIntegrationInputDTO inputDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "application.id", source = "applicationId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateModelFromInput(AppIntegrationInputDTO inputDTO, @MappingTarget AppIntegration model);
}

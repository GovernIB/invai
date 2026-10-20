package es.caib.invai.back.service.mapper.application.integration.requiredRole;

import es.caib.invai.back.persistence.model.application.integration.requiredRole.AppIntegrationRequiredRoleEntity;
import es.caib.invai.back.service.mapper.application.integration.connection.AppIntegrationConnectionMapper;
import es.caib.invai.back.service.model.application.integration.requiredRole.AppIntegrationRequiredRole;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper converting between {@link AppIntegrationRequiredRole} domain models and {@link
 * AppIntegrationRequiredRoleEntity} persistence entities. There is no input/output DTO conversion
 * here: only the Soffid role id is ever created/persisted/read - its name/system/description are
 * resolved live from Soffid on every read (see {@code
 * AppIntegrationConnectionServiceFacadeBean#getRequiredRoles}), never persisted.
 *
 * @since 1.0.5
 */
@Mapper(componentModel = "spring", uses = {AppIntegrationConnectionMapper.class})
public interface AppIntegrationRequiredRoleMapper {

    AppIntegrationRequiredRole toModel(AppIntegrationRequiredRoleEntity entity);

    AppIntegrationRequiredRoleEntity toEntity(AppIntegrationRequiredRole model);
}

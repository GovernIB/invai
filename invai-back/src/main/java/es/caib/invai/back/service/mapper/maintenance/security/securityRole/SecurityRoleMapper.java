package es.caib.invai.back.service.mapper.maintenance.security.securityRole;

import es.caib.invai.back.interna.maintenance.security.securityRole.DTO.SecurityRoleOutputDTO;
import es.caib.invai.back.persistence.model.maintenance.security.securityRole.SecurityRoleEntity;
import es.caib.invai.back.service.model.maintenance.security.securityRole.SecurityRole;
import org.mapstruct.Mapper;

/**
 * MapStruct data mapping abstraction interface providing structural state conversions across
 * Security Role database entities, business domain models, and API transfer schemas.
 *
 * @since 1.0.4
 */
@Mapper(componentModel = "spring")
public interface SecurityRoleMapper {

    /**
     * Converts a database relational layer entity into a plain business domain model.
     *
     * @param entity the source persistent database state representation
     * @return the mapped business domain model instance
     */
    SecurityRole toModel(SecurityRoleEntity entity);

    /**
     * Converts a domain model configuration into an outbound presentation layer REST DTO.
     *
     * @param model the source domain layer data model
     * @return the outbound presentation API data carrier DTO
     */
    SecurityRoleOutputDTO toResponse(SecurityRole model);
}

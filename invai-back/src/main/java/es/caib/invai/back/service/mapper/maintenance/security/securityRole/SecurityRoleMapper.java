package es.caib.invai.back.service.mapper.maintenance.security.securityRole;

import es.caib.invai.back.interna.maintenance.security.securityRole.DTO.SecurityRoleOutputDTO;
import es.caib.invai.back.persistence.model.maintenance.security.securityRole.SecurityRoleEntity;
import es.caib.invai.back.service.model.maintenance.security.securityRole.SecurityRole;
import es.caib.invai.back.rest.soffid.SoffidRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

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
     * Copies the data of a Soffid role - its Soffid id, name, system, and description - into a
     * business domain model with no local id, the row that is saved with a role assignment.
     *
     * @param soffidRole the role as returned by Soffid
     * @return the mapped business domain model instance, still without a local id
     * @since 1.0.6
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roleId", source = "id")
    SecurityRole toModelFromSoffid(SoffidRole soffidRole);

    /**
     * Converts a Soffid role, as returned by its search, into an outbound presentation layer REST
     * DTO. The local {@code id} is left {@code null} since no local row exists yet.
     *
     * @param soffidRole the role as returned by Soffid
     * @return the outbound presentation API data carrier DTO, without a local id
     * @since 1.0.6
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roleId", source = "id")
    SecurityRoleOutputDTO toResponseFromSoffid(SoffidRole soffidRole);

    /**
     * Converts a domain model configuration into an outbound presentation layer REST DTO.
     *
     * @param model the source domain layer data model
     * @return the outbound presentation API data carrier DTO
     */
    SecurityRoleOutputDTO toResponse(SecurityRole model);
}

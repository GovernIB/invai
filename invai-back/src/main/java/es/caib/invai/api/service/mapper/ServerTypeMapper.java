package es.caib.invai.api.service.mapper;

import es.caib.invai.api.interna.maintenance.serverType.DTO.ServerTypeOutputDTO;
import es.caib.invai.api.persistence.model.catalog.LkupServerTypeEntity;
import es.caib.invai.api.service.model.ServerType;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct translation utility specializing in state conversions across server type
 * lookup entities, business model wrappers, and outbound presentation schemas.
 *
 * @since 1.0.2
 */
@Mapper(componentModel = "spring")
public interface ServerTypeMapper {

    /**
     * Converts a database relational lookup entity into a plain business domain model.
     *
     * @param entity the persistent lookup entity source node
     * @return the matching business domain wrapper model
     */
    ServerType toModel(LkupServerTypeEntity entity);

    /**
     * Converts a domain business layer model into a relational database entity representation.
     *
     * @param model the business model context target
     * @return the persistent entity representation mapping the target layout
     */
    LkupServerTypeEntity toEntity(ServerType model);

    /**
     * Converts a domain model configuration into an outbound presentation layer REST DTO.
     *
     * @param model the source domain layer data model
     * @return the outbound presentation API data carrier DTO
     */
    ServerTypeOutputDTO toResponse(ServerType model);

    /**
     * Converts a list of persistent lookup entities into their plain business domain model equivalents.
     *
     * @param entities the source persistent lookup entity list
     * @return the matching list of business domain wrapper models
     */
    List<ServerType> toModelList(List<LkupServerTypeEntity> entities);

    /**
     * Converts a list of domain models into their outbound presentation layer REST DTO equivalents.
     *
     * @param models the source domain layer data model list
     * @return the outbound presentation API data carrier DTO list
     */
    List<ServerTypeOutputDTO> toResponseList(List<ServerType> models);

    /**
     * Contextual lookup reference instantiation utility building a placeholder server type domain unit
     * around a given ID tracking parameter index.
     *
     * @param value target primary identification database key index
     * @return a disconnected server type structure stub tracking the configuration reference ID, or {@code null} if absent
     */
    default ServerType map(Long value) {
        if (value == null) {
            return null;
        }
        ServerType serverType = new ServerType();
        serverType.setId(value);
        return serverType;
    }
}

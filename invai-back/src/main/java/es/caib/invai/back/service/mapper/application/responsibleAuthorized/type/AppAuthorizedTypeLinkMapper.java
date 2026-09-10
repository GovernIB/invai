package es.caib.invai.back.service.mapper.application.responsibleAuthorized.type;

import es.caib.invai.back.persistence.model.application.responsibleAuthorized.type.AppAuthorizedTypeLinkEntity;
import es.caib.invai.back.service.model.application.responsibleAuthorized.type.AppAuthorizedTypeLink;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper converting between {@code AppAuthorizedTypeLink} join entities and their flat
 * business domain model. There are no input/output DTOs for this join: it is never exposed
 * through its own REST controller and is only manipulated internally by
 * {@code AppAuthorizedServiceFacadeBean}.
 *
 * @since 1.0.3
 */
@Mapper(componentModel = "spring")
public interface AppAuthorizedTypeLinkMapper {

    /**
     * Converts a join persistence entity into its flat business domain model equivalent.
     *
     * @param entity the join entity to convert
     * @return the corresponding domain model
     */
    @Mapping(target = "appAuthorizedId", source = "appAuthorized.id")
    @Mapping(target = "authorizationTypeId", source = "authorizationType.id")
    AppAuthorizedTypeLink toModel(AppAuthorizedTypeLinkEntity entity);

    /**
     * Converts a flat business domain model into its join persistence entity equivalent.
     *
     * @param model the domain model to convert
     * @return the corresponding persistence entity
     */
    @Mapping(target = "appAuthorized.id", source = "appAuthorizedId")
    @Mapping(target = "authorizationType.id", source = "authorizationTypeId")
    AppAuthorizedTypeLinkEntity toEntity(AppAuthorizedTypeLink model);
}

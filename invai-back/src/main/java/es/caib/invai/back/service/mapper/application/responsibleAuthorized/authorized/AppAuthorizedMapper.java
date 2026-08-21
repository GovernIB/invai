package es.caib.invai.back.service.mapper.application.responsibleAuthorized.authorized;

import es.caib.invai.back.interna.application.responsibleAuthorized.authorized.DTO.AppAuthorizedInputDTO;
import es.caib.invai.back.interna.application.responsibleAuthorized.authorized.DTO.AppAuthorizedOutputDTO;
import es.caib.invai.back.persistence.model.application.responsibleAuthorized.authorized.AppAuthorizedEntity;
import es.caib.invai.back.service.mapper.application.responsibleAuthorized.core.AppResponsibleAuthorizedMapper;
import es.caib.invai.back.service.mapper.maintenance.responsible.person.PersonMapper;
import es.caib.invai.back.service.model.application.responsibleAuthorized.authorized.AppAuthorized;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * MapStruct data mapping abstraction interface providing structural state conversions across
 * AppAuthorized database entities, business domain models, and API transfer schemas.
 * <p>
 * {@code observation} is editable both on create ({@code toModelFromInput}) and on update
 * ({@code updateModelFromInput}); the dedicated delete flow may also overwrite it with a
 * deactivation reason. Likewise {@code authorizationTypes} on the output DTO is resolved
 * separately by the facade from the active join rows and is ignored here. On update, the parent
 * anchor and the authorized person are immutable: editing only ever changes {@code observation},
 * and (via the facade's join reconciliation) the attached authorization types, so
 * {@code appResponsibleAuthorized} and {@code person} are ignored in {@code updateModelFromInput}.
 * Delegates the nested person sub-graph to {@link PersonMapper}.
 * </p>
 *
 * @since 1.0.3
 */
@Mapper(componentModel = "spring", uses = {PersonMapper.class, AppResponsibleAuthorizedMapper.class})
public interface AppAuthorizedMapper {

    /**
     * Converts a persistence entity into its business domain model equivalent.
     *
     * @param entity the entity to convert
     * @return the corresponding domain model
     */
    AppAuthorized toModel(AppAuthorizedEntity entity);

    /**
     * Converts a business domain model into its persistence entity equivalent.
     *
     * @param model the domain model to convert
     * @return the corresponding persistence entity
     */
    AppAuthorizedEntity toEntity(AppAuthorized model);

    /**
     * Converts a business domain model into its API output representation.
     * {@code authorizationTypes} is intentionally left unset here: it is resolved
     * separately by the facade from the active join rows.
     *
     * @param model the domain model to convert
     * @return the corresponding output DTO
     */
    @Mapping(target = "appResponsibleAuthorizedId", source = "appResponsibleAuthorized.id")
    @Mapping(target = "authorizationTypes", ignore = true)
    AppAuthorizedOutputDTO toResponse(AppAuthorized model);

    /**
     * Converts an inbound input DTO into a new business domain model instance.
     * Audit fields are intentionally left unset.
     *
     * @param inputDTO the input payload to convert
     * @return the corresponding new domain model
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "appResponsibleAuthorized.id", source = "appResponsibleAuthorizedId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "person.id", source = "personId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    AppAuthorized toModelFromInput(AppAuthorizedInputDTO inputDTO);

    /**
     * Applies the mutable fields of an input DTO onto an existing domain model in place. Only
     * {@code observation} is editable this way: the parent anchor and the authorized person are
     * immutable once created, so both are left untouched.
     *
     * @param inputDTO the input payload with the new values
     * @param model the existing domain model to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "appResponsibleAuthorized", ignore = true)
    @Mapping(target = "person", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateModelFromInput(AppAuthorizedInputDTO inputDTO, @MappingTarget AppAuthorized model);
}

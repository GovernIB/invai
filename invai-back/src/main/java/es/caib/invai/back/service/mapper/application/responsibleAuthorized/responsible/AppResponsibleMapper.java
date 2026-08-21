package es.caib.invai.back.service.mapper.application.responsibleAuthorized.responsible;

import es.caib.invai.back.interna.application.responsibleAuthorized.responsible.DTO.AppResponsibleInputDTO;
import es.caib.invai.back.interna.application.responsibleAuthorized.responsible.DTO.AppResponsibleOutputDTO;
import es.caib.invai.back.persistence.model.application.responsibleAuthorized.responsible.AppResponsibleEntity;
import es.caib.invai.back.service.mapper.application.responsibleAuthorized.core.AppResponsibleAuthorizedMapper;
import es.caib.invai.back.service.mapper.maintenance.responsible.person.PersonMapper;
import es.caib.invai.back.service.mapper.catalog.responsibleType.ResponsibleTypeMapper;
import es.caib.invai.back.service.model.application.responsibleAuthorized.responsible.AppResponsible;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct data mapping abstraction interface providing structural state conversions across
 * AppResponsible entities, their business domain model, and API transfer schemas. Delegates the
 * nested anchor, person, and responsible type sub-graphs to {@link AppResponsibleAuthorizedMapper},
 * {@link PersonMapper}, and {@link ResponsibleTypeMapper} respectively.
 *
 * @since 1.0.3
 */
@Mapper(componentModel = "spring", uses = {AppResponsibleAuthorizedMapper.class, PersonMapper.class, ResponsibleTypeMapper.class})
public interface AppResponsibleMapper {

    /**
     * Converts a persistence entity into its business domain model equivalent.
     *
     * @param entity the entity to convert
     * @return the corresponding domain model
     */
    AppResponsible toModel(AppResponsibleEntity entity);

    /**
     * Converts a business domain model into its persistence entity equivalent.
     *
     * @param model the domain model to convert
     * @return the corresponding persistence entity
     */
    AppResponsibleEntity toEntity(AppResponsible model);

    /**
     * Converts a business domain model into its API output representation.
     *
     * @param model the domain model to convert
     * @return the corresponding output DTO
     */
    @Mapping(target = "appResponsibleAuthorizedId", source = "appResponsibleAuthorized.id")
    AppResponsibleOutputDTO toResponse(AppResponsible model);

    /**
     * Converts an inbound input DTO into a new business domain model instance.
     * Audit fields are intentionally left unset.
     *
     * @param inputDTO the input payload to convert
     * @return the corresponding new domain model
     */
    @Mapping(target = "appResponsibleAuthorized.id", source = "appResponsibleAuthorizedId")
    @Mapping(target = "person.id", source = "personId")
    @Mapping(target = "responsibleType.id", source = "responsibleTypeId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    AppResponsible toModelFromInput(AppResponsibleInputDTO inputDTO);

    /**
     * Applies the mutable fields of an input DTO onto an existing domain model in place.
     * Audit fields and {@code responsibleType} are intentionally left untouched: the responsible
     * type held by an assignment cannot be changed via update, only by deleting and creating a
     * new assignment (see {@link AppResponsibleMapper}).
     *
     * @param inputDTO the input payload with the new values
     * @param model the existing domain model to update
     */
    @Mapping(target = "appResponsibleAuthorized.id", source = "appResponsibleAuthorizedId")
    @Mapping(target = "person.id", source = "personId")
    @Mapping(target = "responsibleType", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateModelFromInput(AppResponsibleInputDTO inputDTO, @MappingTarget AppResponsible model);
}

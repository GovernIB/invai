package es.caib.invai.back.service.mapper.catalog.appSection;

import es.caib.invai.back.interna.catalog.appSection.DTO.AppSectionOutputDTO;
import es.caib.invai.back.persistence.model.catalog.appSection.LkupAppSectionEntity;
import es.caib.invai.back.service.model.catalog.appSection.AppSection;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper converting application section lookup entries between entities, domain models
 * and response DTOs.
 *
 * @since 1.0.6
 */
@Mapper(componentModel = "spring")
public interface AppSectionMapper {

    /**
     * Converts a persistence entity into the domain model.
     *
     * @param entity the entity to convert
     * @return the converted domain model
     */
    AppSection toModel(LkupAppSectionEntity entity);

    /**
     * Converts a domain model into the persistence entity.
     *
     * @param model the domain model to convert
     * @return the converted entity
     */
    LkupAppSectionEntity toEntity(AppSection model);

    /**
     * Converts a domain model into the outbound response DTO.
     *
     * @param model the domain model to convert
     * @return the converted response DTO
     */
    AppSectionOutputDTO toResponse(AppSection model);

    /**
     * Converts a list of entities into domain models.
     *
     * @param entities the entities to convert
     * @return the converted domain models
     */
    List<AppSection> toModelList(List<LkupAppSectionEntity> entities);

    /**
     * Converts a list of domain models into response DTOs.
     *
     * @param models the domain models to convert
     * @return the converted response DTOs
     */
    List<AppSectionOutputDTO> toResponseList(List<AppSection> models);
}

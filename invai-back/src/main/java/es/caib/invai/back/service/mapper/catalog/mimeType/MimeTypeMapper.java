package es.caib.invai.back.service.mapper.catalog.mimeType;

import es.caib.invai.back.interna.catalog.mimeType.DTO.MimeTypeOutputDTO;
import es.caib.invai.back.persistence.model.catalog.mimeType.LkupMimeTypeEntity;
import es.caib.invai.back.service.model.catalog.mimeType.MimeType;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper converting file format lookup entries between entities, domain models and
 * response DTOs.
 *
 * @since 1.0.6
 */
@Mapper(componentModel = "spring")
public interface MimeTypeMapper {

    /**
     * Converts a persistence entity into the domain model.
     *
     * @param entity the entity to convert
     * @return the converted domain model
     */
    MimeType toModel(LkupMimeTypeEntity entity);

    /**
     * Converts a domain model into the persistence entity.
     *
     * @param model the domain model to convert
     * @return the converted entity
     */
    LkupMimeTypeEntity toEntity(MimeType model);

    /**
     * Converts a domain model into the outbound response DTO.
     *
     * @param model the domain model to convert
     * @return the converted response DTO
     */
    MimeTypeOutputDTO toResponse(MimeType model);

    /**
     * Converts a list of entities into domain models.
     *
     * @param entities the entities to convert
     * @return the converted domain models
     */
    List<MimeType> toModelList(List<LkupMimeTypeEntity> entities);

    /**
     * Converts a list of domain models into response DTOs.
     *
     * @param models the domain models to convert
     * @return the converted response DTOs
     */
    List<MimeTypeOutputDTO> toResponseList(List<MimeType> models);
}

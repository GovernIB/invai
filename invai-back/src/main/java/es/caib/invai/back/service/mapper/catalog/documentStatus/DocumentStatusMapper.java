package es.caib.invai.back.service.mapper.catalog.documentStatus;

import es.caib.invai.back.interna.catalog.documentStatus.DTO.DocumentStatusOutputDTO;
import es.caib.invai.back.persistence.model.catalog.documentStatus.LkupDocumentStatusEntity;
import es.caib.invai.back.service.model.catalog.documentStatus.DocumentStatus;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper converting document status lookup entries between entities, domain models
 * and response DTOs.
 *
 * @since 1.0.6
 */
@Mapper(componentModel = "spring")
public interface DocumentStatusMapper {

    /**
     * Converts a persistence entity into the domain model.
     *
     * @param entity the entity to convert
     * @return the converted domain model
     */
    DocumentStatus toModel(LkupDocumentStatusEntity entity);

    /**
     * Converts a domain model into the persistence entity.
     *
     * @param model the domain model to convert
     * @return the converted entity
     */
    LkupDocumentStatusEntity toEntity(DocumentStatus model);

    /**
     * Converts a domain model into the outbound response DTO.
     *
     * @param model the domain model to convert
     * @return the converted response DTO
     */
    DocumentStatusOutputDTO toResponse(DocumentStatus model);

    /**
     * Converts a list of entities into domain models.
     *
     * @param entities the entities to convert
     * @return the converted domain models
     */
    List<DocumentStatus> toModelList(List<LkupDocumentStatusEntity> entities);

    /**
     * Converts a list of domain models into response DTOs.
     *
     * @param models the domain models to convert
     * @return the converted response DTOs
     */
    List<DocumentStatusOutputDTO> toResponseList(List<DocumentStatus> models);
}

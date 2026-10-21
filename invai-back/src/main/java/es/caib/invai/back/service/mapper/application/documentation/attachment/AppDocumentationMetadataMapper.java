package es.caib.invai.back.service.mapper.application.documentation.attachment;

import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationMetadataInputDTO;
import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationMetadataOutputDTO;
import es.caib.invai.back.persistence.model.application.documentation.attachment.AppDocumentationMetadataEntity;
import es.caib.invai.back.service.model.application.documentation.attachment.AppDocumentationMetadata;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper converting document metadata between entities, domain models and API transfer
 * schemas.
 *
 * @since 1.0.6
 */
@Mapper(componentModel = "spring")
public interface AppDocumentationMetadataMapper {

    /**
     * Converts a persistence entity into the domain model.
     *
     * @param entity the entity to convert
     * @return the converted domain model
     */
    @Mapping(target = "appDocumentationAttachmentVersionId", source = "appDocumentationAttachmentVersion.id")
    AppDocumentationMetadata toModel(AppDocumentationMetadataEntity entity);

    /**
     * Converts a domain model into the persistence entity, leaving the attachment to be set by
     * the repository.
     *
     * @param model the domain model to convert
     * @return the converted entity
     */
    @Mapping(target = "appDocumentationAttachmentVersion", ignore = true)
    AppDocumentationMetadataEntity toEntity(AppDocumentationMetadata model);

    /**
     * Converts a domain model into the outbound response DTO.
     *
     * @param model the domain model to convert
     * @return the converted response DTO
     */
    @Mapping(target = "key", source = "metaKey")
    @Mapping(target = "value", source = "metaValue")
    AppDocumentationMetadataOutputDTO toResponse(AppDocumentationMetadata model);

    /**
     * Converts an inbound input DTO into a new domain model of the given attachment, leaving audit
     * fields unset.
     *
     * @param inputDTO     the input DTO to convert
     * @param attachmentId identifier of the attachment the metadata belongs to
     * @return the converted domain model
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "appDocumentationAttachmentVersionId", source = "attachmentId")
    @Mapping(target = "metaKey", source = "inputDTO.key")
    @Mapping(target = "metaValue", source = "inputDTO.value")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    AppDocumentationMetadata toModelFromInput(AppDocumentationMetadataInputDTO inputDTO, Long attachmentId);

    /**
     * Converts a list of entities into domain models.
     *
     * @param entities the entities to convert
     * @return the converted domain models
     */
    List<AppDocumentationMetadata> toModelList(List<AppDocumentationMetadataEntity> entities);

    /**
     * Converts a list of domain models into response DTOs.
     *
     * @param models the domain models to convert
     * @return the converted response DTOs
     */
    List<AppDocumentationMetadataOutputDTO> toResponseList(List<AppDocumentationMetadata> models);
}

package es.caib.invai.back.service.mapper.application.documentation.attachment;

import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentVersionInputDTO;
import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentVersionOutputDTO;
import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentInputDTO;
import es.caib.invai.back.persistence.model.application.documentation.attachment.AppDocumentationAttachmentVersionEntity;
import es.caib.invai.back.service.mapper.catalog.mimeType.MimeTypeMapper;
import es.caib.invai.back.service.model.application.documentation.attachment.AppDocumentationAttachmentVersion;
import es.caib.invai.back.service.model.catalog.mimeType.MimeType;
import es.caib.invai.back.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * MapStruct mapper converting document attachments between entities, domain models and response
 * DTOs. The file content is never mapped.
 *
 * @since 1.0.6
 */
@Mapper(componentModel = "spring", uses = {MimeTypeMapper.class, AppDocumentationMetadataMapper.class}, imports = {Utils.class, StringUtils.class})
public interface AppDocumentationAttachmentVersionMapper {

    /**
     * Converts a persistence entity into the domain model, leaving the content out.
     *
     * @param entity the entity to convert
     * @return the converted domain model
     */
    @Mapping(target = "appDocumentationAttachmentId", source = "appDocumentationAttachment.id")
    @Mapping(target = "metadata", ignore = true)
    AppDocumentationAttachmentVersion toModel(AppDocumentationAttachmentVersionEntity entity);

    /**
     * Converts a domain model into the persistence entity, leaving the document, the file format
     * and the content to be set by the repository.
     *
     * @param model the domain model to convert
     * @return the converted entity
     */
    @Mapping(target = "appDocumentationAttachment", ignore = true)
    @Mapping(target = "mimeType", ignore = true)
    @Mapping(target = "content", ignore = true)
    AppDocumentationAttachmentVersionEntity toEntity(AppDocumentationAttachmentVersion model);

    /**
     * Converts a domain model into the outbound response DTO.
     *
     * @param model the domain model to convert
     * @return the converted response DTO
     */
    @Mapping(target = "uploadedAt", source = "createdAt")
    @Mapping(target = "uploadedBy", source = "createdBy")
    AppDocumentationAttachmentVersionOutputDTO toResponse(AppDocumentationAttachmentVersion model);

    /**
     * Converts a list of entities into domain models.
     *
     * @param entities the entities to convert
     * @return the converted domain models
     */
    List<AppDocumentationAttachmentVersion> toModelList(List<AppDocumentationAttachmentVersionEntity> entities);

    /**
     * Converts a list of domain models into response DTOs.
     *
     * @param models the domain models to convert
     * @return the converted response DTOs
     */
    List<AppDocumentationAttachmentVersionOutputDTO> toResponseList(List<AppDocumentationAttachmentVersion> models);

    /**
     * Builds the first attachment of a new document from its input and the uploaded file, as the
     * current one: the file name loses the folders some browsers send, the size and the SHA-256
     * come from the content, and a blank change note becomes {@code null}.
     *
     * @param inputDTO        the data of the new document, which carries the version of its first file
     * @param documentationAttachmentId identifier of the document the attachment belongs to
     * @param file            the uploaded file
     * @param mimeType        the file format matching the extension of the file
     * @param content         the bytes of the file
     * @return the attachment, ready to be stored
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "appDocumentationAttachmentId", source = "documentationAttachmentId")
    @Mapping(target = "versionLabel", source = "inputDTO.versionLabel")
    @Mapping(target = "fileName", expression = "java(Utils.baseName(file.getOriginalFilename()))")
    @Mapping(target = "mimeType", source = "mimeType")
    @Mapping(target = "metadata", ignore = true)
    @Mapping(target = "fileSize", expression = "java(Long.valueOf(content.length))")
    @Mapping(target = "checksumSha256", expression = "java(Utils.sha256Hex(content))")
    @Mapping(target = "current", constant = "true")
    @Mapping(target = "changeNote", expression = "java(StringUtils.trimToNull(inputDTO.getChangeNote()))")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    AppDocumentationAttachmentVersion toModelFromInput(AppDocumentationAttachmentInputDTO inputDTO, Long documentationAttachmentId, MultipartFile file, MimeType mimeType, byte[] content);

    /**
     * Builds a new attachment of an existing document from its input and the uploaded file, as the
     * current one: the file name loses the folders some browsers send, the size and the SHA-256
     * come from the content, and a blank change note becomes {@code null}.
     *
     * @param inputDTO        the data of the new attachment
     * @param documentationAttachmentId identifier of the document the attachment belongs to
     * @param file            the uploaded file
     * @param mimeType        the file format matching the extension of the file
     * @param content         the bytes of the file
     * @return the attachment, ready to be stored
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "appDocumentationAttachmentId", source = "documentationAttachmentId")
    @Mapping(target = "versionLabel", source = "inputDTO.versionLabel")
    @Mapping(target = "fileName", expression = "java(Utils.baseName(file.getOriginalFilename()))")
    @Mapping(target = "mimeType", source = "mimeType")
    @Mapping(target = "metadata", ignore = true)
    @Mapping(target = "fileSize", expression = "java(Long.valueOf(content.length))")
    @Mapping(target = "checksumSha256", expression = "java(Utils.sha256Hex(content))")
    @Mapping(target = "current", constant = "true")
    @Mapping(target = "changeNote", expression = "java(StringUtils.trimToNull(inputDTO.getChangeNote()))")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    AppDocumentationAttachmentVersion toModelFromInput(AppDocumentationAttachmentVersionInputDTO inputDTO, Long documentationAttachmentId, MultipartFile file, MimeType mimeType, byte[] content);
}
package es.caib.invai.back.service.mapper.application.documentation.attachment;

import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentInputDTO;
import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentOutputDTO;
import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentSimpleOutputDTO;
import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentUpdateInputDTO;
import es.caib.invai.back.persistence.model.application.documentation.attachment.AppDocumentationAttachmentEntity;
import es.caib.invai.back.service.mapper.catalog.documentStatus.DocumentStatusMapper;
import es.caib.invai.back.service.mapper.maintenance.documentation.documentCategory.DocumentCategoryMapper;
import es.caib.invai.back.service.model.application.documentation.attachment.AppDocumentationAttachment;
import es.caib.invai.back.service.model.catalog.documentStatus.DocumentStatus;
import es.caib.invai.back.service.model.maintenance.documentation.documentCategory.DocumentCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Objects;

/**
 * MapStruct mapper converting documents between entities, domain models and API transfer schemas.
 * The related records of the inbound payloads are carried as models holding only their
 * identifier; the repository turns them into references.
 *
 * @since 1.0.6
 */
@Mapper(componentModel = "spring", uses = {DocumentStatusMapper.class, DocumentCategoryMapper.class, AppDocumentationAttachmentVersionMapper.class})
public interface AppDocumentationAttachmentMapper {

    /**
     * Converts a persistence entity into the domain model. The attachments are left empty: the
     * facade loads them apart.
     *
     * @param entity the entity to convert
     * @return the converted domain model
     */
    @Mapping(target = "applicationId", source = "application.id")
    @Mapping(target = "currentAttachment", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    AppDocumentationAttachment toModel(AppDocumentationAttachmentEntity entity);

    /**
     * Converts a domain model into the persistence entity, leaving the related records to be set
     * by the repository.
     *
     * @param model the domain model to convert
     * @return the converted entity
     */
    @Mapping(target = "application", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "subcategory", ignore = true)
    @Mapping(target = "status", ignore = true)
    AppDocumentationAttachmentEntity toEntity(AppDocumentationAttachment model);

    /**
     * Converts a domain model into the detail response DTO.
     *
     * @param model the domain model to convert
     * @return the converted response DTO, with its attachments
     */
    @Mapping(target = "category", source = "category", qualifiedByName = "toSummaryResponse")
    @Mapping(target = "subcategory", source = "subcategory", qualifiedByName = "toSummaryResponse")
    AppDocumentationAttachmentOutputDTO toResponse(AppDocumentationAttachment model);

    /**
     * Converts a domain model carrying its current version into one row of the listing.
     *
     * @param model the domain model to convert
     * @return the converted response DTO
     */
    @Mapping(target = "mimeType", source = "currentAttachment.mimeType")
    @Mapping(target = "versionLabel", source = "currentAttachment.versionLabel")
    @Mapping(target = "currentAttachmentId", source = "currentAttachment.id")
    @Mapping(target = "category", source = "category", qualifiedByName = "toSummaryResponse")
    @Mapping(target = "subcategory", source = "subcategory", qualifiedByName = "toSummaryResponse")
    AppDocumentationAttachmentSimpleOutputDTO toSimpleResponse(AppDocumentationAttachment model);

    /**
     * Converts an inbound input DTO into a new domain model, leaving audit fields unset.
     *
     * @param inputDTO the input DTO to convert
     * @return the converted domain model
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", expression = "java(newCategory(inputDTO.getCategoryId()))")
    @Mapping(target = "subcategory", expression = "java(newCategory(inputDTO.getSubcategoryId()))")
    @Mapping(target = "status", expression = "java(newStatus(inputDTO.getStatusId()))")
    @Mapping(target = "currentAttachment", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    AppDocumentationAttachment toModelFromInput(AppDocumentationAttachmentInputDTO inputDTO);

    /**
     * Applies the fields of an inbound update DTO onto an existing domain model, leaving the
     * application, the audit fields and the attachments untouched.
     *
     * @param inputDTO the input DTO holding the new values
     * @param model    the target domain model to update in place
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "applicationId", ignore = true)
    @Mapping(target = "category", expression = "java(newCategory(inputDTO.getCategoryId()))")
    @Mapping(target = "subcategory", expression = "java(newCategory(inputDTO.getSubcategoryId()))")
    @Mapping(target = "status", expression = "java(newStatus(inputDTO.getStatusId()))")
    @Mapping(target = "currentAttachment", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateModelFromInput(AppDocumentationAttachmentUpdateInputDTO inputDTO, @MappingTarget AppDocumentationAttachment model);

    /**
     * Builds a category or subcategory holding only the given identifier.
     *
     * @param id the identifier of the category or subcategory, may be {@code null}
     * @return the category, or {@code null} if there is no identifier
     */
    default DocumentCategory newCategory(Long id) {
        if (Objects.isNull(id)) {
            return null;
        }
        DocumentCategory category = new DocumentCategory();
        category.setId(id);
        return category;
    }

    /**
     * Builds a state holding only the given identifier.
     *
     * @param id the identifier of the state
     * @return the state
     */
    default DocumentStatus newStatus(Long id) {
        DocumentStatus status = new DocumentStatus();
        status.setId(id);
        return status;
    }
}

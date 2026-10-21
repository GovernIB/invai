package es.caib.invai.back.service.mapper.application.documentation.attachment;

import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentInputDTO;
import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentOutputDTO;
import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentSimpleOutputDTO;
import es.caib.invai.back.interna.application.documentation.attachment.DTO.AppDocumentationAttachmentUpdateInputDTO;
import es.caib.invai.back.interna.maintenance.documentation.category.DTO.DocumentCategoryOutputDTO;
import es.caib.invai.back.persistence.model.application.documentation.attachment.AppDocumentationAttachmentEntity;
import es.caib.invai.back.service.mapper.catalog.appSection.AppSectionMapper;
import es.caib.invai.back.service.mapper.catalog.documentStatus.DocumentStatusMapper;
import es.caib.invai.back.service.mapper.maintenance.documentation.documentCategory.DocumentCategoryMapper;
import es.caib.invai.back.service.model.application.documentation.attachment.AppDocumentationAttachment;
import es.caib.invai.back.service.model.catalog.appSection.AppSection;
import es.caib.invai.back.service.model.catalog.documentStatus.DocumentStatus;
import es.caib.invai.back.service.model.maintenance.documentation.documentCategory.DocumentCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

/**
 * MapStruct mapper converting documents between entities, domain models and API transfer schemas.
 * The related records of the inbound payloads are carried as models holding only their
 * identifier; the repository turns them into references.
 *
 * @since 1.0.6
 */
@Mapper(componentModel = "spring", uses = {AppSectionMapper.class, DocumentStatusMapper.class, DocumentCategoryMapper.class, AppDocumentationAttachmentVersionMapper.class})
public interface AppDocumentationAttachmentMapper {

    /**
     * Converts a persistence entity into the domain model. The attachments are left empty: the
     * facade loads them apart.
     *
     * @param entity the entity to convert
     * @return the converted domain model
     */
    @Mapping(target = "applicationId", source = "application.id")
    @Mapping(target = "parentCategory", source = "documentCategory.parent")
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
    @Mapping(target = "section", ignore = true)
    @Mapping(target = "documentCategory", ignore = true)
    @Mapping(target = "status", ignore = true)
    AppDocumentationAttachmentEntity toEntity(AppDocumentationAttachment model);

    /**
     * Converts a domain model into the detail response DTO.
     *
     * @param model the domain model to convert
     * @return the converted response DTO, with its attachments
     */
    @Mapping(target = "category", source = "category", qualifiedByName = "toCategoryOutput")
    @Mapping(target = "subcategory", source = "subcategory", qualifiedByName = "toCategoryOutput")
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
    @Mapping(target = "category", source = "category", qualifiedByName = "toCategoryOutput")
    @Mapping(target = "subcategory", source = "subcategory", qualifiedByName = "toCategoryOutput")
    AppDocumentationAttachmentSimpleOutputDTO toSimpleResponse(AppDocumentationAttachment model);

    /**
     * Converts an inbound input DTO into a new domain model, leaving audit fields unset.
     *
     * @param inputDTO the input DTO to convert
     * @return the converted domain model
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "section", expression = "java(newSection(inputDTO.getSectionId()))")
    @Mapping(target = "documentCategory", expression = "java(newCategory(inputDTO.getCategoryId()))")
    @Mapping(target = "status", expression = "java(newStatus(inputDTO.getStatusId()))")
    @Mapping(target = "parentCategory", ignore = true)
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
    @Mapping(target = "section", expression = "java(newSection(inputDTO.getSectionId()))")
    @Mapping(target = "documentCategory", expression = "java(newCategory(inputDTO.getCategoryId()))")
    @Mapping(target = "status", expression = "java(newStatus(inputDTO.getStatusId()))")
    @Mapping(target = "parentCategory", ignore = true)
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
     * Converts a category or subcategory into its response DTO leaving out the subcategories and
     * the deletion timestamp, which a document never shows.
     *
     * @param model the category or subcategory to convert
     * @return the converted response DTO
     */
    @Named("toCategoryOutput")
    @Mapping(target = "subcategories", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    DocumentCategoryOutputDTO toCategoryOutput(DocumentCategory model);

    /**
     * Builds a section holding only the given identifier.
     *
     * @param id the identifier of the section
     * @return the section
     */
    default AppSection newSection(Long id) {
        AppSection section = new AppSection();
        section.setId(id);
        return section;
    }

    /**
     * Builds a category holding only the given identifier.
     *
     * @param id the identifier of the category or subcategory
     * @return the category
     */
    default DocumentCategory newCategory(Long id) {
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

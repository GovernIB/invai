package es.caib.invai.back.service.mapper.maintenance.documentation.documentCategory;

import es.caib.invai.back.interna.maintenance.documentation.category.DTO.DocumentCategoryInputDTO;
import es.caib.invai.back.interna.maintenance.documentation.category.DTO.DocumentCategoryOutputDTO;
import es.caib.invai.back.persistence.model.maintenance.documentation.documentCategory.DocumentCategoryEntity;
import es.caib.invai.back.service.model.maintenance.documentation.documentCategory.DocumentCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.Objects;

/**
 * MapStruct data mapping abstraction interface providing structural state conversions across
 * documentation category database entities, business domain models, and API transfer schemas.
 *
 * @since 1.0.6
 */
@Mapper(componentModel = "spring")
public interface DocumentCategoryMapper {

    /**
     * Converts a persistence entity into the corresponding domain model. The subcategories are
     * left empty: the repository fills them in when it loads a category.
     *
     * @param entity the entity to convert
     * @return the converted domain model
     */
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "subcategories", ignore = true)
    DocumentCategory toModel(DocumentCategoryEntity entity);

    /**
     * Converts a domain model into the corresponding persistence entity, carrying the parent as a
     * reference holding only its identifier.
     *
     * @param model the domain model to convert
     * @return the converted entity
     */
    @Mapping(target = "parent", source = "parentId", qualifiedByName = "mapParentReference")
    DocumentCategoryEntity toEntity(DocumentCategory model);

    /**
     * Converts a domain model into the corresponding outbound response DTO.
     *
     * @param model the domain model to convert
     * @return the converted response DTO, with its subcategories
     */
    DocumentCategoryOutputDTO toResponse(DocumentCategory model);

    /**
     * Converts an inbound input DTO into a new domain model, leaving audit fields unset.
     *
     * @param inputDTO the input DTO to convert
     * @return the converted domain model
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subcategories", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    DocumentCategory toModelFromInput(DocumentCategoryInputDTO inputDTO);

    /**
     * Applies the fields of an inbound input DTO onto an existing domain model, leaving audit
     * fields and subcategories untouched.
     *
     * @param inputDTO the input DTO holding the new values
     * @param model    the target domain model to update in place
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subcategories", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateModelFromInput(DocumentCategoryInputDTO inputDTO, @MappingTarget DocumentCategory model);

    /**
     * Builds a detached parent reference holding only the identifier.
     *
     * @param parentId identifier of the parent category
     * @return a reference carrying only that identifier, or {@code null} if there is no parent
     */
    @Named("mapParentReference")
    default DocumentCategoryEntity mapParentReference(Long parentId) {
        if (Objects.isNull(parentId)) {
            return null;
        }
        DocumentCategoryEntity parent = new DocumentCategoryEntity();
        parent.setId(parentId);
        return parent;
    }
}

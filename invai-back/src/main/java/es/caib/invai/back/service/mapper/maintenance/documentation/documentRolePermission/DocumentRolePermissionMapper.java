package es.caib.invai.back.service.mapper.maintenance.documentation.documentRolePermission;

import es.caib.invai.back.interna.maintenance.documentation.rolePermission.DTO.DocumentRolePermissionInputDTO;
import es.caib.invai.back.interna.maintenance.documentation.rolePermission.DTO.DocumentRolePermissionOutputDTO;
import es.caib.invai.back.persistence.model.maintenance.documentation.documentCategory.DocumentCategoryEntity;
import es.caib.invai.back.persistence.model.maintenance.documentation.documentRolePermission.DocumentRolePermissionEntity;
import es.caib.invai.back.persistence.model.maintenance.role.RoleEntity;
import es.caib.invai.back.service.model.maintenance.documentation.documentRolePermission.DocumentRolePermission;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.Objects;

/**
 * MapStruct data mapping abstraction interface providing structural state conversions across
 * documentation permission database entities, business domain models, and API transfer schemas.
 *
 * @since 1.0.7
 */
@Mapper(componentModel = "spring")
public interface DocumentRolePermissionMapper {

    /**
     * Converts a persistence entity into the corresponding domain model, flattening the role and
     * the category.
     *
     * @param entity the entity to convert
     * @return the converted domain model
     */
    @Mapping(target = "roleId", source = "role.id")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "roleName", source = "role.name")
    @Mapping(target = "roleDescription", source = "role.description")
    DocumentRolePermission toModel(DocumentRolePermissionEntity entity);

    /**
     * Converts a domain model into the corresponding persistence entity, carrying the role and
     * the category as references holding only their identifier.
     *
     * @param model the domain model to convert
     * @return the converted entity
     */
    @Mapping(target = "role", source = "roleId", qualifiedByName = "mapRoleReference")
    @Mapping(target = "category", source = "categoryId", qualifiedByName = "mapCategoryReference")
    DocumentRolePermissionEntity toEntity(DocumentRolePermission model);

    /**
     * Converts a domain model into the corresponding outbound response DTO.
     *
     * @param model the domain model to convert
     * @return the converted response DTO
     */
    @Mapping(target = "name", source = "roleName")
    @Mapping(target = "description", source = "roleDescription")
    DocumentRolePermissionOutputDTO toResponse(DocumentRolePermission model);

    /**
     * Applies the permissions of an inbound input DTO onto an existing domain model, leaving
     * everything else untouched.
     *
     * @param inputDTO the input DTO holding the new permissions
     * @param model    the target domain model to update in place
     */
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "canRead")
    @Mapping(target = "canUpload")
    @Mapping(target = "canEdit")
    @Mapping(target = "canDelete")
    void updateModelFromInput(DocumentRolePermissionInputDTO inputDTO, @MappingTarget DocumentRolePermission model);

    /**
     * Builds a role reference holding only the given identifier.
     *
     * @param roleId identifier of the role, may be {@code null}
     * @return the reference, or {@code null} if no identifier is given
     */
    @Named("mapRoleReference")
    default RoleEntity mapRoleReference(Long roleId) {
        if (Objects.isNull(roleId)) {
            return null;
        }
        RoleEntity role = new RoleEntity();
        role.setId(roleId);
        return role;
    }

    /**
     * Builds a category reference holding only the given identifier.
     *
     * @param categoryId identifier of the category, may be {@code null}
     * @return the reference, or {@code null} if no identifier is given
     */
    @Named("mapCategoryReference")
    default DocumentCategoryEntity mapCategoryReference(Long categoryId) {
        if (Objects.isNull(categoryId)) {
            return null;
        }
        DocumentCategoryEntity category = new DocumentCategoryEntity();
        category.setId(categoryId);
        return category;
    }
}

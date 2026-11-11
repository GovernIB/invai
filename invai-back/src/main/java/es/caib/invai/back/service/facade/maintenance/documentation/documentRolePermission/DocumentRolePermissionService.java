package es.caib.invai.back.service.facade.maintenance.documentation.documentRolePermission;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.maintenance.documentation.rolePermission.DTO.DocumentRoleCategoryOutputDTO;
import es.caib.invai.back.interna.maintenance.documentation.rolePermission.DTO.DocumentRolePermissionOutputDTO;
import es.caib.invai.back.interna.maintenance.documentation.rolePermission.DTO.DocumentRolePermissionUpdateInputDTO;
import es.caib.invai.back.interna.maintenance.documentation.rolePermission.DTO.DocumentUserPermissionOutputDTO;
import es.caib.invai.back.service.model.maintenance.documentation.documentRolePermission.DocumentPermission;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Set;

/**
 * Service facade contract defining the use cases of the documentation permissions of the roles of
 * the INVAI application, which are set on each category of documents.
 *
 * @since 1.0.7
 */
public interface DocumentRolePermissionService {

    /**
     * Retrieves the permissions every role has in a category, sorted by role name. Every role that
     * has permissions in any category is listed, with all its permissions set to {@code false} in
     * this one if it has none configured in it.
     *
     * @param categoryId identifier of the category
     * @return the permissions of every role in the category
     * @throws BusinessRuleException if the category doesn't exist, isn't active or is a subcategory
     */
    List<DocumentRolePermissionOutputDTO> getByCategory(Long categoryId);

    /**
     * Retrieves the categories assigned to a role and what the role can do in each of them. A
     * category is assigned to the role when the role has some permission in it; deleted categories
     * are left out.
     *
     * @param roleName name of the role
     * @return the categories of the role, each in full with the permissions of the role in it, sorted by category
     * @throws BusinessRuleException if the role has no documentation permissions configured
     */
    List<DocumentRoleCategoryOutputDTO> getByRole(String roleName);

    /**
     * Sets the permissions of the listed roles in a category, all or nothing: the roles that are
     * not listed are left as they are.
     *
     * @param categoryId identifier of the category
     * @param inputDTO   the permissions to set, one entry per role
     * @return the permissions of every role in the category, once updated
     * @throws BusinessRuleException if the category doesn't exist, isn't active or is a subcategory,
     *                               or a role is listed twice or has no permissions configured in any category
     */
    List<DocumentRolePermissionOutputDTO> update(Long categoryId, DocumentRolePermissionUpdateInputDTO inputDTO);

    /**
     * Gives a role that has just been added to the application the permission to read the documents
     * of every active category, and no other. Does nothing if a role with that name already has its
     * permissions configured.
     *
     * @param roleId   identifier of the role
     * @param roleName name of the role
     */
    void assignDefaultPermissions(Long roleId, String roleName);

    /**
     * Gives every role that has permissions the permission to read the documents of a category that
     * has just been created or reactivated, and no other. The roles that already have permissions
     * in it are left as they are.
     *
     * @param categoryId identifier of the category
     */
    void assignDefaultPermissionsToCategory(Long categoryId);

    /**
     * Removes the permissions of a role that has been taken off the application, in every category,
     * as if every permission had been taken away from it. Does nothing if the role has none configured.
     *
     * @param roleId identifier of the role
     */
    void removePermissions(Long roleId);

    /**
     * Retrieves what the logged user can do in each of the categories assigned to them, adding up
     * the permissions of all their roles. A category is assigned to the user when any of their
     * roles has some permission in it. {@code INV_SUPER} can do everything in every active category.
     *
     * @return the permissions of the logged user in their categories, sorted by category
     */
    List<DocumentUserPermissionOutputDTO> getCurrentUserPermissions();

    /**
     * Retrieves the identifiers of the categories whose documents the logged user can read.
     *
     * @return the identifiers, or {@code null} if the user can read every category ({@code INV_SUPER})
     */
    Set<Long> getReadableCategoryIds();

    /**
     * Checks that the logged user has a permission in a category.
     *
     * @param permission the permission the user needs
     * @param categoryId identifier of the category
     * @throws AccessDeniedException if none of the roles of the user has it in that category
     */
    void requirePermission(DocumentPermission permission, Long categoryId);
}

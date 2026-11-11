package es.caib.invai.back.persistence.repository.maintenance.documentation.documentRolePermission;

import es.caib.invai.back.service.model.maintenance.documentation.documentRolePermission.DocumentRolePermission;
import es.caib.invai.back.service.model.maintenance.role.Role;

import java.util.Collection;
import java.util.List;

/**
 * Outbound repository port defining the persistence operations for the documentation permissions
 * of the roles in each category.
 *
 * @since 1.0.7
 */
public interface DocumentRolePermissionRepository {

    /**
     * Retrieves the permissions of every role in a category, sorted by role name.
     *
     * @param categoryId identifier of the category
     * @return the permissions of the category
     */
    List<DocumentRolePermission> findByCategoryId(Long categoryId);

    /**
     * Retrieves the permissions of a role in every category.
     *
     * @param roleId identifier of the role
     * @return the permissions of the role, one per category
     */
    List<DocumentRolePermission> findByRoleId(Long roleId);

    /**
     * Retrieves the permissions of a role in a category.
     *
     * @param roleId     identifier of the role
     * @param categoryId identifier of the category
     * @return the permissions, or {@code null} if the role has none configured in that category
     */
    DocumentRolePermission findByRoleIdAndCategoryId(Long roleId, Long categoryId);

    /**
     * Tells whether any role with the given name has permissions configured.
     *
     * @param roleName name of the role
     * @return {@code true} if a role with that name has permissions
     */
    boolean existsByRoleName(String roleName);

    /**
     * Retrieves the permissions of the roles with any of the given names, in every category.
     *
     * @param roleNames names of the roles
     * @return the permissions of those roles that have any configured
     */
    List<DocumentRolePermission> findByRoleNames(Collection<String> roleNames);

    /**
     * Retrieves the roles that have permissions configured.
     *
     * @return the roles, without repeating any
     */
    List<Role> findRoles();

    /**
     * Persists the permissions of a role in a category where it had none.
     *
     * @param documentRolePermission the permissions to persist
     * @return the persisted permissions
     */
    DocumentRolePermission create(DocumentRolePermission documentRolePermission);

    /**
     * Persists the changes made to the permissions of a role in a category.
     *
     * @param documentRolePermission the permissions with the new values
     * @param id                     identifier of the permissions row
     * @return the persisted permissions
     */
    DocumentRolePermission update(DocumentRolePermission documentRolePermission, Long id);

    /**
     * Deletes for good the permissions of a role in a category, leaving the audit snapshot.
     *
     * @param documentRolePermission the permissions to delete
     */
    void delete(DocumentRolePermission documentRolePermission);
}

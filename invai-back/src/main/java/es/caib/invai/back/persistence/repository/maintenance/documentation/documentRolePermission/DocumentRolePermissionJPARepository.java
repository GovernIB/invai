package es.caib.invai.back.persistence.repository.maintenance.documentation.documentRolePermission;

import es.caib.invai.back.persistence.model.maintenance.documentation.documentRolePermission.DocumentRolePermissionEntity;
import es.caib.invai.back.persistence.model.maintenance.role.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Native Spring Data JPA repository layer interface providing CRUD operations and query methods
 * targeting {@link DocumentRolePermissionEntity} records.
 *
 * @since 1.0.7
 */
@Repository
public interface DocumentRolePermissionJPARepository extends JpaRepository<DocumentRolePermissionEntity, Long> {

    /**
     * Finds the rows of a category, sorted by the name of their role.
     *
     * @param categoryId identifier of the category
     * @return the rows of the category
     */
    List<DocumentRolePermissionEntity> findByCategoryIdOrderByRoleNameAsc(Long categoryId);

    /**
     * Finds the rows of the given role.
     *
     * @param roleId identifier of the role
     * @return the rows of the role, one per category
     */
    List<DocumentRolePermissionEntity> findByRoleId(Long roleId);

    /**
     * Finds the row of the given role in the given category.
     *
     * @param roleId     identifier of the role
     * @param categoryId identifier of the category
     * @return the row, if the role has one in that category
     */
    Optional<DocumentRolePermissionEntity> findByRoleIdAndCategoryId(Long roleId, Long categoryId);

    /**
     * Tells whether any role with the given name has a row.
     *
     * @param roleName name of the role
     * @return {@code true} if a role with that name has a row
     */
    boolean existsByRoleName(String roleName);

    /**
     * Finds the rows of the roles with any of the given names.
     *
     * @param roleNames names of the roles
     * @return the rows of those roles that have any, one per category
     */
    List<DocumentRolePermissionEntity> findByRoleNameIn(Collection<String> roleNames);

    /**
     * Finds the roles that have rows.
     *
     * @return the roles, without repeating any
     */
    @Query("select distinct p.role from DocumentRolePermissionEntity p")
    List<RoleEntity> findDistinctRoles();
}

package es.caib.invai.back.persistence.repository.maintenance.development.role;

import es.caib.invai.back.persistence.model.maintenance.development.role.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing CRUD operations, custom query methods,
 * and constraint validation checks targeting live {@link RoleEntity} records.
 *
 * @since 1.0.2
 */
@Repository
public interface RoleJPARepository extends JpaRepository<RoleEntity, Long>, JpaSpecificationExecutor<RoleEntity> {

    /**
     * Checks whether an active (not logically deleted) role exists with the given name.
     *
     * @param name the role name to check
     * @return {@code true} if an active role with that name exists
     */
    boolean existsByNameAndDeletedAtIsNull(String name);

    /**
     * Checks whether another active role (excluding the given identifier) exists with the given name.
     *
     * @param name the role name to check
     * @param id   the identifier to exclude from the check
     * @return {@code true} if an active role other than the given identifier owns that name
     */
    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id);
}

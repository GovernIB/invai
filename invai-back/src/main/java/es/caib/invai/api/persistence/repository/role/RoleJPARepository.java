package es.caib.invai.api.persistence.repository.role;

import es.caib.invai.api.persistence.model.RoleEntity;
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

    boolean existsByNameAndDeletedAtIsNull(String name);

    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id);
}

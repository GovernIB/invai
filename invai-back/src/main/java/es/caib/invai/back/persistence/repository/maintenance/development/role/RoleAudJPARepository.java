package es.caib.invai.back.persistence.repository.maintenance.development.role;

import es.caib.invai.back.persistence.model.maintenance.development.role.RoleAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing basic CRUD database operations
 * targeting historical {@link RoleAudEntity} snapshots.
 *
 * @since 1.0.2
 */
@Repository
public interface RoleAudJPARepository extends JpaRepository<RoleAudEntity, Long> {
}

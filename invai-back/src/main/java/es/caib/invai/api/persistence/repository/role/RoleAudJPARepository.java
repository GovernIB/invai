package es.caib.invai.api.persistence.repository.role;

import es.caib.invai.api.persistence.model.RoleAudEntity;
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

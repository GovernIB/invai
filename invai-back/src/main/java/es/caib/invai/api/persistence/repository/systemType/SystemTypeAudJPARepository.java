package es.caib.invai.api.persistence.repository.systemType;

import es.caib.invai.api.persistence.model.SystemTypeAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing basic CRUD database operations
 * targeting historical {@link SystemTypeAudEntity} snapshots.
 *
 * @since 1.0.0
 */
@Repository
public interface SystemTypeAudJPARepository extends JpaRepository<SystemTypeAudEntity, Long> {
}
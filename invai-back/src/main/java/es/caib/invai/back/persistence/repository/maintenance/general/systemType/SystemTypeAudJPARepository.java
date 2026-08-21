package es.caib.invai.back.persistence.repository.maintenance.general.systemType;

import es.caib.invai.back.persistence.model.maintenance.general.systemType.SystemTypeAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing basic CRUD database operations
 * targeting historical {@link SystemTypeAudEntity} snapshots.
 *
 * @since 1.0.1
 */
@Repository
public interface SystemTypeAudJPARepository extends JpaRepository<SystemTypeAudEntity, Long> {
}
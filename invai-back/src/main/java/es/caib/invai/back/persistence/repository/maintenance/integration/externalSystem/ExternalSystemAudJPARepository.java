package es.caib.invai.back.persistence.repository.maintenance.integration.externalSystem;

import es.caib.invai.back.persistence.model.maintenance.integration.externalSystem.ExternalSystemAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing basic CRUD database operations
 * targeting historical {@link ExternalSystemAudEntity} snapshots.
 *
 * @since 1.0.5
 */
@Repository
public interface ExternalSystemAudJPARepository extends JpaRepository<ExternalSystemAudEntity, Long> {
}

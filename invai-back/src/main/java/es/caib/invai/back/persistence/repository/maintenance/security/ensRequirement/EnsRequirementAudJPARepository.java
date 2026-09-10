package es.caib.invai.back.persistence.repository.maintenance.security.ensRequirement;

import es.caib.invai.back.persistence.model.maintenance.security.ensRequirement.EnsRequirementAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing basic CRUD database operations
 * targeting historical {@link EnsRequirementAudEntity} snapshots.
 *
 * @since 1.0.4
 */
@Repository
public interface EnsRequirementAudJPARepository extends JpaRepository<EnsRequirementAudEntity, Long> {
}

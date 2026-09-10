package es.caib.invai.back.persistence.repository.maintenance.complianceSituation;

import es.caib.invai.back.persistence.model.maintenance.complianceSituation.ComplianceSituationAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing basic CRUD database operations
 * targeting historical {@link ComplianceSituationAudEntity} snapshots.
 *
 * @since 1.0.4
 */
@Repository
public interface ComplianceSituationAudJPARepository extends JpaRepository<ComplianceSituationAudEntity, Long> {
}

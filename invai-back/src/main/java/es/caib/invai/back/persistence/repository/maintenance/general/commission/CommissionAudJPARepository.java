package es.caib.invai.back.persistence.repository.maintenance.general.commission;

import es.caib.invai.back.persistence.model.maintenance.general.commission.CommissionAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing basic CRUD database operations
 * targeting historical {@link CommissionAudEntity} snapshots.
 *
 * @since 1.0.1
 */
@Repository
public interface CommissionAudJPARepository extends JpaRepository<CommissionAudEntity, Long> {
}
package es.caib.invai.api.persistence.repository.commission;

import es.caib.invai.api.persistence.model.CommissionAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing basic CRUD database operations
 * targeting historical {@link CommissionAudEntity} snapshots.
 *
 * @since 1.0.0
 */
@Repository
public interface CommissionAudJPARepository extends JpaRepository<CommissionAudEntity, Long> {
}
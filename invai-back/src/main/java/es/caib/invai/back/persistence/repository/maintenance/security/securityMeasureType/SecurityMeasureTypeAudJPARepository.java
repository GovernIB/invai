package es.caib.invai.back.persistence.repository.maintenance.security.securityMeasureType;

import es.caib.invai.back.persistence.model.maintenance.security.securityMeasureType.SecurityMeasureTypeAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing basic CRUD database operations
 * targeting historical {@link SecurityMeasureTypeAudEntity} snapshots.
 *
 * @since 1.0.4
 */
@Repository
public interface SecurityMeasureTypeAudJPARepository extends JpaRepository<SecurityMeasureTypeAudEntity, Long> {
}

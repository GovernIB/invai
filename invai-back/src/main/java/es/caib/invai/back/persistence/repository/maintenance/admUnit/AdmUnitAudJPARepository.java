package es.caib.invai.back.persistence.repository.maintenance.admUnit;

import es.caib.invai.back.persistence.model.maintenance.admUnit.AdmUnitAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing basic CRUD database operations
 * targeting historical {@link AdmUnitAudEntity} snapshots.
 *
 * @since 1.0.1
 */
@Repository
public interface AdmUnitAudJPARepository extends JpaRepository<AdmUnitAudEntity, Long> {
}
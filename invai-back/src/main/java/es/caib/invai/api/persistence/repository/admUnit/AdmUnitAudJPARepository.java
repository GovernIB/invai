package es.caib.invai.api.persistence.repository.admUnit;

import es.caib.invai.api.persistence.model.AdmUnitAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing basic CRUD database operations
 * targeting historical {@link AdmUnitAudEntity} snapshots.
 *
 * @since 1.0.0
 */
@Repository
public interface AdmUnitAudJPARepository extends JpaRepository<AdmUnitAudEntity, Long> {
}
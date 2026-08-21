package es.caib.invai.back.persistence.repository.maintenance.development.technology;

import es.caib.invai.back.persistence.model.maintenance.development.technology.TechnologyAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing basic CRUD database operations
 * targeting historical {@link TechnologyAudEntity} snapshots.
 *
 * @since 1.0.2
 */
@Repository
public interface TechnologyAudJPARepository extends JpaRepository<TechnologyAudEntity, Long> {
}

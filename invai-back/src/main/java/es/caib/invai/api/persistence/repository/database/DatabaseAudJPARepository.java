package es.caib.invai.api.persistence.repository.database;

import es.caib.invai.api.persistence.model.DatabaseAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA Infrastructure Interface managing persistence actions
 * for the historical {@link DatabaseAudEntity} audit entity.
 *
 * @since 1.0.2
 */
@Repository
public interface DatabaseAudJPARepository extends JpaRepository<DatabaseAudEntity, Long> {
}
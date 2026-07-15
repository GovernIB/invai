package es.caib.invai.api.persistence.repository.application;

import es.caib.invai.api.persistence.model.ApplicationAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing basic CRUD database operations
 * targeting historical {@link ApplicationAudEntity} snapshots.
 *
 * @since 1.0.0
 */
@Repository
public interface ApplicationAudJPARepository extends JpaRepository<ApplicationAudEntity, Long> {
}
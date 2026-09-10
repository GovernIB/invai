package es.caib.invai.back.persistence.repository.maintenance.security.webContext;

import es.caib.invai.back.persistence.model.maintenance.security.webContext.WebContextAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing basic CRUD database operations
 * targeting historical {@link WebContextAudEntity} snapshots.
 *
 * @since 1.0.4
 */
@Repository
public interface WebContextAudJPARepository extends JpaRepository<WebContextAudEntity, Long> {
}

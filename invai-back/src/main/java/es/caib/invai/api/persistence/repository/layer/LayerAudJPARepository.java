package es.caib.invai.api.persistence.repository.layer;

import es.caib.invai.api.persistence.model.LayerAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing basic CRUD database operations
 * targeting historical {@link LayerAudEntity} snapshots.
 *
 * @since 1.0.2
 */
@Repository
public interface LayerAudJPARepository extends JpaRepository<LayerAudEntity, Long> {
}

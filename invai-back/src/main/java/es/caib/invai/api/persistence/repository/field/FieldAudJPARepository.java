package es.caib.invai.api.persistence.repository.field;

import es.caib.invai.api.persistence.model.FieldAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing basic CRUD database operations
 * targeting historical {@link FieldAudEntity} snapshots.
 *
 * @since 1.0.0
 */
@Repository
public interface FieldAudJPARepository extends JpaRepository<FieldAudEntity, Long> {
}
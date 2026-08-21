package es.caib.invai.back.persistence.repository.maintenance.general.field;

import es.caib.invai.back.persistence.model.maintenance.general.field.FieldAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing basic CRUD database operations
 * targeting historical {@link FieldAudEntity} snapshots.
 *
 * @since 1.0.1
 */
@Repository
public interface FieldAudJPARepository extends JpaRepository<FieldAudEntity, Long> {
}
package es.caib.invai.back.persistence.repository.maintenance.classificationSegment;

import es.caib.invai.back.persistence.model.maintenance.classificationSegment.ClassificationSegmentAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing basic CRUD database operations
 * targeting historical {@link ClassificationSegmentAudEntity} snapshots.
 *
 * @since 1.0.4
 */
@Repository
public interface ClassificationSegmentAudJPARepository extends JpaRepository<ClassificationSegmentAudEntity, Long> {
}

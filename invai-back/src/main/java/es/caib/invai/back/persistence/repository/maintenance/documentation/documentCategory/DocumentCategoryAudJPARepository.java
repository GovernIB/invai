package es.caib.invai.back.persistence.repository.maintenance.documentation.documentCategory;

import es.caib.invai.back.persistence.model.maintenance.documentation.documentCategory.DocumentCategoryAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing basic CRUD database operations
 * targeting historical {@link DocumentCategoryAudEntity} snapshots.
 *
 * @since 1.0.6
 */
@Repository
public interface DocumentCategoryAudJPARepository extends JpaRepository<DocumentCategoryAudEntity, Long> {
}

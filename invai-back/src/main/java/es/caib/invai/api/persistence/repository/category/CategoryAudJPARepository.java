package es.caib.invai.api.persistence.repository.category;

import es.caib.invai.api.persistence.model.CategoryAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing basic CRUD database operations
 * targeting historical {@link CategoryAudEntity} snapshots.
 *
 * @since 1.0.0
 */
@Repository
public interface CategoryAudJPARepository extends JpaRepository<CategoryAudEntity, Long> {
}
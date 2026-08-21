package es.caib.invai.back.persistence.repository.maintenance.general.category;

import es.caib.invai.back.persistence.model.maintenance.general.category.CategoryAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing basic CRUD database operations
 * targeting historical {@link CategoryAudEntity} snapshots.
 *
 * @since 1.0.1
 */
@Repository
public interface CategoryAudJPARepository extends JpaRepository<CategoryAudEntity, Long> {
}
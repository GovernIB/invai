package es.caib.invai.api.persistence.repository.system;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import es.caib.invai.api.persistence.model.SystemAudEntity;

/**
 * Native Spring Data JPA interface mapping historical snapshot persistence
 * for the {@link SystemAudEntity} tracking data store.
 *
 * @since 1.0.1
 */
@Repository
public interface SystemAudJPARepository extends JpaRepository<SystemAudEntity, Long> {
}
package es.caib.invai.back.persistence.repository.maintenance.systems.system;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import es.caib.invai.back.persistence.model.maintenance.systems.system.SystemAudEntity;

/**
 * Native Spring Data JPA interface mapping historical snapshot persistence
 * for the {@link SystemAudEntity} tracking data store.
 *
 * @since 1.0.1
 */
@Repository
public interface SystemAudJPARepository extends JpaRepository<SystemAudEntity, Long> {
}
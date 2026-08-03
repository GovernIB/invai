package es.caib.invai.api.persistence.repository.environment;

import es.caib.invai.api.persistence.model.EnvironmentAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA interface mapping historical snapshot persistence for the {@link EnvironmentAudEntity}.
 *
 * @since 1.0.1
 */
@Repository
public interface EnvironmentAudJPARepository extends JpaRepository<EnvironmentAudEntity, Long> {
}
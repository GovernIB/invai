package es.caib.invai.back.persistence.repository.application.development.core;

import es.caib.invai.back.persistence.model.application.development.core.AppDevelopmentAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository persisting the historical audit trail for {@link AppDevelopmentAudEntity}.
 *
 * @since 1.0.2
 */
public interface AppDevelopmentAudJPARepository extends JpaRepository<AppDevelopmentAudEntity, Long> {
}

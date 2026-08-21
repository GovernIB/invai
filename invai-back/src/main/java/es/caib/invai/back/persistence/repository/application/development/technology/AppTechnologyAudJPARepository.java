package es.caib.invai.back.persistence.repository.application.development.technology;

import es.caib.invai.back.persistence.model.application.development.technology.AppTechnologyAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository persisting the historical audit trail for {@link AppTechnologyAudEntity}.
 *
 * @since 1.0.2
 */
public interface AppTechnologyAudJPARepository extends JpaRepository<AppTechnologyAudEntity, Long> {
}

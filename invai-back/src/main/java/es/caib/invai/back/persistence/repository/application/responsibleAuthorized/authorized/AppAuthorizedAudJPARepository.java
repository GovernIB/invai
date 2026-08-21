package es.caib.invai.back.persistence.repository.application.responsibleAuthorized.authorized;

import es.caib.invai.back.persistence.model.application.responsibleAuthorized.authorized.AppAuthorizedAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository persisting the historical audit trail for {@link AppAuthorizedAudEntity}.
 *
 * @since 1.0.3
 */
public interface AppAuthorizedAudJPARepository extends JpaRepository<AppAuthorizedAudEntity, Long> {
}

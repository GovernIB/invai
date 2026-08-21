package es.caib.invai.back.persistence.repository.application.development.provider;

import es.caib.invai.back.persistence.model.application.development.provider.AppProviderAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository persisting the historical audit trail for {@link AppProviderAudEntity}.
 *
 * @since 1.0.2
 */
public interface AppProviderAudJPARepository extends JpaRepository<AppProviderAudEntity, Long> {
}

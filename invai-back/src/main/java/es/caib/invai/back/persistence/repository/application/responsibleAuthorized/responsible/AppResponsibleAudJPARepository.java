package es.caib.invai.back.persistence.repository.application.responsibleAuthorized.responsible;

import es.caib.invai.back.persistence.model.application.responsibleAuthorized.responsible.AppResponsibleAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository persisting the historical audit trail for {@link AppResponsibleAudEntity}.
 *
 * @since 1.0.3
 */
public interface AppResponsibleAudJPARepository extends JpaRepository<AppResponsibleAudEntity, Long> {
}

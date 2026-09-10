package es.caib.invai.back.persistence.repository.application.security.ensClassification;

import es.caib.invai.back.persistence.model.application.security.ensClassification.AppEnsClassificationAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository providing basic CRUD persistence for
 * {@link AppEnsClassificationAudEntity} audit trail rows.
 *
 * @since 1.0.4
 */
public interface AppEnsClassificationAudJPARepository extends JpaRepository<AppEnsClassificationAudEntity, Long> {
}

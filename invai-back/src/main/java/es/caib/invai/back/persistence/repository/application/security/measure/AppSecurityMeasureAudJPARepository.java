package es.caib.invai.back.persistence.repository.application.security.measure;

import es.caib.invai.back.persistence.model.application.security.measure.AppSecurityMeasureAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository providing basic CRUD persistence for
 * {@link AppSecurityMeasureAudEntity} audit trail rows.
 *
 * @since 1.0.4
 */
public interface AppSecurityMeasureAudJPARepository extends JpaRepository<AppSecurityMeasureAudEntity, Long> {
}

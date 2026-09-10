package es.caib.invai.back.persistence.repository.application.security.risk;

import es.caib.invai.back.persistence.model.application.security.risk.AppSecurityRiskAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository providing basic CRUD persistence for
 * {@link AppSecurityRiskAudEntity} audit trail rows.
 *
 * @since 1.0.4
 */
public interface AppSecurityRiskAudJPARepository extends JpaRepository<AppSecurityRiskAudEntity, Long> {
}

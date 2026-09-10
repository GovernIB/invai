package es.caib.invai.back.persistence.repository.application.security.risk;

import es.caib.invai.back.persistence.model.application.security.risk.AppSecurityRiskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Spring Data Native Bridge Interface infrastructure for root Entity.
 *
 * @since 1.0.4
 */
public interface AppSecurityRiskJPARepository extends JpaRepository<AppSecurityRiskEntity, Long>, JpaSpecificationExecutor<AppSecurityRiskEntity> {
}

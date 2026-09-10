package es.caib.invai.back.persistence.repository.application.security.measure;

import es.caib.invai.back.persistence.model.application.security.measure.AppSecurityMeasureEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Spring Data Native Bridge Interface infrastructure for root Entity.
 *
 * @since 1.0.4
 */
public interface AppSecurityMeasureJPARepository extends JpaRepository<AppSecurityMeasureEntity, Long>, JpaSpecificationExecutor<AppSecurityMeasureEntity> {
}

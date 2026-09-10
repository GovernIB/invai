package es.caib.invai.back.persistence.repository.application.security.ensClassification;

import es.caib.invai.back.persistence.model.application.security.ensClassification.AppEnsClassificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Spring Data Native Bridge Interface infrastructure for root Entity.
 *
 * @since 1.0.4
 */
public interface AppEnsClassificationJPARepository extends JpaRepository<AppEnsClassificationEntity, Long>, JpaSpecificationExecutor<AppEnsClassificationEntity> {
}

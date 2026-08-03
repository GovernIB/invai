package es.caib.invai.api.persistence.repository.application.development.technology;

import es.caib.invai.api.persistence.model.AppTechnologyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Spring Data Native Bridge Interface infrastructure for root Entity.
 *
 * @since 1.0.2
 */
public interface AppTechnologyJPARepository extends JpaRepository<AppTechnologyEntity, Long>, JpaSpecificationExecutor<AppTechnologyEntity> {

    boolean existsByLayerId(Long layerId);

    boolean existsByTechnologyId(Long technologyId);
}

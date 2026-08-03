package es.caib.invai.api.persistence.repository.application.development.provider;

import es.caib.invai.api.persistence.model.AppProviderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Spring Data Native Bridge Interface infrastructure for root Entity.
 *
 * @since 1.0.2
 */
public interface AppProviderJPARepository extends JpaRepository<AppProviderEntity, Long>, JpaSpecificationExecutor<AppProviderEntity> {

    boolean existsByRoleId(Long roleId);
}

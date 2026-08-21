package es.caib.invai.back.persistence.repository.application.development.provider;

import es.caib.invai.back.persistence.model.application.development.provider.AppProviderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Spring Data Native Bridge Interface infrastructure for root Entity.
 *
 * @since 1.0.2
 */
public interface AppProviderJPARepository extends JpaRepository<AppProviderEntity, Long>, JpaSpecificationExecutor<AppProviderEntity> {

    /**
     * Checks whether any provider record references the given role catalog entry.
     *
     * @param roleId the role catalog identifier to check
     * @return {@code true} if at least one provider references the role, {@code false} otherwise
     */
    boolean existsByRoleId(Long roleId);
}

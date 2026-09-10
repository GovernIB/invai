package es.caib.invai.back.persistence.repository.maintenance.security.identityProvider;

import es.caib.invai.back.persistence.model.maintenance.security.identityProvider.IdentityProviderAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing basic CRUD database operations
 * targeting historical {@link IdentityProviderAudEntity} snapshots.
 *
 * @since 1.0.4
 */
@Repository
public interface IdentityProviderAudJPARepository extends JpaRepository<IdentityProviderAudEntity, Long> {
}

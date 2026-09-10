package es.caib.invai.back.persistence.repository.catalog.securityLevel;

import es.caib.invai.back.persistence.model.catalog.securityLevel.LkupSecurityLevelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing read access to the static
 * {@link LkupSecurityLevelEntity} reference dictionary.
 *
 * @since 1.0.4
 */
@Repository
public interface SecurityLevelJPARepository extends JpaRepository<LkupSecurityLevelEntity, Long> {
}

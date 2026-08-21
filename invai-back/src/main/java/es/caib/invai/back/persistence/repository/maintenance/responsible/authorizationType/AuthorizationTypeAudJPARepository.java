package es.caib.invai.back.persistence.repository.maintenance.responsible.authorizationType;

import es.caib.invai.back.persistence.model.maintenance.responsible.authorizationType.AuthorizationTypeAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing basic CRUD database operations
 * targeting historical {@link AuthorizationTypeAudEntity} snapshots.
 *
 * @since 1.0.3
 */
@Repository
public interface AuthorizationTypeAudJPARepository extends JpaRepository<AuthorizationTypeAudEntity, Long> {
}

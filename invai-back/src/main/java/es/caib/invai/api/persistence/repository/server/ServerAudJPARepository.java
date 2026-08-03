package es.caib.invai.api.persistence.repository.server;

import es.caib.invai.api.persistence.model.ServerAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA interface mapping historical snapshot persistence for the {@link ServerAudEntity}.
 *
 * @since 1.0.2
 */
@Repository
public interface ServerAudJPARepository extends JpaRepository<ServerAudEntity, Long> {
}

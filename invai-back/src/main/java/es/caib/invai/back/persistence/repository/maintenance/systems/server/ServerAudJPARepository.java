package es.caib.invai.back.persistence.repository.maintenance.systems.server;

import es.caib.invai.back.persistence.model.maintenance.systems.server.ServerAudEntity;
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

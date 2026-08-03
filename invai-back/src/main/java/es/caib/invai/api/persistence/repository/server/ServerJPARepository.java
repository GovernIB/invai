package es.caib.invai.api.persistence.repository.server;

import es.caib.invai.api.persistence.model.ServerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Native Spring Data JPA repository layer providing entity lifecycle updates for {@link ServerEntity}.
 *
 * @since 1.0.2
 */
@Repository
public interface ServerJPARepository extends JpaRepository<ServerEntity, Long>, JpaSpecificationExecutor<ServerEntity> {

    Optional<ServerEntity> findById(Long id);

    boolean existsByNameAndDeletedAtIsNull(String name);

    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id);
}

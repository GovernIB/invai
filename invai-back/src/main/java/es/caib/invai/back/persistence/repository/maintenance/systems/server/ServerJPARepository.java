package es.caib.invai.back.persistence.repository.maintenance.systems.server;

import es.caib.invai.back.persistence.model.maintenance.systems.server.ServerEntity;
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

    /**
     * Finds a server entity by its unique identifier.
     *
     * @param id the server identifier
     * @return an {@link Optional} containing the matching entity, or empty if none found
     */
    Optional<ServerEntity> findById(Long id);

    /**
     * Checks whether an active (not logically deleted) server exists with the given name.
     *
     * @param name the server name to check
     * @return {@code true} if an active server with that name exists
     */
    boolean existsByNameAndDeletedAtIsNull(String name);

    /**
     * Checks whether another active server (excluding the given identifier) exists with the given name.
     *
     * @param name the server name to check
     * @param id   the identifier to exclude from the check
     * @return {@code true} if an active server other than the given identifier owns that name
     */
    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id);
}

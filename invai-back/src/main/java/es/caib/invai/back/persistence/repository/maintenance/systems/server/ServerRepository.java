package es.caib.invai.back.persistence.repository.maintenance.systems.server;

import es.caib.invai.back.service.model.maintenance.systems.server.Server;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Outbound Port boundary interface declaring relational persistence mechanisms
 * for the host server domain model layer.
 *
 * @since 1.0.2
 */
public interface ServerRepository {

    /**
     * Registers a new host server profile structure inside relational tracking systems.
     */
    Server create(Server server);

    /**
     * Commits changes onto active domain records maps identified by a target primary sequence index.
     */
    Server update(Server server, Long id);

    /**
     * Flags tracking profiles context records as soft-deleted inside persistence layers.
     */
    void delete(Server server);

    /**
     * Resolves an active host server record profile matching an identification primary index.
     */
    Server findById(Long id);

    /**
     * Streams partitioned chunk metrics using pagination layout boundaries.
     */
    Page<Server> findAll(ServerCriteria filter, Pageable pageable);

    /**
     * Confirms uniqueness of the name across active server registries.
     */
    boolean existsByName(String name);

    /**
     * Checks for duplicate name conflicts excluding a target primary reference row.
     */
    boolean existsByNameAndIdNot(String name, Long id);
}

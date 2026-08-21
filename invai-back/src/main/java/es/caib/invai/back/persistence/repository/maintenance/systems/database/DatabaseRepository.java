package es.caib.invai.back.persistence.repository.maintenance.systems.database;

import es.caib.invai.back.service.model.maintenance.systems.database.Database;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Outbound Port boundary interface declaring relational persistence mechanisms
 * for the database domain model layer.
 * Enforces architectural boundary isolation patterns by dealing exclusively with the core {@link Database} domain structure.
 *
 * @since 1.0.2
 */
public interface DatabaseRepository {

    /**
     * Registers a new operational system profile structure inside relational systems.
     *
     * @param database transient domain configuration mapping target variables properties
     * @return persistent model profile containing persistent state data markers
     */
    Database create(Database database);

    /**
     * Commits changes onto active domain records maps identified by a target primary sequence index.
     *
     * @param database business schema properties map detailing updates structures parameters
     * @param id       primary persistent key reference index tracking database entries rows
     * @return updated model domain properties specifications variables
     */
    Database update(Database database, Long id);

    /**
     * Flags tracking profiles context records as soft-deleted inside persistence layers.
     *
     * @param database target domain representation configuration modeling details to deactivate
     */
    void delete(Database database);

    /**
     * Resolves an active database record profile matching an identification primary index.
     *
     * @param id unique database sequence row identifier tracking the asset
     * @return mapped core business model context layer data structures representation
     */
    Database findById(Long id);

    /**
     * Resolves active database records using dynamic search criteria and custom pagination definitions.
     *
     * @param filter   dynamic search criteria and full-text keyword filters
     * @param pageable sorting parameters and page segment definitions thresholds
     * @return paginated container summarizing valid domain entries maps
     */
    Page<Database> findAll(DatabaseCriteria filter, Pageable pageable);

    /**
     * Confirms server host and service uniqueness across active database registries.
     *
     * @param serverId  the network host address or IP of the database server
     * @param service database service schema identifier
     * @return {@code true} if conflicts exist, {@code false} otherwise
     */
    boolean existsByServerAndService(Long serverId, String service);

    /**
     * Checks for host and service usage across alternative active database entries.
     *
     * @param serverId  the network host address or IP of the database server
     * @param service database service schema identifier
     * @param id      database sequence primary reference row token to exclude from tracking
     * @return {@code true} if a duplicate occurs outside the index parameter, {@code false} otherwise
     */
    boolean existsByServerAndServiceAndIdNot(Long serverId, String service, Long id);
}
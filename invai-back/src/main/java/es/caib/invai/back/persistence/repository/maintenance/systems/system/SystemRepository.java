package es.caib.invai.back.persistence.repository.maintenance.systems.system;

import es.caib.invai.back.service.model.maintenance.systems.system.System;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Outbound Port boundary interface declaring relational persistence mechanisms
 * for the system profile domain model layer.
 * Enforces architectural boundary isolation patterns by dealing exclusively with the core {@link System} domain structure.
 *
 * @since 1.0.1
 */
public interface SystemRepository {

    /**
     * Registers a new operational system profile structure inside structural relational systems.
     *
     * @param system transient domain configuration mapping target variables properties
     * @return persistent model profile containing persistent state data markers
     */
    System create(System system);

    /**
     * Commits changes onto active domain records maps identified by a target primary sequence index.
     *
     * @param system business schema properties map detailing updates structures parameters
     * @param id     primary persistent key reference index tracking database entries rows
     * @return updated model domain properties specifications variables
     */
    System update(System system, Long id);

    /**
     * Flags tracking profiles context records as soft-deleted inside persistence layers.
     *
     * @param system target domain representation configuration modeling details to deactivate
     */
    void delete(System system);

    /**
     * Resolves an active system record profile matching an identification primary index.
     *
     * @param id unique database sequence row identifier tracking the asset
     * @return mapped core business model context layer data structures representation
     */
    System findById(Long id);

    /**
     * Streams partitioned chunk metrics using pagination layout boundaries and dynamic criteria filtering.
     *
     * @param filter   dynamic search criteria narrowing the resulting system record collection
     * @param pageable sorting parameters and page segment definitions thresholds
     * @return paginated container summarizing valid domain entries maps
     */
    Page<System> findAll(SystemCriteria filter, Pageable pageable);

    /**
     * Confirms combination uniqueness of host server and instance across active system registries.
     *
     * @param serverId host server identifier
     * @param instance deployment node identifier string
     * @return {@code true} if conflicts exist, {@code false} otherwise
     */
    boolean existsByServerIdAndInstance(Long serverId, String instance);

    /**
     * Checks for server and instance duplicate conflicts excluding a target primary reference row.
     *
     * @param serverId host server identifier
     * @param instance deployment node identifier string
     * @param id       database sequence primary reference row token to exclude from tracking
     * @return {@code true} if duplicates exist, {@code false} otherwise
     */
    boolean existsByServerIdAndInstanceAndId(Long serverId, String instance, Long id);
}
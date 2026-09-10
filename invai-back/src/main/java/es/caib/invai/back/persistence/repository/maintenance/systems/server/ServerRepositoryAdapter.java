package es.caib.invai.back.persistence.repository.maintenance.systems.server;

import es.caib.invai.back.persistence.model.maintenance.systems.server.ServerAudEntity;
import es.caib.invai.back.persistence.model.maintenance.systems.server.ServerEntity;
import es.caib.invai.back.service.mapper.maintenance.systems.server.ServerMapper;
import es.caib.invai.back.service.model.maintenance.systems.server.Server;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Infrastructure repository Adapter implementing the outbound port boundary {@link ServerRepository}.
 * Coordinates transactional mutations and logs snapshots onto the audit ledger database layers.
 *
 * @since 1.0.2
 */
@Repository
@Slf4j
public class ServerRepositoryAdapter implements ServerRepository {

    /** Spring Data JPA repository used for CRUD and query operations on {@link ServerEntity}. */
    @Autowired
    private ServerJPARepository serverJPARepository;

    /** Spring Data JPA repository used to persist historical audit snapshots of server records. */
    @Autowired
    private ServerAudJPARepository serverAudJPARepository;

    /** Mapper used to convert between Server domain models and JPA entities. */
    @Autowired
    private ServerMapper serverMapper;

    /**
     * Registers a new host server profile structure inside relational tracking systems,
     * mirroring an audit snapshot.
     *
     * @param server the server domain model to persist
     * @return the persisted server, including its generated identifier
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public Server create(Server server) {
        log.info("Repository: Persisting new server entity into database");
        try {
            ServerEntity entity = serverMapper.toEntity(server);
            entity = serverJPARepository.save(entity);

            saveAuditRecord(entity, "INSERT");

            return serverMapper.toModel(entity);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure persisting server record", e);
            throw e;
        }
    }

    /**
     * Commits changes onto an active domain record identified by a target primary sequence index,
     * mirroring an audit snapshot.
     *
     * @param server the server domain model holding the updated data
     * @param id     the identifier of the server record to update
     * @return the updated server domain model
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public Server update(Server server, Long id) {
        log.info("Repository: Merging changes into existing server record for ID: {}", id);
        try {
            ServerEntity entity = serverMapper.toEntity(server);
            entity.setId(id);
            entity = serverJPARepository.save(entity);

            saveAuditRecord(entity, "UPDATE");

            return serverMapper.toModel(entity);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure updating server record with ID: {}", id, e);
            throw e;
        }
    }

    /**
     * Flags a tracking profile context record as soft-deleted inside persistence layers,
     * mirroring an audit snapshot.
     *
     * @param server the server domain model to soft-delete
     * @throws DataAccessException if the underlying persistence operation fails
     */
    @Override
    public void delete(Server server) {
        log.info("Repository: Soft deleting server entity with ID: {}", server.getId());
        try {
            ServerEntity entity = serverMapper.toEntity(server);
            entity.setId(server.getId());
            entity = serverJPARepository.save(entity);

            saveAuditRecord(entity, "DELETE");
        } catch (DataAccessException e) {
            log.error("Repository error: Failure executing soft delete workflow for ID: {}", server.getId(), e);
            throw e;
        }
    }

    /**
     * Resolves an active host server record profile matching an identification primary index.
     *
     * @param id the unique server identifier
     * @return the matching {@link Server} domain model, or {@code null} if not found
     * @throws DataAccessException if the underlying data access operation fails
     */
    @Override
    public Server findById(Long id) {
        try {
            return serverJPARepository.findById(id)
                    .map(serverMapper::toModel)
                    .orElse(null);
        } catch (DataAccessException e) {
            log.error("Repository error: Data access failure reading server profile with ID: {}", id, e);
            throw e;
        }
    }

    /**
     * Streams partitioned chunk metrics using pagination layout boundaries.
     *
     * @param filter   the search criteria used to narrow the results
     * @param pageable the pagination and sorting parameters
     * @return a page of matching {@link Server} domain models
     * @throws DataAccessException if the underlying data access operation fails
     */
    @Override
    public Page<Server> findAll(ServerCriteria filter, Pageable pageable) {
        log.debug("Repository: Fetching paged servers using standard query layouts");
        try {
            Specification<ServerEntity> spec = ServerSpecification.filterByCriteria(filter);
            return serverJPARepository.findAll(spec, pageable).map(serverMapper::toModel);
        } catch (DataAccessException e) {
            log.error("Repository error: Paged server collection fetch exception", e);
            throw e;
        }
    }

    /**
     * Confirms uniqueness of the name across active server registries.
     *
     * @param name the server name to check
     * @return {@code true} if an active server with that name exists
     * @throws DataAccessException if the underlying data access operation fails
     */
    @Override
    public boolean existsByName(String name) {
        try {
            return serverJPARepository.existsByNameAndDeletedAtIsNull(name);
        } catch (DataAccessException e) {
            log.error("Repository error: Failed evaluation lookup tracking name uniqueness", e);
            throw e;
        }
    }

    /**
     * Checks for duplicate name conflicts excluding a target primary reference row.
     *
     * @param name the server name to check
     * @param id   the identifier to exclude from the check
     * @return {@code true} if an active server other than the given identifier owns that name
     * @throws DataAccessException if the underlying data access operation fails
     */
    @Override
    public boolean existsByNameAndIdNot(String name, Long id) {
        try {
            return serverJPARepository.existsByNameAndIdNotAndDeletedAtIsNull(name, id);
        } catch (DataAccessException e) {
            log.error("Repository error: Duplicate detection crash analyzing update context for ID: {}", id, e);
            throw e;
        }
    }

    /**
     * Commits a flat audit record mirror tracing the current transactional context phase status.
     */
    private void saveAuditRecord(ServerEntity entity, String action) {
        try {
            ServerAudEntity aud = new ServerAudEntity();

            aud.setServerId(entity.getId());
            aud.setName(entity.getName());
            aud.setEnvironmentId(entity.getEnvironment() != null ? entity.getEnvironment().getId() : null);
            aud.setServerTypeId(entity.getServerType() != null ? entity.getServerType().getId() : null);

            aud.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now());
            aud.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy() : Utils.resolveCurrentUsername());
           
                        aud.setUpdatedAt(entity.getUpdatedAt());
            aud.setUpdatedBy(entity.getUpdatedBy());
            aud.setDeletedAt(entity.getDeletedAt());
            aud.setDeletedBy(entity.getDeletedBy());

            aud.setAudAction(action);
            aud.setAuditDate(LocalDateTime.now());
            aud.setAuditUser(Utils.resolveCurrentUsername());

            serverAudJPARepository.save(aud);
        } catch (DataAccessException e) {
            log.error("Repository trace error: Critical trace audit ledger commit crash mapping server event", e);
            throw e;
        }
    }
}

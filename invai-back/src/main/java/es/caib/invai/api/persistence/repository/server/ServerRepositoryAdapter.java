package es.caib.invai.api.persistence.repository.server;

import es.caib.invai.api.persistence.model.ServerAudEntity;
import es.caib.invai.api.persistence.model.ServerEntity;
import es.caib.invai.api.service.mapper.ServerMapper;
import es.caib.invai.api.service.model.Server;
import es.caib.invai.api.utils.Utils;
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

    @Autowired
    private ServerJPARepository serverJPARepository;

    @Autowired
    private ServerAudJPARepository serverAudJPARepository;

    @Autowired
    private ServerMapper serverMapper;

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

    @Override
    public Page<Server> findAll(ServerCriteria filter, Pageable pageable) {
        log.info("Repository: Fetching paged servers using standard query layouts");
        try {
            Specification<ServerEntity> spec = ServerSpecification.filterByCriteria(filter);
            return serverJPARepository.findAll(spec, pageable).map(serverMapper::toModel);
        } catch (DataAccessException e) {
            log.error("Repository error: Paged server collection fetch exception", e);
            throw e;
        }
    }

    @Override
    public boolean existsByName(String name) {
        try {
            return serverJPARepository.existsByNameAndDeletedAtIsNull(name);
        } catch (DataAccessException e) {
            log.error("Repository error: Failed evaluation lookup tracking name uniqueness", e);
            throw e;
        }
    }

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
           
                        aud.setUpdatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt() : LocalDateTime.now());
            aud.setUpdatedBy(entity.getUpdatedBy() != null ? entity.getUpdatedBy() : Utils.resolveCurrentUsername());
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

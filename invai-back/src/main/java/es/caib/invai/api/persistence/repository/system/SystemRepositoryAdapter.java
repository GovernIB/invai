package es.caib.invai.api.persistence.repository.system;

import es.caib.invai.api.persistence.model.SystemAudEntity;
import es.caib.invai.api.persistence.model.SystemEntity;
import es.caib.invai.api.service.mapper.SystemMapper;
import es.caib.invai.api.service.model.System;
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
 * Infrastructure repository Adapter implementing the outbound port boundary {@link SystemRepository}.
 * <p>
 * Manages transactional operations targeting system operational metadata and handles
 * historical record keeping by coordinating across relational database engines.
 * </p>
 *
 * @since 1.0.1
 */
@Repository
@Slf4j
public class SystemRepositoryAdapter implements SystemRepository {

    @Autowired
    private SystemJPARepository systemJPARepository;

    @Autowired
    private SystemAudJPARepository systemAudJPARepository;

    @Autowired
    private SystemMapper systemMapper;

    @Override
    public System create(System system) {
        log.info("Repository: Persisting new system entity into database");
        try {
            SystemEntity systemEntity = systemMapper.toEntity(system);
            systemEntity = systemJPARepository.save(systemEntity);

            saveAuditRecord(systemEntity, "INSERT");

            return systemMapper.toModel(systemEntity);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure persisting system record into database", e);
            throw e;
        }
    }

    @Override
    public System update(System system, Long id) {
        log.info("Repository: Merging changes into existing system record for ID: {}", id);
        try {
            SystemEntity systemEntity = systemMapper.toEntity(system);
            systemEntity.setId(id);
            systemEntity = systemJPARepository.save(systemEntity);

            saveAuditRecord(systemEntity, "UPDATE");

            return systemMapper.toModel(systemEntity);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure updating system record with ID: {}", id, e);
            throw e;
        }
    }

    @Override
    public void delete(System system) {
        log.info("Repository: Soft deleting system entity with ID: {}", system.getId());
        try {
            SystemEntity systemEntity = systemMapper.toEntity(system);
            systemEntity.setId(system.getId());
            systemEntity = systemJPARepository.save(systemEntity);

            saveAuditRecord(systemEntity, "DELETE");
        } catch (DataAccessException e) {
            log.error("Repository error: Failure executing soft delete workflow for ID: {}", system.getId(), e);
            throw e;
        }
    }

    @Override
    public System findById(Long id) {
        try {
            return systemJPARepository.findById(id)
                    .map(systemMapper::toModel)
                    .orElse(null);
        } catch (DataAccessException e) {
            log.error("Repository error: Data access failure reading system profile with ID: {}", id, e);
            throw e;
        }
    }

    @Override
    public Page<System> findAll(SystemCriteria filter, Pageable pageable) {
        log.info("Repository: Fetching paged systems using standard query layout boundaries");
        try {
            Specification<SystemEntity> spec = SystemSpecification.filterByCriteria(filter);
            return systemJPARepository.findAll(spec, pageable).map(systemMapper::toModel);
        } catch (DataAccessException e) {
            log.error("Repository error: Paged system collection fetch exception applied under pagination constraints", e);
            throw e;
        }
    }

    @Override
    public boolean existsByServerIdAndInstance(Long serverId, String instance) {
        try {
            return systemJPARepository.existsByServerIdAndInstance(serverId, instance);
        } catch (DataAccessException e) {
            log.error("Repository error: Failed evaluation lookup tracking system server and instance uniqueness", e);
            throw e;
        }
    }

    @Override
    public boolean existsByServerIdAndInstanceAndId(Long serverId, String instance, Long id) {
        try {
            return systemJPARepository.existsByServerIdAndInstanceAndId(serverId, instance, id);
        } catch (DataAccessException e) {
            log.error("Repository error: Multi-conditional duplicate detection crash analyzing uniqueness on update context for ID: {}", id, e);
            throw e;
        }
    }

    private void saveAuditRecord(SystemEntity entity, String action) {
        try {
            SystemAudEntity aud = new SystemAudEntity();

            aud.setSystemId(entity.getId());
            aud.setServerId(entity.getServer() != null ? entity.getServer().getId() : null);
            aud.setInstance(entity.getInstance());
            aud.setPort(entity.getPort());
            aud.setVersion(entity.getVersion());
            aud.setDescription(entity.getDescription());

            aud.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now());
            aud.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy() : Utils.resolveCurrentUsername());
           
                        aud.setUpdatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt() : LocalDateTime.now());
            aud.setUpdatedBy(entity.getUpdatedBy() != null ? entity.getUpdatedBy() : Utils.resolveCurrentUsername());
            aud.setDeletedAt(entity.getDeletedAt());
            aud.setDeletedBy(entity.getDeletedBy());

            aud.setAudAction(action);
            aud.setAuditDate(LocalDateTime.now());
            aud.setAuditUser(Utils.resolveCurrentUsername());

            systemAudJPARepository.save(aud);
        } catch (DataAccessException e) {
            log.error("Repository trace error: Critical trace audit ledger commit crash mapping system event", e);
            throw e;
        }
    }
}
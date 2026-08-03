package es.caib.invai.api.persistence.repository.environment;

import es.caib.invai.api.persistence.model.EnvironmentAudEntity;
import es.caib.invai.api.persistence.model.EnvironmentEntity;
import es.caib.invai.api.service.mapper.EnvironmentMapper;
import es.caib.invai.api.service.model.Environment;
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
 * Infrastructure repository Adapter implementing the outbound port boundary {@link EnvironmentRepository}.
 * Coordinates transactional mutations and logs snapshots onto the audit ledger database layers.
 *
 * @since 1.0.1
 */
@Repository
@Slf4j
public class EnvironmentRepositoryAdapter implements EnvironmentRepository {

    @Autowired
    private EnvironmentJPARepository environmentJPARepository;

    @Autowired
    private EnvironmentAudJPARepository environmentAudJPARepository;

    @Autowired
    private EnvironmentMapper environmentMapper;

    @Override
    public Environment create(Environment environment) {
        log.info("Repository: Persisting new environment entity into database");
        try {
            EnvironmentEntity entity = environmentMapper.toEntity(environment);
            entity = environmentJPARepository.save(entity);

            saveAuditRecord(entity, "INSERT");

            return environmentMapper.toModel(entity);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure persisting environment record", e);
            throw e;
        }
    }

    @Override
    public Environment update(Environment environment, Long id) {
        log.info("Repository: Merging changes into existing environment record for ID: {}", id);
        try {
            EnvironmentEntity entity = environmentMapper.toEntity(environment);
            entity.setId(id);
            entity = environmentJPARepository.save(entity);

            saveAuditRecord(entity, "UPDATE");

            return environmentMapper.toModel(entity);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure updating environment record with ID: {}", id, e);
            throw e;
        }
    }

    @Override
    public void delete(Environment environment) {
        log.info("Repository: Soft deleting environment entity with ID: {}", environment.getId());
        try {
            EnvironmentEntity entity = environmentMapper.toEntity(environment);
            entity.setId(environment.getId());
            entity = environmentJPARepository.save(entity);

            saveAuditRecord(entity, "DELETE");
        } catch (DataAccessException e) {
            log.error("Repository error: Failure executing soft delete workflow for ID: {}", environment.getId(), e);
            throw e;
        }
    }

    @Override
    public Environment findById(Long id) {
        try {
            return environmentJPARepository.findById(id)
                    .map(environmentMapper::toModel)
                    .orElse(null);
        } catch (DataAccessException e) {
            log.error("Repository error: Data access failure reading environment profile with ID: {}", id, e);
            throw e;
        }
    }

    @Override
    public Page<Environment> findAll(EnvironmentCriteria filter, Pageable pageable) {
        log.info("Repository: Fetching paged environments using standard query layouts");
        try {
            log.trace("Repository: Processing criteria evaluation logic within active ecosystem metrics");
            Specification<EnvironmentEntity> spec = EnvironmentSpecification.filterByCriteria(filter);
            return environmentJPARepository.findAll(spec, pageable).map(environmentMapper::toModel);
        } catch (DataAccessException e) {
            log.error("Repository error: Paged environment collection fetch exception", e);
            throw e;
        }
    }

    @Override
    public boolean existsByCode(String code) {
        try {
            return environmentJPARepository.existsByCodeAndDeletedAtIsNull(code);
        } catch (DataAccessException e) {
            log.error("Repository error: Failed evaluation lookup tracking code uniqueness", e);
            throw e;
        }
    }

    @Override
    public boolean existsByCodeAndIdNot(String code, Long id) {
        try {
            return environmentJPARepository.existsByCodeAndIdNotAndDeletedAtIsNull(code, id);
        } catch (DataAccessException e) {
            log.error("Repository error: Duplicate detection crash analyzing update context for ID: {}", id, e);
            throw e;
        }
    }

    /**
     * Commits a flat audit record mirror tracing the current transactional context phase status.
     */
    private void saveAuditRecord(EnvironmentEntity entity, String action) {
        try {
            EnvironmentAudEntity aud = new EnvironmentAudEntity();

            aud.setEnvironmentId(entity.getId());
            aud.setCode(entity.getCode());
            aud.setName(entity.getName());
            aud.setNameEs(entity.getNameEs());

            aud.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now());
            aud.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy() : Utils.resolveCurrentUsername());
            aud.setUpdatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt() : LocalDateTime.now());
            aud.setUpdatedBy(entity.getUpdatedBy() != null ? entity.getUpdatedBy() : Utils.resolveCurrentUsername());
            aud.setDeletedAt(entity.getDeletedAt());
            aud.setDeletedBy(entity.getDeletedBy());

            aud.setAudAction(action);
            aud.setAuditDate(LocalDateTime.now());
            aud.setAuditUser(Utils.resolveCurrentUsername());

            environmentAudJPARepository.save(aud);
        } catch (DataAccessException e) {
            log.error("Repository trace error: Critical trace audit ledger commit crash mapping environment event", e);
            throw e;
        }
    }
}
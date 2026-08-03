package es.caib.invai.api.persistence.repository.databaseVendor;

import es.caib.invai.api.persistence.model.DatabaseVendorAudEntity;
import es.caib.invai.api.persistence.model.DatabaseVendorEntity;
import es.caib.invai.api.service.mapper.DatabaseVendorMapper;
import es.caib.invai.api.service.model.DatabaseVendor;
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
 * Infrastructure repository Adapter implementing the outbound port boundary {@link DatabaseVendorRepository}.
 * Coordinates transactional mutations and logs snapshots onto the audit ledger database layers.
 *
 * @since 1.0.2
 */
@Repository
@Slf4j
public class DatabaseVendorRepositoryAdapter implements DatabaseVendorRepository {

    @Autowired
    private DatabaseVendorJPARepository databaseVendorJPARepository;

    @Autowired
    private DatabaseVendorAudJPARepository databaseVendorAudJPARepository;

    @Autowired
    private DatabaseVendorMapper databaseVendorMapper;

    @Override
    public DatabaseVendor create(DatabaseVendor databaseVendor) {
        log.info("Repository: Persisting new database vendor entity into database");
        try {
            DatabaseVendorEntity entity = databaseVendorMapper.toEntity(databaseVendor);
            entity = databaseVendorJPARepository.save(entity);

            saveAuditRecord(entity, "INSERT");

            return databaseVendorMapper.toModel(entity);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure persisting database vendor record", e);
            throw e;
        }
    }

    @Override
    public DatabaseVendor update(DatabaseVendor databaseVendor, Long id) {
        log.info("Repository: Merging changes into existing database vendor record for ID: {}", id);
        try {
            DatabaseVendorEntity entity = databaseVendorMapper.toEntity(databaseVendor);
            entity.setId(id);
            entity = databaseVendorJPARepository.save(entity);

            saveAuditRecord(entity, "UPDATE");

            return databaseVendorMapper.toModel(entity);
        } catch (DataAccessException e) {
            log.error("Repository error: Failure updating database vendor record with ID: {}", id, e);
            throw e;
        }
    }

    @Override
    public void delete(DatabaseVendor databaseVendor) {
        log.info("Repository: Soft deleting database vendor entity with ID: {}", databaseVendor.getId());
        try {
            DatabaseVendorEntity entity = databaseVendorMapper.toEntity(databaseVendor);
            entity.setId(databaseVendor.getId());
            entity = databaseVendorJPARepository.save(entity);

            saveAuditRecord(entity, "DELETE");
        } catch (DataAccessException e) {
            log.error("Repository error: Failure executing soft delete workflow for ID: {}", databaseVendor.getId(), e);
            throw e;
        }
    }

    @Override
    public DatabaseVendor findById(Long id) {
        try {
            return databaseVendorJPARepository.findById(id)
                    .map(databaseVendorMapper::toModel)
                    .orElse(null);
        } catch (DataAccessException e) {
            log.error("Repository error: Data access failure reading database vendor profile with ID: {}", id, e);
            throw e;
        }
    }

    @Override
    public Page<DatabaseVendor> findAll(DatabaseVendorCriteria filter, Pageable pageable) {
        log.info("Repository: Fetching paged database vendors using standard query layout boundaries");
        try {
            log.trace("Repository: Processing criteria evaluation logic within active ecosystem metrics");
            Specification<DatabaseVendorEntity> spec = DatabaseVendorSpecification.filterByCriteria(filter);
            return databaseVendorJPARepository.findAll(spec, pageable).map(databaseVendorMapper::toModel);
        } catch (DataAccessException e) {
            log.error("Repository error: Paged database vendor collection fetch exception", e);
            throw e;
        }
    }

    @Override
    public boolean existsByName(String name) {
        try {
            return databaseVendorJPARepository.existsByNameAndDeletedAtIsNull(name);
        } catch (DataAccessException e) {
            log.error("Repository error: Failed evaluation lookup tracking name uniqueness", e);
            throw e;
        }
    }

    @Override
    public boolean existsByNameAndIdNot(String name, Long id) {
        try {
            return databaseVendorJPARepository.existsByNameAndIdNotAndDeletedAtIsNull(name, id);
        } catch (DataAccessException e) {
            log.error("Repository error: Duplicate detection crash analyzing update context for ID: {}", id, e);
            throw e;
        }
    }

    private void saveAuditRecord(DatabaseVendorEntity entity, String action) {
        try {
            DatabaseVendorAudEntity aud = new DatabaseVendorAudEntity();

            aud.setDatabaseVendorId(entity.getId());
            aud.setName(entity.getName());
            aud.setDefaultPort(entity.getDefaultPort());

            aud.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now());
            aud.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy() : Utils.resolveCurrentUsername());

            aud.setUpdatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt() : LocalDateTime.now());
            aud.setUpdatedBy(entity.getUpdatedBy() != null ? entity.getUpdatedBy() : Utils.resolveCurrentUsername());
            aud.setDeletedAt(entity.getDeletedAt());
            aud.setDeletedBy(entity.getDeletedBy());

            aud.setAudAction(action);
            aud.setAuditDate(LocalDateTime.now());
            aud.setAuditUser(Utils.resolveCurrentUsername());

            databaseVendorAudJPARepository.save(aud);
        } catch (DataAccessException e) {
            log.error("Repository trace error: Critical trace audit ledger commit crash mapping database vendor event", e);
            throw e;
        }
    }
}

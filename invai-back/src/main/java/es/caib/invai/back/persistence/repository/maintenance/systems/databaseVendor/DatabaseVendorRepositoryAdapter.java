package es.caib.invai.back.persistence.repository.maintenance.systems.databaseVendor;

import es.caib.invai.back.persistence.model.maintenance.systems.databaseVendor.DatabaseVendorAudEntity;
import es.caib.invai.back.persistence.model.maintenance.systems.databaseVendor.DatabaseVendorEntity;
import es.caib.invai.back.service.mapper.maintenance.systems.databaseVendor.DatabaseVendorMapper;
import es.caib.invai.back.service.model.maintenance.systems.databaseVendor.DatabaseVendor;
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
 * Infrastructure repository Adapter implementing the outbound port boundary {@link DatabaseVendorRepository}.
 * Coordinates transactional mutations and logs snapshots onto the audit ledger database layers.
 *
 * @since 1.0.2
 */
@Repository
@Slf4j
public class DatabaseVendorRepositoryAdapter implements DatabaseVendorRepository {

    /** Spring Data JPA repository for {@link DatabaseVendorEntity} persistence operations. */
    @Autowired
    private DatabaseVendorJPARepository databaseVendorJPARepository;

    /** Spring Data JPA repository for persisting {@link DatabaseVendorAudEntity} audit snapshots. */
    @Autowired
    private DatabaseVendorAudJPARepository databaseVendorAudJPARepository;

    /** Mapper converting between DatabaseVendor domain models and JPA entities. */
    @Autowired
    private DatabaseVendorMapper databaseVendorMapper;

    /**
     * Persists a new database vendor entity and records an {@code INSERT} audit snapshot.
     *
     * @param databaseVendor the domain model to persist
     * @return the persisted domain model, including its generated identifier
     */
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

    /**
     * Merges updated field values into an existing database vendor record and records an
     * {@code UPDATE} audit snapshot.
     *
     * @param databaseVendor the domain model carrying the updated field values
     * @param id             the identifier of the record to update
     * @return the updated domain model
     */
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

    /**
     * Applies a logical soft-delete to the given database vendor and records a
     * {@code DELETE} audit snapshot.
     *
     * @param databaseVendor the domain model, already flagged as deleted, to persist
     */
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

    /**
     * Resolves a database vendor record by its unique identifier.
     *
     * @param id the record identifier
     * @return the mapped domain model, or {@code null} if not found
     */
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

    /**
     * Fetches database vendor records matching dynamic search criteria and pagination limits.
     *
     * @param filter   dynamic search criteria and full-text keyword filters
     * @param pageable pagination parameters
     * @return a page containing mapped domain objects
     */
    @Override
    public Page<DatabaseVendor> findAll(DatabaseVendorCriteria filter, Pageable pageable) {
        log.debug("Repository: Fetching paged database vendors using standard query layout boundaries");
        try {
            log.trace("Repository: Processing criteria evaluation logic within active ecosystem metrics");
            Specification<DatabaseVendorEntity> spec = DatabaseVendorSpecification.filterByCriteria(filter);
            return databaseVendorJPARepository.findAll(spec, pageable).map(databaseVendorMapper::toModel);
        } catch (DataAccessException e) {
            log.error("Repository error: Paged database vendor collection fetch exception", e);
            throw e;
        }
    }

    /**
     * Checks whether an active database vendor with the given name already exists.
     *
     * @param name the vendor/type name to check
     * @return {@code true} if a matching active record exists, {@code false} otherwise
     */
    @Override
    public boolean existsByName(String name) {
        try {
            return databaseVendorJPARepository.existsByNameAndDeletedAtIsNull(name);
        } catch (DataAccessException e) {
            log.error("Repository error: Failed evaluation lookup tracking name uniqueness", e);
            throw e;
        }
    }

    /**
     * Checks whether an active database vendor with the given name exists,
     * excluding the record identified by {@code id}.
     *
     * @param name the vendor/type name to check
     * @param id   the identifier of the record to exclude from the check
     * @return {@code true} if a conflicting active record exists, {@code false} otherwise
     */
    @Override
    public boolean existsByNameAndIdNot(String name, Long id) {
        try {
            return databaseVendorJPARepository.existsByNameAndIdNotAndDeletedAtIsNull(name, id);
        } catch (DataAccessException e) {
            log.error("Repository error: Duplicate detection crash analyzing update context for ID: {}", id, e);
            throw e;
        }
    }

    /**
     * Compiles a point-in-time snapshot of the given entity and persists it as a historical
     * audit record.
     *
     * @param entity the currently persisted entity state to snapshot
     * @param action the mutation type descriptor (e.g. {@code INSERT}, {@code UPDATE}, {@code DELETE})
     */
    private void saveAuditRecord(DatabaseVendorEntity entity, String action) {
        try {
            DatabaseVendorAudEntity aud = new DatabaseVendorAudEntity();

            aud.setDatabaseVendorId(entity.getId());
            aud.setName(entity.getName());
            aud.setDefaultPort(entity.getDefaultPort());

            aud.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now());
            aud.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy() : Utils.resolveCurrentUsername());

            aud.setUpdatedAt(entity.getUpdatedAt());
            aud.setUpdatedBy(entity.getUpdatedBy());
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

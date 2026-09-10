package es.caib.invai.back.persistence.repository.maintenance.systems.database;

import es.caib.invai.back.service.model.maintenance.systems.database.Database;
import es.caib.invai.back.persistence.model.maintenance.systems.database.DatabaseEntity;
import es.caib.invai.back.persistence.model.maintenance.systems.database.DatabaseAudEntity;
import es.caib.invai.back.service.mapper.maintenance.systems.database.DatabaseMapper;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Infrastructure repository Adapter implementing the outbound port boundary {@link DatabaseRepository}.
 * <p>
 * Orchestrates technical structural operations, routing operations across active database layers
 * while utilizing mapping layers to enforce decoupling rules.
 * </p>
 *
 * @since 1.0.2
 */
@Repository
@Slf4j
public class DatabaseRepositoryAdapter implements DatabaseRepository {

    /** Spring Data JPA repository for {@link DatabaseEntity} persistence operations. */
    @Autowired
    private DatabaseJPARepository databaseJPARepository;

    /** Spring Data JPA repository for persisting {@link DatabaseAudEntity} audit snapshots. */
    @Autowired
    private DatabaseAudJPARepository databaseAudJPARepository;

    /** Mapper converting between Database domain models and JPA entities. */
    @Autowired
    private DatabaseMapper databaseMapper;

    /**
     * Fetches database assets from the database matching dynamic search criteria and pagination limits.
     *
     * @param filter   dynamic search criteria and full-text keyword filters
     * @param pageable pagination parameters
     * @return a page containing mapped domain objects
     */
    @Override
    public Page<Database> findAll(DatabaseCriteria filter, Pageable pageable) {
        Specification<DatabaseEntity> spec = DatabaseSpecification.filterByCriteria(filter);
        return databaseJPARepository.findAll(spec, pageable).map(databaseMapper::toModel);
    }

    /**
     * Resolves a database record by its unique key, filtering out soft-deleted data traces.
     *
     * @param id unique sequence row identifier tracking the asset
     * @return the mapped domain {@link Database} instance, or {@code null} if missing or soft-deleted
     */
    @Override
    public Database findById(Long id) {
        return databaseJPARepository.findById(id)
                .map(databaseMapper::toModel)
                .orElse(null);
    }

    /**
     * Persists a new database entity and records an {@code INSERT} audit snapshot.
     *
     * @param database the domain model to persist
     * @return the persisted domain model, including its generated identifier
     */
    @Override
    public Database create(Database database) {
        log.info("Repository: Persisting new database entity into system catalog");
        DatabaseEntity entity = databaseMapper.toEntity(database);
        entity = databaseJPARepository.save(entity);

        saveAuditRecord(entity, "INSERT");

        return databaseMapper.toModel(entity);
    }

    /**
     * Merges updated field values into an existing database record and records an
     * {@code UPDATE} audit snapshot.
     *
     * @param database the domain model carrying the updated field values
     * @param id       the identifier of the record to update
     * @return the updated domain model
     */
    @Override
    public Database update(Database database, Long id) {
        log.info("Repository: Merging updates into existing database record with ID: {}", id);
        DatabaseEntity entity = databaseMapper.toEntity(database);
        entity.setId(id);
        entity = databaseJPARepository.save(entity);

        saveAuditRecord(entity, "UPDATE");

        return databaseMapper.toModel(entity);
    }

    /**
     * Applies a logical soft-delete to the given database entry and records a
     * {@code DELETE} audit snapshot.
     *
     * @param database the domain model, already flagged as deleted, to persist
     */
    @Override
    public void delete(Database database) {
        log.info("Repository: Applying soft delete routine on database entry ID: {}", database.getId());
        DatabaseEntity entity = databaseMapper.toEntity(database);
        entity.setId(database.getId());
        entity = databaseJPARepository.save(entity);

        saveAuditRecord(entity, "DELETE");
    }

    /**
     * Compiles point-in-time snapshot mirror values from active database entries,
     * resolves identity user info claims tokens, and saves historic compliance tracks.
     *
     * @param entity the currently tracked persistent data state row entity map representation
     * @param action systemic string token descriptor identifying database mutation contexts
     */
    private void saveAuditRecord(DatabaseEntity entity, String action) {
        DatabaseAudEntity aud = new DatabaseAudEntity();

        aud.setDatabaseId(entity.getId());
        aud.setServerId(entity.getServer().getId());
        aud.setService(entity.getService());
        aud.setPort(entity.getPort());
        aud.setDatabaseTypeId(entity.getDatabaseType() != null ? entity.getDatabaseType().getId() : null);
        aud.setDescription(entity.getDescription());

        aud.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now());
        aud.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy() : Utils.resolveCurrentUsername());
       
                    aud.setUpdatedAt(entity.getUpdatedAt());
            aud.setUpdatedBy(entity.getUpdatedBy());
        aud.setDeletedAt(entity.getDeletedAt());
        aud.setDeletedBy(entity.getDeletedBy());

        aud.setAudAction(action);
        aud.setAuditDate(LocalDateTime.now());
        aud.setAuditUser(Utils.resolveCurrentUsername());

        databaseAudJPARepository.save(aud);
    }

    /**
     * Checks whether a database entry already exists for the given host server and service name.
     *
     * @param serverId the host server identifier
     * @param service  the database service schema identifier
     * @return {@code true} if a matching record exists, {@code false} otherwise
     */
    @Override
    public boolean existsByServerAndService(Long serverId, String service) {
        return databaseJPARepository.existsByServerIdAndService(serverId, service);
    }

    /**
     * Checks whether a database entry with the given host server and service name exists,
     * excluding the record identified by {@code id}.
     *
     * @param serverId the host server identifier
     * @param service  the database service schema identifier
     * @param id       the identifier of the record to exclude from the check
     * @return {@code true} if a conflicting record exists, {@code false} otherwise
     */
    @Override
    public boolean existsByServerAndServiceAndIdNot(Long serverId, String service, Long id) {
        return databaseJPARepository.existsByServerIdAndServiceAndIdNot(serverId, service, id);
    }
}
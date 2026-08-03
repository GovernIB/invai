package es.caib.invai.api.persistence.repository.database;

import es.caib.invai.api.service.model.Database;
import es.caib.invai.api.persistence.model.DatabaseEntity;
import es.caib.invai.api.persistence.model.DatabaseAudEntity;
import es.caib.invai.api.service.mapper.DatabaseMapper;
import es.caib.invai.api.utils.SecurityUtils;
import es.caib.invai.api.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
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

    @Autowired
    private DatabaseJPARepository databaseJPARepository;

    @Autowired
    private DatabaseAudJPARepository databaseAudJPARepository;

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

    @Override
    public Database create(Database database) {
        log.info("Repository: Persisting new database entity into system catalog");
        DatabaseEntity entity = databaseMapper.toEntity(database);
        entity = databaseJPARepository.save(entity);

        saveAuditRecord(entity, "INSERT");

        return databaseMapper.toModel(entity);
    }

    @Override
    public Database update(Database database, Long id) {
        log.info("Repository: Merging updates into existing database record with ID: {}", id);
        DatabaseEntity entity = databaseMapper.toEntity(database);
        entity.setId(id);
        entity = databaseJPARepository.save(entity);

        saveAuditRecord(entity, "UPDATE");

        return databaseMapper.toModel(entity);
    }

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
       
                    aud.setUpdatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt() : LocalDateTime.now());
            aud.setUpdatedBy(entity.getUpdatedBy() != null ? entity.getUpdatedBy() : Utils.resolveCurrentUsername());
        aud.setDeletedAt(entity.getDeletedAt());
        aud.setDeletedBy(entity.getDeletedBy());

        aud.setAudAction(action);
        aud.setAuditDate(LocalDateTime.now());
        aud.setAuditUser(Utils.resolveCurrentUsername());

        databaseAudJPARepository.save(aud);
    }

    @Override
    public boolean existsByServerAndService(Long serverId, String service) {
        return databaseJPARepository.existsByServerIdAndService(serverId, service);
    }

    @Override
    public boolean existsByServerAndServiceAndIdNot(Long serverId, String service, Long id) {
        return databaseJPARepository.existsByServerIdAndServiceAndIdNot(serverId, service, id);
    }
}
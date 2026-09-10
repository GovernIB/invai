package es.caib.invai.back.ejb.maintenance.systems.database;

import es.caib.invai.back.interna.maintenance.systems.database.DTO.DatabaseInputDTO;
import es.caib.invai.back.interna.maintenance.systems.database.DTO.DatabaseOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.systems.database.DatabaseCriteria;
import es.caib.invai.back.persistence.repository.maintenance.systems.server.ServerRepository;
import es.caib.invai.back.service.mapper.maintenance.systems.database.DatabaseMapper;
import es.caib.invai.back.persistence.repository.maintenance.systems.database.DatabaseRepository;
import es.caib.invai.back.service.facade.maintenance.systems.database.DatabaseService;
import es.caib.invai.back.service.model.maintenance.systems.database.Database;
import es.caib.invai.back.service.model.maintenance.systems.server.Server;
import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.utils.Constants;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Facade service implementation for managing system Database objects.
 * Handles structural status assignments and filtering via standard pagination rules.
 *
 * @since 1.0.2
 */
@Service
@Slf4j
@Transactional
public class DatabaseServiceFacadeBean implements DatabaseService {

    /** Mapper converting between Database domain models, entities, and DTOs. */
    @Autowired
    private DatabaseMapper databaseMapper;

    /** Outbound port used to persist and query database records. */
    @Autowired
    private DatabaseRepository databaseRepository;

    /** Outbound port used to validate the host server referenced by a database record. */
    @Autowired
    private ServerRepository serverRepository;

    /**
     * Retrieves an active database by its unique database identifier.
     * Evaluates logical deletion properties and structural lifecycle status flags.
     *
     * @param id the unique database metadata record identity pointer
     * @return the mapped {@link DatabaseOutputDTO} response presentation payload
     * @throws BusinessRuleException if the identity does not match any active record or has been logically soft-deleted
     */
    @Override
    @Transactional(readOnly = true)
    public DatabaseOutputDTO getById(Long id) {
        log.debug("Facade: Fetching database by ID: {}", id);
        Database database = databaseRepository.findById(id);

        if (database == null) {
            throw new BusinessRuleException(Constants.ERR_DATABASE_NOT_FOUND);
        }

        return databaseMapper.toResponse(database);
    }

    /**
     * Gets a paginated distribution framework containing database records matching pagination rules.
     *
     * @param pageable sorting parameters and tracking page metadata pagination constraints
     * @return a structured page element populated with converted {@link DatabaseOutputDTO} results
     */
    @Override
    @Transactional(readOnly = true)
    public Page<DatabaseOutputDTO> getAll(DatabaseCriteria filter, Pageable pageable) {
        log.debug("Facade: Fetching databases via pagination boundaries");
        Page<Database> domainPage = databaseRepository.findAll(filter, pageable);
        return domainPage.map(databaseMapper::toResponse);
    }

    /**
     * Validates structural constraints and registers a new database record within the system core.
     * Enforces domain text sanitization and unicity rules regarding the unique combination of server and service.
     *
     * @param inputDTO data transfer container holding properties describing the target database record
     * @return the resulting persistent instance transformed into an {@link DatabaseOutputDTO} structure
     * @throws BusinessRuleException if the server and service combination conflicts with an already registered entry
     */
    @Override
    public DatabaseOutputDTO create(DatabaseInputDTO inputDTO) {
        log.info("Facade: Creating database on host ID: {} with service: {}", inputDTO.getServerId(), inputDTO.getService());

        Utils.sanitize(inputDTO);

        validateServerType(inputDTO.getServerId());

        if (databaseRepository.existsByServerAndService(inputDTO.getServerId(), inputDTO.getService())) {
            throw new BusinessRuleException(Constants.ERR_DATABASE_DUPLICATED);
        }

        Database model = databaseMapper.toModelFromInput(inputDTO);

        Database savedModel = databaseRepository.create(model);
        return databaseMapper.toResponse(savedModel);
    }

    /**
     * Mutates an existing active database entity property by replacing metrics with input payload details.
     * Ensures updates do not overlap unique constraint parameters allocated to sibling records.
     *
     * @param id       the unique database resource key indexing the record targeting modification
     * @param inputDTO data update container outlining property changes intended for persistence merge operations
     * @return the modified domain representation mapped down into an {@link DatabaseOutputDTO}
     * @throws BusinessRuleException if the resource key is non-existent, has been marked soft-deleted,
     * or if input data maps server and service keys owned by another database instance
     */
    @Override
    public DatabaseOutputDTO update(Long id, DatabaseInputDTO inputDTO) {
        log.info("Facade: Updating database ID: {}", id);

        Database existing = databaseRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_DATABASE_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        validateServerType(inputDTO.getServerId());

        if (databaseRepository.existsByServerAndServiceAndIdNot(inputDTO.getServerId(), inputDTO.getService(), id)) {
            throw new BusinessRuleException(Constants.ERR_DATABASE_SERVER_AND_SERVICE_OWNED_BY_OTHER);
        }

        databaseMapper.updateModelFromInput(inputDTO, existing);
        return databaseMapper.toResponse(databaseRepository.update(existing, id));
    }

    /**
     * Executes a logical soft-delete transaction lifecycle phase over a database record.
     * Shifts state configurations to inactive indicators and logs audit metrics profiling execution time and session user.
     *
     * @param id the target identifier mapping the database instance intended for deactivation
     * @throws BusinessRuleException if matching database instance descriptions cannot be found or are already soft-deleted
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Logically deleting database ID: {}", id);
        Database existing = databaseRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_DATABASE_NOT_FOUND);
        }

        if (existing.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_DATABASE_NOT_ACTIVE);
        }

        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy(Utils.resolveCurrentUsername());

        databaseRepository.delete(existing);
    }

    /**
     * Reactivates a logically soft-deleted database record back to active state.
     *
     * @param id the target identifier mapping the database instance intended for reactivation
     * @return the reactivated domain representation mapped into an {@link DatabaseOutputDTO}
     * @throws BusinessRuleException if a matching database cannot be found or is already active
     */
    @Override
    public DatabaseOutputDTO reactivate(Long id) {
        log.info("Facade: Reactivating database ID: {}", id);
        Database existing = databaseRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_DATABASE_NOT_FOUND);
        }

        if (existing.getDeletedAt() == null) {
            throw new BusinessRuleException(Constants.ERR_DATABASE_ACTIVE);
        }

        existing.setDeletedAt(null);
        existing.setDeletedBy(null);

        Database updatedModel = databaseRepository.update(existing, id);
        return databaseMapper.toResponse(updatedModel);
    }

    /**
     * Validates that the referenced host server is explicitly typed for hosting database engines.
     * A nonexistent server ID is intentionally left unchecked here, letting the underlying database
     * foreign key constraint reject it at persist time, consistent with every other FK field in this API.
     *
     * @param serverId the candidate host server identifier submitted in the request payload
     * @throws BusinessRuleException if the server exists but its type is not {@code DATABASE}
     */
    private void validateServerType(Long serverId) {
        Server server = serverRepository.findById(serverId);
        if (server != null
                && (server.getServerType() == null
                || !Constants.SERVER_TYPE_CODE_DATABASE.equals(server.getServerType().getCode()))) {
            throw new BusinessRuleException(Constants.ERR_DATABASE_SERVER_TYPE_INVALID);
        }
    }
}
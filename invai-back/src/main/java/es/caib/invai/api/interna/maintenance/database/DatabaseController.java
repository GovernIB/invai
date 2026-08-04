package es.caib.invai.api.interna.maintenance.database;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.api.persistence.repository.database.DatabaseCriteria;
import es.caib.invai.api.service.facade.DatabaseService;
import es.caib.invai.api.interna.maintenance.database.DTO.DatabaseInputDTO;
import es.caib.invai.api.interna.maintenance.database.DTO.DatabaseOutputDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Internal REST controller handling lifecycle endpoints for managing Database profiles metadata.
 * <p>
 * Access is restricted at the type level to corporate users holding the {@code ROLE_usuari-tipus-E} role.
 * </p>
 *
 * @since 1.0.2
 */
@Tag(name = "Bases de dades", description = "Manteniment del catàleg de bases de dades.")
@RestController
@Slf4j
@RequestMapping("database")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class DatabaseController {

    private final DatabaseService databaseService;

    /**
     * Constructs a new Database controller with necessary dependent business service layer references.
     *
     * @param databaseService domain engine layer interface executing core transactional steps
     */
    @Autowired
    public DatabaseController(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    /**
     * Streams partitioned chunk metrics using pagination layout boundaries.
     * <p>
     * Supports sorting by any {@code DatabaseEntity} property, including the nested linked
     * system's name via {@code sort=server.name} (e.g. {@code ?sort=server.name,asc}).
     * </p>
     *
     * @param filter   dynamic search criteria and full-text keyword filters
     * @param pageable pagination layout boundaries (defaults to size 10 sorted ascending by databaseId)
     * @return a {@link ResponseEntity} wrapping the paged matrix containing mapped {@link DatabaseOutputDTO} definitions
     */
    @GetMapping
    public ResponseEntity<Page<DatabaseOutputDTO>> getAll(
            @ModelAttribute DatabaseCriteria filter,
            @PageableDefault(sort = "id") Pageable pageable) {

        log.info("REST: Fetching paged databases via pagination criteria");
        Page<DatabaseOutputDTO> page = databaseService.getAll(filter, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Resolves matching database settings snapshot based on unique index values.
     *
     * @param id key structure reference primary identifier
     * @return a {@link ResponseEntity} wrapping the matching output properties model details with an HTTP 200 OK status
     */
    @GetMapping("/{id}")
    public ResponseEntity<DatabaseOutputDTO> getById(@PathVariable Long id) {
        log.info("REST: Fetching database by ID: {}", id);
        return ResponseEntity.ok(databaseService.getById(id));
    }

    /**
     * Persists a new system database structure specification parameters configuration into the database.
     * Evaluates active constraint descriptors before routing parameters across execution lines.
     *
     * @param inputDTO properties dataset containing database configurations, variables, parameters data profiles
     * @return a {@link ResponseEntity} wrapping the created snapshot parameters state with an HTTP 201 Created response
     */
    @PostMapping
    public ResponseEntity<DatabaseOutputDTO> create(@Valid @RequestBody DatabaseInputDTO inputDTO) {
        log.info("REST: Request to create new database");
        DatabaseOutputDTO created = databaseService.create(inputDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * Alters active configuration profiles mapping properties corresponding to target database references.
     *
     * @param id       targeted structural identifier element index
     * @param inputDTO property data variables mapping structural items
     * @return a {@link ResponseEntity} containing current modified configuration state properties details with an HTTP 200 OK status
     */
    @PutMapping("/{id}")
    public ResponseEntity<DatabaseOutputDTO> update(@PathVariable Long id, @Valid @RequestBody DatabaseInputDTO inputDTO) {
        log.info("REST: Request to update database ID: {}", id);
        return ResponseEntity.ok(databaseService.update(id, inputDTO));
    }

    /**
     * Routes explicit request signals targeting domain deactivation soft deletion routines.
     *
     * @param id persistent tracking row database reference index targeting removal execution paths
     * @return a {@link ResponseEntity} providing an empty success feedback with an HTTP 204 No Content response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("REST: Request to logical delete database ID: {}", id);
        databaseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reactivates a database and updates its configuration profiles mapping
     * properties corresponding to target database references.
     * <p>
     * This endpoint triggers the transition of the specified database's state,
     * logging the request, performing the reactivation process via the service layer,
     * and returning the freshly modified configuration properties.
     * </p>
     *
     * @param id the targeted structural identifier element index of the database to reactivate
     * @return a {@link ResponseEntity} containing the current modified configuration state
     *         properties details (as an {@link DatabaseOutputDTO}) with an HTTP 200 OK status
     * @throws es.caib.invai.api.exception.BusinessRuleException if no database exists with the provided structural identifier
     */
    @PutMapping("reactivate/{id}")
    public ResponseEntity<DatabaseOutputDTO> reactivate(@PathVariable Long id) {
        log.info("REST: Request to reactivate database ID: {}", id);
        return ResponseEntity.ok(databaseService.reactivate(id));
    }
}

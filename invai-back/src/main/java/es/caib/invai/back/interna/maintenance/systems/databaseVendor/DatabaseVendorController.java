package es.caib.invai.back.interna.maintenance.systems.databaseVendor;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.maintenance.systems.databaseVendor.DTO.DatabaseVendorInputDTO;
import es.caib.invai.back.interna.maintenance.systems.databaseVendor.DTO.DatabaseVendorOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.systems.databaseVendor.DatabaseVendorCriteria;
import es.caib.invai.back.service.facade.maintenance.systems.databaseVendor.DatabaseVendorService;

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
 * Internal REST controller handling lifecycle endpoints for managing DatabaseVendor catalog metadata.
 * <p>
 * Access is restricted at the type level to corporate users holding the {@code ROLE_INV_SUPER} role.
 * </p>
 *
 * @since 1.0.2
 */
@Tag(name = "Fabricants de base de dades", description = "Manteniment del catàleg de fabricants de bases de dades.")
@RestController
@Slf4j
@RequestMapping("database-vendor")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class DatabaseVendorController {

    /** Service facade handling business use cases for DatabaseVendor maintenance. */
    private final DatabaseVendorService databaseVendorService;

    /**
     * Constructs a new DatabaseVendor controller with necessary dependent business service layer references.
     *
     * @param databaseVendorService domain engine layer interface executing core transactional steps
     */
    @Autowired
    public DatabaseVendorController(DatabaseVendorService databaseVendorService) {
        this.databaseVendorService = databaseVendorService;
    }

    /**
     * Streams partitioned chunk metrics using pagination layout boundaries.
     *
     * @param pageable pagination layout boundaries (defaults to size 10 sorted ascending by ID)
     * @return a {@link ResponseEntity} wrapping the partitioned {@link Page} matrix containing mapped {@link DatabaseVendorOutputDTO} definitions
     */
    @GetMapping
    public ResponseEntity<Page<DatabaseVendorOutputDTO>> getAll(
            @ModelAttribute DatabaseVendorCriteria filter,
            @PageableDefault(sort = "id") Pageable pageable) {
        log.debug("REST: Fetching paged database vendors via pagination boundaries");
        Page<DatabaseVendorOutputDTO> page = databaseVendorService.getAll(filter, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Resolves matching database vendor settings snapshot based on unique index values.
     *
     * @param id key structure reference primary identifier
     * @return a {@link ResponseEntity} wrapping the matching output properties model details with an HTTP 200 OK status
     */
    @GetMapping("/{id}")
    public ResponseEntity<DatabaseVendorOutputDTO> getById(@PathVariable Long id) {
        log.debug("REST: Fetching database vendor by ID: {}", id);
        return ResponseEntity.ok(databaseVendorService.getById(id));
    }

    /**
     * Persists a new database vendor specification parameters configuration into the database.
     *
     * @param inputDTO properties dataset containing database vendor configurations, variables, parameters data profiles
     * @return a {@link ResponseEntity} wrapping the created snapshot parameters state with an HTTP 201 Created response
     */
    @PostMapping
    public ResponseEntity<DatabaseVendorOutputDTO> create(@Valid @RequestBody DatabaseVendorInputDTO inputDTO) {
        log.info("REST: Request to create new database vendor");
        DatabaseVendorOutputDTO created = databaseVendorService.create(inputDTO);
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
    public ResponseEntity<DatabaseVendorOutputDTO> update(@PathVariable Long id, @Valid @RequestBody DatabaseVendorInputDTO inputDTO) {
        log.info("REST: Request to update database vendor ID: {}", id);
        return ResponseEntity.ok(databaseVendorService.update(id, inputDTO));
    }

    /**
     * Routes explicit request signals targeting domain deactivation soft deletion routines.
     *
     * @param id persistent tracking row database reference index targeting removal execution paths
     * @return a {@link ResponseEntity} providing an empty success feedback with an HTTP 204 No Content response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("REST: Request to logical delete database vendor ID: {}", id);
        databaseVendorService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reactivates a database vendor and updates its configuration profiles mapping
     * properties corresponding to target database references.
     *
     * @param id the targeted structural identifier element index of the database vendor to reactivate
     * @return a {@link ResponseEntity} containing the current modified configuration state
     *         properties details (as an {@link DatabaseVendorOutputDTO}) with an HTTP 200 OK status
     * @throws es.caib.invai.back.exception.BusinessRuleException if no database vendor exists with the provided structural identifier
     */
    @PutMapping("reactivate/{id}")
    public ResponseEntity<DatabaseVendorOutputDTO> reactivate(@PathVariable Long id) {
        log.info("REST: Request to reactivate database vendor ID: {}", id);
        return ResponseEntity.ok(databaseVendorService.reactivate(id));
    }
}

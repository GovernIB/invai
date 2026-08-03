package es.caib.invai.api.interna.maintenance.system;

import es.caib.invai.api.interna.maintenance.system.DTO.SystemInputDTO;
import es.caib.invai.api.interna.maintenance.system.DTO.SystemOutputDTO;
import es.caib.invai.api.persistence.repository.system.SystemCriteria;
import es.caib.invai.api.service.facade.SystemService;

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
 * Internal REST controller handling lifecycle endpoints for managing System profiles metadata.
 * <p>
 * Access is restricted at the type level to corporate users holding the {@code ROLE_usuari-tipus-E} role.
 * </p>
 *
 * @since 1.0.1
 */
@RestController
@Slf4j
@RequestMapping("system")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class SystemController {

    private final SystemService systemService;

    /**
     * Constructs a new System controller with necessary dependent business service layer references.
     *
     * @param systemService domain engine layer interface executing core transactional steps
     */
    @Autowired
    public SystemController(SystemService systemService) {
        this.systemService = systemService;
    }

    /**
     * Streams partitioned chunk metrics using pagination layout boundaries.
     *
     * @param pageable pagination layout boundaries (defaults to size 10 sorted ascending by ID)
     * @return a {@link ResponseEntity} wrapping the partitioned {@link Page} matrix containing mapped {@link SystemOutputDTO} definitions
     */
    @GetMapping
    public ResponseEntity<Page<SystemOutputDTO>> getAll(
            @ModelAttribute SystemCriteria filter,
            @PageableDefault(sort = "id") Pageable pageable) {

        log.info("REST: Fetching paged systems via pagination boundaries");
        Page<SystemOutputDTO> page = systemService.getAll(filter, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Resolves matching system settings snapshot based on unique index values.
     *
     * @param id key structure reference primary identifier
     * @return a {@link ResponseEntity} wrapping the matching output properties model details with an HTTP 200 OK status
     */
    @GetMapping("/{id}")
    public ResponseEntity<SystemOutputDTO> getById(@PathVariable Long id) {
        log.info("REST: Fetching system by ID: {}", id);
        return ResponseEntity.ok(systemService.getById(id));
    }

    /**
     * Persists a new system specification parameters configuration into the database.
     * Evaluates active constraint descriptors before routing parameters across execution lines.
     *
     * @param inputDTO properties dataset containing system configurations, variables, parameters data profiles
     * @return a {@link ResponseEntity} wrapping the created snapshot parameters state with an HTTP 201 Created response
     */
    @PostMapping
    public ResponseEntity<SystemOutputDTO> create(@Valid @RequestBody SystemInputDTO inputDTO) {
        log.info("REST: Request to create new system");
        SystemOutputDTO created = systemService.create(inputDTO);
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
    public ResponseEntity<SystemOutputDTO> update(@PathVariable Long id, @Valid @RequestBody SystemInputDTO inputDTO) {
        log.info("REST: Request to update system ID: {}", id);
        return ResponseEntity.ok(systemService.update(id, inputDTO));
    }

    /**
     * Routes explicit request signals targeting domain deactivation soft deletion routines.
     *
     * @param id persistent tracking row database reference index targeting removal execution paths
     * @return a {@link ResponseEntity} providing an empty success feedback with an HTTP 204 No Content response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("REST: Request to logical delete system ID: {}", id);
        systemService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reactivates a system and updates its configuration profiles mapping
     * properties corresponding to target database references.
     * <p>
     * This endpoint triggers the transition of the specified system's state,
     * logging the request, performing the reactivation process via the service layer,
     * and returning the freshly modified configuration properties.
     * </p>
     *
     * @param id the targeted structural identifier element index of the system to reactivate
     * @return a {@link ResponseEntity} containing the current modified configuration state
     *         properties details (as an {@link SystemOutputDTO}) with an HTTP 200 OK status
     * @throws es.caib.invai.api.exception.BusinessRuleException if no system exists with the provided structural identifier
     */
    @PutMapping("reactivate/{id}")
    public ResponseEntity<SystemOutputDTO> reactivate(@PathVariable Long id) {
        log.info("REST: Request to reactivate system ID: {}", id);
        return ResponseEntity.ok(systemService.reactivate(id));
    }
}
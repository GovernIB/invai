package es.caib.invai.back.interna.maintenance.systems.environment;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.maintenance.systems.environment.DTO.EnvironmentInputDTO;
import es.caib.invai.back.interna.maintenance.systems.environment.DTO.EnvironmentOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.systems.environment.EnvironmentCriteria;
import es.caib.invai.back.service.facade.maintenance.systems.environment.EnvironmentService;
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
 * Internal REST controller handling lifecycle endpoints for managing Environment profiles metadata.
 * <p>
 * Access is restricted at the type level to corporate users holding the {@code ROLE_INV_SUPER} role.
 * </p>
 *
 * @since 1.0.1
 */
@Tag(name = "Entorns", description = "Manteniment del catàleg d'entorns.")
@RestController
@Slf4j
@RequestMapping("environment")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class EnvironmentController {

    /** Service facade handling business use cases for Environment maintenance. */
    private final EnvironmentService environmentService;

    /**
     * Constructs a new Environment controller with necessary dependent business service layer references.
     *
     * @param environmentService domain engine layer interface executing core transactional steps
     */
    @Autowired
    public EnvironmentController(EnvironmentService environmentService) {
        this.environmentService = environmentService;
    }

    /**
     * Streams partitioned chunk metrics using pagination layout boundaries.
     *
     * @param pageable pagination layout boundaries (defaults to size 10 sorted ascending by ID)
     * @return a {@link ResponseEntity} wrapping the partitioned {@link Page} matrix containing mapped {@link EnvironmentOutputDTO} definitions
     */
    @GetMapping
    public ResponseEntity<Page<EnvironmentOutputDTO>> getAll(
            @ModelAttribute EnvironmentCriteria filter,
            @PageableDefault(sort = "id") Pageable pageable) {
        log.debug("REST: Initiating multi-criteria fetch grid search query parameters");
        Page<EnvironmentOutputDTO> multiQueryResult = environmentService.getAll(filter, pageable);
        return ResponseEntity.ok(multiQueryResult);
    }

    /**
     * Resolves matching environment settings snapshot based on unique index values.
     *
     * @param id key structure reference primary identifier
     * @return a {@link ResponseEntity} wrapping the matching output properties model details with an HTTP 200 OK status
     */
    @GetMapping("/{id}")
    public ResponseEntity<EnvironmentOutputDTO> getById(@PathVariable Long id) {
        log.debug("REST: Fetching environment by ID: {}", id);
        return ResponseEntity.ok(environmentService.getById(id));
    }

    /**
     * Persists a new environment specification parameters configuration into the database.
     * Evaluates active constraint descriptors before routing parameters across execution lines.
     *
     * @param inputDTO properties dataset containing environment configurations, variables, parameters data profiles
     * @return a {@link ResponseEntity} wrapping the created snapshot parameters state with an HTTP 201 Created response
     */
    @PostMapping
    public ResponseEntity<EnvironmentOutputDTO> create(@Valid @RequestBody EnvironmentInputDTO inputDTO) {
        log.info("REST: Request to create new environment");
        EnvironmentOutputDTO created = environmentService.create(inputDTO);
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
    public ResponseEntity<EnvironmentOutputDTO> update(@PathVariable Long id, @Valid @RequestBody EnvironmentInputDTO inputDTO) {
        log.info("REST: Request to update environment ID: {}", id);
        return ResponseEntity.ok(environmentService.update(id, inputDTO));
    }

    /**
     * Routes explicit request signals targeting domain deactivation soft deletion routines.
     *
     * @param id persistent tracking row database reference index targeting removal execution paths
     * @return a {@link ResponseEntity} providing an empty success feedback with an HTTP 204 No Content response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("REST: Request to logical delete environment ID: {}", id);
        environmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reactivates an environment and updates its configuration profiles mapping
     * properties corresponding to target database references.
     *
     * @param id the targeted structural identifier element index of the environment to reactivate
     * @return a {@link ResponseEntity} containing the current modified configuration state
     *         properties details (as an {@link EnvironmentOutputDTO}) with an HTTP 200 OK status
     * @throws es.caib.invai.back.exception.BusinessRuleException if no environment exists with the provided structural identifier
     */
    @PutMapping("reactivate/{id}")
    public ResponseEntity<EnvironmentOutputDTO> reactivate(@PathVariable Long id) {
        log.info("REST: Request to reactivate environment ID: {}", id);
        return ResponseEntity.ok(environmentService.reactivate(id));
    }
}

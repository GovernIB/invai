package es.caib.invai.back.interna.maintenance.systems.server;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.maintenance.systems.server.DTO.ServerInputDTO;
import es.caib.invai.back.interna.maintenance.systems.server.DTO.ServerOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.systems.server.ServerCriteria;
import es.caib.invai.back.service.facade.maintenance.systems.server.ServerService;
import es.caib.invai.back.utils.Constants;

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
 * Internal REST controller handling lifecycle endpoints for managing Server infrastructure metadata.
 * <p>
 * Every endpoint that changes data requires {@code INV_SUPER}; reading is open to every role.
 * </p>
 *
 * <p>
 * Every endpoint that changes data requires {@code INV_SUPER}; reading is open to every role.
 * </p>
 *
 * @since 1.0.2
 */
@Tag(name = "Servidors", description = "Manteniment del catàleg de servidors.")
@RestController
@Slf4j
@RequestMapping("server")
@PreAuthorize(Constants.PRE_AUTH_ANY_ROLE)
public class ServerController {

    /** Facade service handling the business logic for Server infrastructure operations. */
    private final ServerService serverService;

    /**
     * Constructs a new Server controller with necessary dependent business service layer references.
     *
     * @param serverService domain engine layer interface executing core transactional steps
     */
    @Autowired
    public ServerController(ServerService serverService) {
        this.serverService = serverService;
    }

    /**
     * Streams partitioned chunk metrics using pagination layout boundaries.
     *
     * @param filter   dynamic search criteria constraints
     * @param pageable pagination layout boundaries (defaults to size 10 sorted ascending by ID)
     * @return a {@link ResponseEntity} wrapping the partitioned {@link Page} matrix containing mapped {@link ServerOutputDTO} definitions
     */
    @GetMapping
    public ResponseEntity<Page<ServerOutputDTO>> getAll(
            @ModelAttribute ServerCriteria filter,
            @PageableDefault(sort = "id") Pageable pageable) {
        log.debug("REST: Initiating multi-criteria fetch grid search query parameters");
        Page<ServerOutputDTO> multiQueryResult = serverService.getAll(filter, pageable);
        return ResponseEntity.ok(multiQueryResult);
    }

    /**
     * Resolves matching server settings snapshot based on unique index values.
     *
     * @param id key structure reference primary identifier
     * @return a {@link ResponseEntity} wrapping the matching output properties model details with an HTTP 200 OK status
     */
    @GetMapping("/{id}")
    public ResponseEntity<ServerOutputDTO> getById(@PathVariable Long id) {
        log.debug("REST: Fetching server by ID: {}", id);
        return ResponseEntity.ok(serverService.getById(id));
    }

    /**
     * Persists a new server specification parameters configuration into the database.
     *
     * @param inputDTO properties dataset containing server configurations, variables, parameters data profiles
     * @return a {@link ResponseEntity} wrapping the created snapshot parameters state with an HTTP 201 Created response
     */
    @PreAuthorize(Constants.PRE_AUTH_SUPER)
    @PostMapping
    public ResponseEntity<ServerOutputDTO> create(@Valid @RequestBody ServerInputDTO inputDTO) {
        log.info("REST: Request to create new server");
        ServerOutputDTO created = serverService.create(inputDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * Alters active configuration profiles mapping properties corresponding to target database references.
     *
     * @param id       targeted structural identifier element index
     * @param inputDTO property data variables mapping structural items
     * @return a {@link ResponseEntity} containing current modified configuration state properties details with an HTTP 200 OK status
     */
    @PreAuthorize(Constants.PRE_AUTH_SUPER)
    @PutMapping("/{id}")
    public ResponseEntity<ServerOutputDTO> update(@PathVariable Long id, @Valid @RequestBody ServerInputDTO inputDTO) {
        log.info("REST: Request to update server ID: {}", id);
        return ResponseEntity.ok(serverService.update(id, inputDTO));
    }

    /**
     * Routes explicit request signals targeting domain deactivation soft deletion routines.
     *
     * @param id persistent tracking row database reference index targeting removal execution paths
     * @return a {@link ResponseEntity} providing an empty success feedback with an HTTP 204 No Content response
     */
    @PreAuthorize(Constants.PRE_AUTH_SUPER)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("REST: Request to logical delete server ID: {}", id);
        serverService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reactivates a server and updates its configuration profiles mapping
     * properties corresponding to target database references.
     *
     * @param id the targeted structural identifier element index of the server to reactivate
     * @return a {@link ResponseEntity} containing the current modified configuration state
     *         properties details (as an {@link ServerOutputDTO}) with an HTTP 200 OK status
     * @throws es.caib.invai.back.exception.BusinessRuleException if no server exists with the provided structural identifier
     */
    @PreAuthorize(Constants.PRE_AUTH_SUPER)
    @PutMapping("reactivate/{id}")
    public ResponseEntity<ServerOutputDTO> reactivate(@PathVariable Long id) {
        log.info("REST: Request to reactivate server ID: {}", id);
        return ResponseEntity.ok(serverService.reactivate(id));
    }
}

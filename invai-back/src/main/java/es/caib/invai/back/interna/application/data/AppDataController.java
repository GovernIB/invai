package es.caib.invai.back.interna.application.data;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.application.data.DTO.AppDataInputDTO;
import es.caib.invai.back.interna.application.data.DTO.AppDataOutputDTO;
import es.caib.invai.back.service.facade.application.data.AppDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * REST controller exposing CRUD endpoints for the "data" anchor, covering both the "Open Data"
 * and "Reutilització" tabs, whose {@code getById} additionally resolves the owning application's
 * published GET endpoints live from its own external REST API (Open Data) and its own backend
 * (Reutilització). All endpoints require {@code ROLE_INV_SUPER}.
 *
 * @since 1.0.5
 */
@Slf4j
@Validated
@Tag(name = "Dades", description = "Servei de gestió dels ancoratges de dades (open data i reutilització) associats a les aplicacions.")
@RestController
@RequiredArgsConstructor
@RequestMapping("application/data")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class AppDataController {

    /** Service facade handling business operations for data anchors. */
    private final AppDataService appDataService;

    /**
     * Fetches the data anchor by its primary key, together with the owning application's
     * currently published GET endpoints for both tabs (resolved live). Returns 200 OK with a
     * {@code null} body when no anchor exists with this ID.
     *
     * @param id primary key of the data anchor
     * @return the matching anchor, or an empty 200 OK response if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<AppDataOutputDTO> getById(@PathVariable Long id) {
        log.debug("REST: Fetching application data anchor for ID: {}", id);
        AppDataOutputDTO result = appDataService.getById(id);
        return ResponseEntity.ok(result);
    }

    /**
     * Creates a new data anchor for the application identified in the payload. Fails if that
     * application already has an active data anchor.
     *
     * @param inputDTO validated creation payload
     * @return 201 Created with the newly created anchor
     */
    @PostMapping
    public ResponseEntity<AppDataOutputDTO> create(@Valid @RequestBody AppDataInputDTO inputDTO) {
        log.info("REST: Processing persistence request for new application data anchor");
        AppDataOutputDTO created = appDataService.create(inputDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Updates the data anchor identified by {@code id}. Does not check whether the anchor is
     * currently soft-deleted.
     *
     * @param id       primary key of the data anchor to update
     * @param inputDTO payload with the new field values
     * @return 200 OK with the updated anchor
     */
    @PutMapping("/{id}")
    public ResponseEntity<AppDataOutputDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody AppDataInputDTO inputDTO) {
        log.info("REST: Updating application data anchor ID: {}", id);
        return ResponseEntity.ok(appDataService.update(id, inputDTO));
    }

    /**
     * Soft-deletes the data anchor identified by {@code id}.
     *
     * @param id primary key of the data anchor to delete
     * @return 204 No Content on success
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("REST: Triggering logical deletion for application data anchor ID: {}", id);
        appDataService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

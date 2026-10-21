package es.caib.invai.back.interna.application.data;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.application.data.DTO.AppDataInputDTO;
import es.caib.invai.back.interna.application.data.DTO.AppDataOutputDTO;
import es.caib.invai.back.interna.application.data.DTO.AppDataUrlInputDTO;
import es.caib.invai.back.interna.application.data.DTO.AppDataUrlOutputDTO;
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
 * and "Reutilització" tabs. The URL of the swagger of each tab is set on its own, tab by tab, so
 * that changing one can never touch the other, and checked URL by URL with its own endpoint. All
 * endpoints require {@code ROLE_INV_SUPER}.
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
     * Fetches the data anchor by its primary key, with the URL each tab uses (the one typed in by
     * hand, or the default one). No URL is queried: see {@link #checkUrl}. Returns 200 OK with a
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
     * Queries a URL to find out whether it answers with a 2xx status code. A URL that doesn't work
     * is not an error: it comes back marked as not valid. One call checks one URL; to check both
     * tabs, call it once with each.
     *
     * @param url the URL to check
     * @return 200 OK with the URL queried and whether it works
     */
    @GetMapping("/check-url")
    public ResponseEntity<AppDataUrlOutputDTO> checkUrl(@RequestParam String url) {
        log.debug("REST: Checking the data URL {}", url);
        return ResponseEntity.ok(appDataService.checkUrl(url));
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
     * Updates the observation of the data anchor identified by {@code id}; the URL of each tab is
     * set on its own (see {@link #updateOpenDataUrl} and {@link #updateReuseUrl}). Does not check
     * whether the anchor is currently soft-deleted.
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
     * Sets the URL of the swagger of the "Open Data" tab, typed in by the user after the one in
     * use turned out not to work or because they want a different one; a {@code null} URL goes
     * back to the default one. The URL the tab will use is queried to record whether it answers
     * with a 2xx status code; one that doesn't is saved anyway, as not valid.
     *
     * @param id       primary key of the data anchor
     * @param inputDTO validated payload carrying the URL
     * @return 200 OK with the URL the tab uses (the one typed in, or else the default one), and whether it works
     */
    @PutMapping("/{id}/open-data")
    public ResponseEntity<AppDataUrlOutputDTO> updateOpenDataUrl(
            @PathVariable Long id,
            @Valid @RequestBody AppDataUrlInputDTO inputDTO) {
        log.info("REST: Setting the Open Data URL of application data anchor ID: {}", id);
        return ResponseEntity.ok(appDataService.updateOpenDataUrl(id, inputDTO));
    }

    /**
     * Sets the URL of the swagger of the "Reutilització" tab, typed in by the user after the one in
     * use turned out not to work or because they want a different one; a {@code null} URL goes
     * back to the default one. The URL the tab will use is queried to record whether it answers
     * with a 2xx status code; one that doesn't is saved anyway, as not valid.
     *
     * @param id       primary key of the data anchor
     * @param inputDTO validated payload carrying the URL
     * @return 200 OK with the URL the tab uses (the one typed in, or else the default one), and whether it works
     */
    @PutMapping("/{id}/reuse")
    public ResponseEntity<AppDataUrlOutputDTO> updateReuseUrl(
            @PathVariable Long id,
            @Valid @RequestBody AppDataUrlInputDTO inputDTO) {
        log.info("REST: Setting the Reutilitzacio URL of application data anchor ID: {}", id);
        return ResponseEntity.ok(appDataService.updateReuseUrl(id, inputDTO));
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

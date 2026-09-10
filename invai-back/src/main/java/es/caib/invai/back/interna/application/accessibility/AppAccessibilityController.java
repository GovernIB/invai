package es.caib.invai.back.interna.application.accessibility;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.application.accessibility.DTO.AppAccessibilityInputDTO;
import es.caib.invai.back.interna.application.accessibility.DTO.AppAccessibilityOutputDTO;
import es.caib.invai.back.service.facade.application.accessibility.AppAccessibilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * REST controller exposing CRUD endpoints for the "AppAccessibility" (Accessibilitat) tab anchor.
 * All endpoints require {@code ROLE_INV_SUPER}.
 *
 * @since 1.0.4
 */
@Slf4j
@Validated
@Tag(name = "Accessibilitat", description = "Servei de gestió dels ancoratges d'accessibilitat associats a les aplicacions.")
@RestController
@RequiredArgsConstructor
@RequestMapping("application/accessibility")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class AppAccessibilityController {

    /** Service facade handling business operations for accessibility anchors. */
    private final AppAccessibilityService appAccessibilityService;

    /**
     * Fetches the accessibility anchor by its primary key. Returns 200 OK with a {@code null}
     * body when no anchor exists with this ID — the facade does not throw for a missing anchor here.
     *
     * @param id primary key of the accessibility anchor
     * @return the matching anchor, or an empty 200 OK response if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<AppAccessibilityOutputDTO> getById(@PathVariable Long id) {
        log.debug("REST: Initiating lookup operation for application accessibility ID: {}", id);
        AppAccessibilityOutputDTO result = appAccessibilityService.getById(id);
        return ResponseEntity.ok(result);
    }

    /**
     * Creates a new accessibility anchor for the application identified in the payload. Fails if
     * that application already has an active accessibility anchor.
     *
     * @param inputDTO validated creation payload
     * @return 201 Created with the newly created anchor
     */
    @PostMapping
    public ResponseEntity<AppAccessibilityOutputDTO> create(@Valid @RequestBody AppAccessibilityInputDTO inputDTO) {
        log.info("REST: Processing persistence request for new application accessibility anchor");
        AppAccessibilityOutputDTO completedPayload = appAccessibilityService.create(inputDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(completedPayload);
    }

    /**
     * Updates the accessibility anchor identified by {@code id}. Does not check whether the
     * anchor is currently soft-deleted.
     *
     * @param id       primary key of the accessibility anchor to update
     * @param inputDTO payload with the new field values
     * @return 200 OK with the updated anchor
     */
    @PutMapping("/{id}")
    public ResponseEntity<AppAccessibilityOutputDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody AppAccessibilityInputDTO inputDTO) {
        log.info("REST: Merging mutation parameters for tracking reference target identity: {}", id);
        return ResponseEntity.ok(appAccessibilityService.update(id, inputDTO));
    }

    /**
     * Soft-deletes the accessibility anchor identified by {@code id}.
     *
     * @param id primary key of the accessibility anchor to delete
     * @return 204 No Content on success
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("REST: Triggering logical deletion lifecycle sequencing for identity context: {}", id);
        appAccessibilityService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

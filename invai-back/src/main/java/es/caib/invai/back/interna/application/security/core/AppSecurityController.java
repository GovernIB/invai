package es.caib.invai.back.interna.application.security.core;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.application.security.core.DTO.AppSecurityInputDTO;
import es.caib.invai.back.interna.application.security.core.DTO.AppSecurityOutputDTO;
import es.caib.invai.back.service.facade.application.security.core.AppSecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Primary Inbound REST Adapter providing exposed endpoints for administrative operations
 * targeting the Application-to-Security anchors under CAIB governance.
 *
 * @since 1.0.4
 */
@Slf4j
@Validated
@Tag(name = "Seguretat", description = "Servei de gestió dels ancoratges de seguretat associats a les aplicacions.")
@RestController
@RequiredArgsConstructor
@RequestMapping("application/security")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class AppSecurityController {

    /** Service facade handling business operations for security anchors. */
    private final AppSecurityService appSecurityService;

    /**
     * Retrieves a paginated sequence of application security anchors scoped to
     * a single parent application. The application identifier is a mandatory path variable: this
     * endpoint never lists every anchor in the database.
     *
     * @param id mandatory parent application identifier scoping the result set
     * @return a paginated payload containing corresponding transfer representations
     */
    @GetMapping("/{id}")
    public ResponseEntity<AppSecurityOutputDTO> getAllSecurityId(
            @PathVariable Long id) {
        log.debug("REST: Initiating dynamic paginated search operation for Application ID: {}", id);
        AppSecurityOutputDTO targetPage = appSecurityService.getById(id);
        return ResponseEntity.ok(targetPage);
    }

    /**
     * Executes a transactional instantiation command to persist a new tracking schema.
     *
     * @param inputDTO validated data configuration schema
     * @return outbound structural representation of the newly created entity
     */
    @PostMapping
    public ResponseEntity<AppSecurityOutputDTO> create(@Valid @RequestBody AppSecurityInputDTO inputDTO) {
        log.info("REST: Processing persistence request for new application security anchor");
        AppSecurityOutputDTO completedPayload = appSecurityService.create(inputDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(completedPayload);
    }

    /**
     * Updates an active relational registry with modified enterprise metadata parameters.
     *
     * @param id       primary corporate tracking reference key
     * @param inputDTO mutated parameter dataset structures
     * @return updated transfer data mapping payload state
     */
    @PutMapping("/{id}")
    public ResponseEntity<AppSecurityOutputDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody AppSecurityInputDTO inputDTO) {
        log.info("REST: Merging mutation parameters for tracking reference target identity: {}", id);
        return ResponseEntity.ok(appSecurityService.update(id, inputDTO));
    }

    /**
     * Transitions a target tracking record into an inactive state by enforcing logical deletion structures.
     *
     * @param id target primary structural key to process for deprecation
     * @return an empty response body confirming success status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("REST: Triggering logical deletion lifecycle sequencing for identity context: {}", id);
        appSecurityService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

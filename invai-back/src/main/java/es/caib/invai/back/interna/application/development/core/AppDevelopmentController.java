package es.caib.invai.back.interna.application.development.core;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.application.development.core.DTO.DevelopmentInputDTO;
import es.caib.invai.back.interna.application.development.core.DTO.DevelopmentOutputDTO;
import es.caib.invai.back.service.facade.application.development.core.AppDevelopmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * REST endpoints for the "AppDevelopment" tab (single development record per application, see
 * {@link es.caib.invai.back.service.model.application.development.core.AppDevelopment}). All
 * endpoints require {@code ROLE_INV_SUPER}.
 *
 * @since 1.0.2
 */
@Slf4j
@Validated
@Tag(name = "Desenvolupaments", description = "Servei de gestió dels desenvolupaments associats a les aplicacions.")
@RestController
@RequiredArgsConstructor
@RequestMapping("application/development")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class AppDevelopmentController {

    /** Facade service handling the business logic for AppDevelopment operations. */
    private final AppDevelopmentService appDevelopmentService;

    /**
     * Fetches a development record by its own primary key ({@code id} here is the development
     * record's id, not the parent application's id — see
     * {@link es.caib.invai.back.ejb.application.development.core.AppDevelopmentServiceFacadeBean#getById}).
     * Returns a single object, not a page.
     *
     * @param id the development record's own identifier
     * @return 200 with the mapped development DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<DevelopmentOutputDTO> getById(
            @PathVariable Long id) {
        log.debug("REST: Initiating dynamic paginated search operation for Application ID: {}", id);
        DevelopmentOutputDTO targetPage = appDevelopmentService.getById(id);
        return ResponseEntity.ok(targetPage);
    }

    /**
     * Creates the development record for an application, rejecting the request if that application
     * already has an active one (see the 1-to-1 enforcement in the facade's {@code create}).
     *
     * @param inputDTO the application, environment and lookup references for the new record
     * @return 201 with the mapped development DTO
     */
    @PostMapping
    public ResponseEntity<DevelopmentOutputDTO> create(@Valid @RequestBody DevelopmentInputDTO inputDTO) {
        log.info("REST: Processing persistence request for new application development record");
        DevelopmentOutputDTO completedPayload = appDevelopmentService.create(inputDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(completedPayload);
    }

    /**
     * Updates an existing development record in place.
     *
     * @param id       the development record's own identifier
     * @param inputDTO the replacement field values
     * @return 200 with the mapped development DTO reflecting the applied changes
     */
    @PutMapping("/{id}")
    public ResponseEntity<DevelopmentOutputDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody DevelopmentInputDTO inputDTO) {
        log.info("REST: Merging mutation parameters for development target identity: {}", id);
        return ResponseEntity.ok(appDevelopmentService.update(id, inputDTO));
    }

    /**
     * Soft-deletes a development record (stamps {@code deletedAt}/{@code deletedBy}); the row itself
     * is not removed.
     *
     * @param id the development record's own identifier
     * @return 204 with no body
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("REST: Triggering logical deletion lifecycle sequencing for identity context: {}", id);
        appDevelopmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

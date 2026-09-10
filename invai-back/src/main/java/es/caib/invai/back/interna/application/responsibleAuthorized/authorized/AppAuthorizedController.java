package es.caib.invai.back.interna.application.responsibleAuthorized.authorized;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.application.responsibleAuthorized.authorized.DTO.AppAuthorizedDeleteDTO;
import es.caib.invai.back.interna.application.responsibleAuthorized.authorized.DTO.AppAuthorizedInputDTO;
import es.caib.invai.back.interna.application.responsibleAuthorized.authorized.DTO.AppAuthorizedOutputDTO;
import es.caib.invai.back.persistence.repository.application.responsibleAuthorized.authorized.AppAuthorizedCriteria;
import es.caib.invai.back.service.facade.application.responsibleAuthorized.authorized.AppAuthorizedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Internal REST controller handling lifecycle endpoints for managing person authorizations
 * linked to applications.
 * <p>
 * Deletion ("donar de baixa") is a soft delete performed through the standard
 * {@code DELETE /{id}} endpoint, which accepts an optional free-text observation as its body.
 * </p>
 *
 * @since 1.0.3
 */
@Slf4j
@Validated
@Tag(name = "Autoritzats d'aplicació", description = "Servei de gestió de les persones autoritzades associades a les aplicacions.")
@RestController
@RequiredArgsConstructor
@RequestMapping("application/authorized")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class AppAuthorizedController {

    /** Facade service handling the business logic for AppAuthorized operations. */
    private final AppAuthorizedService appAuthorizedService;

    /**
     * Retrieves a paginated, filtered listing of authorized-person assignments scoped to a single
     * "Responsables i Autoritzats" anchor.
     *
     * @param appResponsibleAuthorizedId mandatory parent anchor identifier scoping the result set
     * @param criteria      optional filter values (status, person, free-text search)
     * @param pageable      pagination and sorting parameters
     * @return the matching page of authorizations, each carrying its resolved authorization types
     */
    @GetMapping("/{appResponsibleAuthorizedId}")
    public ResponseEntity<Page<AppAuthorizedOutputDTO>> getAllByAppResponsibleAuthorizedId(
            @PathVariable Long appResponsibleAuthorizedId,
            @ModelAttribute AppAuthorizedCriteria criteria,
            @PageableDefault(sort = "id") Pageable pageable) {
        log.debug("REST: Fetching paged application authorized records for AppResponsibleAuthorized ID: {} and criteria: {}", appResponsibleAuthorizedId, criteria);
        Page<AppAuthorizedOutputDTO> targetPage = appAuthorizedService.getAll(appResponsibleAuthorizedId, criteria, pageable);
        return ResponseEntity.ok(targetPage);
    }

    /**
     * Creates a new authorized-person assignment together with its initial set of authorization
     * types.
     *
     * @param inputDTO validated create payload
     * @return the newly created assignment, with HTTP 201 status
     */
    @PostMapping
    public ResponseEntity<AppAuthorizedOutputDTO> create(@Valid @RequestBody AppAuthorizedInputDTO inputDTO) {
        log.info("REST: Processing persistence request for new application authorized assignment");
        AppAuthorizedOutputDTO created = appAuthorizedService.create(inputDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Updates an active authorized assignment, reconciling its attached authorization types
     * against the requested list.
     *
     * @param id       identifier of the assignment to update
     * @param inputDTO validated update payload
     * @return the updated assignment
     */
    @PutMapping("/{id}")
    public ResponseEntity<AppAuthorizedOutputDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody AppAuthorizedInputDTO inputDTO) {
        log.info("REST: Merging mutation parameters for application authorized target identity: {}", id);
        return ResponseEntity.ok(appAuthorizedService.update(id, inputDTO));
    }

    /**
     * Soft-deletes an active authorized assignment ("donar de baixa"), optionally capturing a
     * free-text observation.
     *
     * @param id  identifier of the assignment to deactivate
     * @param dto optional payload carrying the deletion observation
     * @return an empty response body confirming success status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestBody(required = false) AppAuthorizedDeleteDTO dto) {
        log.info("REST: Triggering deletion lifecycle sequencing for application authorized ID: {}", id);
        appAuthorizedService.delete(id, dto);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reactivates a previously deactivated authorized assignment.
     *
     * @param id identifier of the assignment to reactivate
     * @return a {@link ResponseEntity} containing the reactivated payload with an HTTP 200 OK status
     */
    @PutMapping("reactivate/{id}")
    public ResponseEntity<AppAuthorizedOutputDTO> reactivate(@PathVariable Long id) {
        log.info("REST: Request to reactivate application authorized ID: {}", id);
        return ResponseEntity.ok(appAuthorizedService.reactivate(id));
    }
}

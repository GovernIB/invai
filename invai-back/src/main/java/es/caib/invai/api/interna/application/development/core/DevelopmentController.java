package es.caib.invai.api.interna.application.development.core;

import es.caib.invai.api.interna.application.development.core.DTO.DevelopmentInputDTO;
import es.caib.invai.api.interna.application.development.core.DTO.DevelopmentOutputDTO;
import es.caib.invai.api.service.facade.DevelopmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Primary Inbound REST Adapter providing exposed endpoints for administrative operations
 * targeting the main Application Development module detail under CAIB governance.
 *
 * @since 1.0.2
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("application/development")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class DevelopmentController {

    private final DevelopmentService developmentService;

    /**
     * Retrieves a paginated sequence of development records scoped to a single parent application.
     * The application identifier is a mandatory path variable: this endpoint never lists every
     * development record in the database.
     *
     * @param id mandatory parent application identifier scoping the result set
     * @return a paginated payload containing corresponding transfer representations
     */
    @GetMapping("/{id}")
    public ResponseEntity<DevelopmentOutputDTO> getById(
            @PathVariable Long id) {
        log.info("REST: Initiating dynamic paginated search operation for Application ID: {}", id);
        DevelopmentOutputDTO targetPage = developmentService.getById(id);
        return ResponseEntity.ok(targetPage);
    }

    /**
     * Executes a transactional instantiation command to persist a new development record.
     *
     * @param inputDTO validated data configuration schema
     * @return outbound structural representation of the newly created entity
     */
    @PostMapping
    public ResponseEntity<DevelopmentOutputDTO> create(@Valid @RequestBody DevelopmentInputDTO inputDTO) {
        log.info("REST: Processing persistence request for new application development record");
        DevelopmentOutputDTO completedPayload = developmentService.create(inputDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(completedPayload);
    }

    /**
     * Updates an active development registry with modified metadata parameters.
     *
     * @param id       primary corporate tracking reference key
     * @param inputDTO mutated parameter dataset structures
     * @return updated transfer data mapping payload state
     */
    @PutMapping("/{id}")
    public ResponseEntity<DevelopmentOutputDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody DevelopmentInputDTO inputDTO) {
        log.info("REST: Merging mutation parameters for development target identity: {}", id);
        return ResponseEntity.ok(developmentService.update(id, inputDTO));
    }

    /**
     * Transitions a target development record into an inactive state by enforcing logical deletion structures.
     *
     * @param id target primary structural key to process for deprecation
     * @return an empty response body confirming success status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("REST: Triggering logical deletion lifecycle sequencing for identity context: {}", id);
        developmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

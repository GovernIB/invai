package es.caib.invai.back.interna.application.development.technology;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.application.development.technology.DTO.AppTechnologyInputDTO;
import es.caib.invai.back.interna.application.development.technology.DTO.AppTechnologyOutputDTO;
import es.caib.invai.back.persistence.repository.application.development.technology.AppTechnologyCriteria;
import es.caib.invai.back.service.facade.application.development.technology.AppTechnologyService;
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
 * Primary Inbound REST Adapter providing exposed endpoints for administrative operations
 * targeting technology stack entries linked to Application Development modules under CAIB governance.
 *
 * @since 1.0.2
 */
@Slf4j
@Validated
@Tag(name = "Tecnologies d'aplicació", description = "Servei de gestió de les tecnologies associades als desenvolupaments de les aplicacions.")
@RestController
@RequiredArgsConstructor
@RequestMapping("application/development/technology")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class AppTechnologyController {

    /** Facade service handling the business logic for AppTechnology operations. */
    private final AppTechnologyService appTechnologyService;

    /**
     * Retrieves a paginated sequence of technology records scoped to a single parent development
     * module, additionally filtered by dynamic criteria. The development identifier is a mandatory
     * path variable: this endpoint never lists every technology entry in the database.
     *
     * @param appDevelopmentId mandatory parent development identifier scoping the result set
     * @param criteria         the multi-parameter business query filter boundaries
     * @param pageable         pagination structural constraints
     * @return a paginated payload containing corresponding transfer representations
     */
    @GetMapping("/{appDevelopmentId}")
    public ResponseEntity<Page<AppTechnologyOutputDTO>> getAllByAppDevelopmentId(
            @PathVariable Long appDevelopmentId,
            @ModelAttribute AppTechnologyCriteria criteria,
            @PageableDefault(sort = "id") Pageable pageable) {
        log.info("REST: Initiating dynamic paginated search operation for Development ID: {} and criteria: {}", appDevelopmentId, criteria);
        Page<AppTechnologyOutputDTO> targetPage = appTechnologyService.getAll(appDevelopmentId, criteria, pageable);
        return ResponseEntity.ok(targetPage);
    }

    /**
     * Executes a transactional instantiation command to persist a new technology record.
     *
     * @param inputDTO validated data configuration schema
     * @return outbound structural representation of the newly created entity
     */
    @PostMapping
    public ResponseEntity<AppTechnologyOutputDTO> create(@Valid @RequestBody AppTechnologyInputDTO inputDTO) {
        log.info("REST: Processing persistence request for new technology entry");
        AppTechnologyOutputDTO completedPayload = appTechnologyService.create(inputDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(completedPayload);
    }

    /**
     * Updates an active technology registry with modified metadata parameters.
     *
     * @param id       primary corporate tracking reference key
     * @param inputDTO mutated parameter dataset structures
     * @return updated transfer data mapping payload state
     */
    @PutMapping("/{id}")
    public ResponseEntity<AppTechnologyOutputDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody AppTechnologyInputDTO inputDTO) {
        log.info("REST: Merging mutation parameters for technology target identity: {}", id);
        return ResponseEntity.ok(appTechnologyService.update(id, inputDTO));
    }

    /**
     * Transitions a target technology record into an inactive state by enforcing logical deletion structures.
     *
     * @param id target primary structural key to process for deprecation
     * @return an empty response body confirming success status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("REST: Triggering logical deletion lifecycle sequencing for identity context: {}", id);
        appTechnologyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

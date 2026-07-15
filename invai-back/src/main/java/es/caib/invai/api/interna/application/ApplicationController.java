package es.caib.invai.api.interna.application;

import es.caib.invai.api.service.facade.ApplicationService;
import es.caib.invai.api.interna.application.DTO.ApplicationInputDTO;
import es.caib.invai.api.interna.application.DTO.ApplicationOutputDTO;
import es.caib.invai.api.persistence.repository.application.ApplicationCriteria;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Internal REST controller handling lifecycle endpoints for managing Application profiles metadata.
 * <p>
 * Access is restricted at the type level to corporate users holding the {@code ROLE_usuari-tipus-E} role.
 * </p>
 *
 * @since 1.0.0
 */
@RestController
@Slf4j
@RequestMapping("interna/application")
@PreAuthorize("hasRole('ROLE_usuari-tipus-E')")
public class ApplicationController {

    private final ApplicationService applicationService;

    /**
     * Constructs a new Application controller with necessary dependent business service layer references.
     *
     * @param applicationService domain engine layer interface executing core transactional steps
     */
    @Autowired
    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /**
     * Streams partitioned chunk metrics using advanced query filter parameters and specifications.
     *
     * @param criteria dynamic constraint filter maps targeting application properties
     * @param pageable pagination layout boundaries (defaults to size 10 sorted ascending by ID)
     * @return a {@link ResponseEntity} wrapping the filtered {@link Page} matrix containing mapped {@link ApplicationOutputDTO} definitions
     */
    @GetMapping
    public ResponseEntity<Page<ApplicationOutputDTO>> getAll(
            ApplicationCriteria criteria,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {

        log.info("REST: Fetching paged applications via advanced filters and quick search criteria");
        Page<ApplicationOutputDTO> page = applicationService.getAll(criteria, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Resolves matching application settings snapshot based on unique index values.
     *
     * @param id key structure reference primary identifier
     * @return a {@link ResponseEntity} wrapping the matching output properties model details with an HTTP 200 OK status
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApplicationOutputDTO> getById(@PathVariable Long id) {
        log.info("REST: Fetching application by ID: {}", id);
        return ResponseEntity.ok(applicationService.getById(id));
    }

    /**
     * Persists a new system application structure specification parameters configuration into the database.
     * Evaluates active constraint descriptors before routing parameters across execution lines.
     *
     * @param inputDTO properties dataset containing application configurations, variables, parameters data profiles
     * @return a {@link ResponseEntity} wrapping the created snapshot parameters state with an HTTP 201 Created response
     */
    @PostMapping
    public ResponseEntity<ApplicationOutputDTO> create(@Valid @RequestBody ApplicationInputDTO inputDTO) {
        log.info("REST: Request to create new application");
        ApplicationOutputDTO created = applicationService.create(inputDTO);
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
    public ResponseEntity<ApplicationOutputDTO> update(@PathVariable Long id, @Valid @RequestBody ApplicationInputDTO inputDTO) {
        log.info("REST: Request to update application ID: {}", id);
        return ResponseEntity.ok(applicationService.update(id, inputDTO));
    }

    /**
     * Routes explicit request signals targeting domain deactivation soft deletion routines.
     *
     * @param id persistent tracking row database reference index targeting removal execution paths
     * @return a {@link ResponseEntity} providing an empty success feedback with an HTTP 204 No Content response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("REST: Request to logical delete application ID: {}", id);
        applicationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
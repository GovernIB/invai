package es.caib.invai.back.interna.application.core;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.service.facade.application.core.ApplicationService;
import es.caib.invai.back.interna.application.core.DTO.ApplicationInputDTO;
import es.caib.invai.back.interna.application.core.DTO.ApplicationOutputDTO;
import es.caib.invai.back.persistence.repository.application.core.ApplicationCriteria;
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
 * REST controller exposing CRUD and soft-delete/reactivate endpoints for {@code Application}, the
 * root inventory record each of the Development, Accessibility, Security, System/Database, and
 * Responsible/Authorized tabs hangs off of.
 * <p>
 * Every endpoint requires the {@code ROLE_INV_SUPER} role.
 * </p>
 *
 * @since 1.0.1
 */
@Tag(name = "Aplicacions", description = "Servei de gestió de les aplicacions informàtiques de l'inventari.")
@RestController
@Slf4j
@RequestMapping("application")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class ApplicationController {

    /** Facade implementing the Application use cases invoked by this controller. */
    private final ApplicationService applicationService;

    /**
     * @param applicationService facade this controller delegates all Application use cases to
     */
    @Autowired
    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /**
     * Lists applications matching the given dynamic search criteria, paginated. Each row's {@code
     * incomplete} flag is computed per application unless {@code criteria.incomplete} filters on it,
     * in which case every returned row shares that filter value (see {@code
     * ApplicationServiceFacadeBean#getAll}).
     *
     * @param criteria dynamic filtering constraints applied to the application query
     * @param pageable pagination and sorting parameters (defaults to size 10 sorted ascending by ID)
     * @return a {@link ResponseEntity} wrapping a page of mapped {@link ApplicationOutputDTO} results
     */
    @GetMapping
    public ResponseEntity<Page<ApplicationOutputDTO>> getAll(
            ApplicationCriteria criteria,
            @PageableDefault(sort = "id") Pageable pageable) {

        log.debug("REST: Fetching paged applications via advanced filters and quick search criteria");
        Page<ApplicationOutputDTO> page = applicationService.getAll(criteria, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Fetches a single application, together with the completeness flags of every tab (Responsables,
     * Desenvolupament, Accessibilitat, Seguretat, Sistemes/Bases de dades) computed for it — unlike
     * {@link #getAll}, which only ever returns the aggregate {@code incomplete} flag.
     *
     * @param id primary key of the application to fetch
     * @return a {@link ResponseEntity} wrapping the mapped {@link ApplicationOutputDTO}, HTTP 200
     * @throws BusinessRuleException if no application exists with the given ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApplicationOutputDTO> getById(@PathVariable Long id) {
        log.debug("REST: Fetching application by ID: {}", id);
        return ResponseEntity.ok(applicationService.getById(id));
    }

    /**
     * Creates a new application, rejecting a duplicate {@code code} or {@code prefix}, and
     * automatically instantiates the default (empty) Development, Security, System/Database, and
     * Responsible/Authorized tab records bound to it.
     *
     * @param inputDTO application data to persist
     * @return a {@link ResponseEntity} wrapping the created {@link ApplicationOutputDTO}, HTTP 201
     */
    @PostMapping
    public ResponseEntity<ApplicationOutputDTO> create(@Valid @RequestBody ApplicationInputDTO inputDTO) {
        log.info("REST: Request to create new application");
        ApplicationOutputDTO created = applicationService.create(inputDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * Updates an existing, non-deleted application, rejecting a {@code code} or {@code prefix}
     * already owned by a different application, and backfills any of the Development, Security,
     * System/Database, or Responsible/Authorized tab records missing on it.
     *
     * @param id       primary key of the application to update
     * @param inputDTO new application data
     * @return a {@link ResponseEntity} wrapping the updated {@link ApplicationOutputDTO}, HTTP 200
     * @throws BusinessRuleException if no application exists with the given ID, or it is already deleted
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApplicationOutputDTO> update(@PathVariable Long id, @Valid @RequestBody ApplicationInputDTO inputDTO) {
        log.info("REST: Request to update application ID: {}", id);
        return ResponseEntity.ok(applicationService.update(id, inputDTO));
    }

    /**
     * Soft-deletes the application: stamps {@code deletedAt}/{@code deletedBy} and sets its status
     * to inactive, cascading the same soft delete to its linked System/Database, Development,
     * Responsible/Authorized, and Security records.
     *
     * @param id primary key of the application to delete
     * @return a {@link ResponseEntity} with no body, HTTP 204
     * @throws BusinessRuleException if no application exists with the given ID, or it is already deleted
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("REST: Request to logical delete application ID: {}", id);
        applicationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reactivates a soft-deleted application: clears {@code deletedAt}/{@code deletedBy} and
     * restores its status to active, cascading the same reactivation to its linked
     * System/Database, Development, Responsible/Authorized, and Security records.
     *
     * @param id primary key of the application to reactivate
     * @return a {@link ResponseEntity} wrapping the reactivated {@link ApplicationOutputDTO}, HTTP 200
     * @throws BusinessRuleException if no application exists with the given ID, or it is not deleted
     */
    @PutMapping("reactivate/{id}")
    public ResponseEntity<ApplicationOutputDTO> reactivate(@PathVariable Long id) {
        log.info("REST: Request to update application ID: {}", id);
        return ResponseEntity.ok(applicationService.reactivate(id));
    }
}

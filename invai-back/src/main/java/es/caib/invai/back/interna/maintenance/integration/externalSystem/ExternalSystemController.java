package es.caib.invai.back.interna.maintenance.integration.externalSystem;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.maintenance.integration.externalSystem.DTO.ExternalSystemInputDTO;
import es.caib.invai.back.interna.maintenance.integration.externalSystem.DTO.ExternalSystemOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.integration.externalSystem.ExternalSystemCriteria;
import es.caib.invai.back.service.facade.maintenance.integration.externalSystem.ExternalSystemService;
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
 * Internal REST controller that manages lifecycle endpoints and routing rules for External System assets.
 *
 * @since 1.0.5
 */
@Tag(name = "Sistemes externs", description = "Manteniment del catàleg de sistemes externs.")
@RestController
@Slf4j
@RequestMapping("external-system")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class ExternalSystemController {

    /** Facade service handling the business use cases for External System resources. */
    private final ExternalSystemService externalSystemService;

    /**
     * Creates a controller instance with the given service dependency injected.
     *
     * @param externalSystemService the facade service used to perform External System operations
     */
    @Autowired
    public ExternalSystemController(ExternalSystemService externalSystemService) {
        this.externalSystemService = externalSystemService;
    }

    /**
     * Retrieves an external system by its identifier.
     *
     * @param id the external system identifier
     * @return HTTP 200 with the matching external system
     */
    @GetMapping("/{id}")
    public ResponseEntity<ExternalSystemOutputDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(externalSystemService.getById(id));
    }

    /**
     * Retrieves a paginated list of external systems matching the given filter criteria.
     *
     * @param filter   search and status filter criteria
     * @param pageable pagination and sorting information
     * @return HTTP 200 with the matching page of external systems
     */
    @GetMapping
    public ResponseEntity<Page<ExternalSystemOutputDTO>> getAll(
            @ModelAttribute ExternalSystemCriteria filter,
            @PageableDefault(sort = "id") Pageable pageable) {
        return ResponseEntity.ok(externalSystemService.getAll(filter, pageable));
    }

    /**
     * Creates a new external system record.
     *
     * @param inputDTO the external system data to create
     * @return HTTP 201 with the created external system
     */
    @PostMapping
    public ResponseEntity<ExternalSystemOutputDTO> create(@Valid @RequestBody ExternalSystemInputDTO inputDTO) {
        return new ResponseEntity<>(externalSystemService.create(inputDTO), HttpStatus.CREATED);
    }

    /**
     * Updates an existing external system with the given input data.
     *
     * @param id       the identifier of the external system to update
     * @param inputDTO the new external system data
     * @return HTTP 200 with the updated external system
     */
    @PutMapping("/{id}")
    public ResponseEntity<ExternalSystemOutputDTO> update(@PathVariable Long id, @Valid @RequestBody ExternalSystemInputDTO inputDTO) {
        return ResponseEntity.ok(externalSystemService.update(id, inputDTO));
    }

    /**
     * Logically deletes an external system by its identifier.
     *
     * @param id the identifier of the external system to delete
     * @return HTTP 204 with no content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        externalSystemService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reactivates a previously deleted external system.
     *
     * @param id the identifier of the external system to reactivate
     * @return HTTP 200 with the reactivated external system
     */
    @PutMapping("reactivate/{id}")
    public ResponseEntity<ExternalSystemOutputDTO> reactivate(@PathVariable Long id) {
        return ResponseEntity.ok(externalSystemService.reactivate(id));
    }
}

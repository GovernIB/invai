package es.caib.invai.back.interna.maintenance.security.ensRequirement;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.maintenance.security.ensRequirement.DTO.EnsRequirementInputDTO;
import es.caib.invai.back.interna.maintenance.security.ensRequirement.DTO.EnsRequirementOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.security.ensRequirement.EnsRequirementCriteria;
import es.caib.invai.back.service.facade.maintenance.security.ensRequirement.EnsRequirementService;
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
 * Internal REST controller that manages lifecycle endpoints and routing rules for EnsRequirement assets.
 *
 * @since 1.0.4
 */
@Tag(name = "Requeriments ENS", description = "Manteniment del catàleg de requeriments ENS.")
@RestController
@Slf4j
@RequestMapping("ens-requirement")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class EnsRequirementController {

    /** Service facade handling EnsRequirement business use cases. */
    private final EnsRequirementService ensRequirementService;

    /**
     * Creates the controller with its required service dependency.
     *
     * @param ensRequirementService the service facade to delegate business operations to
     */
    @Autowired
    public EnsRequirementController(EnsRequirementService ensRequirementService) {
        this.ensRequirementService = ensRequirementService;
    }

    /**
     * Retrieves an ENS requirement entry by its identifier.
     *
     * @param id the ENS requirement entry identifier
     * @return the matching ENS requirement entry
     */
    @GetMapping("/{id}")
    public ResponseEntity<EnsRequirementOutputDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ensRequirementService.getById(id));
    }

    /**
     * Retrieves a paginated, filtered list of ENS requirement entries.
     *
     * @param filter search criteria used to narrow down results
     * @param pageable pagination and sorting instructions
     * @return a page of matching ENS requirement entries
     */
    @GetMapping
    public ResponseEntity<Page<EnsRequirementOutputDTO>> getAll(
            @ModelAttribute EnsRequirementCriteria filter,
            @PageableDefault(sort = "id") Pageable pageable) {
        return ResponseEntity.ok(ensRequirementService.getAll(filter, pageable));
    }

    /**
     * Creates a new ENS requirement entry.
     *
     * @param inputDTO the data for the new ENS requirement entry
     * @return the created ENS requirement entry, with HTTP 201 status
     */
    @PostMapping
    public ResponseEntity<EnsRequirementOutputDTO> create(@Valid @RequestBody EnsRequirementInputDTO inputDTO) {
        return new ResponseEntity<>(ensRequirementService.create(inputDTO), HttpStatus.CREATED);
    }

    /**
     * Updates an existing ENS requirement entry.
     *
     * @param id the identifier of the ENS requirement entry to update
     * @param inputDTO the new data to apply
     * @return the updated ENS requirement entry
     */
    @PutMapping("/{id}")
    public ResponseEntity<EnsRequirementOutputDTO> update(@PathVariable Long id, @Valid @RequestBody EnsRequirementInputDTO inputDTO) {
        return ResponseEntity.ok(ensRequirementService.update(id, inputDTO));
    }

    /**
     * Logically deletes an ENS requirement entry by its identifier.
     *
     * @param id the identifier of the ENS requirement entry to delete
     * @return an empty response with HTTP 204 status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ensRequirementService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reactivates a previously deleted ENS requirement entry.
     *
     * @param id the identifier of the ENS requirement entry to reactivate
     * @return the reactivated ENS requirement entry
     */
    @PutMapping("reactivate/{id}")
    public ResponseEntity<EnsRequirementOutputDTO> reactivate(@PathVariable Long id) {
        return ResponseEntity.ok(ensRequirementService.reactivate(id));
    }
}

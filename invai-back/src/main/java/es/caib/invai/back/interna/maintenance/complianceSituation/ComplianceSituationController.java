package es.caib.invai.back.interna.maintenance.complianceSituation;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.maintenance.complianceSituation.DTO.ComplianceSituationInputDTO;
import es.caib.invai.back.interna.maintenance.complianceSituation.DTO.ComplianceSituationOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.complianceSituation.ComplianceSituationCriteria;
import es.caib.invai.back.service.facade.maintenance.complianceSituation.ComplianceSituationService;
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
 * Internal REST controller that manages lifecycle endpoints and routing rules for ComplianceSituation assets.
 *
 * @since 1.0.4
 */
@Tag(name = "Situació de cumpliment", description = "Manteniment del catàleg de situacions de cumpliment d'accessibilitat.")
@RestController
@Slf4j
@RequestMapping("compliance-situation")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class ComplianceSituationController {

    /** Service facade handling ComplianceSituation business use cases. */
    private final ComplianceSituationService complianceSituationService;

    /**
     * Creates the controller with its required service dependency.
     *
     * @param complianceSituationService the service facade to delegate business operations to
     */
    @Autowired
    public ComplianceSituationController(ComplianceSituationService complianceSituationService) {
        this.complianceSituationService = complianceSituationService;
    }

    /**
     * Retrieves a compliance situation entry by its identifier.
     *
     * @param id the compliance situation entry identifier
     * @return the matching compliance situation entry
     */
    @GetMapping("/{id}")
    public ResponseEntity<ComplianceSituationOutputDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(complianceSituationService.getById(id));
    }

    /**
     * Retrieves a paginated, filtered list of compliance situation entries.
     *
     * @param filter search criteria used to narrow down results
     * @param pageable pagination and sorting instructions
     * @return a page of matching compliance situation entries
     */
    @GetMapping
    public ResponseEntity<Page<ComplianceSituationOutputDTO>> getAll(
            @ModelAttribute ComplianceSituationCriteria filter,
            @PageableDefault(sort = "id") Pageable pageable) {
        return ResponseEntity.ok(complianceSituationService.getAll(filter, pageable));
    }

    /**
     * Creates a new compliance situation entry.
     *
     * @param inputDTO the data for the new compliance situation entry
     * @return the created compliance situation entry, with HTTP 201 status
     */
    @PostMapping
    public ResponseEntity<ComplianceSituationOutputDTO> create(@Valid @RequestBody ComplianceSituationInputDTO inputDTO) {
        return new ResponseEntity<>(complianceSituationService.create(inputDTO), HttpStatus.CREATED);
    }

    /**
     * Updates an existing compliance situation entry.
     *
     * @param id the identifier of the compliance situation entry to update
     * @param inputDTO the new data to apply
     * @return the updated compliance situation entry
     */
    @PutMapping("/{id}")
    public ResponseEntity<ComplianceSituationOutputDTO> update(@PathVariable Long id, @Valid @RequestBody ComplianceSituationInputDTO inputDTO) {
        return ResponseEntity.ok(complianceSituationService.update(id, inputDTO));
    }

    /**
     * Logically deletes a compliance situation entry by its identifier.
     *
     * @param id the identifier of the compliance situation entry to delete
     * @return an empty response with HTTP 204 status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        complianceSituationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reactivates a previously deleted compliance situation entry.
     *
     * @param id the identifier of the compliance situation entry to reactivate
     * @return the reactivated compliance situation entry
     */
    @PutMapping("reactivate/{id}")
    public ResponseEntity<ComplianceSituationOutputDTO> reactivate(@PathVariable Long id) {
        return ResponseEntity.ok(complianceSituationService.reactivate(id));
    }
}

package es.caib.invai.back.interna.maintenance.development.technology;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.maintenance.development.technology.DTO.TechnologyInputDTO;
import es.caib.invai.back.interna.maintenance.development.technology.DTO.TechnologyOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.development.technology.TechnologyCriteria;
import es.caib.invai.back.service.facade.maintenance.development.technology.TechnologyService;
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
 * Internal REST controller that manages lifecycle endpoints and routing rules for catalog Technology assets.
 *
 * @since 1.0.2
 */
@Tag(name = "Tecnologies", description = "Manteniment del catàleg de tecnologies.")
@RestController
@Slf4j
@RequestMapping("technology")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class TechnologyController {

    /** Service facade handling business use cases for catalog Technologies. */
    private final TechnologyService technologyService;

    /**
     * Creates the controller with the required service facade dependency.
     *
     * @param technologyService the service facade to delegate to
     */
    @Autowired
    public TechnologyController(TechnologyService technologyService) {
        this.technologyService = technologyService;
    }

    /**
     * Retrieves a single technology by its identifier.
     *
     * @param id the technology identifier
     * @return HTTP 200 with the matching technology
     */
    @GetMapping("/{id}")
    public ResponseEntity<TechnologyOutputDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(technologyService.getById(id));
    }

    /**
     * Retrieves a paginated list of technologies matching the given filter criteria.
     *
     * @param filter   the search criteria to apply
     * @param pageable the pagination and sorting configuration
     * @return HTTP 200 with a page of matching technologies
     */
    @GetMapping
    public ResponseEntity<Page<TechnologyOutputDTO>> getAll(
            @ModelAttribute TechnologyCriteria filter,
            @PageableDefault(sort = "id") Pageable pageable) {
        return ResponseEntity.ok(technologyService.getAll(filter, pageable));
    }

    /**
     * Creates a new technology record.
     *
     * @param inputDTO the data for the technology to create
     * @return HTTP 201 with the created technology
     */
    @PostMapping
    public ResponseEntity<TechnologyOutputDTO> create(@Valid @RequestBody TechnologyInputDTO inputDTO) {
        return new ResponseEntity<>(technologyService.create(inputDTO), HttpStatus.CREATED);
    }

    /**
     * Updates an existing technology record.
     *
     * @param id       the identifier of the technology to update
     * @param inputDTO the new data for the technology
     * @return HTTP 200 with the updated technology
     */
    @PutMapping("/{id}")
    public ResponseEntity<TechnologyOutputDTO> update(@PathVariable Long id, @Valid @RequestBody TechnologyInputDTO inputDTO) {
        return ResponseEntity.ok(technologyService.update(id, inputDTO));
    }

    /**
     * Logically deletes a technology.
     *
     * @param id the identifier of the technology to delete
     * @return HTTP 204 with no content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        technologyService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reactivates a previously logically-deleted technology.
     *
     * @param id the identifier of the technology to reactivate
     * @return HTTP 200 with the reactivated technology
     */
    @PutMapping("reactivate/{id}")
    public ResponseEntity<TechnologyOutputDTO> reactivate(@PathVariable Long id) {
        return ResponseEntity.ok(technologyService.reactivate(id));
    }
}

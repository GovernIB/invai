package es.caib.invai.back.interna.maintenance.responsible.company;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.maintenance.responsible.company.DTO.CompanyInputDTO;
import es.caib.invai.back.interna.maintenance.responsible.company.DTO.CompanyOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.responsible.company.CompanyCriteria;
import es.caib.invai.back.service.facade.maintenance.responsible.company.CompanyService;
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
 * Internal REST controller that manages lifecycle endpoints and routing rules for Company assets.
 *
 * @since 1.0.3
 */
@Tag(name = "Empreses", description = "Manteniment del catàleg d'empreses.")
@RestController
@Slf4j
@RequestMapping("company")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class CompanyController {

    /** Facade service handling the business use cases for Company resources. */
    private final CompanyService companyService;

    /**
     * Creates a controller instance with the given service dependency injected.
     *
     * @param companyService the facade service used to perform Company operations
     */
    @Autowired
    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    /**
     * Retrieves a company by its identifier.
     *
     * @param id the company identifier
     * @return HTTP 200 with the matching company
     */
    @GetMapping("/{id}")
    public ResponseEntity<CompanyOutputDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.getById(id));
    }

    /**
     * Retrieves a paginated list of companies matching the given filter criteria.
     *
     * @param filter   search and status filter criteria
     * @param pageable pagination and sorting information
     * @return HTTP 200 with the matching page of companies
     */
    @GetMapping
    public ResponseEntity<Page<CompanyOutputDTO>> getAll(
            @ModelAttribute CompanyCriteria filter,
            @PageableDefault(sort = "id") Pageable pageable) {
        return ResponseEntity.ok(companyService.getAll(filter, pageable));
    }

    /**
     * Creates a new company record.
     *
     * @param inputDTO the company data to create
     * @return HTTP 201 with the created company
     */
    @PostMapping
    public ResponseEntity<CompanyOutputDTO> create(@Valid @RequestBody CompanyInputDTO inputDTO) {
        return new ResponseEntity<>(companyService.create(inputDTO), HttpStatus.CREATED);
    }

    /**
     * Updates an existing company with the given input data.
     *
     * @param id       the identifier of the company to update
     * @param inputDTO the new company data
     * @return HTTP 200 with the updated company
     */
    @PutMapping("/{id}")
    public ResponseEntity<CompanyOutputDTO> update(@PathVariable Long id, @Valid @RequestBody CompanyInputDTO inputDTO) {
        return ResponseEntity.ok(companyService.update(id, inputDTO));
    }

    /**
     * Logically deletes a company by its identifier.
     *
     * @param id the identifier of the company to delete
     * @return HTTP 204 with no content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        companyService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reactivates a previously deleted company.
     *
     * @param id the identifier of the company to reactivate
     * @return HTTP 200 with the reactivated company
     */
    @PutMapping("reactivate/{id}")
    public ResponseEntity<CompanyOutputDTO> reactivate(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.reactivate(id));
    }
}

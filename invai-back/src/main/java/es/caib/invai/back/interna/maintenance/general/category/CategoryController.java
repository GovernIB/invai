package es.caib.invai.back.interna.maintenance.general.category;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.maintenance.general.category.DTO.CategoryInputDTO;
import es.caib.invai.back.interna.maintenance.general.category.DTO.CategoryOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.general.category.CategoryCriteria;
import es.caib.invai.back.service.facade.maintenance.general.category.CategoryService;
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
 * Internal REST controller that manages lifecycle endpoints and routing rules
 * for Category taxonomy assets.
 * <p>
 * Secured globally at the type level, allowing access exclusively to authenticated
 * corporate operators assigned the {@code ROLE_usuari-tipus-E} role.
 * </p>
 *
 * @since 1.0.1
 */
@Tag(name = "Categories", description = "Manteniment del catàleg de categories.")
@RestController
@Slf4j
@RequestMapping("category")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class CategoryController {

    /** Application service handling business logic and orchestration for Category resources. */
    private final CategoryService categoryService;

    /**
     * Constructs a CategoryController instance with its required backing business layer dependency.
     *
     * @param categoryService the transaction-bounded orchestration service engine interface
     */
    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Resolves matching Category settings data profiles using a given unique entity reference key.
     *
     * @param id persistent index sequence row identifier
     * @return a {@link ResponseEntity} wrapping the matched {@link CategoryOutputDTO} structure with an HTTP 200 OK status
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryOutputDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    /**
     * Retrieves a paginated and sorted block matrix across all non-deleted Category data records.
     *
     * @param filter   dynamic search criteria used to build the query specification
     * @param pageable pagination and sorting query configuration parameters
     * @return a {@link ResponseEntity} containing a structured {@link Page} of mapped {@link CategoryOutputDTO} entries with an HTTP 200 OK status
     */
    @GetMapping
    public ResponseEntity<Page<CategoryOutputDTO>> getAll(
            @ModelAttribute CategoryCriteria filter,
            @PageableDefault(sort = "id") Pageable pageable) {
        return ResponseEntity.ok(categoryService.getAll(filter, pageable));
    }

    /**
     * Commits a new Category profile asset representation to the system data stores.
     * Evaluates incoming request constraints context rules before opening application logic scopes.
     *
     * @param inputDTO validated data payload capturing fields for the new category descriptor
     * @return a {@link ResponseEntity} containing the initialized tracking model properties as a {@link CategoryOutputDTO} with an HTTP 201 Created status
     */
    @PostMapping
    public ResponseEntity<CategoryOutputDTO> create(@Valid @RequestBody CategoryInputDTO inputDTO) {
        return new ResponseEntity<>(categoryService.create(inputDTO), HttpStatus.CREATED);
    }

    /**
     * Modifies configured properties on an active record index matching target primary identifiers.
     *
     * @param id       persistent identity identifier row index targeted for updates
     * @param inputDTO property data schema mapping updated dataset fields
     * @return a {@link ResponseEntity} wrapping current modified configuration variables as a {@link CategoryOutputDTO} with an HTTP 200 OK status
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoryOutputDTO> update(@PathVariable Long id, @Valid @RequestBody CategoryInputDTO inputDTO) {
        return ResponseEntity.ok(categoryService.update(id, inputDTO));
    }

    /**
     * Triggers localized soft deletion workflows against a target unique database sequence index.
     *
     * @param id entity target row metadata database reference identifier to change status flags
     * @return an empty {@link ResponseEntity} displaying success parameters with an HTTP 204 No Content status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reactivates a Category and restores its configuration profiles mapping
     * properties corresponding to target database references.
     *
     * @param id the targeted structural identifier element index of the category to reactivate
     * @return a {@link ResponseEntity} containing the current modified configuration state
     *         properties details (as an {@link CategoryOutputDTO}) with an HTTP 200 OK status
     */
    @PutMapping("reactivate/{id}")
    public ResponseEntity<CategoryOutputDTO> reactivate(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.reactivate(id));
    }
}

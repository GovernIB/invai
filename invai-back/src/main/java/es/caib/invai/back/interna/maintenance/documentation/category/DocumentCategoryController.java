package es.caib.invai.back.interna.maintenance.documentation.category;

import io.swagger.v3.oas.annotations.tags.Tag;
import es.caib.invai.back.interna.maintenance.documentation.category.DTO.DocumentCategoryInputDTO;
import es.caib.invai.back.interna.maintenance.documentation.category.DTO.DocumentCategoryOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.documentation.documentCategory.DocumentCategoryCriteria;
import es.caib.invai.back.service.facade.maintenance.documentation.documentCategory.DocumentCategoryService;
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
 * Internal REST controller that manages lifecycle endpoints and routing rules for documentation
 * categories and subcategories, which share the same endpoints.
 *
 * @since 1.0.6
 */
@Tag(name = "Categories documentals", description = "Manteniment de les categories i subcategories documentals.")
@RestController
@Slf4j
@RequestMapping("documentation/category")
@PreAuthorize("hasRole('ROLE_INV_SUPER')")
public class DocumentCategoryController {

    /** Facade service handling the business use cases for documentation categories. */
    private final DocumentCategoryService documentCategoryService;

    /**
     * Creates a controller instance with the given service dependency injected.
     *
     * @param documentCategoryService the facade service used to perform documentation category operations
     */
    @Autowired
    public DocumentCategoryController(DocumentCategoryService documentCategoryService) {
        this.documentCategoryService = documentCategoryService;
    }

    /**
     * Retrieves a category or subcategory by its identifier.
     *
     * @param id the identifier
     * @return HTTP 200 with the matching record (a category with its subcategories)
     */
    @GetMapping("/{id}")
    public ResponseEntity<DocumentCategoryOutputDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(documentCategoryService.getById(id));
    }

    /**
     * Retrieves a paginated list of the categories matching the given filter criteria, each one
     * with all its subcategories.
     *
     * @param filter   search and status filter criteria
     * @param pageable pagination and sorting information
     * @return HTTP 200 with the matching page of categories
     */
    @GetMapping
    public ResponseEntity<Page<DocumentCategoryOutputDTO>> getAll(
            @ModelAttribute DocumentCategoryCriteria filter,
            @PageableDefault(sort = "name") Pageable pageable) {
        return ResponseEntity.ok(documentCategoryService.getAll(filter, pageable));
    }

    /**
     * Creates a new category, or a subcategory when {@code parentId} is given.
     *
     * @param inputDTO the data to create
     * @return HTTP 201 with the created record
     */
    @PostMapping
    public ResponseEntity<DocumentCategoryOutputDTO> create(@Valid @RequestBody DocumentCategoryInputDTO inputDTO) {
        return new ResponseEntity<>(documentCategoryService.create(inputDTO), HttpStatus.CREATED);
    }

    /**
     * Updates an existing category or subcategory with the given input data.
     *
     * @param id       the identifier of the record to update
     * @param inputDTO the new data
     * @return HTTP 200 with the updated record
     */
    @PutMapping("/{id}")
    public ResponseEntity<DocumentCategoryOutputDTO> update(@PathVariable Long id, @Valid @RequestBody DocumentCategoryInputDTO inputDTO) {
        return ResponseEntity.ok(documentCategoryService.update(id, inputDTO));
    }

    /**
     * Logically deletes a category or subcategory by its identifier.
     *
     * @param id the identifier of the record to delete
     * @return HTTP 204 with no content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        documentCategoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reactivates a previously deleted category or subcategory.
     *
     * @param id the identifier of the record to reactivate
     * @return HTTP 200 with the reactivated record
     */
    @PutMapping("reactivate/{id}")
    public ResponseEntity<DocumentCategoryOutputDTO> reactivate(@PathVariable Long id) {
        return ResponseEntity.ok(documentCategoryService.reactivate(id));
    }
}

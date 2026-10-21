package es.caib.invai.back.service.facade.maintenance.documentation.documentCategory;

import es.caib.invai.back.interna.maintenance.documentation.category.DTO.DocumentCategoryInputDTO;
import es.caib.invai.back.interna.maintenance.documentation.category.DTO.DocumentCategoryOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.documentation.documentCategory.DocumentCategoryCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Facade boundary interface declaring business use cases and orchestration rules
 * targeting documentation categories and subcategories. Both are managed by the same use cases:
 * a record without a parent is a category, one with a parent is a subcategory of it.
 *
 * @since 1.0.6
 */
public interface DocumentCategoryService {

    /**
     * Retrieves a category or subcategory by its identifier. A category comes with its subcategories.
     *
     * @param id the identifier
     * @return the matching record as a response DTO
     */
    DocumentCategoryOutputDTO getById(Long id);

    /**
     * Retrieves a paginated list of the categories matching the given filter criteria, each one
     * with all its subcategories.
     *
     * @param filter   search and status filter criteria
     * @param pageable pagination and sorting information
     * @return a page of matching categories as response DTOs
     */
    Page<DocumentCategoryOutputDTO> getAll(DocumentCategoryCriteria filter, Pageable pageable);

    /**
     * Creates a new category (no {@code parentId}) or subcategory (the {@code parentId} of an
     * active category).
     *
     * @param inputDTO the data to create
     * @return the created record as a response DTO
     */
    DocumentCategoryOutputDTO create(DocumentCategoryInputDTO inputDTO);

    /**
     * Updates an existing, active category or subcategory. A category stays a category and a
     * subcategory stays a subcategory, though it can move to another active category.
     *
     * @param id       the identifier of the record to update
     * @param inputDTO the new data
     * @return the updated record as a response DTO
     */
    DocumentCategoryOutputDTO update(Long id, DocumentCategoryInputDTO inputDTO);

    /**
     * Logically deletes a category or subcategory. A category with active subcategories can't be
     * deleted.
     *
     * @param id the identifier of the record to delete
     */
    void delete(Long id);

    /**
     * Reactivates a previously deleted category or subcategory. A subcategory can only be
     * reactivated while its category is active.
     *
     * @param id the identifier of the record to reactivate
     * @return the reactivated record as a response DTO
     */
    DocumentCategoryOutputDTO reactivate(Long id);
}

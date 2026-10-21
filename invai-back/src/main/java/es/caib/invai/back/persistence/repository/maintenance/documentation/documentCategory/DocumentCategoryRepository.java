package es.caib.invai.back.persistence.repository.maintenance.documentation.documentCategory;

import es.caib.invai.back.service.model.maintenance.documentation.documentCategory.DocumentCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Core business domain outbound Port boundary interface declaring relational persistence mechanisms
 * for the documentation category and subcategory layer.
 *
 * @since 1.0.6
 */
public interface DocumentCategoryRepository {

    /**
     * Finds a category or subcategory by its identifier. A category comes with all its subcategories.
     *
     * @param id the identifier
     * @return the matching record, or {@code null} if none is found
     */
    DocumentCategory findById(Long id);

    /**
     * Finds the categories matching the given filter criteria, paginated. Each one comes with all
     * its subcategories.
     *
     * @param filter   search and status filter criteria
     * @param pageable pagination and sorting information
     * @return a page of matching categories
     */
    Page<DocumentCategory> findAll(DocumentCategoryCriteria filter, Pageable pageable);

    /**
     * Persists a new category or subcategory.
     *
     * @param documentCategory the record to create
     * @return the persisted record
     */
    DocumentCategory create(DocumentCategory documentCategory);

    /**
     * Persists changes to an existing category or subcategory.
     *
     * @param documentCategory the data to persist
     * @param id               the identifier of the record to update
     * @return the updated record
     */
    DocumentCategory update(DocumentCategory documentCategory, Long id);

    /**
     * Logically deletes the given category or subcategory.
     *
     * @param documentCategory the record to delete
     */
    void delete(DocumentCategory documentCategory);

    /**
     * Checks whether another active record with the given name exists among the categories (when
     * {@code parentId} is {@code null}) or among the subcategories of the given category.
     *
     * @param name      the name to check
     * @param parentId  identifier of the category the name is scoped to, {@code null} for the categories themselves
     * @param excludeId identifier of the record to leave out of the check, {@code null} to leave none out
     * @return {@code true} if such an active record exists
     */
    boolean existsActiveWithName(String name, Long parentId, Long excludeId);

    /**
     * Same as {@link #existsActiveWithName} but for the Spanish name.
     *
     * @param nameEs    the Spanish name to check
     * @param parentId  identifier of the category the name is scoped to, {@code null} for the categories themselves
     * @param excludeId identifier of the record to leave out of the check, {@code null} to leave none out
     * @return {@code true} if such an active record exists
     */
    boolean existsActiveWithNameEs(String nameEs, Long parentId, Long excludeId);

    /**
     * Checks whether the given category has at least one active (non-deleted) subcategory.
     *
     * @param categoryId identifier of the category
     * @return {@code true} if it has one
     */
    boolean hasActiveSubcategories(Long categoryId);
}

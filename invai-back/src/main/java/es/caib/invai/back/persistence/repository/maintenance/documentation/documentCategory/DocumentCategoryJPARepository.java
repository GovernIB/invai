package es.caib.invai.back.persistence.repository.maintenance.documentation.documentCategory;

import es.caib.invai.back.persistence.model.maintenance.documentation.documentCategory.DocumentCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * Native Spring Data JPA repository layer interface providing CRUD operations, custom query methods,
 * and constraint validation checks targeting live {@link DocumentCategoryEntity} records.
 *
 * @since 1.0.6
 */
@Repository
public interface DocumentCategoryJPARepository extends JpaRepository<DocumentCategoryEntity, Long>, JpaSpecificationExecutor<DocumentCategoryEntity> {

    /**
     * Finds every subcategory (active or deleted) of the given categories, sorted by name.
     *
     * @param parentIds identifiers of the categories
     * @return the subcategories of those categories
     */
    List<DocumentCategoryEntity> findAllByParentIdInOrderByNameAsc(Collection<Long> parentIds);

    /**
     * Checks whether the given category has at least one active (non-deleted) subcategory.
     *
     * @param parentId identifier of the category
     * @return {@code true} if one exists
     */
    boolean existsByParentIdAndDeletedAtIsNull(Long parentId);

    /**
     * Checks whether an active category exists with the given name.
     *
     * @param name the name to check
     * @return {@code true} if a matching active category exists
     */
    boolean existsByNameAndParentIsNullAndDeletedAtIsNull(String name);

    /**
     * Checks whether an active category exists with the given name and a different identifier.
     *
     * @param name the name to check
     * @param id   the identifier to exclude from the check
     * @return {@code true} if a matching active category exists with a different ID
     */
    boolean existsByNameAndParentIsNullAndIdNotAndDeletedAtIsNull(String name, Long id);

    /**
     * Checks whether an active subcategory of the given category exists with the given name.
     *
     * @param name     the name to check
     * @param parentId identifier of the category
     * @return {@code true} if a matching active subcategory exists
     */
    boolean existsByNameAndParentIdAndDeletedAtIsNull(String name, Long parentId);

    /**
     * Checks whether an active subcategory of the given category exists with the given name and a
     * different identifier.
     *
     * @param name     the name to check
     * @param parentId identifier of the category
     * @param id       the identifier to exclude from the check
     * @return {@code true} if a matching active subcategory exists with a different ID
     */
    boolean existsByNameAndParentIdAndIdNotAndDeletedAtIsNull(String name, Long parentId, Long id);

    /**
     * Checks whether an active category exists with the given Spanish name.
     *
     * @param nameEs the Spanish name to check
     * @return {@code true} if a matching active category exists
     */
    boolean existsByNameEsAndParentIsNullAndDeletedAtIsNull(String nameEs);

    /**
     * Checks whether an active category exists with the given Spanish name and a different identifier.
     *
     * @param nameEs the Spanish name to check
     * @param id     the identifier to exclude from the check
     * @return {@code true} if a matching active category exists with a different ID
     */
    boolean existsByNameEsAndParentIsNullAndIdNotAndDeletedAtIsNull(String nameEs, Long id);

    /**
     * Checks whether an active subcategory of the given category exists with the given Spanish name.
     *
     * @param nameEs   the Spanish name to check
     * @param parentId identifier of the category
     * @return {@code true} if a matching active subcategory exists
     */
    boolean existsByNameEsAndParentIdAndDeletedAtIsNull(String nameEs, Long parentId);

    /**
     * Checks whether an active subcategory of the given category exists with the given Spanish
     * name and a different identifier.
     *
     * @param nameEs   the Spanish name to check
     * @param parentId identifier of the category
     * @param id       the identifier to exclude from the check
     * @return {@code true} if a matching active subcategory exists with a different ID
     */
    boolean existsByNameEsAndParentIdAndIdNotAndDeletedAtIsNull(String nameEs, Long parentId, Long id);
}

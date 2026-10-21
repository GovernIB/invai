package es.caib.invai.back.persistence.repository.maintenance.documentation.documentCategory;

import es.caib.invai.back.persistence.model.maintenance.documentation.documentCategory.DocumentCategoryEntity;
import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Utility factory class responsible for building programmatic JPA {@link Specification}
 * structures using the Criteria API targeting documentation categories.
 *
 * @since 1.0.6
 */
@NoArgsConstructor
public final class DocumentCategorySpecification {

    /**
     * Builds a JPA {@link Specification} selecting categories (records without a parent) that
     * fulfil the given criteria.
     *
     * @param criteria the filter criteria to translate into predicates, may be {@code null}
     * @return a specification matching the categories fulfilling the given criteria
     */
    public static Specification<DocumentCategoryEntity> filterByCriteria(DocumentCategoryCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("parent")));
            if (Objects.isNull(criteria)) {
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            Long statusId = criteria.getStatusId();
            if (Objects.nonNull(statusId)) {
                boolean isActive = StatusEnum.ACTIVE.getId().equals(statusId);
                predicates.add(isActive
                        ? cb.isNull(root.get("deletedAt"))
                        : cb.isNotNull(root.get("deletedAt"))
                );
            }

            if (Objects.nonNull(criteria.getName()) && !criteria.getName().trim().isEmpty()) {
                predicates.add(matchesCategoryOrSubcategory(root, query, cb, criteria.getName()));
            }

            if (Objects.nonNull(criteria.getSearch()) && !criteria.getSearch().trim().isEmpty()) {
                predicates.add(matchesCategoryOrSubcategory(root, query, cb, criteria.getSearch()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Builds the predicate that holds for a category when the given text is contained, ignoring
     * case, in its Catalan or Spanish name or in the Catalan or Spanish name of any of its
     * subcategories.
     *
     * @param root  the category being filtered
     * @param query the query the predicate belongs to, used to build the subcategories subquery
     * @param cb    the criteria builder
     * @param text  the text to look for
     * @return the predicate matching the category or any of its subcategories
     */
    private static Predicate matchesCategoryOrSubcategory(Root<DocumentCategoryEntity> root, CriteriaQuery<?> query,
                                                          CriteriaBuilder cb, String text) {
        String pattern = "%" + text.toLowerCase() + "%";
        Subquery<Long> subcategoryMatches = query.subquery(Long.class);
        Root<DocumentCategoryEntity> child = subcategoryMatches.from(DocumentCategoryEntity.class);
        subcategoryMatches.select(child.get("id")).where(
                cb.equal(child.get("parent"), root),
                cb.or(
                        cb.like(cb.lower(child.get("name")), pattern),
                        cb.like(cb.lower(child.get("nameEs")), pattern)
                )
        );
        return cb.or(
                cb.like(cb.lower(root.get("name")), pattern),
                cb.like(cb.lower(root.get("nameEs")), pattern),
                cb.exists(subcategoryMatches)
        );
    }
}

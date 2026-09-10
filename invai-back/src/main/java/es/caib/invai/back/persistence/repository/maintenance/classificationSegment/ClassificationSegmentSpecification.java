package es.caib.invai.back.persistence.repository.maintenance.classificationSegment;

import es.caib.invai.back.persistence.model.maintenance.classificationSegment.ClassificationSegmentEntity;
import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility factory class responsible for building programmatic JPA {@link Specification}
 * structures using the Criteria API targeting ClassificationSegment entities.
 *
 * @since 1.0.4
 */
@NoArgsConstructor
public final class ClassificationSegmentSpecification {

    /**
     * Builds a JPA {@link Specification} that filters {@link ClassificationSegmentEntity} records
     * according to the given criteria (status, name, Spanish name and free-text search).
     *
     * @param criteria the search criteria, may be {@code null} for no filtering
     * @return a specification combining all applicable predicates with AND
     */
    public static Specification<ClassificationSegmentEntity> filterByCriteria(ClassificationSegmentCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria == null) {
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            Long statusId = criteria.getStatusId();
            if (statusId != null) {
                boolean isActive = StatusEnum.ACTIVE.getId().equals(statusId);
                predicates.add(isActive
                        ? cb.isNull(root.get("deletedAt"))
                        : cb.isNotNull(root.get("deletedAt"))
                );
            }

            if (criteria.getName() != null && !criteria.getName().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + criteria.getName().toLowerCase() + "%"));
            }

            if (criteria.getNameEs() != null && !criteria.getNameEs().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("nameEs")), "%" + criteria.getNameEs().toLowerCase() + "%"));
            }

            if (criteria.getSearch() != null && !criteria.getSearch().trim().isEmpty()) {
                String pattern = "%" + criteria.getSearch().toLowerCase() + "%";

                Predicate searchName = cb.like(cb.lower(root.get("name")), pattern);
                Predicate searchNameEs = cb.like(cb.lower(root.get("nameEs")), pattern);

                predicates.add(cb.or(searchName, searchNameEs));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

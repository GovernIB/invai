package es.caib.invai.api.persistence.repository.environment;

import es.caib.invai.api.persistence.model.EnvironmentEntity;
import es.caib.invai.api.service.model.StatusEnum;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility factory class responsible for building programmatic JPA {@link Specification}
 * structures using the Criteria API.
 * <p>
 * Evaluates fields, checks for soft deletion states, and handles global full-text query
 * searches across primary and localized properties (e.g., Catalan name vs Spanish {@code nameEs}).
 * </p>
 *
 * @since 1.0.1
 */
@NoArgsConstructor
public final class EnvironmentSpecification {

    /**
     * Compiles variable search boundaries from an input criteria record, constructing
     * query expressions with appropriate mapping predicates.
     *
     * @param criteria DTO containing search filters, flags, and full-text keyword strings
     * @return a JPA {@link Specification} object targeting EnvironmentEntity query environments
     */
    public static Specification<EnvironmentEntity> filterByCriteria(EnvironmentCriteria criteria) {
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

            if (criteria.getCode() != null && !criteria.getCode().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("code")), "%" + criteria.getCode().toLowerCase() + "%"));
            }

            if (criteria.getName() != null && !criteria.getName().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + criteria.getName().toLowerCase() + "%"));
            }

            if (criteria.getNameEs() != null && !criteria.getNameEs().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("nameEs")), "%" + criteria.getNameEs().toLowerCase() + "%"));
            }

            if (criteria.getSearch() != null && !criteria.getSearch().trim().isEmpty()) {
                String pattern = "%" + criteria.getSearch().toLowerCase() + "%";

                Predicate searchCode = cb.like(cb.lower(root.get("code")), pattern);
                Predicate searchName = cb.like(cb.lower(root.get("name")), pattern);
                Predicate searchNameEs = cb.like(cb.lower(root.get("nameEs")), pattern);

                predicates.add(cb.or(searchCode, searchName, searchNameEs));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
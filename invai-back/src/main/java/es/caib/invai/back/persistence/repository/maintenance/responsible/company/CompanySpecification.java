package es.caib.invai.back.persistence.repository.maintenance.responsible.company;

import es.caib.invai.back.persistence.model.maintenance.responsible.company.CompanyEntity;
import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility factory class responsible for building programmatic JPA {@link Specification}
 * structures using the Criteria API targeting Company entities.
 *
 * @since 1.0.3
 */
@NoArgsConstructor
public final class CompanySpecification {

    /**
     * Builds a JPA {@link Specification} combining status and name/search predicates
     * derived from the given criteria.
     *
     * @param criteria the filter criteria to translate into predicates, may be {@code null}
     * @return a specification matching companies fulfilling the given criteria
     */
    public static Specification<CompanyEntity> filterByCriteria(CompanyCriteria criteria) {
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

            if (criteria.getSearch() != null && !criteria.getSearch().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + criteria.getSearch().toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

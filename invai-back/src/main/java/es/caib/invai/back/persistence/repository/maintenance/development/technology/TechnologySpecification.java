package es.caib.invai.back.persistence.repository.maintenance.development.technology;

import es.caib.invai.back.persistence.model.maintenance.development.technology.TechnologyEntity;
import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility factory class responsible for building programmatic JPA {@link Specification}
 * structures using the Criteria API targeting Technology entities.
 *
 * @since 1.0.2
 */
@NoArgsConstructor
public final class TechnologySpecification {

    /**
     * Builds a JPA {@link Specification} combining predicates for status, name, layer, and free-text
     * search filters found in the given criteria.
     *
     * @param criteria the search filters to translate into predicates, may be {@code null}
     * @return a specification matching the given criteria, or one with no restrictions if criteria is {@code null}
     */
    public static Specification<TechnologyEntity> filterByCriteria(TechnologyCriteria criteria) {
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

            if (criteria.getLayerId() != null) {
                predicates.add(cb.equal(root.get("layer").get("id"), criteria.getLayerId()));
            }

            if (criteria.getSearch() != null && !criteria.getSearch().trim().isEmpty()) {
                String pattern = "%" + criteria.getSearch().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), pattern));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

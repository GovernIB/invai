package es.caib.invai.back.persistence.repository.maintenance.integration.externalSystem;

import es.caib.invai.back.persistence.model.maintenance.integration.externalSystem.ExternalSystemEntity;
import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Utility factory class responsible for building programmatic JPA {@link Specification}
 * structures using the Criteria API targeting External System entities.
 *
 * @since 1.0.5
 */
@NoArgsConstructor
public final class ExternalSystemSpecification {

    /**
     * Builds a JPA {@link Specification} combining status, name/search and company predicates
     * derived from the given criteria.
     *
     * @param criteria the filter criteria to translate into predicates, may be {@code null}
     * @return a specification matching external systems fulfilling the given criteria
     */
    public static Specification<ExternalSystemEntity> filterByCriteria(ExternalSystemCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

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

            if (Objects.nonNull(criteria.getCompanyId())) {
                predicates.add(cb.equal(root.get("company").get("id"), criteria.getCompanyId()));
            }

            if (Objects.nonNull(criteria.getName()) && !criteria.getName().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + criteria.getName().toLowerCase() + "%"));
            }

            if (Objects.nonNull(criteria.getSearch()) && !criteria.getSearch().trim().isEmpty()) {
                String pattern = "%" + criteria.getSearch().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), pattern));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

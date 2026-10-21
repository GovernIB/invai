package es.caib.invai.back.persistence.repository.maintenance.development.role;

import es.caib.invai.back.persistence.model.maintenance.development.role.RoleEntity;
import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Utility factory class responsible for building programmatic JPA {@link Specification}
 * structures using the Criteria API targeting Role entities.
 *
 * @since 1.0.2
 */
@NoArgsConstructor
public final class RoleSpecification {

    /**
     * Compiles variable search boundaries from an input criteria record, constructing
     * query expressions with appropriate mapping predicates.
     *
     * @param criteria DTO containing search filters, flags, and full-text keyword strings
     * @return a JPA {@link Specification} object targeting RoleEntity query environments
     */
    public static Specification<RoleEntity> filterByCriteria(RoleCriteria criteria) {
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

            if (Objects.nonNull(criteria.getName()) && !criteria.getName().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + criteria.getName().toLowerCase() + "%"));
            }

            if (Objects.nonNull(criteria.getNameEs()) && !criteria.getNameEs().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("nameEs")), "%" + criteria.getNameEs().toLowerCase() + "%"));
            }

            if (Objects.nonNull(criteria.getSearch()) && !criteria.getSearch().trim().isEmpty()) {
                String pattern = "%" + criteria.getSearch().toLowerCase() + "%";

                Predicate searchName = cb.like(cb.lower(root.get("name")), pattern);
                Predicate searchNameEs = cb.like(cb.lower(root.get("nameEs")), pattern);

                predicates.add(cb.or(searchName, searchNameEs));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

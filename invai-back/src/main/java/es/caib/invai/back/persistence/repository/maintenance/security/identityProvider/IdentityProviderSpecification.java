package es.caib.invai.back.persistence.repository.maintenance.security.identityProvider;

import es.caib.invai.back.persistence.model.maintenance.security.identityProvider.IdentityProviderEntity;
import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility factory class responsible for building programmatic JPA {@link Specification}
 * structures using the Criteria API targeting IdentityProvider entities.
 *
 * @since 1.0.4
 */
@NoArgsConstructor
public final class IdentityProviderSpecification {

    /**
     * Builds a JPA {@link Specification} that filters {@link IdentityProviderEntity} records
     * according to the given criteria (status, name and free-text search).
     *
     * @param criteria the search criteria, may be {@code null} for no filtering
     * @return a specification combining all applicable predicates with AND
     */
    public static Specification<IdentityProviderEntity> filterByCriteria(IdentityProviderCriteria criteria) {
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
                String pattern = "%" + criteria.getSearch().toLowerCase() + "%";

                predicates.add(cb.like(cb.lower(root.get("name")), pattern));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

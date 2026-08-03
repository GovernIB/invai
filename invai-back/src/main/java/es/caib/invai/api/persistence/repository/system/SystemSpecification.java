package es.caib.invai.api.persistence.repository.system;

import es.caib.invai.api.persistence.model.SystemEntity;
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
 * searches across primary system descriptor properties (e.g., {@code name}, {@code instance},
 * and {@code version}).
 * </p>
 *
 * @since 1.0.1
 */
@NoArgsConstructor
public final class SystemSpecification {

    /**
     * Compiles variable search boundaries from an input criteria record, constructing
     * query expressions with appropriate mapping predicates.
     *
     * @param criteria DTO containing search filters, flags, and full-text keyword strings
     * @return a JPA {@link Specification} object targeting SystemEntity query environments
     */
    public static Specification<SystemEntity> filterByCriteria(SystemCriteria criteria) {
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

            if (criteria.getServerId() != null) {
                predicates.add(cb.equal(root.get("server").get("id"), criteria.getServerId()));
            }

            if (criteria.getInstance() != null && !criteria.getInstance().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("instance")), "%" + criteria.getInstance().toLowerCase() + "%"));
            }

            if (criteria.getVersion() != null && !criteria.getVersion().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("version")), "%" + criteria.getVersion().toLowerCase() + "%"));
            }

            if (criteria.getSearch() != null && !criteria.getSearch().trim().isEmpty()) {
                String pattern = "%" + criteria.getSearch().toLowerCase() + "%";
                Predicate searchServer = cb.like(cb.lower(root.get("server").get("name")), pattern);
                Predicate searchInstance = cb.like(cb.lower(root.get("instance")), pattern);
                Predicate searchVersion = cb.like(cb.lower(root.get("version")), pattern);

                predicates.add(cb.or(searchServer,searchInstance, searchVersion));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

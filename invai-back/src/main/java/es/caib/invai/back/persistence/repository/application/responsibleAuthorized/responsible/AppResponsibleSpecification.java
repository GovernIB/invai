package es.caib.invai.back.persistence.repository.application.responsibleAuthorized.responsible;

import es.caib.invai.back.persistence.model.application.responsibleAuthorized.responsible.AppResponsibleEntity;
import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility factory class responsible for building programmatic JPA {@link Specification}
 * structures using the Criteria API targeting AppResponsible entities. The parent
 * {@code appResponsibleAuthorizedId} is a mandatory scoping constraint: results are always
 * restricted to responsibles belonging to the specified "Responsables i Autoritzats" anchor,
 * never to the entire table.
 *
 * @since 1.0.3
 */
public final class AppResponsibleSpecification {

    /** Prevents instantiation of this static utility class. */
    private AppResponsibleSpecification() {
    }

    /**
     * Compiles a query specification filtering AppResponsible records for the given anchor,
     * optionally narrowed by status, person, responsible type, and free-text search on the
     * assigned person's name and e-mail.
     *
     * @param appResponsibleAuthorizedId mandatory parent anchor identifier scoping the result set
     * @param criteria                   optional filter values mapped from presentation inputs
     * @return an evaluated JPA {@link Specification} ready to be executed against the repository
     */
    public static Specification<AppResponsibleEntity> filterByCriteria(Long appResponsibleAuthorizedId, AppResponsibleCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("appResponsibleAuthorized").get("id"), appResponsibleAuthorizedId));

            if (criteria == null) {
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            Long statusId = criteria.getStatusId();
            if (statusId != null) {
                boolean isActive = StatusEnum.ACTIVE.getId().equals(statusId);
                predicates.add(isActive ? cb.isNull(root.get("deletedAt")) : cb.isNotNull(root.get("deletedAt")));
            }

            if (criteria.getPersonId() != null) {
                predicates.add(cb.equal(root.get("person").get("id"), criteria.getPersonId()));
            }

            if (criteria.getResponsibleTypeId() != null) {
                predicates.add(cb.equal(root.get("responsibleType").get("id"), criteria.getResponsibleTypeId()));
            }

            if (criteria.getSearch() != null && !criteria.getSearch().trim().isEmpty()) {
                String searchPattern = "%" + criteria.getSearch().trim().toLowerCase() + "%";
                List<Predicate> searchPredicates = new ArrayList<>();
                searchPredicates.add(cb.like(cb.lower(root.get("person").get("firstName")), searchPattern));
                searchPredicates.add(cb.like(cb.lower(root.get("person").get("lastName")), searchPattern));
                searchPredicates.add(cb.like(cb.lower(root.get("person").get("email")), searchPattern));
                predicates.add(cb.or(searchPredicates.toArray(new Predicate[0])));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

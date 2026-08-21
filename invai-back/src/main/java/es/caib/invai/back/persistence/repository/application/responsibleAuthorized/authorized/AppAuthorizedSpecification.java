package es.caib.invai.back.persistence.repository.application.responsibleAuthorized.authorized;

import es.caib.invai.back.persistence.model.application.responsibleAuthorized.authorized.AppAuthorizedEntity;
import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * Specification provider executing dynamic predicate tree evaluation against the
 * {@link AppAuthorizedEntity} persistence mapping via standard Criteria APIs.
 *
 * @since 1.0.3
 */
@Slf4j
@Component
@NoArgsConstructor
public class AppAuthorizedSpecification {

    /**
     * Compiles the query specification filter. The parent application identifier is a mandatory
     * scoping constraint: results are always restricted to authorized assignments belonging to the
     * specified application, never to the entire table.
     *
     * @param appResponsibleAuthorizedId mandatory parent Responsables tab anchor identifier scoping the result set
     * @param criteria      contextual data parameter inputs mapped from presentation interfaces
     * @return an evaluated, isolated execution blueprint specification
     */
    public static Specification<AppAuthorizedEntity> filterByCriteria(Long appResponsibleAuthorizedId, AppAuthorizedCriteria criteria) {
        return (root, query, cb) -> {
            log.debug("Specification: Compiling operational predicates based on evaluation state: {}", criteria);
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("appResponsibleAuthorized").get("id"), appResponsibleAuthorizedId));

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

            if (criteria.getPersonId() != null) {
                predicates.add(cb.equal(root.get("person").get("id"), criteria.getPersonId()));
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

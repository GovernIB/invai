package es.caib.invai.back.persistence.repository.application.security.webContext;

import es.caib.invai.back.persistence.model.application.security.webContext.AppWebContextEntity;
import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * Corporate specification provider executing defensive dynamic predicate tree evaluation
 * against the persistence mapping entities via standard javax Criteria APIs.
 *
 * @since 1.0.4
 */
@Slf4j
@Component
@NoArgsConstructor
public class AppWebContextSpecification {

    /**
     * Compiles a highly scannable, multi-tenant compatible query specification criteria filter,
     * scoped to a single parent security anchor: results are always restricted to web context
     * links belonging to that security anchor, never to the entire table. Used by Security's
     * read-only listing endpoint.
     *
     * @param appSecurityId mandatory parent security anchor identifier scoping the result set
     * @param criteria      contextual data parameter inputs mapped from presentation interfaces
     * @return an evaluated, isolated execution blueprint specification
     */
    public static Specification<AppWebContextEntity> filterByAppSecurityId(Long appSecurityId, AppWebContextCriteria criteria) {
        return (root, query, cb) -> buildPredicate(root, cb, cb.equal(root.get("appSecurity").get("id"), appSecurityId), criteria);
    }

    /**
     * Compiles the same predicate tree as {@link #filterByAppSecurityId}, but scoped to a single
     * parent development anchor instead. Used by Development's CRUD listing endpoint - the only
     * place from which web context links can be created, edited, or deleted.
     *
     * @param appDevelopmentId mandatory parent development anchor identifier scoping the result set
     * @param criteria         contextual data parameter inputs mapped from presentation interfaces
     * @return an evaluated, isolated execution blueprint specification
     */
    public static Specification<AppWebContextEntity> filterByAppDevelopmentId(Long appDevelopmentId, AppWebContextCriteria criteria) {
        return (root, query, cb) -> buildPredicate(root, cb, cb.equal(root.get("appDevelopment").get("id"), appDevelopmentId), criteria);
    }

    private static Predicate buildPredicate(jakarta.persistence.criteria.Root<AppWebContextEntity> root, jakarta.persistence.criteria.CriteriaBuilder cb, Predicate mandatoryScope, AppWebContextCriteria criteria) {
        log.debug("Specification: Compiling operational predicates based on evaluation state: {}", criteria);
        List<Predicate> predicates = new ArrayList<>();

        predicates.add(mandatoryScope);

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

        if (criteria.getAppSecurityId() != null) {
            predicates.add(cb.equal(root.get("appSecurity").get("id"), criteria.getAppSecurityId()));
        }
        if (criteria.getAppDevelopmentId() != null) {
            predicates.add(cb.equal(root.get("appDevelopment").get("id"), criteria.getAppDevelopmentId()));
        }
        if (criteria.getWebContextId() != null) {
            predicates.add(cb.equal(root.get("webContext").get("id"), criteria.getWebContextId()));
        }
        if (criteria.getFieldId() != null) {
            predicates.add(cb.equal(root.get("field").get("id"), criteria.getFieldId()));
        }

        if (criteria.getUrl() != null && !criteria.getUrl().trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("url")), "%" + criteria.getUrl().trim().toLowerCase() + "%"));
        }

        if (criteria.getSearch() != null && !criteria.getSearch().trim().isEmpty()) {
            String searchPattern = "%" + criteria.getSearch().trim().toLowerCase() + "%";
            List<Predicate> searchPredicates = new ArrayList<>();

            searchPredicates.add(cb.like(cb.lower(root.get("webContext").get("name")), searchPattern));
            searchPredicates.add(cb.like(cb.lower(root.get("field").get("name")), searchPattern));
            searchPredicates.add(cb.like(cb.lower(root.get("url")), searchPattern));

            predicates.add(cb.or(searchPredicates.toArray(new Predicate[0])));
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}

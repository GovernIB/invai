package es.caib.invai.back.persistence.repository.application.security.role;

import es.caib.invai.back.persistence.model.application.security.role.AppRoleEntity;
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
public class AppRoleSpecification {

    /**
     * Compiles a highly scannable, multi-tenant compatible query specification criteria filter.
     * The parent security anchor identifier is a mandatory scoping constraint: results are always
     * restricted to role assignments belonging to the specified security anchor, never to the entire table.
     *
     * @param appSecurityId mandatory parent security anchor identifier scoping the result set
     * @param criteria      contextual data parameter inputs mapped from presentation interfaces
     * @return an evaluated, isolated execution blueprint specification
     */
    public static Specification<AppRoleEntity> filterByCriteria(Long appSecurityId, AppRoleCriteria criteria) {
        return (root, query, cb) -> {
            log.debug("Specification: Compiling operational predicates based on evaluation state: {}", criteria);
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("appSecurity").get("id"), appSecurityId));

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
            if (criteria.getSecurityRoleId() != null) {
                predicates.add(cb.equal(root.get("securityRole").get("id"), criteria.getSecurityRoleId()));
            }
            if (criteria.getSecurityRoleName() != null && !criteria.getSecurityRoleName().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("securityRole").get("name")),
                        "%" + criteria.getSecurityRoleName().trim().toLowerCase() + "%"));
            }
            if (criteria.getSystem() != null && !criteria.getSystem().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("securityRole").get("system")),
                        "%" + criteria.getSystem().trim().toLowerCase() + "%"));
            }

            if (criteria.getSearch() != null && !criteria.getSearch().trim().isEmpty()) {
                String searchPattern = "%" + criteria.getSearch().trim().toLowerCase() + "%";
                List<Predicate> searchPredicates = new ArrayList<>();

                searchPredicates.add(cb.like(cb.lower(root.get("securityRole").get("name")), searchPattern));
                searchPredicates.add(cb.like(cb.lower(root.get("securityRole").get("description")), searchPattern));

                predicates.add(cb.or(searchPredicates.toArray(new Predicate[0])));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

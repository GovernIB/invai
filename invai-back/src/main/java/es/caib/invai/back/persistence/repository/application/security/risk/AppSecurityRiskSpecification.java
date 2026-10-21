package es.caib.invai.back.persistence.repository.application.security.risk;

import es.caib.invai.back.persistence.model.application.security.risk.AppSecurityRiskEntity;
import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Corporate specification provider executing defensive dynamic predicate tree evaluation
 * against the persistence mapping entities via standard javax Criteria APIs.
 *
 * @since 1.0.4
 */
@Slf4j
@Component
@NoArgsConstructor
public class AppSecurityRiskSpecification {

    /**
     * Compiles a highly scannable, multi-tenant compatible query specification criteria filter.
     * The parent security anchor identifier is a mandatory scoping constraint: results are always
     * restricted to risks belonging to the specified security anchor, never to the entire table.
     *
     * @param appSecurityId mandatory parent security anchor identifier scoping the result set
     * @param criteria      contextual data parameter inputs mapped from presentation interfaces
     * @return an evaluated, isolated execution blueprint specification
     */
    public static Specification<AppSecurityRiskEntity> filterByCriteria(Long appSecurityId, AppSecurityRiskCriteria criteria) {
        return (root, query, cb) -> {
            log.debug("Specification: Compiling operational predicates based on evaluation state: {}", criteria);
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("appSecurity").get("id"), appSecurityId));

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

            if (Objects.nonNull(criteria.getAppSecurityId())) {
                predicates.add(cb.equal(root.get("appSecurity").get("id"), criteria.getAppSecurityId()));
            }
            if (Objects.nonNull(criteria.getLevelId())) {
                predicates.add(cb.equal(root.get("level").get("id"), criteria.getLevelId()));
            }
            if (Objects.nonNull(criteria.getFieldId())) {
                predicates.add(cb.equal(root.get("field").get("id"), criteria.getFieldId()));
            }

            if (Objects.nonNull(criteria.getSearch()) && !criteria.getSearch().trim().isEmpty()) {
                String searchPattern = "%" + criteria.getSearch().trim().toLowerCase() + "%";
                List<Predicate> searchPredicates = new ArrayList<>();

                searchPredicates.add(cb.like(cb.lower(root.get("field").get("name")), searchPattern));
                searchPredicates.add(cb.like(cb.lower(root.get("level").get("name")), searchPattern));

                predicates.add(cb.or(searchPredicates.get(0), searchPredicates.get(1)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

package es.caib.invai.back.persistence.repository.application.development.technology;

import es.caib.invai.back.persistence.model.application.development.technology.AppTechnologyEntity;
import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds the JPA {@link Specification} used by {@link AppTechnologyJPARepository#findAll} to filter
 * {@link AppTechnologyEntity} rows dynamically.
 *
 * @since 1.0.2
 */
@Slf4j
@Component
@NoArgsConstructor
public class AppTechnologySpecification {

    /**
     * Builds a specification always scoped to {@code appDevelopmentId} — a caller can never widen
     * the query beyond a single development's technology entries — plus, when {@code criteria} is
     * non-null, an active/inactive filter on {@code deletedAt} (by {@code statusId}), an extra
     * {@code appDevelopmentId} equality check (redundant with the mandatory one above), and a
     * {@code search} term matched as an exact numeric id when purely digits, otherwise as a
     * case-insensitive substring of the linked technology's name.
     *
     * @param appDevelopmentId mandatory parent development identifier scoping the result set
     * @param criteria         optional additional filters; {@code null} applies only the development scope
     * @return the composed specification
     */
    public static Specification<AppTechnologyEntity> filterByCriteria(Long appDevelopmentId, AppTechnologyCriteria criteria) {
        return (root, query, cb) -> {
            log.debug("Specification: Compiling operational predicates based on evaluation state: {}", criteria);
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("appDevelopment").get("id"), appDevelopmentId));

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

            if (criteria.getAppDevelopmentId() != null) {
                predicates.add(cb.equal(root.get("appDevelopment").get("id"), criteria.getAppDevelopmentId()));
            }

            if (criteria.getSearch() != null && !criteria.getSearch().trim().isEmpty()) {
                String searchPattern = "%" + criteria.getSearch().trim().toLowerCase() + "%";
                List<Predicate> searchPredicates = new ArrayList<>();

                if (criteria.getSearch().trim().matches("\\d+")) {
                    Long numericSearchValue = Long.valueOf(criteria.getSearch().trim());
                    searchPredicates.add(cb.equal(root.get("id"), numericSearchValue));
                } else {
                    searchPredicates.add(cb.like(cb.lower(root.get("technology").get("name")), searchPattern));
                }

                predicates.add(cb.or(searchPredicates.toArray(new Predicate[0])));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

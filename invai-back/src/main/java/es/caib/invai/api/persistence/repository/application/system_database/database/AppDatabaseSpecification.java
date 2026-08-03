package es.caib.invai.api.persistence.repository.application.system_database.database;

import es.caib.invai.api.persistence.model.AppDatabaseEntity;
import es.caib.invai.api.service.model.StatusEnum;
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
 * @since 1.0.2
 */
@Slf4j
@Component
@NoArgsConstructor
public class AppDatabaseSpecification {

    /**
     * Compiles a highly scannable, multi-tenant compatible query specification criteria filter.
     * The parent application identifier is a mandatory scoping constraint: results are always
     * restricted to database links belonging to the specified application, never to the entire table.
     *
     * @param informationSystemDbId mandatory parent application identifier scoping the result set
     * @param criteria      contextual data parameter inputs mapped from presentation interfaces
     * @return an evaluated, isolated execution blueprint specification
     */
    public static Specification<AppDatabaseEntity> filterByCriteria(Long informationSystemDbId, AppDatabaseCriteria criteria) {
        return (root, query, cb) -> {
            log.debug("Specification: Compiling operational predicates based on evaluation state: {}", criteria);
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("informationSystemDb").get("id"), informationSystemDbId));

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

            if (criteria.getInformationSystemDbId() != null) {
                predicates.add(cb.equal(root.get("informationSystemDb").get("id"), criteria.getInformationSystemDbId()));
            }
            if (criteria.getDatabaseId() != null) {
                predicates.add(cb.equal(root.get("database").get("id"), criteria.getDatabaseId()));
            }

            if (criteria.getSearch() != null && !criteria.getSearch().trim().isEmpty()) {
                String searchPattern = "%" + criteria.getSearch().trim().toLowerCase() + "%";
                List<Predicate> searchPredicates = new ArrayList<>();

                searchPredicates.add(cb.like(cb.lower(root.get("database").get("server").get("name")), searchPattern));
                searchPredicates.add(cb.like(cb.lower(root.get("database").get("server").get("environment").get("name")), searchPattern));
                searchPredicates.add(cb.like(cb.lower(root.get("database").get("service")), searchPattern));

                predicates.add(cb.or(searchPredicates.toArray(new Predicate[0])));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

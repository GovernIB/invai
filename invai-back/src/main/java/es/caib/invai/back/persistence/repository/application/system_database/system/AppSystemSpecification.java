package es.caib.invai.back.persistence.repository.application.system_database.system;

import es.caib.invai.back.persistence.model.application.system_database.system.AppSystemEntity;
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
 * @since 1.0.2
 */
@Slf4j
@Component
@NoArgsConstructor
public class AppSystemSpecification {

    /**
     * Compiles a highly scannable, multi-tenant compatible query specification criteria filter.
     * The parent application identifier is a mandatory scoping constraint: results are always
     * restricted to system links belonging to the specified application, never to the entire table.
     *
     * @param informationSystemDbId mandatory parent application identifier scoping the result set
     * @param criteria      contextual data parameter inputs mapped from presentation interfaces
     * @return an evaluated, isolated execution blueprint specification
     */
    public static Specification<AppSystemEntity> filterByCriteria(Long informationSystemDbId, AppSystemCriteria criteria) {
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
            if (criteria.getSystemId() != null) {
                predicates.add(cb.equal(root.get("system").get("id"), criteria.getSystemId()));
            }

            if (criteria.getSearch() != null && !criteria.getSearch().trim().isEmpty()) {
                String searchPattern = "%" + criteria.getSearch().trim().toLowerCase() + "%";

                Predicate searchServer = cb.like(cb.lower(root.get("system").get("server").get("name")), searchPattern);
                Predicate searchInstance = cb.like(cb.lower(root.get("system").get("instance")), searchPattern);
                Predicate searchVersion = cb.like(cb.lower(root.get("system").get("version")), searchPattern);

                predicates.add(cb.or(searchServer,searchInstance, searchVersion));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

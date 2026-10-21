package es.caib.invai.back.persistence.repository.maintenance.systems.database;

import es.caib.invai.back.persistence.model.application.system_database.database.AppDatabaseEntity;
import es.caib.invai.back.persistence.model.maintenance.systems.database.DatabaseEntity;
import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Utility factory class responsible for building programmatic JPA {@link Specification}
 * structures using the Criteria API.
 * <p>
 * Evaluates fields, checks for soft deletion states, and handles global full-text query
 * searches across primary and descriptive properties (e.g., {@code service} vs {@code description}).
 * </p>
 *
 * @since 1.0.2
 */
@NoArgsConstructor
public final class DatabaseSpecification {

    /**
     * Compiles variable search boundaries from an input criteria record, constructing
     * query expressions with appropriate mapping predicates.
     *
     * @param criteria DTO containing search filters, flags, and full-text keyword strings
     * @return a JPA {@link Specification} object targeting DatabaseEntity query environments
     */
    public static Specification<DatabaseEntity> filterByCriteria(DatabaseCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

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

            if (Objects.nonNull(criteria.getServerId())) {
                predicates.add(cb.equal(root.get("server").get("id"), criteria.getServerId()));
            }

            if (Objects.nonNull(criteria.getService()) && !criteria.getService().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("service")), "%" + criteria.getService().toLowerCase() + "%"));
            }

            if (Objects.nonNull(criteria.getDatabaseTypeId())) {
                predicates.add(cb.equal(root.get("databaseType").get("id"), criteria.getDatabaseTypeId()));
            }

            if (Objects.nonNull(criteria.getUnassignedToInformationSystemDbId())) {
                Subquery<Long> subquery = query.subquery(Long.class);
                Root<AppDatabaseEntity> appDatabaseRoot = subquery.from(AppDatabaseEntity.class);
                subquery.select(appDatabaseRoot.get("id"));
                subquery.where(cb.and(
                        cb.equal(appDatabaseRoot.get("database").get("id"), root.get("id")),
                        cb.equal(appDatabaseRoot.get("informationSystemDb").get("id"), criteria.getUnassignedToInformationSystemDbId()),
                        cb.isNull(appDatabaseRoot.get("deletedAt"))
                ));
                predicates.add(cb.not(cb.exists(subquery)));
            }


            if (Objects.nonNull(criteria.getSearch()) && !criteria.getSearch().trim().isEmpty()) {
                String pattern = "%" + criteria.getSearch().toLowerCase() + "%";

                Predicate searchServer = cb.like(cb.lower(root.get("server").get("name")), pattern);
                Predicate searchService = cb.like(cb.lower(root.get("service")), pattern);
                Predicate searchDescription = cb.like(cb.lower(root.get("description")), pattern);

                predicates.add(cb.or(searchServer, searchService, searchDescription));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

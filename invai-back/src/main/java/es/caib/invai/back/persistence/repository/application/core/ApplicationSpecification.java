package es.caib.invai.back.persistence.repository.application.core;

import es.caib.invai.back.persistence.model.application.core.ApplicationEntity;
import es.caib.invai.back.persistence.model.application.responsibleAuthorized.responsible.AppResponsibleEntity;
import es.caib.invai.back.persistence.model.application.system_database.database.AppDatabaseEntity;
import es.caib.invai.back.persistence.model.application.system_database.system.AppSystemEntity;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility factory class responsible for building programmatic JPA {@link Specification}
 * structures using the Criteria API.
 * <p>
 * Evaluates fields, checks for soft deletion states, and handles global full-text query multi-table
 * joins across primary and localized properties (e.g., Catalan name vs Spanish {@code nameEs}).
 * </p>
 *
 * @since 1.0.1
 */
public final class ApplicationSpecification {

    /**
     * Suppresses default constructor instantiation routines to guarantee utility pattern isolation.
     */
    private ApplicationSpecification() {
    }

    /**
     * Compiles variable search boundaries from an input criteria record, constructing
     * query expressions with appropriate mapping predicates.
     *
     * @param criteria DTO containing search filters, flags, and full-text keyword strings
     * @return a JPA {@link Specification} object targeting query environments definitions
     */
    public static Specification<ApplicationEntity> filterByCriteria(ApplicationCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria == null) {
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            if (criteria.getPrefix() != null && !criteria.getPrefix().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("prefix")), "%" + criteria.getPrefix().toLowerCase() + "%"));
            }

            if (criteria.getApplicationName() != null && !criteria.getApplicationName().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + criteria.getApplicationName().toLowerCase() + "%"));
            }

            if (criteria.getCategoryId() != null && !criteria.getCategoryId().toString().isEmpty()) {
                predicates.add(cb.equal(root.get("category").get("id"), criteria.getCategoryId()));
            }

            if (criteria.getSystemTypeId() != null && !criteria.getSystemTypeId().toString().isEmpty()) {
                predicates.add(cb.equal(root.get("systemType").get("id"), criteria.getSystemTypeId()));
            }

            if (criteria.getFieldId() != null && !criteria.getFieldId().isEmpty()) {
                predicates.add(cb.equal(root.get("field").get("id"), criteria.getFieldId()));
            }

            if (criteria.getCommissionId() != null && !criteria.getCommissionId().toString().isEmpty()) {
                predicates.add(cb.equal(root.get("csCommission").get("id"), criteria.getCommissionId()));
            }

            if (criteria.getAdmUnitId() != null && !criteria.getAdmUnitId().toString().isEmpty()) {
                predicates.add(cb.equal(root.get("admUnit").get("id"), criteria.getAdmUnitId()));
            }

            if (criteria.getStatusId() != null && !criteria.getStatusId().isEmpty()) {
                predicates.add(cb.equal(root.get("status").get("id"), criteria.getStatusId()));
            }

            if (criteria.getDescription() != null && !criteria.getDescription().trim().isEmpty()) {
                predicates.add(((HibernateCriteriaBuilder) cb).ilike(root.get("description"), "%" + criteria.getDescription() + "%"));
            }

            if (criteria.getIncomplete() != null) {
                predicates.add(cb.equal(root.get("incomplete"), criteria.getIncomplete()));
            }

            if (criteria.getResponsibleId() != null) {
                Subquery<Long> subquery = query.subquery(Long.class);
                Root<AppResponsibleEntity> appResponsibleRoot = subquery.from(AppResponsibleEntity.class);
                subquery.select(appResponsibleRoot.get("id"));
                subquery.where(cb.and(
                        cb.equal(appResponsibleRoot.get("appResponsibleAuthorized").get("application").get("id"), root.get("id")),
                        cb.equal(appResponsibleRoot.get("person").get("id"), criteria.getResponsibleId()),
                        cb.isNull(appResponsibleRoot.get("deletedAt"))
                ));
                predicates.add(cb.exists(subquery));
            }

            if (criteria.getDatabaseId() != null) {
                Subquery<Long> subquery = query.subquery(Long.class);
                Root<AppDatabaseEntity> appDatabaseRoot = subquery.from(AppDatabaseEntity.class);
                subquery.select(appDatabaseRoot.get("id"));
                subquery.where(cb.and(
                        cb.equal(appDatabaseRoot.get("informationSystemDb").get("application").get("id"), root.get("id")),
                        cb.equal(appDatabaseRoot.get("database").get("id"), criteria.getDatabaseId()),
                        cb.isNull(appDatabaseRoot.get("deletedAt"))
                ));
                predicates.add(cb.exists(subquery));
            }

            if (criteria.getServerId() != null) {
                Subquery<Long> systemSubquery = query.subquery(Long.class);
                Root<AppSystemEntity> appSystemRoot = systemSubquery.from(AppSystemEntity.class);
                systemSubquery.select(appSystemRoot.get("id"));
                systemSubquery.where(cb.and(
                        cb.equal(appSystemRoot.get("informationSystemDb").get("application").get("id"), root.get("id")),
                        cb.equal(appSystemRoot.get("system").get("server").get("id"), criteria.getServerId()),
                        cb.isNull(appSystemRoot.get("deletedAt"))
                ));

                Subquery<Long> databaseSubquery = query.subquery(Long.class);
                Root<AppDatabaseEntity> appDatabaseServerRoot = databaseSubquery.from(AppDatabaseEntity.class);
                databaseSubquery.select(appDatabaseServerRoot.get("id"));
                databaseSubquery.where(cb.and(
                        cb.equal(appDatabaseServerRoot.get("informationSystemDb").get("application").get("id"), root.get("id")),
                        cb.equal(appDatabaseServerRoot.get("database").get("server").get("id"), criteria.getServerId()),
                        cb.isNull(appDatabaseServerRoot.get("deletedAt"))
                ));

                predicates.add(cb.or(cb.exists(systemSubquery), cb.exists(databaseSubquery)));
            }

            if (criteria.getEnvironmentId() != null) {
                Subquery<Long> systemSubquery = query.subquery(Long.class);
                Root<AppSystemEntity> appSystemRoot = systemSubquery.from(AppSystemEntity.class);
                systemSubquery.select(appSystemRoot.get("id"));
                systemSubquery.where(cb.and(
                        cb.equal(appSystemRoot.get("informationSystemDb").get("application").get("id"), root.get("id")),
                        cb.equal(appSystemRoot.get("system").get("server").get("environment").get("id"), criteria.getEnvironmentId()),
                        cb.isNull(appSystemRoot.get("deletedAt"))
                ));

                Subquery<Long> databaseSubquery = query.subquery(Long.class);
                Root<AppDatabaseEntity> appDatabaseServerRoot = databaseSubquery.from(AppDatabaseEntity.class);
                databaseSubquery.select(appDatabaseServerRoot.get("id"));
                databaseSubquery.where(cb.and(
                        cb.equal(appDatabaseServerRoot.get("informationSystemDb").get("application").get("id"), root.get("id")),
                        cb.equal(appDatabaseServerRoot.get("database").get("server").get("environment").get("id"), criteria.getEnvironmentId()),
                        cb.isNull(appDatabaseServerRoot.get("deletedAt"))
                ));

                predicates.add(cb.or(cb.exists(systemSubquery), cb.exists(databaseSubquery)));
            }

            if (criteria.getQuickSearch() != null && !criteria.getQuickSearch().trim().isEmpty()) {
                String pattern = "%" + criteria.getQuickSearch().toLowerCase() + "%";

                Predicate searchCode = cb.like(cb.lower(root.get("code")), pattern);
                Predicate searchPrefix = cb.like(cb.lower(root.get("prefix")), pattern);
                Predicate searchName = cb.like(cb.lower(root.get("name")), pattern);
                Predicate searchDesc = ((HibernateCriteriaBuilder) cb).ilike(root.get("description"), pattern);

                Predicate searchCategory = cb.like(cb.lower(root.get("category").get("name")), pattern);
                Predicate searchSystem = cb.like(cb.lower(root.get("systemType").get("name")), pattern);
                Predicate searchField = cb.like(cb.lower(root.get("field").get("name")), pattern);
                Predicate searchCommission = cb.like(cb.lower(root.get("csCommission").get("name")), pattern);
                Predicate searchAdmUnit = cb.like(cb.lower(root.get("admUnit").get("name")), pattern);
                Predicate searchStatus = cb.like(cb.lower(root.get("status").get("name")), pattern);

                Predicate searchCategoryEs = cb.like(cb.lower(root.get("category").get("nameEs")), pattern);
                Predicate searchSystemEs = cb.like(cb.lower(root.get("systemType").get("nameEs")), pattern);
                Predicate searchFieldEs = cb.like(cb.lower(root.get("field").get("nameEs")), pattern);
                Predicate searchCommissionEs = cb.like(cb.lower(root.get("csCommission").get("nameEs")), pattern);
                Predicate searchAdmUnitEs = cb.like(cb.lower(root.get("admUnit").get("nameEs")), pattern);

                predicates.add(cb.or(
                        searchCode, searchPrefix, searchName, searchDesc,
                        searchCategory, searchCategoryEs,
                        searchSystem, searchSystemEs,
                        searchField, searchFieldEs,
                        searchCommission, searchCommissionEs,
                        searchAdmUnit, searchAdmUnitEs,
                        searchStatus
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
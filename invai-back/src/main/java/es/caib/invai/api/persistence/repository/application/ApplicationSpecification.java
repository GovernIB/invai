package es.caib.invai.api.persistence.repository.application;

import es.caib.invai.api.persistence.model.ApplicationEntity;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;
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
 * @since 1.0.0
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

            predicates.add(cb.isNull(root.get("deletedAt")));

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
                predicates.add(cb.like(cb.lower(root.get("description")), "%" + criteria.getDescription().toLowerCase() + "%"));
            }

            if (criteria.getIncomplete() != null) {
                predicates.add(cb.equal(root.get("incomplete"), criteria.getIncomplete()));
            }

            // Universal global full-text search block mapping across fields, dependencies and translation labels
            if (criteria.getQuickSearch() != null && !criteria.getQuickSearch().trim().isEmpty()) {
                String pattern = "%" + criteria.getQuickSearch().toLowerCase() + "%";

                Predicate searchCode = cb.like(cb.lower(root.get("code")), pattern);
                Predicate searchPrefix = cb.like(cb.lower(root.get("prefix")), pattern);
                Predicate searchName = cb.like(cb.lower(root.get("name")), pattern);
                Predicate searchDesc = cb.like(cb.lower(root.get("description")), pattern);

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
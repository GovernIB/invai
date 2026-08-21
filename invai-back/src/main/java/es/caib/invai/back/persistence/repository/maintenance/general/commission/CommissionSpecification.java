package es.caib.invai.back.persistence.repository.maintenance.general.commission;

import es.caib.invai.back.persistence.model.maintenance.general.commission.CommissionEntity;
import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility factory class responsible for building programmatic JPA {@link Specification}
 * structures using the Criteria API.
 * <p>
 * Evaluates fields, checks for soft deletion states, and handles global full-text query
 * searches across primary and localized properties (e.g., Catalan name vs Spanish {@code nameEs}).
 * </p>
 *
 * @since 1.0.1
 */
public final class CommissionSpecification {

    /**
     * Suppresses default constructor instantiation routines to guarantee utility pattern isolation.
     */
    private CommissionSpecification() {
    }

    /**
     * Compiles variable search boundaries from an input criteria record, constructing
     * query expressions with appropriate mapping predicates.
     *
     * @param criteria DTO containing search filters, flags, and full-text keyword strings
     * @return a JPA {@link Specification} object targeting CommissionEntity query environments
     */
    public static Specification<CommissionEntity> filterByCriteria(CommissionCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

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

            if (criteria.getName() != null && !criteria.getName().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + criteria.getName().toLowerCase() + "%"));
            }

            if (criteria.getNameEs() != null && !criteria.getNameEs().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("nameEs")), "%" + criteria.getNameEs().toLowerCase() + "%"));
            }

            if (criteria.getCommissionType() != null) {
                predicates.add(cb.equal(root.get("commissionType"), criteria.getCommissionType()));
            }

            if (criteria.getApprovalDate() != null) {
                predicates.add(cb.equal(root.get("approvalDate"), criteria.getApprovalDate()));
            }

            if (criteria.getExpedientNumber() != null && !criteria.getExpedientNumber().trim().isEmpty()) {
                predicates.add(cb.equal(root.get("expedientNumber"), criteria.getExpedientNumber().trim()));
            }

            if (criteria.getSearch() != null && !criteria.getSearch().trim().isEmpty()) {
                String pattern = "%" + criteria.getSearch().toLowerCase() + "%";

                Predicate searchName = cb.like(cb.lower(root.get("name")), pattern);
                Predicate searchNameEs = cb.like(cb.lower(root.get("nameEs")), pattern);
                Predicate searchExpedientNumber = cb.like(cb.lower(root.get("expedientNumber")), pattern);

                predicates.add(cb.or(searchName, searchNameEs, searchExpedientNumber));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
package es.caib.invai.back.persistence.repository.application.integration.connection;

import es.caib.invai.back.persistence.model.application.integration.connection.AppIntegrationConnectionEntity;
import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility factory class responsible for building programmatic JPA {@link Specification}
 * structures using the Criteria API targeting integration connection entities. The parent anchor
 * identifier is a mandatory scoping constraint: results are always restricted to connections
 * belonging to the specified integration anchor, never to the entire table.
 *
 * @since 1.0.5
 */
@NoArgsConstructor
public final class AppIntegrationConnectionSpecification {

    /**
     * Builds a JPA {@link Specification} combining the mandatory anchor scope with status and
     * FK predicates derived from the given criteria.
     *
     * @param appIntegrationId mandatory parent anchor identifier scoping the result set
     * @param criteria         the filter criteria to translate into predicates, may be {@code null}
     * @return a specification matching integration connections fulfilling the given criteria
     */
    public static Specification<AppIntegrationConnectionEntity> filterByCriteria(Long appIntegrationId, AppIntegrationConnectionCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("appIntegration").get("id"), appIntegrationId));

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

            if (criteria.getApplicationId() != null) {
                predicates.add(cb.equal(root.get("application").get("id"), criteria.getApplicationId()));
            }
            if (criteria.getExternalSystemId() != null) {
                predicates.add(cb.equal(root.get("externalSystem").get("id"), criteria.getExternalSystemId()));
            }
            if (criteria.getTechnologyId() != null) {
                predicates.add(cb.equal(root.get("technology").get("id"), criteria.getTechnologyId()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

package es.caib.invai.back.persistence.repository.maintenance.systems.server;

import es.caib.invai.back.persistence.model.maintenance.systems.server.ServerEntity;
import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility factory class responsible for building programmatic JPA {@link Specification}
 * structures using the Criteria API for {@link ServerEntity} queries.
 *
 * @since 1.0.2
 */
@NoArgsConstructor
public final class ServerSpecification {

    /**
     * Compiles variable search boundaries from an input criteria record, constructing
     * query expressions with appropriate mapping predicates.
     *
     * @param criteria DTO containing search filters, flags, and full-text keyword strings
     * @return a JPA {@link Specification} object targeting ServerEntity query environments
     */
    public static Specification<ServerEntity> filterByCriteria(ServerCriteria criteria) {
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

            if (criteria.getEnvironmentId() != null) {
                predicates.add(cb.equal(root.get("environment").get("id"), criteria.getEnvironmentId()));
            }

            if (criteria.getServerTypeId() != null) {
                predicates.add(cb.equal(root.get("serverType").get("id"), criteria.getServerTypeId()));
            }

            if (criteria.getServerTypeCode() != null && !criteria.getServerTypeCode().trim().isEmpty()) {
                predicates.add(cb.equal(root.get("serverType").get("code"), criteria.getServerTypeCode().trim()));
            }

            if (criteria.getSearch() != null && !criteria.getSearch().trim().isEmpty()) {
                String pattern = "%" + criteria.getSearch().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), pattern));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

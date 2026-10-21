package es.caib.invai.back.persistence.repository.maintenance.responsible.person;

import es.caib.invai.back.persistence.model.maintenance.responsible.person.PersonEntity;
import es.caib.invai.back.service.model.catalog.status.StatusEnum;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Utility factory class responsible for building programmatic JPA {@link Specification}
 * structures using the Criteria API for {@link PersonEntity} queries.
 *
 * @since 1.0.3
 */
@NoArgsConstructor
public final class PersonSpecification {

    /**
     * Compiles variable search boundaries from an input criteria record, constructing
     * query expressions with appropriate mapping predicates.
     *
     * @param criteria DTO containing search filters, flags, and full-text keyword strings
     * @return a JPA {@link Specification} object targeting PersonEntity query environments
     */
    public static Specification<PersonEntity> filterByCriteria(PersonCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (Objects.isNull(criteria)) {
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            if (Objects.nonNull(criteria.getPersonalCaib())) {
                predicates.add(cb.equal(root.get("personalCaib"), criteria.getPersonalCaib()));
            }

            Long statusId = criteria.getStatusId();
            if (Objects.nonNull(statusId)) {
                boolean isActive = StatusEnum.ACTIVE.getId().equals(statusId);
                predicates.add(isActive
                        ? cb.isNull(root.get("deletedAt"))
                        : cb.isNotNull(root.get("deletedAt"))
                );
            }

            if (Objects.nonNull(criteria.getFirstName()) && !criteria.getFirstName().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("firstName")), "%" + criteria.getFirstName().toLowerCase() + "%"));
            }

            if (Objects.nonNull(criteria.getLastName()) && !criteria.getLastName().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("lastName")), "%" + criteria.getLastName().toLowerCase() + "%"));
            }

            if (Objects.nonNull(criteria.getEmail()) && !criteria.getEmail().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("email")), "%" + criteria.getEmail().toLowerCase() + "%"));
            }

            if (Objects.nonNull(criteria.getCompanyId())) {
                predicates.add(cb.equal(root.get("company").get("id"), criteria.getCompanyId()));
            }

            if (Objects.nonNull(criteria.getExcludeId())) {
                predicates.add(cb.notEqual(root.get("id"), criteria.getExcludeId()));
            }

            if (Objects.nonNull(criteria.getSearch()) && !criteria.getSearch().trim().isEmpty()) {
                String pattern = "%" + criteria.getSearch().trim().toLowerCase().replaceAll("\\s+", " ") + "%";

                Predicate searchFirstName = cb.like(cb.lower(root.get("firstName")), pattern);
                Predicate searchLastName = cb.like(cb.lower(root.get("lastName")), pattern);
                Predicate searchEmail = cb.like(cb.lower(root.get("email")), pattern);
                Predicate searchFullName = cb.like(
                        cb.lower(cb.concat(cb.concat(root.get("firstName"), " "), root.get("lastName"))),
                        pattern
                );
                Predicate searchUserName = cb.like(cb.lower(root.get("userName")), pattern);

                predicates.add(cb.or(searchFirstName, searchLastName, searchEmail, searchFullName, searchUserName));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

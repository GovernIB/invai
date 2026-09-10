package es.caib.invai.back.persistence.repository.application.security.ensClassification;

import es.caib.invai.back.persistence.model.application.security.ensClassification.AppEnsClassificationEntity;
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
 * @since 1.0.4
 */
@Slf4j
@Component
@NoArgsConstructor
public class AppEnsClassificationSpecification {

    /**
     * Compiles a highly scannable, multi-tenant compatible query specification criteria filter.
     * The parent security anchor identifier is a mandatory scoping constraint: results are always
     * restricted to ENS classification records belonging to the specified security anchor, never
     * to the entire table.
     *
     * @param appSecurityId mandatory parent security anchor identifier scoping the result set
     * @param criteria      contextual data parameter inputs mapped from presentation interfaces
     * @return an evaluated, isolated execution blueprint specification
     */
    public static Specification<AppEnsClassificationEntity> filterByCriteria(Long appSecurityId, AppEnsClassificationCriteria criteria) {
        return (root, query, cb) -> {
            log.debug("Specification: Compiling operational predicates based on evaluation state: {}", criteria);
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("appSecurity").get("id"), appSecurityId));

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

            if (criteria.getAppSecurityId() != null) {
                predicates.add(cb.equal(root.get("appSecurity").get("id"), criteria.getAppSecurityId()));
            }
            if (criteria.getIdentityProviderId() != null) {
                predicates.add(cb.equal(root.get("identityProvider").get("id"), criteria.getIdentityProviderId()));
            }
            if (criteria.getEnsSubjectId() != null) {
                predicates.add(cb.equal(root.get("ensSubject").get("id"), criteria.getEnsSubjectId()));
            }
            if (criteria.getPersonalDataProcessingId() != null) {
                predicates.add(cb.equal(root.get("personalDataProcessing").get("id"), criteria.getPersonalDataProcessingId()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

package es.caib.invai.back.persistence.repository.application.responsibleAuthorized.authorized;

import es.caib.invai.back.persistence.model.application.responsibleAuthorized.authorized.AppAuthorizedEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data Native Bridge Interface infrastructure for the {@link AppAuthorizedEntity} anchor.
 *
 * @since 1.0.3
 */
public interface AppAuthorizedJPARepository extends JpaRepository<AppAuthorizedEntity, Long>, JpaSpecificationExecutor<AppAuthorizedEntity> {

    /**
     * Checks whether an active (not soft-deleted) authorization already exists for the given
     * anchor/person pair.
     *
     * @param appResponsibleAuthorizedId the parent anchor identifier
     * @param personId the person identifier
     * @return {@code true} if a matching active authorization exists
     */
    boolean existsByAppResponsibleAuthorizedIdAndPersonIdAndDeletedAtIsNull(Long appResponsibleAuthorizedId, Long personId);

    /**
     * Checks whether another active (not soft-deleted) authorization already exists for the given
     * anchor/person pair, excluding a specific row identifier.
     *
     * @param appResponsibleAuthorizedId the parent anchor identifier
     * @param personId the person identifier
     * @param id identifier to exclude from the check
     * @return {@code true} if a matching active authorization exists
     */
    boolean existsByAppResponsibleAuthorizedIdAndPersonIdAndIdNotAndDeletedAtIsNull(Long appResponsibleAuthorizedId, Long personId, Long id);

    /**
     * Finds every active (not soft-deleted) authorization currently held by the given person,
     * across all applications.
     *
     * @param personId the person identifier
     * @return the matching active authorization entities
     */
    List<AppAuthorizedEntity> findAllByPersonIdAndDeletedAtIsNull(Long personId);

    /**
     * Finds the currently active (not soft-deleted) authorization, if any, held by the given
     * person on the given anchor.
     *
     * @param appResponsibleAuthorizedId the parent anchor identifier
     * @param personId the person identifier
     * @return the matching active authorization entity, if present
     */
    Optional<AppAuthorizedEntity> findByAppResponsibleAuthorizedIdAndPersonIdAndDeletedAtIsNull(Long appResponsibleAuthorizedId, Long personId);

    /**
     * Finds every active (not soft-deleted) authorization currently held on the given anchor,
     * across every person.
     *
     * @param appResponsibleAuthorizedId the parent anchor identifier
     * @return the matching active authorization entities
     */
    List<AppAuthorizedEntity> findAllByAppResponsibleAuthorizedIdAndDeletedAtIsNull(Long appResponsibleAuthorizedId);
}

package es.caib.invai.back.persistence.repository.application.accessibility;

import es.caib.invai.back.persistence.model.application.accessibility.AppAccessibilityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository providing CRUD access to {@link AppAccessibilityEntity} rows, plus
 * the by-application lookup used to enforce "at most one anchor per application" and to fetch the
 * anchor backing an application's "Accessibilitat" tab.
 *
 * @since 1.0.4
 */
public interface AppAccessibilityJPARepository extends JpaRepository<AppAccessibilityEntity, Long> {

    /**
     * Looks up the accessibility row for the given application, regardless of soft-delete status
     * — both active and previously soft-deleted rows match. Callers (e.g. the repository
     * adapter's create-duplicate check) inspect {@code deletedAt} themselves to tell them apart.
     *
     * @param applicationId identifier of the owning application
     * @return the matching row, if any (present even if soft-deleted)
     */
    Optional<AppAccessibilityEntity> findByApplicationId(Long applicationId);

}

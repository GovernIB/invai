package es.caib.invai.back.persistence.repository.application.data;

import es.caib.invai.back.persistence.model.application.data.AppDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository providing CRUD access to {@link AppDataEntity} rows, plus the
 * by-application lookup used to fetch the anchor backing an application's "Open Data" tab.
 *
 * @since 1.0.5
 */
public interface AppDataJPARepository extends JpaRepository<AppDataEntity, Long> {

    /**
     * Looks up the open data row for the given application, regardless of soft-delete status.
     *
     * @param applicationId identifier of the owning application
     * @return the matching row, if any (present even if soft-deleted)
     */
    Optional<AppDataEntity> findByApplicationId(Long applicationId);
}

package es.caib.invai.back.persistence.repository.application.system_database.core;

import es.caib.invai.back.persistence.model.application.system_database.core.AppInformationSystemDbEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data Native Bridge Interface infrastructure for root Entity.
 *
 * @since 1.0.2
 */
public interface AppInformationSystemDbJPARepository extends JpaRepository<AppInformationSystemDbEntity, Long> {

    /**
     * Resolves a paginated sequence of information system database groupings scoped to a single
     * parent application.
     *
     * @param applicationId mandatory parent application identifier scoping the result set
     * @return the paginated matrix of matching entities
     */
    Optional<AppInformationSystemDbEntity> findByApplicationId(Long applicationId);

    /**
     * Resolves every information system database grouping whose parent application
     * identifier is contained in the given collection.
     *
     * @param applicationIds parent application identifiers to filter by
     * @return the list of matching entities
     */
    List<AppInformationSystemDbEntity> findByApplicationIdIn(List<Long> applicationIds);
}

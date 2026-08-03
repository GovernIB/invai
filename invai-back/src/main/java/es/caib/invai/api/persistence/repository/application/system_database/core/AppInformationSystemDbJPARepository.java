package es.caib.invai.api.persistence.repository.application.system_database.core;

import es.caib.invai.api.persistence.model.AppInformationSystemDbEntity;
import es.caib.invai.api.service.model.AppInformationSystemDb;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

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

    List<AppInformationSystemDbEntity> findByApplicationIdIn(List<Long> applicationIds);
}

package es.caib.invai.back.persistence.repository.application.development.technology;

import es.caib.invai.back.persistence.model.application.development.technology.AppTechnologyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Spring Data JPA repository for {@link AppTechnologyEntity}, with specification support for
 * dynamic criteria filtering (see {@link AppTechnologySpecification}).
 *
 * @since 1.0.2
 */
public interface AppTechnologyJPARepository extends JpaRepository<AppTechnologyEntity, Long>, JpaSpecificationExecutor<AppTechnologyEntity> {

    /**
     * Checks whether any technology record references the given architecture layer catalog entry.
     *
     * @param layerId the layer catalog identifier to check
     * @return {@code true} if at least one technology entry references the layer, {@code false} otherwise
     */
    boolean existsByLayerId(Long layerId);

    /**
     * Checks whether any technology record references the given technology catalog entry.
     *
     * @param technologyId the technology catalog identifier to check
     * @return {@code true} if at least one technology entry references the catalog entry, {@code false} otherwise
     */
    boolean existsByTechnologyId(Long technologyId);
}

package es.caib.invai.back.persistence.repository.maintenance.development.technology;

import es.caib.invai.back.persistence.model.maintenance.development.technology.TechnologyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing CRUD operations, custom query methods,
 * and constraint validation checks targeting live {@link TechnologyEntity} records.
 *
 * @since 1.0.2
 */
@Repository
public interface TechnologyJPARepository extends JpaRepository<TechnologyEntity, Long>, JpaSpecificationExecutor<TechnologyEntity> {

    /**
     * Checks whether a non-deleted technology exists with the given name.
     *
     * @param name the name to check
     * @return {@code true} if a matching non-deleted technology exists
     */
    boolean existsByNameAndDeletedAtIsNull(String name);

    /**
     * Checks whether a non-deleted technology other than the given ID exists with the given name.
     *
     * @param name the name to check
     * @param id   the identifier to exclude from the check
     * @return {@code true} if a matching non-deleted technology exists
     */
    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id);

    /**
     * Checks whether any technology is associated with the given architecture layer.
     *
     * @param layerId the layer identifier to check
     * @return {@code true} if at least one technology references the layer
     */
    boolean existsByLayerId(Long layerId);
}

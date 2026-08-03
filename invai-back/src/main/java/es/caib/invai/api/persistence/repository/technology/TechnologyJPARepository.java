package es.caib.invai.api.persistence.repository.technology;

import es.caib.invai.api.persistence.model.TechnologyEntity;
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

    boolean existsByNameAndDeletedAtIsNull(String name);

    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id);

    boolean existsByLayerId(Long layerId);
}

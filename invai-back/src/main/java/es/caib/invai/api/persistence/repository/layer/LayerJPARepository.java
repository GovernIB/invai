package es.caib.invai.api.persistence.repository.layer;

import es.caib.invai.api.persistence.model.LayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing CRUD operations, custom query methods,
 * and constraint validation checks targeting live {@link LayerEntity} records.
 *
 * @since 1.0.2
 */
@Repository
public interface LayerJPARepository extends JpaRepository<LayerEntity, Long>, JpaSpecificationExecutor<LayerEntity> {

    boolean existsByNameAndDeletedAtIsNull(String name);

    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id);
}

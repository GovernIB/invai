package es.caib.invai.api.persistence.repository.technology;

import es.caib.invai.api.service.model.Technology;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Core business domain outbound Port boundary interface declaring relational persistence mechanisms
 * for the catalog Technology layer.
 *
 * @since 1.0.2
 */
public interface TechnologyRepository {

    Technology findById(Long id);

    Page<Technology> findAll(TechnologyCriteria filter, Pageable pageable);

    Technology create(Technology technology);

    Technology update(Technology technology, Long id);

    void delete(Technology technology);

    boolean existsByNameAndDeletedAtIsNull(String name);

    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id);

    boolean existsByLayerId(Long layerId);
}

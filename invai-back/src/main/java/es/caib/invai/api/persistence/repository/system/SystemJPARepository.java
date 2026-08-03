package es.caib.invai.api.persistence.repository.system;

import es.caib.invai.api.persistence.model.SystemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Native Spring Data JPA repository layer providing entity lifecycle updates, custom queries,
 * and constraint validations for {@link SystemEntity}.
 *
 * @since 1.0.1
 */
@Repository
public interface SystemJPARepository extends JpaRepository<SystemEntity, Long>, JpaSpecificationExecutor<SystemEntity> {

    Optional<SystemEntity> findById(Long id);

    /**
     * Determines whether an active system tracking server and instance constraint violation occurs.
     */
    boolean existsByServerIdAndInstance(Long serverId, String instance);

    /**
     * Verifies if an alternative active entity conflicts with a given server and instance, excluding a target ID.
     */
    boolean existsByServerIdAndInstanceAndId(Long serverId, String instance, Long id);
}
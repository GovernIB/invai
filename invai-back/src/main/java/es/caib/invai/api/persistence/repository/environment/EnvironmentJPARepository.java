package es.caib.invai.api.persistence.repository.environment;

import es.caib.invai.api.persistence.model.CommissionEntity;
import es.caib.invai.api.persistence.model.EnvironmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Native Spring Data JPA repository layer providing entity lifecycle updates for {@link EnvironmentEntity}.
 *
 * @since 1.0.1
 */
@Repository
public interface EnvironmentJPARepository extends JpaRepository<EnvironmentEntity, Long>, JpaSpecificationExecutor<EnvironmentEntity> {

    Optional<EnvironmentEntity> findById(Long id);

    boolean existsByCodeAndDeletedAtIsNull(String code);

    boolean existsByCodeAndIdNotAndDeletedAtIsNull(String code, Long id);

}
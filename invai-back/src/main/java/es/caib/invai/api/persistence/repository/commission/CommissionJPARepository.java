package es.caib.invai.api.persistence.repository.commission;

import es.caib.invai.api.persistence.model.CommissionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing CRUD operations, custom query methods,
 * and constraint validation checks targeting live {@link CommissionEntity} working group records.
 *
 * @author invai-team
 * @since 1.0.1
 */
@Repository
public interface CommissionJPARepository extends JpaRepository<CommissionEntity, Long>, JpaSpecificationExecutor<CommissionEntity> {

    boolean existsByExpedientNumberAndDeletedAtIsNull(String expedientNumber);

    boolean existsByExpedientNumberAndIdNotAndDeletedAtIsNull(String expedientNumber, Long id);

    boolean existsByNameAndDeletedAtIsNull(String name);

    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id);
}
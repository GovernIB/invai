package es.caib.invai.api.persistence.repository.database;

import es.caib.invai.api.persistence.model.DatabaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA Infrastructure Interface providing out-of-the-box operations
 * for the {@link DatabaseEntity} database entity.
 *
 * @since 1.0.2
 */
@Repository
public interface DatabaseJPARepository extends JpaRepository<DatabaseEntity, Long>, JpaSpecificationExecutor<DatabaseEntity> {
    boolean existsByServerIdAndService(Long serverId, String service);
    boolean existsByServerIdAndServiceAndIdNot(Long serverId, String service, Long id);
}
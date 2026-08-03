package es.caib.invai.api.persistence.repository.databaseVendor;

import es.caib.invai.api.persistence.model.DatabaseVendorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Native Spring Data JPA repository layer providing entity lifecycle updates, custom queries,
 * and constraint validations for {@link DatabaseVendorEntity}.
 *
 * @since 1.0.2
 */
@Repository
public interface DatabaseVendorJPARepository extends JpaRepository<DatabaseVendorEntity, Long>, JpaSpecificationExecutor<DatabaseVendorEntity> {

    Optional<DatabaseVendorEntity> findById(Long id);

    boolean existsByNameAndDeletedAtIsNull(String name);

    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id);
}

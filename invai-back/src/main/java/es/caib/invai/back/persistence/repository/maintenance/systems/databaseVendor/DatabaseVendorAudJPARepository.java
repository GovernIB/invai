package es.caib.invai.back.persistence.repository.maintenance.systems.databaseVendor;

import es.caib.invai.back.persistence.model.maintenance.systems.databaseVendor.DatabaseVendorAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA interface mapping historical snapshot persistence for the {@link DatabaseVendorAudEntity}.
 *
 * @since 1.0.2
 */
@Repository
public interface DatabaseVendorAudJPARepository extends JpaRepository<DatabaseVendorAudEntity, Long> {
}

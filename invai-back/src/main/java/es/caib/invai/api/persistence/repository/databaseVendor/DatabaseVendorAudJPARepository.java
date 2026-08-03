package es.caib.invai.api.persistence.repository.databaseVendor;

import es.caib.invai.api.persistence.model.DatabaseVendorAudEntity;
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

package es.caib.invai.back.persistence.repository.maintenance.responsible.company;

import es.caib.invai.back.persistence.model.maintenance.responsible.company.CompanyAudEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing basic CRUD database operations
 * targeting historical {@link CompanyAudEntity} snapshots.
 *
 * @since 1.0.3
 */
@Repository
public interface CompanyAudJPARepository extends JpaRepository<CompanyAudEntity, Long> {
}

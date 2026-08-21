package es.caib.invai.back.persistence.repository.maintenance.responsible.company;

import es.caib.invai.back.persistence.model.maintenance.responsible.company.CompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing CRUD operations, custom query methods,
 * and constraint validation checks targeting live {@link CompanyEntity} records.
 *
 * @since 1.0.3
 */
@Repository
public interface CompanyJPARepository extends JpaRepository<CompanyEntity, Long>, JpaSpecificationExecutor<CompanyEntity> {

    /**
     * Checks whether an active (non-deleted) company exists with the given name.
     *
     * @param name the company name to check
     * @return {@code true} if a matching non-deleted company exists
     */
    boolean existsByNameAndDeletedAtIsNull(String name);

    /**
     * Checks whether an active (non-deleted) company exists with the given name and a different identifier.
     *
     * @param name the company name to check
     * @param id   the identifier to exclude from the check
     * @return {@code true} if a matching non-deleted company exists with a different ID
     */
    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id);
}

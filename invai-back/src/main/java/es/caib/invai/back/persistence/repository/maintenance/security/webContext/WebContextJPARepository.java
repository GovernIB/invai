package es.caib.invai.back.persistence.repository.maintenance.security.webContext;

import es.caib.invai.back.persistence.model.maintenance.security.webContext.WebContextEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Native Spring Data JPA repository layer interface providing CRUD operations, custom query methods,
 * and constraint validation checks targeting live {@link WebContextEntity} records.
 *
 * @since 1.0.4
 */
@Repository
public interface WebContextJPARepository extends JpaRepository<WebContextEntity, Long>, JpaSpecificationExecutor<WebContextEntity> {

    /**
     * Checks whether an active (not logically deleted) web context with the given name exists.
     *
     * @param name the name to check
     * @return {@code true} if a matching active record exists
     */
    boolean existsByNameAndDeletedAtIsNull(String name);

    /**
     * Checks whether an active (not logically deleted) web context with the given name exists,
     * excluding the record with the given ID.
     *
     * @param name the name to check
     * @param id the identifier to exclude from the check
     * @return {@code true} if a matching active record exists other than the one with the given ID
     */
    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id);
}

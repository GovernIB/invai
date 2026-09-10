package es.caib.invai.back.persistence.repository.maintenance.general.commission;

import es.caib.invai.back.persistence.model.maintenance.general.commission.CommissionEntity;
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

    /**
     * Checks whether an active (non soft-deleted) commission exists with the given expedient number.
     *
     * @param expedientNumber the expedient dossier tracking code to check
     * @return {@code true} if a matching active commission exists, {@code false} otherwise
     */
    boolean existsByExpedientNumberAndDeletedAtIsNull(String expedientNumber);

    /**
     * Checks whether an active (non soft-deleted) commission other than the given ID exists with the given expedient number.
     *
     * @param expedientNumber the expedient dossier tracking code to check
     * @param id              the commission ID to exclude from the check
     * @return {@code true} if a conflicting active commission exists, {@code false} otherwise
     */
    boolean existsByExpedientNumberAndIdNotAndDeletedAtIsNull(String expedientNumber, Long id);

    /**
     * Checks whether an active (non soft-deleted) commission exists with the given name.
     *
     * @param name the commission name to check
     * @return {@code true} if a matching active commission exists, {@code false} otherwise
     */
    boolean existsByNameAndDeletedAtIsNull(String name);

    /**
     * Checks whether an active (non soft-deleted) commission other than the given ID exists with the given name.
     *
     * @param name the commission name to check
     * @param id   the commission ID to exclude from the check
     * @return {@code true} if a conflicting active commission exists, {@code false} otherwise
     */
    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id);

    /**
     * Determines whether an active Commission entry matching a specific Spanish name already exists.
     *
     * @param nameEs target Spanish name value to verify
     * @return {@code true} if a matching active record is found, {@code false} otherwise
     */
    boolean existsByNameEsAndDeletedAtIsNull(String nameEs);

    /**
     * Determines whether an alternative active Commission matching a targeted Spanish name exists,
     * excluding a designated record reference ID. Typically utilized during update uniqueness checks.
     *
     * @param nameEs target Spanish name value to verify
     * @param id     the persistent primary reference identity to exclude from evaluation scopes
     * @return {@code true} if a conflicting record matches the given criteria, {@code false} otherwise
     */
    boolean existsByNameEsAndIdNotAndDeletedAtIsNull(String nameEs, Long id);
}
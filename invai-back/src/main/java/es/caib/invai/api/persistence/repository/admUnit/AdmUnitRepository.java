package es.caib.invai.api.persistence.repository.admUnit;

import es.caib.invai.api.service.model.AdmUnit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Core business domain outbound Port boundary interface handling persistence requirements
 * for administrative units.
 * <p>
 * Decouples domain engine transactional components from relational frameworks and driver specifics
 * by working exclusively with domain-agnostic or plain business models ({@link AdmUnit}).
 * </p>
 *
 * @since 1.0.0
 */
public interface AdmUnitRepository {

    /**
     * Resolves an active administrative unit business model matching the given primary key structure.
     *
     * @param id unique database sequence row identifier tracking the asset
     * @return the mapped domain {@link AdmUnit} instance, or {@code null} if missing or soft-deleted
     */
    AdmUnit findById(Long id);

    /**
     * Extracts a paginated sequence wrapper matrix across active domain administrative unit nodes.
     *
     * @param pageable sorting configuration boundaries and limit arguments
     * @return a page structure containing mapped domain {@link AdmUnit} entities
     */
    Page<AdmUnit> findAll(Pageable pageable);

    /**
     * Commits a clean domain entity specification record state parameters map to database systems.
     *
     * @param admUnit transient domain model capturing targets properties variables data
     * @return a detailed persistent domain snapshot representation mirroring saved data variables
     */
    AdmUnit create(AdmUnit admUnit);

    /**
     * Synchronizes and updates data structures for an active database identity row.
     *
     * @param admUnit property modifications schema containing target update variables data
     * @param id      persistent identity identifier index tracking state records
     * @return a fresh modified data model instance mapping persistent state representations
     */
    AdmUnit update(AdmUnit admUnit, Long id);

    /**
     * Finalizes execution pathways logic scopes routing parameter assets towards data deletion states.
     *
     * @param admUnit the data schema reference instance targeted for deactivation
     */
    void delete(AdmUnit admUnit);

    /**
     * Evaluates active namespace tracking records to check for name descriptor collisions.
     *
     * @param name target description name query criteria
     * @return {@code true} if conflicts exist, {@code false} otherwise
     */
    boolean existsByNameAndDeletedAtIsNull(String name);

    /**
     * Verifies if alternative active entities share a requested name flag, filtering target references.
     *
     * @param name target description name query criteria
     * @param id   row entry identification key index to ignore
     * @return {@code true} if duplicates exist outside the index parameter, {@code false} otherwise
     */
    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id);

    /**
     * Evaluates structural alphanumerical values to verify unique code constraints.
     *
     * @param code unique registry identifier criteria to inspect
     * @return {@code true} if conflicting attributes map onto active registries, {@code false} otherwise
     */
    boolean existsByCodeAndDeletedAtIsNull(String code);

    /**
     * Verifies if alternative active entities share a requested tracking code, filtering target references.
     *
     * @param code unique registry identifier criteria to inspect
     * @param id   row entry identification key index to ignore
     * @return {@code true} if duplicates exist outside the index parameter, {@code false} otherwise
     */
    boolean existsByCodeAndIdNotAndDeletedAtIsNull(String code, Long id);
}
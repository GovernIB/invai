package es.caib.invai.api.persistence.repository.admUnit;

import es.caib.invai.api.service.model.AdmUnit;
import es.caib.invai.api.persistence.model.AdmUnitEntity;
import es.caib.invai.api.persistence.model.AdmUnitAudEntity;
import es.caib.invai.api.service.mapper.AdmUnitMapper;
import es.caib.invai.api.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Infrastructure repository Adapter implementing the outbound port boundary {@link AdmUnitRepository}.
 * <p>
 * Orchestrates technical structural operations, routing operations across active database layers
 * ({@link AdmUnitJPARepository} and {@link AdmUnitAudJPARepository}) while utilizing
 * mapping layers to enforce decoupling rules.
 * </p>
 *
 * @since 1.0.0
 */
@Repository
@Slf4j
public class AdmUnitRepositoryAdapter implements AdmUnitRepository {

    @Autowired
    private AdmUnitJPARepository admUnitJPARepository;

    @Autowired
    private AdmUnitAudJPARepository admUnitAudJPARepository;

    @Autowired
    private AdmUnitMapper admUnitMapper;

    /**
     * Resolves an administrative unit record by its unique key, filtering out soft-deleted data.
     *
     * @param id unique sequence row identifier tracking the asset
     * @return the mapped domain {@link AdmUnit} instance, or {@code null} if missing or soft-deleted
     */
    @Override
    public AdmUnit findById(Long id) {
        return admUnitJPARepository.findById(id)
                .filter(f -> f.getDeletedAt() == null)
                .map(admUnitMapper::toModel)
                .orElse(null);
    }

    /**
     * Fetches all active administrative units from the database and maps them to domain models.
     *
     * @param pageable pagination and sorting parameters
     * @return a page containing the mapped domain objects
     */
    @Override
    public Page<AdmUnit> findAll(Pageable pageable) {
        return admUnitJPARepository.findAllActive(pageable).map(admUnitMapper::toModel);
    }

    /**
     * Delegates uniqueness validation based on name attributes to the active JPA layer.
     *
     * @param name target descriptive name text to verify
     * @return {@code true} if a match exists, {@code false} otherwise
     */
    @Override
    public boolean existsByNameAndDeletedAtIsNull(String name) {
        return admUnitJPARepository.existsByNameAndDeletedAtIsNull(name);
    }

    /**
     * Delegates update-specific name conflict validation queries to the underlying JPA layer.
     *
     * @param name target descriptive name text to verify
     * @param id   the primary identifier sequence key to exclude
     * @return {@code true} if a naming conflict occurs, {@code false} otherwise
     */
    @Override
    public boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id) {
        return admUnitJPARepository.existsByNameAndIdNotAndDeletedAtIsNull(name, id);
    }

    /**
     * Delegates corporate uniqueness validation parameters checking code rows onto underlying database drivers.
     *
     * @param code target identification tracking alphanumeric value
     * @return {@code true} if a match exists, {@code false} otherwise
     */
    @Override
    public boolean existsByCodeAndDeletedAtIsNull(String code) {
        return admUnitJPARepository.existsByCodeAndDeletedAtIsNull(code);
    }

    /**
     * Delegates update-specific code conflict validation queries to the underlying JPA layer.
     *
     * @param code target identification tracking alphanumeric value
     * @param id   the primary identifier sequence key to exclude
     * @return {@code true} if a code conflict occurs, {@code false} otherwise
     */
    @Override
    public boolean existsByCodeAndIdNotAndDeletedAtIsNull(String code, Long id) {
        return admUnitJPARepository.existsByCodeAndIdNotAndDeletedAtIsNull(code, id);
    }

    /**
     * Maps a transient domain model, persists it into operational relational systems,
     * registers historical track entries, and returns a business context data snapshot.
     *
     * @param admUnit the data schema model profile values containing registration variables
     * @return persistent domain profile containing data markers
     */
    @Override
    public AdmUnit create(AdmUnit admUnit) {
        log.info("Repository: Persisting new administrative unit entity into database");
        AdmUnitEntity entity = admUnitMapper.toEntity(admUnit);

        entity = admUnitJPARepository.save(entity);

        saveAuditRecord(entity, "INSERT");

        return admUnitMapper.toModel(entity);
    }

    /**
     * Maps updating metrics configurations onto relational records, commits changes to active data rows,
     * registers historical records, and forwards mapped outcomes.
     *
     * @param admUnit property modifications schema containing target update variables data
     * @param id      persistent identity identifier index tracking state records
     * @return updated model domain properties specifications variables
     */
    @Override
    public AdmUnit update(AdmUnit admUnit, Long id) {
        log.info("Repository: Merging changes into existing administrative unit record for ID: {}", id);
        AdmUnitEntity entity = admUnitMapper.toEntity(admUnit);
        entity.setId(id);

        entity = admUnitJPARepository.save(entity);

        saveAuditRecord(entity, "UPDATE");

        return admUnitMapper.toModel(entity);
    }

    /**
     * Performs a soft logical deactivation by rewriting active tracking entities,
     * flags record timestamps, and processes transactional logging.
     *
     * @param admUnit target domain representation configuration modeling details to remove
     */
    @Override
    public void delete(AdmUnit admUnit) {
        log.info("Repository: Soft deleting administrative unit entity with ID: {}", admUnit.getId());
        AdmUnitEntity entity = admUnitMapper.toEntity(admUnit);
        entity.setId(admUnit.getId());

        entity = admUnitJPARepository.save(entity);

        saveAuditRecord(entity, "DELETE");
    }

    /**
     * Compiles point-in-time snapshot mirror values from active business properties entities,
     * appends session authentication context tokens data, and writes audit trails into systemic indices.
     *
     * @param entity the currently tracked persistent data state row entity map representation
     * @param action systemic string token descriptor identifying database mutation contexts (e.g., INSERT, UPDATE, DELETE)
     */
    private void saveAuditRecord(AdmUnitEntity entity, String action) {
        AdmUnitAudEntity aud = new AdmUnitAudEntity();

        aud.setAdmUnitId(entity.getId());
        aud.setCode(entity.getCode());
        aud.setName(entity.getName());

        aud.setCreatedAt(entity.getCreatedAt());
        aud.setCreatedBy(entity.getCreatedBy());
        aud.setUpdatedAt(entity.getUpdatedAt());
        aud.setUpdatedBy(entity.getUpdatedBy());
        aud.setDeletedAt(entity.getDeletedAt());
        aud.setDeletedBy(entity.getDeletedBy());

        aud.setAudAction(action);
        aud.setAuditDate(LocalDateTime.now());

        OidcUserInfo currentUser = (OidcUserInfo) SecurityUtils.getCurrentUser();
        if (currentUser != null && currentUser.getClaims().get("preferred_username") != null) {
            aud.setAuditUser((String) currentUser.getClaims().get("preferred_username"));
        } else {
            aud.setAuditUser("SYSTEM");
        }

        admUnitAudJPARepository.save(aud);
    }
}
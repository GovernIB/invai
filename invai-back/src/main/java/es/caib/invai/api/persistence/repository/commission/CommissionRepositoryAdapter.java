package es.caib.invai.api.persistence.repository.commission;

import es.caib.invai.api.service.model.Commission;
import es.caib.invai.api.persistence.model.CommissionEntity;
import es.caib.invai.api.persistence.model.CommissionAudEntity;
import es.caib.invai.api.service.mapper.CommissionMapper;
import es.caib.invai.api.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Infrastructure repository Adapter implementing the outbound port boundary {@link CommissionRepository}.
 * <p>
 * Orchestrates technical structural operations, routing operations across active database layers
 * ({@link CommissionJPARepository} and {@link CommissionAudJPARepository}) while utilizing
 * mapping layers to enforce decoupling rules.
 * </p>
 *
 * @since 1.0.0
 */
@Repository
@Slf4j
public class CommissionRepositoryAdapter implements CommissionRepository {

    @Autowired
    private CommissionJPARepository commissionJPARepository;

    @Autowired
    private CommissionAudJPARepository commissionAudJPARepository;

    @Autowired
    private CommissionMapper commissionMapper;

    /**
     * Resolves a commission record by its unique key, filtering out soft-deleted data traces.
     *
     * @param id unique sequence row identifier tracking the asset
     * @return the mapped domain {@link Commission} instance, or {@code null} if missing or soft-deleted
     */
    @Override
    public Commission findById(Long id) {
        return commissionJPARepository.findById(id)
                .filter(f -> f.getDeletedAt() == null)
                .map(commissionMapper::toModel)
                .orElse(null);
    }

    /**
     * Fetches all active operational commissions from the database and maps them to domain models.
     *
     * @param pageable pagination and sorting parameters
     * @return a page containing the mapped domain objects
     */
    @Override
    public Page<Commission> findAll(Pageable pageable) {
        return commissionJPARepository.findAllActive(pageable).map(commissionMapper::toModel);
    }

    /**
     * Delegates uniqueness validation based on commission descriptors to the active JPA layer.
     *
     * @param name target descriptive name text to verify
     * @return {@code true} if a match exists, {@code false} otherwise
     */
    @Override
    public boolean existsByNameAndDeletedAtIsNull(String name) {
        return commissionJPARepository.existsByNameAndDeletedAtIsNull(name);
    }

    /**
     * Delegates update-specific label conflict validation queries to the underlying JPA layer.
     *
     * @param name target descriptive name text to verify
     * @param id   the primary identifier sequence key to exclude
     * @return {@code true} if a duplicate collision occurs, {@code false} otherwise
     */
    @Override
    public boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, Long id) {
        return commissionJPARepository.existsByNameAndIdNotAndDeletedAtIsNull(name, id);
    }

    /**
     * Maps a transient domain commission object, persists it into relational systems,
     * registers historical snapshots, and returns a business context data schema model.
     *
     * @param commission the data schema model profile values containing registration variables
     * @return persistent domain profile containing data markers
     */
    @Override
    public Commission create(Commission commission) {
        log.info("Repository: Persisting new commission entity into database");
        CommissionEntity entity = commissionMapper.toEntity(commission);

        entity = commissionJPARepository.save(entity);

        saveAuditRecord(entity, "INSERT");

        return commissionMapper.toModel(entity);
    }

    /**
     * Maps updated commission configurations onto relational records, commits changes to active data rows,
     * appends audit trails, and returns fresh outcomes.
     *
     * @param commission property modifications schema containing target update variables data
     * @param id         persistent identity identifier index tracking state records
     * @return updated model domain properties specifications variables
     */
    @Override
    public Commission update(Commission commission, Long id) {
        log.info("Repository: Merging changes into existing commission record for ID: {}", id);
        CommissionEntity entity = commissionMapper.toEntity(commission);
        entity.setId(id);

        entity = commissionJPARepository.save(entity);

        saveAuditRecord(entity, "UPDATE");

        return commissionMapper.toModel(entity);
    }

    /**
     * Performs a soft logical deactivation by rewriting active commission entities,
     * flagging timeline delete records, and saving tracking logs.
     *
     * @param commission target domain representation configuration modeling details to remove
     */
    @Override
    public void delete(Commission commission) {
        log.info("Repository: Soft deleting commission entity with ID: {}", commission.getId());
        CommissionEntity entity = commissionMapper.toEntity(commission);
        entity.setId(commission.getId());

        entity = commissionJPARepository.save(entity);

        saveAuditRecord(entity, "DELETE");
    }

    /**
     * Compiles point-in-time snapshot mirror values from active commission entries,
     * resolves identity user info claims tokens, and saves historic compliance tracks.
     *
     * @param entity the currently tracked persistent data state row entity map representation
     * @param action systemic string token descriptor identifying database mutation contexts (e.g., INSERT, UPDATE, DELETE)
     */
    private void saveAuditRecord(CommissionEntity entity, String action) {
        CommissionAudEntity aud = new CommissionAudEntity();

        aud.setCommissionId(entity.getId());
        aud.setName(entity.getName());
        aud.setNameEs(entity.getNameEs());

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

        commissionAudJPARepository.save(aud);
    }
}
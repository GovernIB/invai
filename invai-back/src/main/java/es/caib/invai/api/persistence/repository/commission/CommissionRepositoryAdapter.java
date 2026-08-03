package es.caib.invai.api.persistence.repository.commission;

import es.caib.invai.api.service.model.Commission;
import es.caib.invai.api.persistence.model.CommissionEntity;
import es.caib.invai.api.persistence.model.CommissionAudEntity;
import es.caib.invai.api.service.mapper.CommissionMapper;
import es.caib.invai.api.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Infrastructure repository Adapter implementing the outbound port boundary {@link CommissionRepository}.
 * <p>
 * Orchestrates technical structural operations, routing operations across active database layers
 * ({@link CommissionJPARepository} and {@link CommissionAudJPARepository}) while utilizing
 * mapping layers to enforce decoupling rules and documenting audit trail deltas.
 * </p>
 *
 * @author invai-team
 * @since 1.0.1
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
    public Page<Commission> findAll(CommissionCriteria criteria, Pageable pageable) {
        log.info("Repository: Fetching paged applications using isolated Specification component");
        try {
            Specification<CommissionEntity> spec = CommissionSpecification.filterByCriteria(criteria);

            return commissionJPARepository.findAll(spec,pageable).map(commissionMapper::toModel);
        } catch (DataAccessException e) {
            log.error("Repository error: Paged application collection fetch exception applied under dynamic filters", e);
            throw e;
        }
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
     * Evaluates active tracking metrics to check for unique expedient dossier tracking code collisions.
     *
     * @param expedientNumber unique tracking dossier string reference
     * @return {@code true} if an active entity matching the tracker exists, {@code false} otherwise
     */
    @Override
    public boolean existsByExpedientNumberAndDeletedAtIsNull(String expedientNumber) {
        return commissionJPARepository.existsByExpedientNumberAndDeletedAtIsNull(expedientNumber);
    }

    /**
     * Verifies if alternative active entities share a requested corporate dossier reference tracking code.
     *
     * @param expedientNumber unique tracking dossier string reference
     * @param id              row entry identification key sequence to exclude from the lookup evaluations
     * @return {@code true} if a duplicate collision is discovered outside the index domain, {@code false} otherwise
     */
    @Override
    public boolean existsByExpedientNumberAndIdNotAndDeletedAtIsNull(String expedientNumber, Long id) {
        return commissionJPARepository.existsByExpedientNumberAndIdNotAndDeletedAtIsNull(expedientNumber, id);
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
     * serializes the updated schema refactoring fields, resolves identity user tokens,
     * and saves historic compliance tracks.
     *
     * @param entity the currently tracked persistent data state row entity map representation
     * @param action systemic string token descriptor identifying database mutation contexts
     */
    private void saveAuditRecord(CommissionEntity entity, String action) {
        CommissionAudEntity aud = new CommissionAudEntity();

        aud.setCommissionId(entity.getId());
        aud.setName(entity.getName());
        aud.setNameEs(entity.getNameEs());

        aud.setExpedientNumber(entity.getExpedientNumber());
        aud.setApprovalDate(entity.getApprovalDate());
        aud.setCommissionType(entity.getCommissionType() != null ? entity.getCommissionType().name() : null);

        aud.setCreatedAt(entity.getCreatedAt() != null ? entity.getCreatedAt() : LocalDateTime.now());
        aud.setCreatedBy(entity.getCreatedBy() != null ? entity.getCreatedBy() : Utils.resolveCurrentUsername());
       
                    aud.setUpdatedAt(entity.getUpdatedAt() != null ? entity.getUpdatedAt() : LocalDateTime.now());
            aud.setUpdatedBy(entity.getUpdatedBy() != null ? entity.getUpdatedBy() : Utils.resolveCurrentUsername());
        aud.setDeletedAt(entity.getDeletedAt());
        aud.setDeletedBy(entity.getDeletedBy());

        aud.setAudAction(action);
        aud.setAuditDate(LocalDateTime.now());
        aud.setAuditUser(Utils.resolveCurrentUsername());

        commissionAudJPARepository.save(aud);
    }
}
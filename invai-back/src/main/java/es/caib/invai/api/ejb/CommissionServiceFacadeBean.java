package es.caib.invai.api.ejb;

import es.caib.invai.api.interna.maintenance.commission.DTO.CommissionInputDTO;
import es.caib.invai.api.interna.maintenance.commission.DTO.CommissionOutputDTO;
import es.caib.invai.api.persistence.repository.commission.CommissionCriteria;
import es.caib.invai.api.service.mapper.CommissionMapper;
import es.caib.invai.api.persistence.repository.commission.CommissionRepository;
import es.caib.invai.api.persistence.repository.application.core.ApplicationRepository;
import es.caib.invai.api.service.facade.CommissionService;
import es.caib.invai.api.service.model.Commission;
import es.caib.invai.api.exception.BusinessRuleException;
import es.caib.invai.api.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static es.caib.invai.api.utils.Constants.*;

/**
 * Facade service implementation for managing Commission domain modules.
 * Ensures data validation workflow boundaries are maintained before executing database transactions.
 * <p>
 * Evaluates both descriptive naming metrics and unique dossier numbers to guarantee
 * system data integrity constraints prior to persistence.
 * </p>
 *
 * @author invai-team
 * @since 1.0.1
 */
@Service
@Slf4j
@Transactional
public class CommissionServiceFacadeBean implements CommissionService {

    @Autowired
    private CommissionMapper commissionMapper;

    @Autowired
    private CommissionRepository commissionRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    /**
     * Computes details of a registered Commission entity by ID.
     *
     * @param id primary unique reference code
     * @return target matching representation mapping schema properties
     * @throws BusinessRuleException if matching entries cannot be verified
     */
    @Override
    @Transactional(readOnly = true)
    public CommissionOutputDTO getById(Long id) {
        log.info("Facade: Fetching commission by id: {}", id);
        Commission commission = commissionRepository.findById(id);
        if (commission == null) {
            throw new BusinessRuleException(ERR_COMMISSION_NOT_FOUND);
        }
        return commissionMapper.toResponse(commission);
    }

    /**
     * Streams continuous page blocks across available active entries.
     *
     * @param pageable parameters regulating boundaries criteria
     * @return formatted data layout structures matching target responses
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CommissionOutputDTO> getAll(CommissionCriteria criteria, Pageable pageable) {
        log.info("Facade: Fetching paged commissions");
        return commissionRepository.findAll(criteria, pageable).map(commissionMapper::toResponse);
    }

    /**
     * Registers a new commission mapping definition onto persistent databases.
     * Verifies that neither the descriptive name nor the expedient dossier number collides with an active asset.
     *
     * @param inputDTO parameters framing item details
     * @return detailed system properties outputs
     * @throws BusinessRuleException if name descriptors or expedient tracking IDs duplicate active structures
     */
    @Override
    public CommissionOutputDTO create(CommissionInputDTO inputDTO) {
        log.info("Facade: Creating commission with name: {} and expedient: {}", inputDTO.getName(), inputDTO.getExpedientNumber());

        if (commissionRepository.existsByNameAndDeletedAtIsNull(inputDTO.getName())) {
            throw new BusinessRuleException(ERR_COMMISSION_DUPLICATED);
        }
        if (commissionRepository.existsByExpedientNumberAndDeletedAtIsNull(inputDTO.getExpedientNumber())) {
            throw new BusinessRuleException(ERR_COMMISSION_DUPLICATED);
        }

        Commission model = commissionMapper.toModelFromInput(inputDTO);
        Commission savedModel = commissionRepository.create(model);
        return commissionMapper.toResponse(savedModel);
    }

    /**
     * Alters active configuration parameters on tracked commission rows.
     * Ensures update variables do not cause collisions on unique tracking indexes.
     *
     * @param id       target identifier pointing to database objects
     * @param inputDTO parameter payloads describing changes to commit
     * @return current configuration status layout representations
     * @throws BusinessRuleException if records are not found or conflict with tracking parameters of alternative rows
     */
    @Override
    public CommissionOutputDTO update(Long id, CommissionInputDTO inputDTO) {
        log.info("Facade: Updating commission ID: {}", id);

        Commission existing = commissionRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException(ERR_COMMISSION_NOT_FOUND);
        }

        if (commissionRepository.existsByNameAndIdNotAndDeletedAtIsNull(inputDTO.getName(), id)) {
            throw new BusinessRuleException(ERR_COMMISSION_DUPLICATED);
        }
        if (commissionRepository.existsByExpedientNumberAndIdNotAndDeletedAtIsNull(inputDTO.getExpedientNumber(), id)) {
            throw new BusinessRuleException(ERR_COMMISSION_DUPLICATED);
        }

        commissionMapper.updateModelFromInput(inputDTO, existing);
        Commission updatedModel = commissionRepository.update(existing, id);
        return commissionMapper.toResponse(updatedModel);
    }

    /**
     * Logs explicit soft tracking markers declaring items functionally deprecated.
     * Evaluates explicit relational integrity constraints across external domain contexts via the
     * application port boundary before allowing logical soft deactivation workflows to proceed.
     *
     * @param id data identity target
     * @throws BusinessRuleException if required references are missing or target profile remains actively bound to application assets
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Executing logical delete for commission ID: {}", id);
        Commission existing = commissionRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException(ERR_COMMISSION_NOT_FOUND);
        }

        if (applicationRepository.existsByCommissionId(id)) {
            throw new BusinessRuleException(ERR_COMMISSION_DELETE_HAS_DEPENDENCIES);
        }

        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy(Utils.resolveCurrentUsername());

        commissionRepository.delete(existing);
    }

    /**
     * Reactivates a logically soft-deleted commission record back to active state.
     *
     * @param id the target identifier mapping the commission instance intended for reactivation
     * @return the reactivated domain representation mapped into an {@link CommissionOutputDTO}
     * @throws BusinessRuleException if a matching commission cannot be found or is already active
     */
    @Override
    public CommissionOutputDTO reactivate(Long id) {
        log.info("Facade: Reactivating commission ID: {}", id);
        Commission existing = commissionRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(ERR_COMMISSION_NOT_FOUND);
        }

        if (existing.getDeletedAt() == null) {
            throw new BusinessRuleException(ERR_COMMISSION_ACTIVE);
        }

        existing.setDeletedAt(null);
        existing.setDeletedBy(null);

        Commission updatedModel = commissionRepository.update(existing, id);
        return commissionMapper.toResponse(updatedModel);
    }
}
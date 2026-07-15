package es.caib.invai.api.ejb;

import es.caib.invai.api.interna.commission.DTO.CommissionInputDTO;
import es.caib.invai.api.interna.commission.DTO.CommissionOutputDTO;
import es.caib.invai.api.utils.SecurityUtils;
import es.caib.invai.api.service.mapper.CommissionMapper;
import es.caib.invai.api.persistence.repository.commission.CommissionRepository;
import es.caib.invai.api.service.facade.CommissionService;
import es.caib.invai.api.service.model.Commission;
import es.caib.invai.api.exception.BusinessRuleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Facade service implementation for managing Commission domain modules.
 * Ensures data validation workflow boundaries are maintained before executing database transactions.
 *
 * @since 1.0.0
 */
@Service
@Slf4j
@Transactional
public class CommissionServiceFacadeBean implements CommissionService {

    @Autowired
    private CommissionMapper commissionMapper;

    @Autowired
    private CommissionRepository commissionRepository;

    /**
     * Fetches details of a registered Commission entity by ID.
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
            throw new BusinessRuleException("exception.commission.notfound");
        }
        return commissionMapper.toResponse(commission);
    }

    /**
     * Streams continuous page blocks across available entries.
     *
     * @param pageable parameters regulating boundaries criteria
     * @return formatted data layout structures matching target responses
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CommissionOutputDTO> getAll(Pageable pageable) {
        log.info("Facade: Fetching paged commissions");
        return commissionRepository.findAll(pageable).map(commissionMapper::toResponse);
    }

    /**
     * Registers a new commission mapping definition onto persistent databases.
     *
     * @param inputDTO parameters framing item details
     * @return detailed system properties outputs
     * @throws BusinessRuleException if name descriptors duplicate active structures
     */
    @Override
    public CommissionOutputDTO create(CommissionInputDTO inputDTO) {
        log.info("Facade: Creating commission with name: {}", inputDTO.getName());

        if (commissionRepository.existsByNameAndDeletedAtIsNull(inputDTO.getName())) {
            throw new BusinessRuleException("exception.commission.duplicated");
        }

        Commission model = commissionMapper.toModelFromInput(inputDTO);
        Commission savedModel = commissionRepository.create(model);
        return commissionMapper.toResponse(savedModel);
    }

    /**
     * Alters active configuration parameters on tracked commission rows.
     *
     * @param id       target identifier pointing to database objects
     * @param inputDTO parameter payloads describing changes to commit
     * @return current configuration status layout representations
     * @throws BusinessRuleException if records are not found or conflict with name parameters of other rows
     */
    @Override
    public CommissionOutputDTO update(Long id, CommissionInputDTO inputDTO) {
        log.info("Facade: Updating commission ID: {}", id);

        Commission existing = commissionRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException("exception.commission.notfound");
        }

        if (commissionRepository.existsByNameAndIdNotAndDeletedAtIsNull(inputDTO.getName(), id)) {
            throw new BusinessRuleException("exception.commission.duplicated");
        }

        commissionMapper.updateModelFromInput(inputDTO, existing);
        Commission updatedModel = commissionRepository.update(existing, id);
        return commissionMapper.toResponse(updatedModel);
    }

    /**
     * Logs explicit soft tracking markers declaring items functionally deprecated.
     *
     * @param id data identity target
     * @throws BusinessRuleException if required references are missing
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Executing logical delete for commission ID: {}", id);
        Commission existing = commissionRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException("exception.commission.notfound");
        }

        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy("SYSTEM");

        OidcUserInfo currentUser = (OidcUserInfo) SecurityUtils.getCurrentUser();

        if (currentUser != null && currentUser.getClaims().get("preferred_username") != null) {
            existing.setDeletedBy((String) currentUser.getClaims().get("preferred_username"));
        }

        commissionRepository.delete(existing);
    }
}
package es.caib.invai.back.ejb.maintenance.security.ensRequirement;

import es.caib.invai.back.interna.maintenance.security.ensRequirement.DTO.EnsRequirementInputDTO;
import es.caib.invai.back.interna.maintenance.security.ensRequirement.DTO.EnsRequirementOutputDTO;
import es.caib.invai.back.service.mapper.maintenance.security.ensRequirement.EnsRequirementMapper;
import es.caib.invai.back.persistence.repository.maintenance.security.ensRequirement.EnsRequirementCriteria;
import es.caib.invai.back.persistence.repository.maintenance.security.ensRequirement.EnsRequirementRepository;
import es.caib.invai.back.service.facade.maintenance.security.ensRequirement.EnsRequirementService;
import es.caib.invai.back.service.model.maintenance.security.ensRequirement.EnsRequirement;
import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.utils.Constants;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Facade service implementation for administrative EnsRequirements.
 *
 * @since 1.0.4
 */
@Service
@Slf4j
@Transactional
public class EnsRequirementServiceFacadeBean implements EnsRequirementService {

    /** Mapper used to convert between EnsRequirement domain models, entities and DTOs. */
    @Autowired
    private EnsRequirementMapper ensRequirementMapper;

    /** Repository port used to access EnsRequirement persistence operations. */
    @Autowired
    private EnsRequirementRepository ensRequirementRepository;

    /**
     * Retrieves an ENS requirement entry by its identifier.
     *
     * @param id the ENS requirement entry identifier
     * @return the matching ENS requirement entry as an output DTO
     * @throws BusinessRuleException if no ENS requirement entry exists with the given ID
     */
    @Override
    @Transactional(readOnly = true)
    public EnsRequirementOutputDTO getById(Long id) {
        log.debug("Facade: Fetching ENS requirement entry by ID: {}", id);
        EnsRequirement ensRequirement = ensRequirementRepository.findById(id);

        if (ensRequirement == null) {
            throw new BusinessRuleException(Constants.ERR_ENSREQUIREMENT_NOT_FOUND);
        }

        return ensRequirementMapper.toResponse(ensRequirement);
    }

    /**
     * Retrieves a paginated list of ENS requirement entries matching the given filter criteria.
     *
     * @param filter search criteria used to narrow down results
     * @param pageable pagination and sorting instructions
     * @return a page of matching ENS requirement entries as output DTOs
     */
    @Override
    @Transactional(readOnly = true)
    public Page<EnsRequirementOutputDTO> getAll(EnsRequirementCriteria filter, Pageable pageable) {
        log.debug("Facade: Fetching ENS requirement entries via pagination boundaries");
        Page<EnsRequirement> domainPage = ensRequirementRepository.findAll(filter, pageable);
        return domainPage.map(ensRequirementMapper::toResponse);
    }

    /**
     * Creates a new ENS requirement entry after sanitizing the input and checking for name duplicates.
     *
     * @param inputDTO the data for the new ENS requirement entry
     * @return the created ENS requirement entry as an output DTO
     * @throws BusinessRuleException if an active ENS requirement entry with the same name already exists
     */
    @Override
    public EnsRequirementOutputDTO create(EnsRequirementInputDTO inputDTO) {
        log.info("Facade: Creating new ENS requirement entry record with name: {}", inputDTO.getName());

        Utils.sanitize(inputDTO);

        if (ensRequirementRepository.existsByNameAndDeletedAtIsNull(inputDTO.getName())) {
            throw new BusinessRuleException(Constants.ERR_ENSREQUIREMENT_DUPLICATED);
        }

        EnsRequirement model = ensRequirementMapper.toModelFromInput(inputDTO);

        EnsRequirement savedModel = ensRequirementRepository.create(model);
        return ensRequirementMapper.toResponse(savedModel);
    }

    /**
     * Updates an existing ENS requirement entry, validating existence and name uniqueness.
     *
     * @param id the identifier of the ENS requirement entry to update
     * @param inputDTO the new data to apply
     * @return the updated ENS requirement entry as an output DTO
     * @throws BusinessRuleException if the ENS requirement entry does not exist or the name is already used by another record
     */
    @Override
    public EnsRequirementOutputDTO update(Long id, EnsRequirementInputDTO inputDTO) {
        log.info("Facade: Updating ENS requirement entry with ID: {}", id);

        EnsRequirement existing = ensRequirementRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_ENSREQUIREMENT_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        if (ensRequirementRepository.existsByNameAndIdNotAndDeletedAtIsNull(inputDTO.getName(), id)) {
            throw new BusinessRuleException(Constants.ERR_ENSREQUIREMENT_DUPLICATED);
        }

        ensRequirementMapper.updateModelFromInput(inputDTO, existing);
        return ensRequirementMapper.toResponse(ensRequirementRepository.update(existing, id));
    }

    /**
     * Logically deletes an ENS requirement entry by stamping its deletion timestamp and author.
     *
     * @param id the identifier of the ENS requirement entry to delete
     * @throws BusinessRuleException if the ENS requirement entry does not exist or is already deleted
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Logically deleting ENS requirement entry with ID: {}", id);
        EnsRequirement existing = ensRequirementRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_ENSREQUIREMENT_NOT_FOUND);
        }

        if (existing.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_ENSREQUIREMENT_NOT_ACTIVE);
        }

        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy(Utils.resolveCurrentUsername());

        ensRequirementRepository.delete(existing);
    }

    /**
     * Reactivates a previously deleted ENS requirement entry by clearing its deletion timestamp and author.
     *
     * @param id the identifier of the ENS requirement entry to reactivate
     * @return the reactivated ENS requirement entry as an output DTO
     * @throws BusinessRuleException if the ENS requirement entry does not exist or is already active
     */
    @Override
    public EnsRequirementOutputDTO reactivate(Long id) {
        log.info("Facade: Reactivating ENS requirement entry with ID: {}", id);
        EnsRequirement existing = ensRequirementRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_ENSREQUIREMENT_NOT_FOUND);
        }

        if (existing.getDeletedAt() == null) {
            throw new BusinessRuleException(Constants.ERR_ENSREQUIREMENT_ACTIVE);
        }

        existing.setDeletedAt(null);
        existing.setDeletedBy(null);

        EnsRequirement updatedModel = ensRequirementRepository.update(existing, id);
        return ensRequirementMapper.toResponse(updatedModel);
    }
}

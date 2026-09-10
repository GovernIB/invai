package es.caib.invai.back.ejb.maintenance.complianceSituation;

import es.caib.invai.back.interna.maintenance.complianceSituation.DTO.ComplianceSituationInputDTO;
import es.caib.invai.back.interna.maintenance.complianceSituation.DTO.ComplianceSituationOutputDTO;
import es.caib.invai.back.service.mapper.maintenance.complianceSituation.ComplianceSituationMapper;
import es.caib.invai.back.persistence.repository.maintenance.complianceSituation.ComplianceSituationCriteria;
import es.caib.invai.back.persistence.repository.maintenance.complianceSituation.ComplianceSituationRepository;
import es.caib.invai.back.service.facade.maintenance.complianceSituation.ComplianceSituationService;
import es.caib.invai.back.service.model.maintenance.complianceSituation.ComplianceSituation;
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
 * Facade service implementation for administrative ComplianceSituations.
 *
 * @since 1.0.4
 */
@Service
@Slf4j
@Transactional
public class ComplianceSituationServiceFacadeBean implements ComplianceSituationService {

    /** Mapper used to convert between ComplianceSituation domain models, entities and DTOs. */
    @Autowired
    private ComplianceSituationMapper complianceSituationMapper;

    /** Repository port used to access ComplianceSituation persistence operations. */
    @Autowired
    private ComplianceSituationRepository complianceSituationRepository;

    /**
     * Retrieves a compliance situation entry by its identifier.
     *
     * @param id the compliance situation entry identifier
     * @return the matching compliance situation entry as an output DTO
     * @throws BusinessRuleException if no compliance situation entry exists with the given ID
     */
    @Override
    @Transactional(readOnly = true)
    public ComplianceSituationOutputDTO getById(Long id) {
        log.debug("Facade: Fetching compliance situation entry by ID: {}", id);
        ComplianceSituation complianceSituation = complianceSituationRepository.findById(id);

        if (complianceSituation == null) {
            throw new BusinessRuleException(Constants.ERR_COMPLIANCESITUATION_NOT_FOUND);
        }

        return complianceSituationMapper.toResponse(complianceSituation);
    }

    /**
     * Retrieves a paginated list of compliance situation entries matching the given filter criteria.
     *
     * @param filter search criteria used to narrow down results
     * @param pageable pagination and sorting instructions
     * @return a page of matching compliance situation entries as output DTOs
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ComplianceSituationOutputDTO> getAll(ComplianceSituationCriteria filter, Pageable pageable) {
        log.debug("Facade: Fetching compliance situation entries via pagination boundaries");
        Page<ComplianceSituation> domainPage = complianceSituationRepository.findAll(filter, pageable);
        return domainPage.map(complianceSituationMapper::toResponse);
    }

    /**
     * Creates a new compliance situation entry after sanitizing the input and checking for name duplicates.
     *
     * @param inputDTO the data for the new compliance situation entry
     * @return the created compliance situation entry as an output DTO
     * @throws BusinessRuleException if an active compliance situation entry with the same name already exists
     */
    @Override
    public ComplianceSituationOutputDTO create(ComplianceSituationInputDTO inputDTO) {
        log.info("Facade: Creating new compliance situation entry record with name: {}", inputDTO.getName());

        Utils.sanitize(inputDTO);

        if (complianceSituationRepository.existsByNameAndDeletedAtIsNull(inputDTO.getName())) {
            throw new BusinessRuleException(Constants.ERR_COMPLIANCESITUATION_DUPLICATED);
        }

        ComplianceSituation model = complianceSituationMapper.toModelFromInput(inputDTO);

        ComplianceSituation savedModel = complianceSituationRepository.create(model);
        return complianceSituationMapper.toResponse(savedModel);
    }

    /**
     * Updates an existing compliance situation entry, validating existence and name uniqueness.
     *
     * @param id the identifier of the compliance situation entry to update
     * @param inputDTO the new data to apply
     * @return the updated compliance situation entry as an output DTO
     * @throws BusinessRuleException if the compliance situation entry does not exist or the name is already used by another record
     */
    @Override
    public ComplianceSituationOutputDTO update(Long id, ComplianceSituationInputDTO inputDTO) {
        log.info("Facade: Updating compliance situation entry with ID: {}", id);

        ComplianceSituation existing = complianceSituationRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_COMPLIANCESITUATION_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        if (complianceSituationRepository.existsByNameAndIdNotAndDeletedAtIsNull(inputDTO.getName(), id)) {
            throw new BusinessRuleException(Constants.ERR_COMPLIANCESITUATION_DUPLICATED);
        }

        complianceSituationMapper.updateModelFromInput(inputDTO, existing);
        return complianceSituationMapper.toResponse(complianceSituationRepository.update(existing, id));
    }

    /**
     * Logically deletes a compliance situation entry by stamping its deletion timestamp and author.
     *
     * @param id the identifier of the compliance situation entry to delete
     * @throws BusinessRuleException if the compliance situation entry does not exist or is already deleted
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Logically deleting compliance situation entry with ID: {}", id);
        ComplianceSituation existing = complianceSituationRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_COMPLIANCESITUATION_NOT_FOUND);
        }

        if (existing.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_COMPLIANCESITUATION_NOT_ACTIVE);
        }

        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy(Utils.resolveCurrentUsername());

        complianceSituationRepository.delete(existing);
    }

    /**
     * Reactivates a previously deleted compliance situation entry by clearing its deletion timestamp and author.
     *
     * @param id the identifier of the compliance situation entry to reactivate
     * @return the reactivated compliance situation entry as an output DTO
     * @throws BusinessRuleException if the compliance situation entry does not exist or is already active
     */
    @Override
    public ComplianceSituationOutputDTO reactivate(Long id) {
        log.info("Facade: Reactivating compliance situation entry with ID: {}", id);
        ComplianceSituation existing = complianceSituationRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_COMPLIANCESITUATION_NOT_FOUND);
        }

        if (existing.getDeletedAt() == null) {
            throw new BusinessRuleException(Constants.ERR_COMPLIANCESITUATION_ACTIVE);
        }

        existing.setDeletedAt(null);
        existing.setDeletedBy(null);

        ComplianceSituation updatedModel = complianceSituationRepository.update(existing, id);
        return complianceSituationMapper.toResponse(updatedModel);
    }
}

package es.caib.invai.back.ejb.maintenance.security.personalDataProcessing;

import es.caib.invai.back.interna.maintenance.security.personalDataProcessing.DTO.PersonalDataProcessingInputDTO;
import es.caib.invai.back.interna.maintenance.security.personalDataProcessing.DTO.PersonalDataProcessingOutputDTO;
import es.caib.invai.back.service.mapper.maintenance.security.personalDataProcessing.PersonalDataProcessingMapper;
import es.caib.invai.back.persistence.repository.maintenance.security.personalDataProcessing.PersonalDataProcessingCriteria;
import es.caib.invai.back.persistence.repository.maintenance.security.personalDataProcessing.PersonalDataProcessingRepository;
import es.caib.invai.back.service.facade.maintenance.security.personalDataProcessing.PersonalDataProcessingService;
import es.caib.invai.back.service.model.maintenance.security.personalDataProcessing.PersonalDataProcessing;
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
 * Facade service implementation for administrative PersonalDataProcessings.
 *
 * @since 1.0.4
 */
@Service
@Slf4j
@Transactional
public class PersonalDataProcessingServiceFacadeBean implements PersonalDataProcessingService {

    /** Mapper used to convert between PersonalDataProcessing domain models, entities and DTOs. */
    @Autowired
    private PersonalDataProcessingMapper personalDataProcessingMapper;

    /** Repository port used to access PersonalDataProcessing persistence operations. */
    @Autowired
    private PersonalDataProcessingRepository personalDataProcessingRepository;

    /**
     * Retrieves an personal data processing entry by its identifier.
     *
     * @param id the personal data processing entry identifier
     * @return the matching personal data processing entry as an output DTO
     * @throws BusinessRuleException if no personal data processing entry exists with the given ID
     */
    @Override
    @Transactional(readOnly = true)
    public PersonalDataProcessingOutputDTO getById(Long id) {
        log.debug("Facade: Fetching personal data processing entry by ID: {}", id);
        PersonalDataProcessing personalDataProcessing = personalDataProcessingRepository.findById(id);

        if (personalDataProcessing == null) {
            throw new BusinessRuleException(Constants.ERR_PERSONALDATAPROCESSING_NOT_FOUND);
        }

        return personalDataProcessingMapper.toResponse(personalDataProcessing);
    }

    /**
     * Retrieves a paginated list of personal data processing entries matching the given filter criteria.
     *
     * @param filter search criteria used to narrow down results
     * @param pageable pagination and sorting instructions
     * @return a page of matching personal data processing entries as output DTOs
     */
    @Override
    @Transactional(readOnly = true)
    public Page<PersonalDataProcessingOutputDTO> getAll(PersonalDataProcessingCriteria filter, Pageable pageable) {
        log.debug("Facade: Fetching personal data processing entries via pagination boundaries");
        Page<PersonalDataProcessing> domainPage = personalDataProcessingRepository.findAll(filter, pageable);
        return domainPage.map(personalDataProcessingMapper::toResponse);
    }

    /**
     * Creates a new personal data processing entry after sanitizing the input and checking for name duplicates.
     *
     * @param inputDTO the data for the new personal data processing entry
     * @return the created personal data processing entry as an output DTO
     * @throws BusinessRuleException if an active personal data processing entry with the same name already exists
     */
    @Override
    public PersonalDataProcessingOutputDTO create(PersonalDataProcessingInputDTO inputDTO) {
        log.info("Facade: Creating new personal data processing entry record with name: {}", inputDTO.getName());

        Utils.sanitize(inputDTO);

        if (personalDataProcessingRepository.existsByNameAndDeletedAtIsNull(inputDTO.getName())) {
            throw new BusinessRuleException(Constants.ERR_PERSONALDATAPROCESSING_DUPLICATED);
        }

        PersonalDataProcessing model = personalDataProcessingMapper.toModelFromInput(inputDTO);

        PersonalDataProcessing savedModel = personalDataProcessingRepository.create(model);
        return personalDataProcessingMapper.toResponse(savedModel);
    }

    /**
     * Updates an existing personal data processing entry, validating existence and name uniqueness.
     *
     * @param id the identifier of the personal data processing entry to update
     * @param inputDTO the new data to apply
     * @return the updated personal data processing entry as an output DTO
     * @throws BusinessRuleException if the personal data processing entry does not exist or the name is already used by another record
     */
    @Override
    public PersonalDataProcessingOutputDTO update(Long id, PersonalDataProcessingInputDTO inputDTO) {
        log.info("Facade: Updating personal data processing entry with ID: {}", id);

        PersonalDataProcessing existing = personalDataProcessingRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_PERSONALDATAPROCESSING_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        if (personalDataProcessingRepository.existsByNameAndIdNotAndDeletedAtIsNull(inputDTO.getName(), id)) {
            throw new BusinessRuleException(Constants.ERR_PERSONALDATAPROCESSING_DUPLICATED);
        }

        personalDataProcessingMapper.updateModelFromInput(inputDTO, existing);
        return personalDataProcessingMapper.toResponse(personalDataProcessingRepository.update(existing, id));
    }

    /**
     * Logically deletes an personal data processing entry by stamping its deletion timestamp and author.
     *
     * @param id the identifier of the personal data processing entry to delete
     * @throws BusinessRuleException if the personal data processing entry does not exist or is already deleted
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Logically deleting personal data processing entry with ID: {}", id);
        PersonalDataProcessing existing = personalDataProcessingRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_PERSONALDATAPROCESSING_NOT_FOUND);
        }

        if (existing.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_PERSONALDATAPROCESSING_NOT_ACTIVE);
        }

        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy(Utils.resolveCurrentUsername());

        personalDataProcessingRepository.delete(existing);
    }

    /**
     * Reactivates a previously deleted personal data processing entry by clearing its deletion timestamp and author.
     *
     * @param id the identifier of the personal data processing entry to reactivate
     * @return the reactivated personal data processing entry as an output DTO
     * @throws BusinessRuleException if the personal data processing entry does not exist or is already active
     */
    @Override
    public PersonalDataProcessingOutputDTO reactivate(Long id) {
        log.info("Facade: Reactivating personal data processing entry with ID: {}", id);
        PersonalDataProcessing existing = personalDataProcessingRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_PERSONALDATAPROCESSING_NOT_FOUND);
        }

        if (existing.getDeletedAt() == null) {
            throw new BusinessRuleException(Constants.ERR_PERSONALDATAPROCESSING_ACTIVE);
        }

        existing.setDeletedAt(null);
        existing.setDeletedBy(null);

        PersonalDataProcessing updatedModel = personalDataProcessingRepository.update(existing, id);
        return personalDataProcessingMapper.toResponse(updatedModel);
    }
}

package es.caib.invai.back.ejb.maintenance.security.securityMeasureType;

import es.caib.invai.back.interna.maintenance.security.securityMeasureType.DTO.SecurityMeasureTypeInputDTO;
import es.caib.invai.back.interna.maintenance.security.securityMeasureType.DTO.SecurityMeasureTypeOutputDTO;
import es.caib.invai.back.service.mapper.maintenance.security.securityMeasureType.SecurityMeasureTypeMapper;
import es.caib.invai.back.persistence.repository.maintenance.security.securityMeasureType.SecurityMeasureTypeCriteria;
import es.caib.invai.back.persistence.repository.maintenance.security.securityMeasureType.SecurityMeasureTypeRepository;
import es.caib.invai.back.service.facade.maintenance.security.securityMeasureType.SecurityMeasureTypeService;
import es.caib.invai.back.service.model.maintenance.security.securityMeasureType.SecurityMeasureType;
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
 * Facade service implementation for administrative SecurityMeasureTypes.
 *
 * @since 1.0.4
 */
@Service
@Slf4j
@Transactional
public class SecurityMeasureTypeServiceFacadeBean implements SecurityMeasureTypeService {

    /** Mapper used to convert between SecurityMeasureType domain models, entities and DTOs. */
    @Autowired
    private SecurityMeasureTypeMapper securityMeasureTypeMapper;

    /** Repository port used to access SecurityMeasureType persistence operations. */
    @Autowired
    private SecurityMeasureTypeRepository securityMeasureTypeRepository;

    /**
     * Retrieves a security measure type entry by its identifier.
     *
     * @param id the security measure type entry identifier
     * @return the matching security measure type entry as an output DTO
     * @throws BusinessRuleException if no security measure type entry exists with the given ID
     */
    @Override
    @Transactional(readOnly = true)
    public SecurityMeasureTypeOutputDTO getById(Long id) {
        log.debug("Facade: Fetching security measure type entry by ID: {}", id);
        SecurityMeasureType securityMeasureType = securityMeasureTypeRepository.findById(id);

        if (securityMeasureType == null) {
            throw new BusinessRuleException(Constants.ERR_SECURITYMEASURETYPE_NOT_FOUND);
        }

        return securityMeasureTypeMapper.toResponse(securityMeasureType);
    }

    /**
     * Retrieves a paginated list of security measure type entries matching the given filter criteria.
     *
     * @param filter search criteria used to narrow down results
     * @param pageable pagination and sorting instructions
     * @return a page of matching security measure type entries as output DTOs
     */
    @Override
    @Transactional(readOnly = true)
    public Page<SecurityMeasureTypeOutputDTO> getAll(SecurityMeasureTypeCriteria filter, Pageable pageable) {
        log.debug("Facade: Fetching security measure type entries via pagination boundaries");
        Page<SecurityMeasureType> domainPage = securityMeasureTypeRepository.findAll(filter, pageable);
        return domainPage.map(securityMeasureTypeMapper::toResponse);
    }

    /**
     * Creates a new security measure type entry after sanitizing the input and checking for name duplicates.
     *
     * @param inputDTO the data for the new security measure type entry
     * @return the created security measure type entry as an output DTO
     * @throws BusinessRuleException if an active security measure type entry with the same name already exists
     */
    @Override
    public SecurityMeasureTypeOutputDTO create(SecurityMeasureTypeInputDTO inputDTO) {
        log.info("Facade: Creating new security measure type entry record with name: {}", inputDTO.getName());

        Utils.sanitize(inputDTO);

        if (securityMeasureTypeRepository.existsByNameAndDeletedAtIsNull(inputDTO.getName())) {
            throw new BusinessRuleException(Constants.ERR_SECURITYMEASURETYPE_DUPLICATED);
        }

        SecurityMeasureType model = securityMeasureTypeMapper.toModelFromInput(inputDTO);

        SecurityMeasureType savedModel = securityMeasureTypeRepository.create(model);
        return securityMeasureTypeMapper.toResponse(savedModel);
    }

    /**
     * Updates an existing security measure type entry, validating existence and name uniqueness.
     *
     * @param id the identifier of the security measure type entry to update
     * @param inputDTO the new data to apply
     * @return the updated security measure type entry as an output DTO
     * @throws BusinessRuleException if the security measure type entry does not exist or the name is already used by another record
     */
    @Override
    public SecurityMeasureTypeOutputDTO update(Long id, SecurityMeasureTypeInputDTO inputDTO) {
        log.info("Facade: Updating security measure type entry with ID: {}", id);

        SecurityMeasureType existing = securityMeasureTypeRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_SECURITYMEASURETYPE_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        if (securityMeasureTypeRepository.existsByNameAndIdNotAndDeletedAtIsNull(inputDTO.getName(), id)) {
            throw new BusinessRuleException(Constants.ERR_SECURITYMEASURETYPE_DUPLICATED);
        }

        securityMeasureTypeMapper.updateModelFromInput(inputDTO, existing);
        return securityMeasureTypeMapper.toResponse(securityMeasureTypeRepository.update(existing, id));
    }

    /**
     * Logically deletes a security measure type entry by stamping its deletion timestamp and author.
     *
     * @param id the identifier of the security measure type entry to delete
     * @throws BusinessRuleException if the security measure type entry does not exist or is already deleted
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Logically deleting security measure type entry with ID: {}", id);
        SecurityMeasureType existing = securityMeasureTypeRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_SECURITYMEASURETYPE_NOT_FOUND);
        }

        if (existing.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_SECURITYMEASURETYPE_NOT_ACTIVE);
        }

        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy(Utils.resolveCurrentUsername());

        securityMeasureTypeRepository.delete(existing);
    }

    /**
     * Reactivates a previously deleted security measure type entry by clearing its deletion timestamp and author.
     *
     * @param id the identifier of the security measure type entry to reactivate
     * @return the reactivated security measure type entry as an output DTO
     * @throws BusinessRuleException if the security measure type entry does not exist or is already active
     */
    @Override
    public SecurityMeasureTypeOutputDTO reactivate(Long id) {
        log.info("Facade: Reactivating security measure type entry with ID: {}", id);
        SecurityMeasureType existing = securityMeasureTypeRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_SECURITYMEASURETYPE_NOT_FOUND);
        }

        if (existing.getDeletedAt() == null) {
            throw new BusinessRuleException(Constants.ERR_SECURITYMEASURETYPE_ACTIVE);
        }

        existing.setDeletedAt(null);
        existing.setDeletedBy(null);

        SecurityMeasureType updatedModel = securityMeasureTypeRepository.update(existing, id);
        return securityMeasureTypeMapper.toResponse(updatedModel);
    }
}

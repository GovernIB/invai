package es.caib.invai.back.ejb.maintenance.integration.externalSystem;

import es.caib.invai.back.interna.maintenance.integration.externalSystem.DTO.ExternalSystemInputDTO;
import es.caib.invai.back.interna.maintenance.integration.externalSystem.DTO.ExternalSystemOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.integration.externalSystem.ExternalSystemCriteria;
import es.caib.invai.back.persistence.repository.maintenance.integration.externalSystem.ExternalSystemRepository;
import es.caib.invai.back.persistence.repository.maintenance.responsible.company.CompanyRepository;
import es.caib.invai.back.service.facade.maintenance.integration.externalSystem.ExternalSystemService;
import es.caib.invai.back.service.mapper.maintenance.integration.externalSystem.ExternalSystemMapper;
import es.caib.invai.back.service.model.maintenance.integration.externalSystem.ExternalSystem;
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
 * Facade service implementation for administrative External Systems.
 *
 * @since 1.0.5
 */
@Service
@Slf4j
@Transactional
public class ExternalSystemServiceFacadeBean implements ExternalSystemService {

    /** Mapper used to convert between External System domain models, entities, and DTOs. */
    @Autowired
    private ExternalSystemMapper externalSystemMapper;

    /** Repository port used to persist and query External System domain models. */
    @Autowired
    private ExternalSystemRepository externalSystemRepository;

    /** Repository port used to validate the referenced company actually exists. */
    @Autowired
    private CompanyRepository companyRepository;

    /**
     * Retrieves an external system by its identifier.
     *
     * @param id the external system identifier
     * @return the matching external system as a response DTO
     * @throws BusinessRuleException if no external system exists with the given ID
     */
    @Override
    @Transactional(readOnly = true)
    public ExternalSystemOutputDTO getById(Long id) {
        log.debug("Facade: Fetching external system by ID: {}", id);
        ExternalSystem externalSystem = externalSystemRepository.findById(id);

        if (externalSystem == null) {
            throw new BusinessRuleException(Constants.ERR_EXTERNALSYSTEM_NOT_FOUND);
        }

        return externalSystemMapper.toResponse(externalSystem);
    }

    /**
     * Retrieves a paginated list of external systems matching the given filter criteria.
     *
     * @param filter   search and status filter criteria
     * @param pageable pagination and sorting information
     * @return a page of matching external systems as response DTOs
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ExternalSystemOutputDTO> getAll(ExternalSystemCriteria filter, Pageable pageable) {
        log.debug("Facade: Fetching external systems via pagination boundaries");
        Page<ExternalSystem> domainPage = externalSystemRepository.findAll(filter, pageable);
        return domainPage.map(externalSystemMapper::toResponse);
    }

    /**
     * Creates a new external system record after sanitizing the input and checking for name duplicates.
     *
     * @param inputDTO the external system data to create
     * @return the created external system as a response DTO
     * @throws BusinessRuleException if a non-deleted external system with the same name already
     * exists, or {@code companyId} doesn't reference an existing company
     */
    @Override
    public ExternalSystemOutputDTO create(ExternalSystemInputDTO inputDTO) {
        log.info("Facade: Creating new external system record with name: {}", inputDTO.getName());

        Utils.sanitize(inputDTO);

        if (companyRepository.findById(inputDTO.getCompanyId()) == null) {
            throw new BusinessRuleException(Constants.ERR_COMPANY_NOT_FOUND);
        }

        if (externalSystemRepository.existsByNameAndDeletedAtIsNull(inputDTO.getName())) {
            throw new BusinessRuleException(Constants.ERR_EXTERNALSYSTEM_DUPLICATED);
        }

        ExternalSystem model = externalSystemMapper.toModelFromInput(inputDTO);

        ExternalSystem savedModel = externalSystemRepository.create(model);
        return externalSystemMapper.toResponse(savedModel);
    }

    /**
     * Updates an existing external system with the given input data.
     *
     * @param id       the identifier of the external system to update
     * @param inputDTO the new external system data
     * @return the updated external system as a response DTO
     * @throws BusinessRuleException if the external system does not exist or the new name is
     * already used by another non-deleted external system
     */
    @Override
    public ExternalSystemOutputDTO update(Long id, ExternalSystemInputDTO inputDTO) {
        log.info("Facade: Updating external system with ID: {}", id);

        ExternalSystem existing = externalSystemRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_EXTERNALSYSTEM_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        if (externalSystemRepository.existsByNameAndIdNotAndDeletedAtIsNull(inputDTO.getName(), id)) {
            throw new BusinessRuleException(Constants.ERR_EXTERNALSYSTEM_DUPLICATED);
        }

        externalSystemMapper.updateModelFromInput(inputDTO, existing);
        return externalSystemMapper.toResponse(externalSystemRepository.update(existing, id));
    }

    /**
     * Logically deletes an external system by setting its deletion timestamp and author.
     *
     * @param id the identifier of the external system to delete
     * @throws BusinessRuleException if the external system does not exist or is already deleted
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Logically deleting external system with ID: {}", id);
        ExternalSystem existing = externalSystemRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_EXTERNALSYSTEM_NOT_FOUND);
        }

        if (existing.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_EXTERNALSYSTEM_NOT_ACTIVE);
        }

        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy(Utils.resolveCurrentUsername());

        externalSystemRepository.delete(existing);
    }

    /**
     * Reactivates a previously deleted external system by clearing its deletion timestamp and author.
     *
     * @param id the identifier of the external system to reactivate
     * @return the reactivated external system as a response DTO
     * @throws BusinessRuleException if the external system does not exist or is not currently deleted
     */
    @Override
    public ExternalSystemOutputDTO reactivate(Long id) {
        log.info("Facade: Reactivating external system with ID: {}", id);
        ExternalSystem existing = externalSystemRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_EXTERNALSYSTEM_NOT_FOUND);
        }

        if (existing.getDeletedAt() == null) {
            throw new BusinessRuleException(Constants.ERR_EXTERNALSYSTEM_ACTIVE);
        }

        existing.setDeletedAt(null);
        existing.setDeletedBy(null);

        ExternalSystem updatedModel = externalSystemRepository.update(existing, id);
        return externalSystemMapper.toResponse(updatedModel);
    }
}

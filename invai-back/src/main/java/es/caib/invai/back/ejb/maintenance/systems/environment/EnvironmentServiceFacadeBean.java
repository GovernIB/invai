package es.caib.invai.back.ejb.maintenance.systems.environment;

import es.caib.invai.back.interna.maintenance.systems.environment.DTO.EnvironmentInputDTO;
import es.caib.invai.back.interna.maintenance.systems.environment.DTO.EnvironmentOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.systems.environment.EnvironmentCriteria;
import es.caib.invai.back.service.mapper.maintenance.systems.environment.EnvironmentMapper;
import es.caib.invai.back.persistence.repository.maintenance.systems.environment.EnvironmentRepository;
import es.caib.invai.back.service.facade.maintenance.systems.environment.EnvironmentService;
import es.caib.invai.back.service.model.maintenance.systems.environment.Environment;
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
 * Facade service implementation for managing environment core configurations.
 * Enforces transactional safety bounds and verifies logical deletion metrics.
 *
 * @since 1.0.1
 */
@Service
@Slf4j
@Transactional
public class EnvironmentServiceFacadeBean implements EnvironmentService {

    /** Mapper converting between Environment domain models, entities, and DTOs. */
    @Autowired
    private EnvironmentMapper environmentMapper;

    /** Outbound port used to persist and query environment records. */
    @Autowired
    private EnvironmentRepository environmentRepository;

    /**
     * Retrieves an active environment by its unique identifier.
     *
     * @param id the unique environment record identifier
     * @return the mapped {@link EnvironmentOutputDTO} response
     * @throws BusinessRuleException if no matching record exists
     */
    @Override
    @Transactional(readOnly = true)
    public EnvironmentOutputDTO getById(Long id) {
        log.debug("Facade: Fetching environment by ID: {}", id);
        Environment environment = environmentRepository.findById(id);

        if (environment == null) {
            throw new BusinessRuleException(Constants.ERR_ENVIRONMENT_NOT_FOUND);
        }

        return environmentMapper.toResponse(environment);
    }

    /**
     * Retrieves a paginated list of environments matching the given search criteria.
     *
     * @param filter   dynamic search criteria and full-text keyword filters
     * @param pageable pagination and sorting parameters
     * @return a page of mapped {@link EnvironmentOutputDTO} results
     */
    @Override
    @Transactional(readOnly = true)
    public Page<EnvironmentOutputDTO> getAll(EnvironmentCriteria filter, Pageable pageable) {
        log.debug("Facade: Fetching environments via pagination boundaries");
        Page<Environment> domainPage = environmentRepository.findAll(filter, pageable);
        return domainPage.map(environmentMapper::toResponse);
    }

    /**
     * Validates and registers a new environment record.
     * Enforces code uniqueness among active records.
     *
     * @param inputDTO the data describing the environment to create
     * @return the persisted environment mapped into an {@link EnvironmentOutputDTO}
     * @throws BusinessRuleException if a record with the same code already exists
     */
    @Override
    public EnvironmentOutputDTO create(EnvironmentInputDTO inputDTO) {
        log.info("Facade: Creating new environment record with code: {}", inputDTO.getCode());

        Utils.sanitize(inputDTO);

        if (environmentRepository.existsByCode(inputDTO.getCode())) {
            throw new BusinessRuleException(Constants.ERR_ENVIRONMENT_DUPLICATED);
        }

        Environment model = environmentMapper.toModelFromInput(inputDTO);
        Environment savedModel = environmentRepository.create(model);
        return environmentMapper.toResponse(savedModel);
    }

    /**
     * Updates the modifiable fields of an existing environment record.
     *
     * @param id       the identifier of the record to update
     * @param inputDTO the new data to apply to the record
     * @return the updated environment mapped into an {@link EnvironmentOutputDTO}
     * @throws BusinessRuleException if the record does not exist, or if the new code conflicts with another record
     */
    @Override
    public EnvironmentOutputDTO update(Long id, EnvironmentInputDTO inputDTO) {
        log.info("Facade: Updating environment with ID: {}", id);

        Environment existing = environmentRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_ENVIRONMENT_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        if (environmentRepository.existsByCodeAndIdNot(inputDTO.getCode(), id)) {
            throw new BusinessRuleException(Constants.ERR_ENVIRONMENT_DUPLICATED);
        }

        environmentMapper.updateModelFromInput(inputDTO, existing);
        return environmentMapper.toResponse(environmentRepository.update(existing, id));
    }

    /**
     * Logically soft-deletes an environment record.
     *
     * @param id the identifier of the record to deactivate
     * @throws BusinessRuleException if the record does not exist or is already inactive
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Logically deleting environment with ID: {}", id);
        Environment existing = environmentRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_ENVIRONMENT_NOT_FOUND);
        }

        if (existing.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_ENVIRONMENT_NOT_FOUND);
        }

        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy(Utils.resolveCurrentUsername());

        environmentRepository.delete(existing);
    }

    /**
     * Reactivates a logically soft-deleted environment record back to active state.
     *
     * @param id the target identifier mapping the environment instance intended for reactivation
     * @return the reactivated domain representation mapped into an {@link EnvironmentOutputDTO}
     * @throws BusinessRuleException if matching environment cannot be found or is already active
     */
    @Override
    public EnvironmentOutputDTO reactivate(Long id) {
        log.info("Facade: Reactivating environment with ID: {}", id);
        Environment existing = environmentRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_ENVIRONMENT_NOT_FOUND);
        }

        if (existing.getDeletedAt() == null) {
            throw new BusinessRuleException(Constants.ERR_ENVIRONMENT_ACTIVE);
        }

        existing.setDeletedAt(null);
        existing.setDeletedBy(null);

        return environmentMapper.toResponse(environmentRepository.update(existing, id));
    }
}
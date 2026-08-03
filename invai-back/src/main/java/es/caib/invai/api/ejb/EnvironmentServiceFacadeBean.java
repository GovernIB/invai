package es.caib.invai.api.ejb;

import es.caib.invai.api.interna.maintenance.environment.DTO.EnvironmentInputDTO;
import es.caib.invai.api.interna.maintenance.environment.DTO.EnvironmentOutputDTO;
import es.caib.invai.api.persistence.repository.environment.EnvironmentCriteria;
import es.caib.invai.api.service.mapper.EnvironmentMapper;
import es.caib.invai.api.persistence.repository.environment.EnvironmentRepository;
import es.caib.invai.api.service.facade.EnvironmentService;
import es.caib.invai.api.service.model.Environment;
import es.caib.invai.api.exception.BusinessRuleException;
import es.caib.invai.api.utils.Constants;
import es.caib.invai.api.utils.Utils;
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

    @Autowired
    private EnvironmentMapper environmentMapper;

    @Autowired
    private EnvironmentRepository environmentRepository;

    @Override
    @Transactional(readOnly = true)
    public EnvironmentOutputDTO getById(Long id) {
        log.info("Facade: Fetching environment by ID: {}", id);
        Environment environment = environmentRepository.findById(id);

        if (environment == null) {
            throw new BusinessRuleException(Constants.ERR_ENVIRONMENT_NOT_FOUND);
        }

        return environmentMapper.toResponse(environment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EnvironmentOutputDTO> getAll(EnvironmentCriteria filter, Pageable pageable) {
        log.info("Facade: Fetching environments via pagination boundaries");
        Page<Environment> domainPage = environmentRepository.findAll(filter, pageable);
        return domainPage.map(environmentMapper::toResponse);
    }

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

    @Override
    public EnvironmentOutputDTO update(Long id, EnvironmentInputDTO inputDTO) {
        log.info("Facade: Updating environment with ID: {}", id);

        Environment existing = environmentRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_ENVIRONMENT_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        if (environmentRepository.existsByCodeAndIdNot(inputDTO.getCode(), id)) {
            throw new BusinessRuleException(Constants.ERR_DATABASE_DUPLICATED);
        }

        environmentMapper.updateModelFromInput(inputDTO, existing);
        return environmentMapper.toResponse(environmentRepository.update(existing, id));
    }

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
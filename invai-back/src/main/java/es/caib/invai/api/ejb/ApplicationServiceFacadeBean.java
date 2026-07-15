package es.caib.invai.api.ejb;

import es.caib.invai.api.interna.application.DTO.ApplicationInputDTO;
import es.caib.invai.api.interna.application.DTO.ApplicationOutputDTO;
import es.caib.invai.api.service.mapper.ApplicationMapper;
import es.caib.invai.api.persistence.repository.application.ApplicationRepository;
import es.caib.invai.api.persistence.repository.application.ApplicationCriteria;
import es.caib.invai.api.service.facade.ApplicationService;
import es.caib.invai.api.service.model.Application;
import es.caib.invai.api.exception.BusinessRuleException;
import es.caib.invai.api.service.model.StatusEnum;
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
 * Facade service implementation for managing system Application objects.
 * Handles structural length checks, status assignments, and filtering via customizable dynamic criteria.
 *
 * @since 1.0.0
 */
@Service
@Slf4j
@Transactional
public class ApplicationServiceFacadeBean implements ApplicationService {

    @Autowired
    private ApplicationMapper applicationMapper;

    @Autowired
    private ApplicationRepository applicationRepository;

    /**
     * Retrieves an active application by its unique database identifier.
     * Evaluates logical deletion properties and structural lifecycle status flags.
     *
     * @param id the unique application metadata record identity pointer
     * @return the mapped {@link ApplicationOutputDTO} response presentation payload
     * @throws BusinessRuleException if the identity does not match any record, has been
     * logically soft-deleted, or exhibits an {@link StatusEnum#INACTIVE} state
     */
    @Override
    @Transactional(readOnly = true)
    public ApplicationOutputDTO getById(Long id) {
        log.info(Constants.LOG_FACADE_FETCH_BY_ID, id);
        Application application = applicationRepository.findById(id);

        if (application == null || application.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_APP_NOT_FOUND);
        }

        if (application.getStatus() == StatusEnum.INACTIVE) {
            throw new BusinessRuleException(Constants.ERR_APP_NOT_ACTIVE);
        }
        return applicationMapper.toResponse(application);
    }

    /**
     * Gets a paginated distribution framework containing application records matching specified criteria.
     *
     * @param criteria search filter constraints and specification parameters wrapped in {@link ApplicationCriteria}
     * @param pageable sorting parameters and tracking page metadata pagination constraints
     * @return a structured page element populated with converted {@link ApplicationOutputDTO} results
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ApplicationOutputDTO> getAll(ApplicationCriteria criteria, Pageable pageable) {
        log.info("Facade: Fetching applications using dynamic criteria specifications");
        Page<Application> domainPage = applicationRepository.findAll(criteria, pageable);
        return domainPage.map(applicationMapper::toResponse);
    }

    /**
     * Validates structural constraints and registers a new application record within the system core.
     * Enforces domain text sanitization and unicity rules regarding both field code and operational prefix labels.
     *
     * @param inputDTO data transfer container holding properties describing the target application record
     * @return the resulting persistent instance transformed into an {@link ApplicationOutputDTO} structure
     * @throws BusinessRuleException if text formats fail physical bounds, or if the code or prefix targets
     * conflict with an already registered system application entry
     */
    @Override
    public ApplicationOutputDTO create(ApplicationInputDTO inputDTO) {
        log.info(Constants.LOG_FACADE_CREATE, inputDTO.getCode());

        Utils.sanitize(inputDTO);
        validateStructuralConstraints(inputDTO);

        if (applicationRepository.existsByCode(inputDTO.getCode())) {
            throw new BusinessRuleException(Constants.ERR_CODE_DUPLICATED);
        }
        if (applicationRepository.existsByPrefix(inputDTO.getPrefix())) {
            throw new BusinessRuleException(Constants.ERR_PREFIX_DUPLICATED);
        }

        Application model = applicationMapper.toModelFromInput(inputDTO);

        if (model.getStatus() == null) {
            model.setStatus(StatusEnum.ACTIVE);
        }

        Application savedModel = applicationRepository.create(model);
        return applicationMapper.toResponse(savedModel);
    }

    /**
     * Mutates an existing active application entity property by replacing metrics with input payload details.
     * Ensures updates do not overlap unique constraint parameters allocated to sibling records.
     *
     * @param id       the unique database resource key indexing the record targeting modification
     * @param inputDTO data update container outlining property changes intended for persistence merge operations
     * @return the modified domain representation mapped down into an {@link ApplicationOutputDTO}
     * @throws BusinessRuleException if the resource key is non-existent, has been marked soft-deleted,
     * or if input data maps field identifiers owned by another application
     */
    @Override
    public ApplicationOutputDTO update(Long id, ApplicationInputDTO inputDTO) {
        log.info(Constants.LOG_FACADE_UPDATE, id);

        Application existing = applicationRepository.findById(id);
        if (existing == null || existing.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_APP_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);
        validateStructuralConstraints(inputDTO);

        if (applicationRepository.existsByCodeAndIdNot(inputDTO.getCode(), id)) {
            throw new BusinessRuleException(Constants.ERR_CODE_OWNED_BY_OTHER);
        }
        if (applicationRepository.existsByPrefixAndIdNot(inputDTO.getPrefix(), id)) {
            throw new BusinessRuleException(Constants.ERR_PREFIX_OWNED_BY_OTHER);
        }

        applicationMapper.updateModelFromInput(inputDTO, existing);
        Application updatedModel = applicationRepository.update(existing, id);
        return applicationMapper.toResponse(updatedModel);
    }

    /**
     * Executes a logical soft-delete transaction lifecycle phase over an application record.
     * Shifts state configurations to inactive indicators and logs audit metrics profiling execution time and session user.
     *
     * @param id the target identifier mapping the application instance intended for termination
     * @throws BusinessRuleException if matching application instance descriptions cannot be found or are already soft-deleted
     */
    @Override
    public void delete(Long id) {
        log.info(Constants.LOG_FACADE_DEACTIVATE, id);
        Application existing = applicationRepository.findById(id);

        if (existing == null || existing.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_APP_NOT_FOUND);
        }

        existing.setStatus(StatusEnum.INACTIVE);
        existing.setDeletedAt(LocalDateTime.now());

        existing.setDeletedBy(Utils.resolveCurrentUsername());

        applicationRepository.delete(existing);
    }

    /**
     * Evaluates input payload boundaries prior to database interaction layers.
     * Acts as an application guardrail to prevent low-level engine exceptions such as ORA-12899 value overflows.
     *
     * @param inputDTO data transfer container tracking parameters requiring structural boundary assessment
     * @throws BusinessRuleException if code bounds are below min boundaries, or prefix/name sizes exceed limits
     */
    private void validateStructuralConstraints(ApplicationInputDTO inputDTO) {
        if (inputDTO.getCode() == null || inputDTO.getCode().length() < Constants.CODE_MIN_LENGTH) {
            throw new BusinessRuleException(Constants.ERR_CODE_MIN_SIZE);
        }
        if (inputDTO.getPrefix() != null && inputDTO.getPrefix().length() > 3) {
            throw new BusinessRuleException(Constants.ERR_PREFIX_OVERFLOW);
        }
        if (inputDTO.getName() != null && inputDTO.getName().length() > 100) {
            throw new BusinessRuleException(Constants.ERR_NAME_OVERFLOW);
        }
    }
}
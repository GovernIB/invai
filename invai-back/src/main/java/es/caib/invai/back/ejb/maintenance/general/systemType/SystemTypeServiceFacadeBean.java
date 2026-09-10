package es.caib.invai.back.ejb.maintenance.general.systemType;

import es.caib.invai.back.interna.maintenance.general.systemType.DTO.SystemTypeInputDTO;
import es.caib.invai.back.interna.maintenance.general.systemType.DTO.SystemTypeOutputDTO;
import es.caib.invai.back.service.mapper.maintenance.general.systemType.SystemTypeMapper;
import es.caib.invai.back.persistence.repository.maintenance.general.systemType.SystemTypeCriteria;
import es.caib.invai.back.persistence.repository.maintenance.general.systemType.SystemTypeRepository;
import es.caib.invai.back.persistence.repository.application.core.ApplicationRepository;
import es.caib.invai.back.service.facade.maintenance.general.systemType.SystemTypeService;
import es.caib.invai.back.service.model.maintenance.general.systemType.SystemType;
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
 * Facade service implementation for categorizing asset classification environments (SystemType).
 * Handles structural length checks, status assignments, and filtering via standard pagination rules.
 *
 * @since 1.0.2
 */
@Service
@Slf4j
@Transactional
public class SystemTypeServiceFacadeBean implements SystemTypeService {

    /** Mapper converting between SystemType domain models, entities and DTOs. */
    @Autowired
    private SystemTypeMapper systemTypeMapper;

    /** Repository port used to access and persist system type domain models. */
    @Autowired
    private SystemTypeRepository systemTypeRepository;

    /** Repository used to check for existing application dependencies before deletion. */
    @Autowired
    private ApplicationRepository applicationRepository;

    /**
     * Retrieves an active system type configuration by its unique database identifier.
     * Evaluates logical deletion properties and structural lifecycle status flags.
     *
     * @param id the unique system type metadata record identity pointer
     * @return the mapped {@link SystemTypeOutputDTO} response presentation payload
     * @throws BusinessRuleException if the identity does not match any active record or has been logically soft-deleted
     */
    @Override
    @Transactional(readOnly = true)
    public SystemTypeOutputDTO getById(Long id) {
        log.debug("Facade: Fetching system type by ID: {}", id);
        SystemType systemType = systemTypeRepository.findById(id);

        if (systemType == null) {
            throw new BusinessRuleException(Constants.ERR_SYSTEM_TYPE_NOT_FOUND);
        }

        return systemTypeMapper.toResponse(systemType);
    }

    /**
     * Gets a paginated distribution framework containing system type records matching pagination rules.
     *
     * @param pageable sorting parameters and tracking page metadata pagination constraints
     * @return a structured page element populated with converted {@link SystemTypeOutputDTO} results
     */
    @Override
    @Transactional(readOnly = true)
    public Page<SystemTypeOutputDTO> getAll(SystemTypeCriteria filter, Pageable pageable) {
        log.debug("Facade: Fetching system types via pagination boundaries");
        Page<SystemType> domainPage = systemTypeRepository.findAll(filter, pageable);
        return domainPage.map(systemTypeMapper::toResponse);
    }

    /**
     * Validates structural constraints and registers a new system type record within the system core.
     * Enforces domain text sanitization and unicity rules regarding the name of the system type.
     *
     * @param inputDTO data transfer container holding properties describing the target system type record
     * @return the resulting persistent instance transformed into an {@link SystemTypeOutputDTO} structure
     * @throws BusinessRuleException if text formats fail physical bounds, or if the name
     * conflicts with an already registered system type configuration entry
     */
    @Override
    public SystemTypeOutputDTO create(SystemTypeInputDTO inputDTO) {
        log.info("Facade: Creating new system type record with name: {}", inputDTO.getName());

        Utils.sanitize(inputDTO);

        if (systemTypeRepository.existsByNameAndDeletedAtIsNull(inputDTO.getName())) {
            throw new BusinessRuleException(Constants.ERR_SYSTEM_TYPE_DUPLICATED);
        }
        if (systemTypeRepository.existsByNameEsAndDeletedAtIsNull(inputDTO.getNameEs())) {
            throw new BusinessRuleException(Constants.ERR_SYSTEM_TYPE_DUPLICATED_ES);
        }

        SystemType model = systemTypeMapper.toModelFromInput(inputDTO);

        SystemType savedModel = systemTypeRepository.create(model);
        return systemTypeMapper.toResponse(savedModel);
    }

    /**
     * Mutates an existing active system type entity property by replacing metrics with input payload details.
     * Ensures updates do not overlap unique constraint parameters allocated to sibling records.
     *
     * @param id       the unique database resource key indexing the record targeting modification
     * @param inputDTO data update container outlining property changes intended for persistence merge operations
     * @return the modified domain representation mapped down into an {@link SystemTypeOutputDTO}
     * @throws BusinessRuleException if the resource key is non-existent, has been marked soft-deleted,
     * or if input data maps identifier fields owned by another system type
     */
    @Override
    public SystemTypeOutputDTO update(Long id, SystemTypeInputDTO inputDTO) {
        log.info("Facade: Updating system type with ID: {}", id);

        SystemType existing = systemTypeRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_SYSTEM_TYPE_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        if (systemTypeRepository.existsByNameAndIdNotAndDeletedAtIsNull(inputDTO.getName(), id)) {
            throw new BusinessRuleException(Constants.ERR_SYSTEM_TYPE_DUPLICATED);
        }
        if (systemTypeRepository.existsByNameEsAndIdNotAndDeletedAtIsNull(inputDTO.getNameEs(), id)) {
            throw new BusinessRuleException(Constants.ERR_SYSTEM_TYPE_DUPLICATED_ES);
        }

        systemTypeMapper.updateModelFromInput(inputDTO, existing);
        return systemTypeMapper.toResponse(systemTypeRepository.update(existing, id));
    }

    /**
     * Executes a logical soft-delete transaction lifecycle phase over a system type record.
     * Shifts state configurations to inactive indicators and logs audit metrics profiling execution time and session user.
     *
     * @param id the target identifier mapping the system type instance intended for deactivation
     * @throws BusinessRuleException if matching system type instance descriptions cannot be found, are already soft-deleted,
     * or if there are active application dependencies mapped to this system type
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Logically deleting system type with ID: {}", id);
        SystemType existing = systemTypeRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_SYSTEM_TYPE_NOT_FOUND);
        }

        if (existing.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_SYSTEM_TYPE_NOT_ACTIVE);
        }

        if (applicationRepository.existsBySystemTypeId(id)) {
            throw new BusinessRuleException(Constants.ERR_SYSTEM_TYPE_DELETE_HAS_DEPENDENCIES);
        }

        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy(Utils.resolveCurrentUsername());

        systemTypeRepository.delete(existing);
    }

    /**
     * Reactivates a logically soft-deleted system type record back to active state.
     *
     * @param id the target identifier mapping the system type instance intended for reactivation
     * @return the reactivated domain representation mapped into an {@link SystemTypeOutputDTO}
     * @throws BusinessRuleException if matching system type cannot be found or is already active
     */
    @Override
    public SystemTypeOutputDTO reactivate(Long id) {
        log.info("Facade: Reactivating system type with ID: {}", id);
        SystemType existing = systemTypeRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_SYSTEM_TYPE_NOT_FOUND);
        }

        if (existing.getDeletedAt() == null) {
            throw new BusinessRuleException(Constants.ERR_SYSTEM_TYPE_ACTIVE);
        }

        existing.setDeletedAt(null);
        existing.setDeletedBy(null);

        SystemType updatedModel = systemTypeRepository.update(existing, id);
        return systemTypeMapper.toResponse(updatedModel);
    }
}
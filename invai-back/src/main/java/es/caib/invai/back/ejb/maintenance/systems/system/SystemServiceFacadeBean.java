package es.caib.invai.back.ejb.maintenance.systems.system;

import es.caib.invai.back.interna.maintenance.systems.system.DTO.SystemInputDTO;
import es.caib.invai.back.interna.maintenance.systems.system.DTO.SystemOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.systems.server.ServerRepository;
import es.caib.invai.back.persistence.repository.maintenance.systems.system.SystemCriteria;
import es.caib.invai.back.service.mapper.maintenance.systems.system.SystemMapper;
import es.caib.invai.back.persistence.repository.maintenance.systems.system.SystemRepository;
import es.caib.invai.back.service.facade.maintenance.systems.system.SystemService;
import es.caib.invai.back.service.model.maintenance.systems.server.Server;
import es.caib.invai.back.service.model.maintenance.systems.system.System;
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
 * Facade service implementation for managing system core configurations.
 * Handles structural length checks, status assignments, and filtering via standard pagination rules.
 *
 * @since 1.0.1
 */
@Service
@Slf4j
@Transactional
public class SystemServiceFacadeBean implements SystemService {

    /** Mapper used to convert between System domain models and their DTO representations. */
    @Autowired
    private SystemMapper systemMapper;

    /** Repository providing persistence operations for System records. */
    @Autowired
    private SystemRepository systemRepository;

    /** Repository used to look up the host server referenced by a system, to validate its server type. */
    @Autowired
    private ServerRepository serverRepository;

    /**
     * Retrieves an active system configuration by its unique database identifier.
     * Evaluates logical deletion properties and structural lifecycle status flags.
     *
     * @param id the unique system metadata record identity pointer
     * @return the mapped {@link SystemOutputDTO} response presentation payload
     * @throws BusinessRuleException if the identity does not match any active record or has been logically soft-deleted
     */
    @Override
    @Transactional(readOnly = true)
    public SystemOutputDTO getById(Long id) {
        log.debug("Facade: Fetching system by ID: {}", id);
        System system = systemRepository.findById(id);

        if (system == null) {
            throw new BusinessRuleException(Constants.ERR_SYSTEM_NOT_FOUND);
        }

        return systemMapper.toResponse(system);
    }

    /**
     * Gets a paginated distribution framework containing system records matching pagination rules.
     *
     * @param pageable sorting parameters and tracking page metadata pagination constraints
     * @return a structured page element populated with converted {@link SystemOutputDTO} results
     */
    @Override
    @Transactional(readOnly = true)
    public Page<SystemOutputDTO> getAll(SystemCriteria filter, Pageable pageable) {
        log.debug("Facade: Fetching systems via pagination boundaries");
        Page<System> domainPage = systemRepository.findAll(filter, pageable);
        return domainPage.map(systemMapper::toResponse);
    }

    /**
     * Validates structural constraints and registers a new system record within the system core.
     * Enforces domain text sanitization and unicity rules regarding the combination of name and instance.
     *
     * @param inputDTO data transfer container holding properties describing the target system record
     * @return the resulting persistent instance transformed into an {@link SystemOutputDTO} structure
     * @throws BusinessRuleException if text formats fail physical bounds, or if the name and instance combination
     * conflicts with an already registered system configuration entry
     */
    @Override
    public SystemOutputDTO create(SystemInputDTO inputDTO) {
        log.info("Facade: Creating new system record with server ID: {} and instance: {}", inputDTO.getServerId(), inputDTO.getInstance());

        Utils.sanitize(inputDTO);

        validateServerType(inputDTO.getServerId());

        if (systemRepository.existsByServerIdAndInstance(inputDTO.getServerId(), inputDTO.getInstance())) {
            throw new BusinessRuleException(Constants.ERR_SYSTEM_NAME_AND_INSTANCE_DUPLICATED);
        }

        System model = systemMapper.toModelFromInput(inputDTO);

        System savedModel = systemRepository.create(model);
        return systemMapper.toResponse(savedModel);
    }

    /**
     * Mutates an existing active system entity property by replacing metrics with input payload details.
     * Ensures updates do not overlap unique constraint parameters allocated to sibling records.
     *
     * @param id       the unique database resource key indexing the record targeting modification
     * @param inputDTO data update container outlining property changes intended for persistence merge operations
     * @return the modified domain representation mapped down into an {@link SystemOutputDTO}
     * @throws BusinessRuleException if the resource key is non-existent, has been marked soft-deleted,
     * or if input data maps identifier fields owned by another system
     */
    @Override
    public SystemOutputDTO update(Long id, SystemInputDTO inputDTO) {
        log.info("Facade: Updating system with ID: {}", id);

        System existing = systemRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_SYSTEM_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        validateServerType(inputDTO.getServerId());

        if (systemRepository.existsByServerIdAndInstanceAndId(inputDTO.getServerId(), inputDTO.getInstance(), id)) {
            throw new BusinessRuleException(Constants.ERR_SYSTEM_NAME_AND_INSTANCE_OWNED_BY_OTHER);
        }

        systemMapper.updateModelFromInput(inputDTO, existing);
        return systemMapper.toResponse(systemRepository.update(existing, id));
    }

    /**
     * Executes a logical soft-delete transaction lifecycle phase over a system record.
     * Shifts state configurations to inactive indicators and logs audit metrics profiling execution time and session user.
     *
     * @param id the target identifier mapping the system instance intended for deactivation
     * @throws BusinessRuleException if matching system instance descriptions cannot be found or are already soft-deleted
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Logically deleting system with ID: {}", id);
        System existing = systemRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_SYSTEM_NOT_FOUND);
        }

        if(existing.getDeletedAt() != null){
            throw new BusinessRuleException(Constants.ERR_SYSTEM_NOT_ACTIVE);
        }

        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy(Utils.resolveCurrentUsername());

        systemRepository.delete(existing);
    }

    /**
     * Reactivates a logically soft-deleted system record back to active state.
     *
     * @param id the target identifier mapping the system instance intended for reactivation
     * @return the reactivated domain representation mapped into an {@link SystemOutputDTO}
     * @throws BusinessRuleException if matching system instance descriptions cannot be found or are already active
     */
    @Override
    public SystemOutputDTO reactivate(Long id) {
        log.info("Facade: Reactivating system with ID: {}", id);
        System existing = systemRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_SYSTEM_NOT_FOUND);
        }

        if (existing.getDeletedAt() == null) {
            throw new BusinessRuleException(Constants.ERR_SYSTEM_ACTIVE);
        }

        existing.setDeletedAt(null);
        existing.setDeletedBy(null);

        return systemMapper.toResponse(systemRepository.update(existing, id));
    }

    /**
     * Validates that the referenced host server is explicitly typed for hosting application instances.
     * A nonexistent server ID is intentionally left unchecked here, letting the underlying database
     * foreign key constraint reject it at persist time, consistent with every other FK field in this API.
     *
     * @param serverId the candidate host server identifier submitted in the request payload
     * @throws BusinessRuleException if the server exists but its type is not {@code APPLICATION}
     */
    private void validateServerType(Long serverId) {
        Server server = serverRepository.findById(serverId);
        if (server != null
                && (server.getServerType() == null
                || !Constants.SERVER_TYPE_CODE_APPLICATION.equals(server.getServerType().getCode()))) {
            throw new BusinessRuleException(Constants.ERR_SYSTEM_SERVER_TYPE_INVALID);
        }
    }
}
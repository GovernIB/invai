package es.caib.invai.back.ejb.maintenance.development.role;

import es.caib.invai.back.interna.maintenance.development.role.DTO.RoleInputDTO;
import es.caib.invai.back.interna.maintenance.development.role.DTO.RoleOutputDTO;
import es.caib.invai.back.service.mapper.maintenance.development.role.RoleMapper;
import es.caib.invai.back.persistence.repository.maintenance.development.role.RoleCriteria;
import es.caib.invai.back.persistence.repository.maintenance.development.role.RoleRepository;
import es.caib.invai.back.persistence.repository.application.development.provider.AppProviderRepository;
import es.caib.invai.back.service.facade.maintenance.development.role.RoleService;
import es.caib.invai.back.service.model.maintenance.development.role.Role;
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
 * Facade service implementation for administrative provider Roles.
 *
 * @since 1.0.2
 */
@Service
@Slf4j
@Transactional
public class RoleServiceFacadeBean implements RoleService {

    /** Mapper used to convert between Role domain models and their DTO representations. */
    @Autowired
    private RoleMapper roleMapper;

    /** Repository providing persistence operations for Role records. */
    @Autowired
    private RoleRepository roleRepository;

    /** Repository used to verify whether a role is still referenced by application providers. */
    @Autowired
    private AppProviderRepository appProviderRepository;

    /**
     * Retrieves a role by its unique database identifier.
     *
     * @param id the unique role record identifier
     * @return the mapped {@link RoleOutputDTO} response payload
     * @throws BusinessRuleException if no role matches the given identifier
     */
    @Override
    @Transactional(readOnly = true)
    public RoleOutputDTO getById(Long id) {
        log.debug("Facade: Fetching role by ID: {}", id);
        Role role = roleRepository.findById(id);

        if (role == null) {
            throw new BusinessRuleException(Constants.ERR_ROLE_NOT_FOUND);
        }

        return roleMapper.toResponse(role);
    }

    /**
     * Retrieves a paginated list of roles matching the given filter criteria.
     *
     * @param filter   the search criteria used to narrow the results
     * @param pageable the pagination and sorting parameters
     * @return a page of mapped {@link RoleOutputDTO} results
     */
    @Override
    @Transactional(readOnly = true)
    public Page<RoleOutputDTO> getAll(RoleCriteria filter, Pageable pageable) {
        log.debug("Facade: Fetching roles via pagination boundaries");
        Page<Role> domainPage = roleRepository.findAll(filter, pageable);
        return domainPage.map(roleMapper::toResponse);
    }

    /**
     * Validates and creates a new role record.
     *
     * @param inputDTO the data used to create the new role
     * @return the persisted role mapped into a {@link RoleOutputDTO}
     * @throws BusinessRuleException if an active role with the same name already exists
     */
    @Override
    public RoleOutputDTO create(RoleInputDTO inputDTO) {
        log.info("Facade: Creating new role record with name: {}", inputDTO.getName());

        Utils.sanitize(inputDTO);

        if (roleRepository.existsByNameAndDeletedAtIsNull(inputDTO.getName())) {
            throw new BusinessRuleException(Constants.ERR_ROLE_DUPLICATED);
        }

        Role model = roleMapper.toModelFromInput(inputDTO);

        Role savedModel = roleRepository.create(model);
        return roleMapper.toResponse(savedModel);
    }

    /**
     * Validates and updates an existing role record with the given input.
     *
     * @param id       the identifier of the role to update
     * @param inputDTO the data used to update the role
     * @return the updated role mapped into a {@link RoleOutputDTO}
     * @throws BusinessRuleException if the role does not exist or the name is already owned by another active role
     */
    @Override
    public RoleOutputDTO update(Long id, RoleInputDTO inputDTO) {
        log.info("Facade: Updating role with ID: {}", id);

        Role existing = roleRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_ROLE_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        if (roleRepository.existsByNameAndIdNotAndDeletedAtIsNull(inputDTO.getName(), id)) {
            throw new BusinessRuleException(Constants.ERR_ROLE_DUPLICATED);
        }

        roleMapper.updateModelFromInput(inputDTO, existing);
        return roleMapper.toResponse(roleRepository.update(existing, id));
    }

    /**
     * Logically deletes (soft-deletes) an active role record.
     *
     * @param id the identifier of the role to delete
     * @throws BusinessRuleException if the role does not exist, is already deleted, or is still referenced
     * by an application provider
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Logically deleting role with ID: {}", id);
        Role existing = roleRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_ROLE_NOT_FOUND);
        }

        if (existing.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_ROLE_NOT_ACTIVE);
        }

        if (appProviderRepository.existsByRoleId(id)) {
            throw new BusinessRuleException(Constants.ERR_ROLE_DELETE_HAS_DEPENDENCIES);
        }

        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy(Utils.resolveCurrentUsername());

        roleRepository.delete(existing);
    }

    /**
     * Reactivates a logically deleted role record back to active state.
     *
     * @param id the identifier of the role to reactivate
     * @return the reactivated role mapped into a {@link RoleOutputDTO}
     * @throws BusinessRuleException if the role does not exist or is already active
     */
    @Override
    public RoleOutputDTO reactivate(Long id) {
        log.info("Facade: Reactivating role with ID: {}", id);
        Role existing = roleRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_ROLE_NOT_FOUND);
        }

        if (existing.getDeletedAt() == null) {
            throw new BusinessRuleException(Constants.ERR_ROLE_ACTIVE);
        }

        existing.setDeletedAt(null);
        existing.setDeletedBy(null);

        Role updatedModel = roleRepository.update(existing, id);
        return roleMapper.toResponse(updatedModel);
    }
}

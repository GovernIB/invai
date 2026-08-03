package es.caib.invai.api.ejb;

import es.caib.invai.api.interna.maintenance.role.DTO.RoleInputDTO;
import es.caib.invai.api.interna.maintenance.role.DTO.RoleOutputDTO;
import es.caib.invai.api.service.mapper.RoleMapper;
import es.caib.invai.api.persistence.repository.role.RoleCriteria;
import es.caib.invai.api.persistence.repository.role.RoleRepository;
import es.caib.invai.api.persistence.repository.application.development.provider.AppProviderRepository;
import es.caib.invai.api.service.facade.RoleService;
import es.caib.invai.api.service.model.Role;
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
 * Facade service implementation for administrative provider Roles.
 *
 * @since 1.0.2
 */
@Service
@Slf4j
@Transactional
public class RoleServiceFacadeBean implements RoleService {

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AppProviderRepository appProviderRepository;

    @Override
    @Transactional(readOnly = true)
    public RoleOutputDTO getById(Long id) {
        log.info("Facade: Fetching role by ID: {}", id);
        Role role = roleRepository.findById(id);

        if (role == null) {
            throw new BusinessRuleException(Constants.ERR_ROLE_NOT_FOUND);
        }

        return roleMapper.toResponse(role);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoleOutputDTO> getAll(RoleCriteria filter, Pageable pageable) {
        log.info("Facade: Fetching roles via pagination boundaries");
        Page<Role> domainPage = roleRepository.findAll(filter, pageable);
        return domainPage.map(roleMapper::toResponse);
    }

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

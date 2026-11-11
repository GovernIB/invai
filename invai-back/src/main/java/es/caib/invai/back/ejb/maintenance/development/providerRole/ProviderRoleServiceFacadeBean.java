package es.caib.invai.back.ejb.maintenance.development.providerRole;

import es.caib.invai.back.interna.maintenance.development.providerRole.DTO.ProviderRoleInputDTO;
import es.caib.invai.back.interna.maintenance.development.providerRole.DTO.ProviderRoleOutputDTO;
import es.caib.invai.back.service.mapper.maintenance.development.providerRole.ProviderRoleMapper;
import es.caib.invai.back.persistence.repository.maintenance.development.providerRole.ProviderRoleCriteria;
import es.caib.invai.back.persistence.repository.maintenance.development.providerRole.ProviderRoleRepository;
import es.caib.invai.back.persistence.repository.application.development.provider.AppProviderRepository;
import es.caib.invai.back.service.facade.maintenance.development.providerRole.ProviderRoleService;
import es.caib.invai.back.service.model.maintenance.development.providerRole.ProviderRole;
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
import java.util.Objects;

/**
 * Facade service implementation for administrative provider Roles.
 *
 * @since 1.0.2
 */
@Service
@Slf4j
@Transactional
public class ProviderRoleServiceFacadeBean implements ProviderRoleService {

    /** Mapper used to convert between Role domain models and their DTO representations. */
    @Autowired
    private ProviderRoleMapper providerRoleMapper;

    /** Repository providing persistence operations for Role records. */
    @Autowired
    private ProviderRoleRepository providerRoleRepository;

    /** Repository used to verify whether a role is still referenced by application providers. */
    @Autowired
    private AppProviderRepository appProviderRepository;

    /**
     * Retrieves a role by its unique database identifier.
     *
     * @param id the unique role record identifier
     * @return the mapped {@link ProviderRoleOutputDTO} response payload
     * @throws BusinessRuleException if no role matches the given identifier
     */
    @Override
    @Transactional(readOnly = true)
    public ProviderRoleOutputDTO getById(Long id) {
        log.debug("Facade: Fetching role by ID: {}", id);
        ProviderRole role = providerRoleRepository.findById(id);

        if (Objects.isNull(role)) {
            throw new BusinessRuleException(Constants.ERR_ROLE_NOT_FOUND);
        }

        return providerRoleMapper.toResponse(role);
    }

    /**
     * Retrieves a paginated list of roles matching the given filter criteria.
     *
     * @param filter   the search criteria used to narrow the results
     * @param pageable the pagination and sorting parameters
     * @return a page of mapped {@link ProviderRoleOutputDTO} results
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ProviderRoleOutputDTO> getAll(ProviderRoleCriteria filter, Pageable pageable) {
        log.debug("Facade: Fetching roles via pagination boundaries");
        Page<ProviderRole> domainPage = providerRoleRepository.findAll(filter, pageable);
        return domainPage.map(providerRoleMapper::toResponse);
    }

    /**
     * Validates and creates a new role record.
     *
     * @param inputDTO the data used to create the new role
     * @return the persisted role mapped into a {@link ProviderRoleOutputDTO}
     * @throws BusinessRuleException if an active role with the same name already exists
     */
    @Override
    public ProviderRoleOutputDTO create(ProviderRoleInputDTO inputDTO) {
        log.info("Facade: Creating new role record with name: {}", inputDTO.getName());

        Utils.sanitize(inputDTO);

        if (providerRoleRepository.existsByNameAndDeletedAtIsNull(inputDTO.getName())) {
            throw new BusinessRuleException(Constants.ERR_ROLE_DUPLICATED);
        }

        ProviderRole model = providerRoleMapper.toModelFromInput(inputDTO);

        ProviderRole savedModel = providerRoleRepository.create(model);
        return providerRoleMapper.toResponse(savedModel);
    }

    /**
     * Validates and updates an existing role record with the given input.
     *
     * @param id       the identifier of the role to update
     * @param inputDTO the data used to update the role
     * @return the updated role mapped into a {@link ProviderRoleOutputDTO}
     * @throws BusinessRuleException if the role does not exist or the name is already owned by another active role
     */
    @Override
    public ProviderRoleOutputDTO update(Long id, ProviderRoleInputDTO inputDTO) {
        log.info("Facade: Updating role with ID: {}", id);

        ProviderRole existing = providerRoleRepository.findById(id);
        if (Objects.isNull(existing)) {
            throw new BusinessRuleException(Constants.ERR_ROLE_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        if (providerRoleRepository.existsByNameAndIdNotAndDeletedAtIsNull(inputDTO.getName(), id)) {
            throw new BusinessRuleException(Constants.ERR_ROLE_DUPLICATED);
        }

        providerRoleMapper.updateModelFromInput(inputDTO, existing);
        return providerRoleMapper.toResponse(providerRoleRepository.update(existing, id));
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
        ProviderRole existing = providerRoleRepository.findById(id);

        if (Objects.isNull(existing)) {
            throw new BusinessRuleException(Constants.ERR_ROLE_NOT_FOUND);
        }

        if (Objects.nonNull(existing.getDeletedAt())) {
            throw new BusinessRuleException(Constants.ERR_ROLE_NOT_ACTIVE);
        }

        if (appProviderRepository.existsByRoleId(id)) {
            throw new BusinessRuleException(Constants.ERR_ROLE_DELETE_HAS_DEPENDENCIES);
        }

        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy(Utils.resolveCurrentUsername());

        providerRoleRepository.delete(existing);
    }

    /**
     * Reactivates a logically deleted role record back to active state.
     *
     * @param id the identifier of the role to reactivate
     * @return the reactivated role mapped into a {@link ProviderRoleOutputDTO}
     * @throws BusinessRuleException if the role does not exist or is already active
     */
    @Override
    public ProviderRoleOutputDTO reactivate(Long id) {
        log.info("Facade: Reactivating role with ID: {}", id);
        ProviderRole existing = providerRoleRepository.findById(id);

        if (Objects.isNull(existing)) {
            throw new BusinessRuleException(Constants.ERR_ROLE_NOT_FOUND);
        }

        if (Objects.isNull(existing.getDeletedAt())) {
            throw new BusinessRuleException(Constants.ERR_ROLE_ACTIVE);
        }

        existing.setDeletedAt(null);
        existing.setDeletedBy(null);

        ProviderRole updatedModel = providerRoleRepository.update(existing, id);
        return providerRoleMapper.toResponse(updatedModel);
    }
}

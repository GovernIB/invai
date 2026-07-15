package es.caib.invai.api.ejb;

import es.caib.invai.api.interna.systemType.DTO.SystemTypeInputDTO;
import es.caib.invai.api.interna.systemType.DTO.SystemTypeOutputDTO;
import es.caib.invai.api.utils.SecurityUtils;
import es.caib.invai.api.service.mapper.SystemTypeMapper;
import es.caib.invai.api.persistence.repository.systemType.SystemTypeRepository;
import es.caib.invai.api.service.facade.SystemTypeService;
import es.caib.invai.api.service.model.SystemType;
import es.caib.invai.api.exception.BusinessRuleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Facade service implementation for categorizing asset classification environments (SystemType).
 * Enforces business uniqueness constraints across active system architecture descriptors.
 *
 * @since 1.0.0
 */
@Service
@Slf4j
@Transactional
public class SystemTypeServiceFacadeBean implements SystemTypeService {

    @Autowired
    private SystemTypeMapper systemTypeMapper;

    @Autowired
    private SystemTypeRepository systemTypeRepository;

    /**
     * Resolves single SystemType properties mapping configurations based on its unique index.
     *
     * @param id targeted structural identifier element index
     * @return transformed configuration profiles mapping properties output details
     * @throws BusinessRuleException if requested parameters do not align with verified components
     */
    @Override
    @Transactional(readOnly = true)
    public SystemTypeOutputDTO getById(Long id) {
        log.info("Facade: Fetching system type by id: {}", id);
        SystemType systemType = systemTypeRepository.findById(id);
        if (systemType == null) {
            throw new BusinessRuleException("exception.systemtype.notfound");
        }
        return systemTypeMapper.toResponse(systemType);
    }

    /**
     * Evaluates continuous block items traversing active system architectural data models.
     *
     * @param pageable constraints bounding partition ranges
     * @return parsed response configurations details wrapped into a page structure
     */
    @Override
    @Transactional(readOnly = true)
    public Page<SystemTypeOutputDTO> getAll(Pageable pageable) {
        log.info("Facade: Fetching paged system types");
        return systemTypeRepository.findAll(pageable).map(systemTypeMapper::toResponse);
    }

    /**
     * Creates new system platform descriptors inside permanent system logs.
     *
     * @param inputDTO parameters framing item details
     * @return operational snapshots containing new structure settings properties
     * @throws BusinessRuleException if matching labels overlap active systems data items
     */
    @Override
    public SystemTypeOutputDTO create(SystemTypeInputDTO inputDTO) {
        log.info("Facade: Creating system type with name: {}", inputDTO.getName());

        if (systemTypeRepository.existsByNameAndDeletedAtIsNull(inputDTO.getName())) {
            throw new BusinessRuleException("exception.systemtype.duplicated");
        }

        SystemType model = systemTypeMapper.toModelFromInput(inputDTO);
        SystemType savedModel = systemTypeRepository.create(model);
        return systemTypeMapper.toResponse(savedModel);
    }

    /**
     * Updates active profile properties variables matching target configuration schemas.
     *
     * @param id       target identifier indexing row configurations data items
     * @param inputDTO updating variable metrics payload models
     * @return parsed output properties indicating updated structural trends
     * @throws BusinessRuleException if elements are missing or description labels clash with existing items
     */
    @Override
    public SystemTypeOutputDTO update(Long id, SystemTypeInputDTO inputDTO) {
        log.info("Facade: Updating system type ID: {}", id);

        SystemType existing = systemTypeRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException("exception.systemtype.notfound");
        }

        if (systemTypeRepository.existsByNameAndIdNotAndDeletedAtIsNull(inputDTO.getName(), id)) {
            throw new BusinessRuleException("exception.systemtype.duplicated");
        }

        systemTypeMapper.updateModelFromInput(inputDTO, existing);
        SystemType updatedModel = systemTypeRepository.update(existing, id);
        return systemTypeMapper.toResponse(updatedModel);
    }

    /**
     * Marks specific database components functionally offline via standard soft deletion auditing routines.
     *
     * @param id reference index specifying target architectural parameters rows
     * @throws BusinessRuleException if targets fail mapping to tracking data
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Executing logical delete for system type ID: {}", id);
        SystemType existing = systemTypeRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException("exception.systemtype.notfound");
        }

        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy("SYSTEM");

        OidcUserInfo currentUser = (OidcUserInfo) SecurityUtils.getCurrentUser();

        if (currentUser != null && currentUser.getClaims().get("preferred_username") != null) {
            existing.setDeletedBy((String) currentUser.getClaims().get("preferred_username"));
        }

        systemTypeRepository.delete(existing);
    }
}
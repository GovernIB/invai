package es.caib.invai.api.ejb;

import es.caib.invai.api.interna.field.DTO.FieldInputDTO;
import es.caib.invai.api.interna.field.DTO.FieldOutputDTO;
import es.caib.invai.api.utils.SecurityUtils;
import es.caib.invai.api.service.mapper.FieldMapper;
import es.caib.invai.api.persistence.repository.field.FieldRepository;
import es.caib.invai.api.service.facade.FieldService;
import es.caib.invai.api.service.model.Field;
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
 * Facade service implementation for handling structural sectors or operational Fields.
 * Maps operational validation checks before routing to the underlying persistent layer.
 *
 * @since 1.0.0
 */
@Service
@Slf4j
@Transactional
public class FieldServiceFacadeBean implements FieldService {

    @Autowired
    private FieldMapper fieldMapper;

    @Autowired
    private FieldRepository fieldRepository;

    /**
     * Finds a specific operational Field dataset by its key.
     *
     * @param id key structure reference
     * @return transformed properties mapped to data layout models
     * @throws BusinessRuleException if target data references are missing
     */
    @Override
    @Transactional(readOnly = true)
    public FieldOutputDTO getById(Long id) {
        log.info("Facade: Fetching field by id: {}", id);
        Field field = fieldRepository.findById(id);
        if (field == null) {
            throw new BusinessRuleException("exception.field.notfound");
        }
        return fieldMapper.toResponse(field);
    }

    /**
     * Lists sorted chunks across all accessible field structures.
     *
     * @param pageable sizing configurations bounds
     * @return formatted data pages mapping data layout structures
     */
    @Override
    @Transactional(readOnly = true)
    public Page<FieldOutputDTO> getAll(Pageable pageable) {
        log.info("Facade: Fetching paged fields");
        return fieldRepository.findAll(pageable).map(fieldMapper::toResponse);
    }

    /**
     * Registers a unique context component representation into permanent system schemas.
     *
     * @param inputDTO parameters framing item details
     * @return object validation snapshot structures
     * @throws BusinessRuleException if matching structural constraints exist elsewhere
     */
    @Override
    public FieldOutputDTO create(FieldInputDTO inputDTO) {
        log.info("Facade: Creating field with name: {}", inputDTO.getName());

        if (fieldRepository.existsByNameAndDeletedAtIsNull(inputDTO.getName())) {
            throw new BusinessRuleException("exception.field.duplicated");
        }

        Field model = fieldMapper.toModelFromInput(inputDTO);
        Field savedModel = fieldRepository.create(model);
        return fieldMapper.toResponse(savedModel);
    }

    /**
     * Updates field settings properties on active records.
     *
     * @param id       context structural row primary index tracking configurations
     * @param inputDTO property data variables mapping structural items
     * @return data outputs mapping response types
     * @throws BusinessRuleException if the field doesn't exist or conflicts with existing active field descriptors
     */
    @Override
    public FieldOutputDTO update(Long id, FieldInputDTO inputDTO) {
        log.info("Facade: Updating field ID: {}", id);

        Field existing = fieldRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException("exception.field.notfound");
        }

        if (fieldRepository.existsByNameAndIdNotAndDeletedAtIsNull(inputDTO.getName(), id)) {
            throw new BusinessRuleException("exception.field.duplicated");
        }

        fieldMapper.updateModelFromInput(inputDTO, existing);
        Field updatedModel = fieldRepository.update(existing, id);
        return fieldMapper.toResponse(updatedModel);
    }

    /**
     * Applies logical deactivation rules against indexed target elements.
     *
     * @param id system index targeting data deletion
     * @throws BusinessRuleException if referenced structural variables are missing
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Executing logical delete for field ID: {}", id);
        Field existing = fieldRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException("exception.field.notfound");
        }

        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy("SYSTEM");

        OidcUserInfo currentUser = (OidcUserInfo) SecurityUtils.getCurrentUser();

        if (currentUser != null && currentUser.getClaims().get("preferred_username") != null) {
            existing.setDeletedBy((String) currentUser.getClaims().get("preferred_username"));
        }

        fieldRepository.delete(existing);
    }
}
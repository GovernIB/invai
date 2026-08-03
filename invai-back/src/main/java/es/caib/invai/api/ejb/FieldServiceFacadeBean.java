package es.caib.invai.api.ejb;

import es.caib.invai.api.interna.maintenance.field.DTO.FieldInputDTO;
import es.caib.invai.api.interna.maintenance.field.DTO.FieldOutputDTO;
import es.caib.invai.api.service.mapper.FieldMapper;
import es.caib.invai.api.persistence.repository.field.FieldCriteria;
import es.caib.invai.api.persistence.repository.field.FieldRepository;
import es.caib.invai.api.persistence.repository.application.core.ApplicationRepository;
import es.caib.invai.api.service.facade.FieldService;
import es.caib.invai.api.service.model.Field;
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
 * Facade service implementation for managing field configurations.
 * Handles structural length checks, status assignments, and filtering via standard pagination rules.
 *
 * @since 1.0.1
 */
@Service
@Slf4j
@Transactional
public class FieldServiceFacadeBean implements FieldService {

    @Autowired
    private FieldMapper fieldMapper;

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    /**
     * Retrieves an active field configuration by its unique database identifier.
     * Evaluates logical deletion properties and structural lifecycle status flags.
     *
     * @param id the unique field metadata record identity pointer
     * @return the mapped {@link FieldOutputDTO} response presentation payload
     * @throws BusinessRuleException if the identity does not match any active record or has been logically soft-deleted
     */
    @Override
    @Transactional(readOnly = true)
    public FieldOutputDTO getById(Long id) {
        log.info("Facade: Fetching field by ID: {}", id);
        Field field = fieldRepository.findById(id);

        if (field == null) {
            throw new BusinessRuleException(Constants.FIELD_NOT_FOUND);
        }

        return fieldMapper.toResponse(field);
    }

    /**
     * Gets a paginated distribution framework containing field records matching pagination rules.
     *
     * @param pageable sorting parameters and tracking page metadata pagination constraints
     * @return a structured page element populated with converted {@link FieldOutputDTO} results
     */
    @Override
    @Transactional(readOnly = true)
    public Page<FieldOutputDTO> getAll(FieldCriteria filter, Pageable pageable) {
        log.info("Facade: Fetching fields via pagination boundaries");
        Page<Field> domainPage = fieldRepository.findAll(filter, pageable);
        return domainPage.map(fieldMapper::toResponse);
    }

    /**
     * Validates structural constraints and registers a new field record within the system core.
     * Enforces domain text sanitization and unicity rules regarding the name of the field.
     *
     * @param inputDTO data transfer container holding properties describing the target field record
     * @return the resulting persistent instance transformed into an {@link FieldOutputDTO} structure
     * @throws BusinessRuleException if text formats fail physical bounds, or if the name
     * conflicts with an already registered field configuration entry
     */
    @Override
    public FieldOutputDTO create(FieldInputDTO inputDTO) {
        log.info("Facade: Creating new field record with name: {}", inputDTO.getName());

        Utils.sanitize(inputDTO);

        if (fieldRepository.existsByNameAndDeletedAtIsNull(inputDTO.getName())) {
            throw new BusinessRuleException(Constants.FIELD_DUPLICATED);
        }

        Field model = fieldMapper.toModelFromInput(inputDTO);

        Field savedModel = fieldRepository.create(model);
        return fieldMapper.toResponse(savedModel);
    }

    /**
     * Mutates an existing active field entity property by replacing metrics with input payload details.
     * Ensures updates do not overlap unique constraint parameters allocated to sibling records.
     *
     * @param id       the unique database resource key indexing the record targeting modification
     * @param inputDTO data update container outlining property changes intended for persistence merge operations
     * @return the modified domain representation mapped down into an {@link FieldOutputDTO}
     * @throws BusinessRuleException if the resource key is non-existent, has been marked soft-deleted,
     * or if input data maps identifier fields owned by another field
     */
    @Override
    public FieldOutputDTO update(Long id, FieldInputDTO inputDTO) {
        log.info("Facade: Updating field with ID: {}", id);

        Field existing = fieldRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException(Constants.FIELD_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        if (fieldRepository.existsByNameAndIdNotAndDeletedAtIsNull(inputDTO.getName(), id)) {
            throw new BusinessRuleException(Constants.FIELD_DUPLICATED);
        }

        fieldMapper.updateModelFromInput(inputDTO, existing);
        return fieldMapper.toResponse(fieldRepository.update(existing, id));
    }

    /**
     * Executes a logical soft-delete transaction lifecycle phase over a field record.
     * Shifts state configurations to inactive indicators and logs audit metrics profiling execution time and session user.
     *
     * @param id the target identifier mapping the field instance intended for deactivation
     * @throws BusinessRuleException if matching field instance descriptions cannot be found, are already soft-deleted,
     * or if there are active application dependencies mapped to this field
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Logically deleting field with ID: {}", id);
        Field existing = fieldRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.FIELD_NOT_FOUND);
        }

        if (existing.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.FIELD_NOT_ACTIVE);
        }

        if (applicationRepository.existsByFieldId(id)) {
            throw new BusinessRuleException(Constants.FIELD_DELETE_HAS_DEPENDENCIES);
        }

        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy(Utils.resolveCurrentUsername());

        fieldRepository.delete(existing);
    }

    /**
     * Reactivates a logically soft-deleted field record back to active state.
     *
     * @param id the target identifier mapping the field instance intended for reactivation
     * @return the reactivated domain representation mapped into an {@link FieldOutputDTO}
     * @throws BusinessRuleException if matching field cannot be found or is already active
     */
    @Override
    public FieldOutputDTO reactivate(Long id) {
        log.info("Facade: Reactivating field with ID: {}", id);
        Field existing = fieldRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.FIELD_NOT_FOUND);
        }

        if (existing.getDeletedAt() == null) {
            throw new BusinessRuleException(Constants.FIELD_ACTIVE);
        }

        existing.setDeletedAt(null);
        existing.setDeletedBy(null);

        Field updatedModel = fieldRepository.update(existing, id);
        return fieldMapper.toResponse(updatedModel);
    }
}
package es.caib.invai.api.ejb;

import es.caib.invai.api.interna.maintenance.admUnit.DTO.AdmUnitInputDTO;
import es.caib.invai.api.interna.maintenance.admUnit.DTO.AdmUnitOutputDTO;
import es.caib.invai.api.persistence.repository.admUnit.AdmUnitCriteria;
import es.caib.invai.api.service.mapper.AdmUnitMapper;
import es.caib.invai.api.persistence.repository.admUnit.AdmUnitRepository;
import es.caib.invai.api.persistence.repository.application.core.ApplicationRepository;
import es.caib.invai.api.service.facade.AdmUnitService;
import es.caib.invai.api.service.model.AdmUnit;
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
 * Facade service implementation for managing Administrative Units (AdmUnit).
 * Handles transactional business operations, validation rules, and logical deletion audit trailing.
 *
 * @since 1.0.1
 */
@Service
@Slf4j
@Transactional
public class AdmUnitServiceFacadeBean implements AdmUnitService {

    @Autowired
    private AdmUnitMapper admUnitMapper;

    @Autowired
    private AdmUnitRepository admUnitRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    /**
     * Retrieves an active administrative unit by its unique database identifier.
     * Evaluates logical deletion properties and structural lifecycle status flags.
     *
     * @param id the unique administrative unit metadata record identity pointer
     * @return the mapped {@link AdmUnitOutputDTO} response presentation payload
     * @throws BusinessRuleException if the identity does not match any active record or has been logically soft-deleted
     */
    @Override
    @Transactional(readOnly = true)
    public AdmUnitOutputDTO getById(Long id) {
        log.info("Facade: Fetching administrative unit by ID: {}", id);
        AdmUnit admUnit = admUnitRepository.findById(id);

        if (admUnit == null) {
            throw new BusinessRuleException(Constants.ERR_ADMUNIT_NOT_FOUND);
        }

        return admUnitMapper.toResponse(admUnit);
    }

    /**
     * Gets a paginated distribution framework containing administrative unit records matching pagination
     * rules and dynamic search criteria.
     *
     * @param filter   dynamic search criteria used to build the query predicates
     * @param pageable sorting parameters and tracking page metadata pagination constraints
     * @return a structured page element populated with converted {@link AdmUnitOutputDTO} results
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AdmUnitOutputDTO> getAll(AdmUnitCriteria filter, Pageable pageable) {
        log.info("Facade: Fetching administrative units via pagination boundaries");
        Page<AdmUnit> domainPage = admUnitRepository.findAll(filter, pageable);
        return domainPage.map(admUnitMapper::toResponse);
    }

    /**
     * Validates structural constraints and registers a new administrative unit within the core.
     * Enforces domain text sanitization and unicity rules regarding unit codes and names.
     *
     * @param inputDTO data transfer container holding properties describing the target unit record
     * @return the resulting persistent instance transformed into an {@link AdmUnitOutputDTO} structure
     * @throws BusinessRuleException if text formats fail physical bounds, or if the code or name
     * conflicts with an already registered administrative unit entry
     */
    @Override
    public AdmUnitOutputDTO create(AdmUnitInputDTO inputDTO) {
        log.info("Facade: Creating new administrative unit with code: {} and name: {}", inputDTO.getCode(), inputDTO.getName());

        Utils.sanitize(inputDTO);

        if (admUnitRepository.existsByCodeAndDeletedAtIsNull(inputDTO.getCode())) {
            throw new BusinessRuleException(Constants.ERR_ADMUNIT_CODE_DUPLICATED);
        }
        if (admUnitRepository.existsByNameAndDeletedAtIsNull(inputDTO.getName())) {
            throw new BusinessRuleException(Constants.ERR_ADMUNIT_DUPLICATED);
        }

        AdmUnit model = admUnitMapper.toModelFromInput(inputDTO);

        AdmUnit savedModel = admUnitRepository.create(model);
        return admUnitMapper.toResponse(savedModel);
    }

    /**
     * Mutates an existing active administrative unit property by replacing metrics with input payload details.
     * Ensures updates do not overlap unique constraint parameters allocated to sibling records.
     *
     * @param id       the unique database resource key indexing the record targeting modification
     * @param inputDTO data update container outlining property changes intended for persistence merge operations
     * @return the modified domain representation mapped down into an {@link AdmUnitOutputDTO}
     * @throws BusinessRuleException if the resource key is non-existent, has been marked soft-deleted,
     * or if input data maps identifier fields owned by another administrative unit
     */
    @Override
    public AdmUnitOutputDTO update(Long id, AdmUnitInputDTO inputDTO) {
        log.info("Facade: Updating administrative unit with ID: {}", id);

        AdmUnit existing = admUnitRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_ADMUNIT_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        if (admUnitRepository.existsByCodeAndIdNotAndDeletedAtIsNull(inputDTO.getCode(), id)) {
            throw new BusinessRuleException(Constants.ERR_ADMUNIT_CODE_DUPLICATED);
        }
        if (admUnitRepository.existsByNameAndIdNotAndDeletedAtIsNull(inputDTO.getName(), id)) {
            throw new BusinessRuleException(Constants.ERR_ADMUNIT_DUPLICATED);
        }

        admUnitMapper.updateModelFromInput(inputDTO, existing);
        return admUnitMapper.toResponse(admUnitRepository.update(existing, id));
    }

    /**
     * Executes a logical soft-delete transaction lifecycle phase over an administrative unit record.
     * Shifts state configurations to inactive indicators and logs audit metrics profiling execution time and session user.
     *
     * @param id the target identifier mapping the administrative unit instance intended for deactivation
     * @throws BusinessRuleException if matching instance descriptions cannot be found, are already soft-deleted,
     * or remain actively bound to application assets
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Logically deleting administrative unit with ID: {}", id);
        AdmUnit existing = admUnitRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_ADMUNIT_NOT_FOUND);
        }

        if (existing.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_ADMUNIT_NOT_ACTIVE);
        }

        if (applicationRepository.existsByAdmUnitId(id)) {
            throw new BusinessRuleException(Constants.ERR_ADMUNIT_DELETE_HAS_DEPENDENCIES);
        }

        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy(Utils.resolveCurrentUsername());

        admUnitRepository.delete(existing);
    }

    /**
     * Reactivates a logically soft-deleted administrative unit record back to active state.
     *
     * @param id the target identifier mapping the administrative unit instance intended for reactivation
     * @return the reactivated domain representation mapped into an {@link AdmUnitOutputDTO}
     * @throws BusinessRuleException if matching administrative unit cannot be found or is already active
     */
    @Override
    public AdmUnitOutputDTO reactivate(Long id) {
        log.info("Facade: Reactivating administrative unit with ID: {}", id);
        AdmUnit existing = admUnitRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_ADMUNIT_NOT_FOUND);
        }

        if (existing.getDeletedAt() == null) {
            throw new BusinessRuleException(Constants.ERR_ADMUNIT_ACTIVE);
        }

        existing.setDeletedAt(null);
        existing.setDeletedBy(null);

        AdmUnit updatedModel = admUnitRepository.update(existing, id);
        return admUnitMapper.toResponse(updatedModel);
    }
}
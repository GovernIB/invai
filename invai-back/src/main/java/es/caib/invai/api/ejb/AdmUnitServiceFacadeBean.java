package es.caib.invai.api.ejb;

import es.caib.invai.api.interna.admUnit.DTO.AdmUnitInputDTO;
import es.caib.invai.api.interna.admUnit.DTO.AdmUnitOutputDTO;
import es.caib.invai.api.utils.SecurityUtils;
import es.caib.invai.api.service.mapper.AdmUnitMapper;
import es.caib.invai.api.persistence.repository.admUnit.AdmUnitRepository;
import es.caib.invai.api.service.facade.AdmUnitService;
import es.caib.invai.api.service.model.AdmUnit;
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
 * Facade service implementation for managing Administrative Units (AdmUnit).
 * Handles transactional business operations, validation rules, and logical deletion audit trailing.
 *
 * @since 1.0.0
 */
@Service
@Slf4j
@Transactional
public class AdmUnitServiceFacadeBean implements AdmUnitService {

    @Autowired
    private AdmUnitMapper admUnitMapper;

    @Autowired
    private AdmUnitRepository admUnitRepository;

    /**
     * Retrieves an administrative unit by its unique identifier.
     *
     * @param id the unique ID of the administrative unit
     * @return the mapped {@link AdmUnitOutputDTO} response
     * @throws BusinessRuleException if no active entity matches the given ID
     */
    @Override
    @Transactional(readOnly = true)
    public AdmUnitOutputDTO getById(Long id) {
        log.info("Facade: Fetching administrative unit by id: {}", id);
        AdmUnit admUnit = admUnitRepository.findById(id);
        if (admUnit == null) {
            throw new BusinessRuleException("exception.admunit.notfound");
        }
        return admUnitMapper.toResponse(admUnit);
    }

    /**
     * Retrieves a paginated list of all administrative units.
     *
     * @param pageable the pagination and sorting parameters
     * @return a {@link Page} containing the requested {@link AdmUnitOutputDTO} elements
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AdmUnitOutputDTO> getAll(Pageable pageable) {
        log.info("Facade: Fetching paged administrative units");
        return admUnitRepository.findAll(pageable).map(admUnitMapper::toResponse);
    }

    /**
     * Creates a new administrative unit after validating that its code and name are unique.
     *
     * @param inputDTO the data payload containing the fields for the new unit
     * @return the created {@link AdmUnitOutputDTO}
     * @throws BusinessRuleException if either the unit code or name already exists in an active record
     */
    @Override
    public AdmUnitOutputDTO create(AdmUnitInputDTO inputDTO) {
        log.info("Facade: Creating administrative unit with code: {} and name: {}", inputDTO.getCode(), inputDTO.getName());

        if (admUnitRepository.existsByCodeAndDeletedAtIsNull(inputDTO.getCode())) {
            throw new BusinessRuleException("exception.admunit.code.duplicated");
        }
        if (admUnitRepository.existsByNameAndDeletedAtIsNull(inputDTO.getName())) {
            throw new BusinessRuleException("exception.admunit.duplicated");
        }

        AdmUnit model = admUnitMapper.toModelFromInput(inputDTO);
        AdmUnit savedModel = admUnitRepository.create(model);
        return admUnitMapper.toResponse(savedModel);
    }

    /**
     * Updates an existing administrative unit's information.
     *
     * @param id       the ID of the target record to update
     * @param inputDTO the updated data payload parameters
     * @return the updated {@link AdmUnitOutputDTO} response
     * @throws BusinessRuleException if the target ID is missing, or if code/name duplications clash with other active items
     */
    @Override
    public AdmUnitOutputDTO update(Long id, AdmUnitInputDTO inputDTO) {
        log.info("Facade: Updating administrative unit ID: {}", id);

        AdmUnit existing = admUnitRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException("exception.admunit.notfound");
        }

        if (admUnitRepository.existsByCodeAndIdNotAndDeletedAtIsNull(inputDTO.getCode(), id)) {
            throw new BusinessRuleException("exception.admunit.code.duplicated");
        }
        if (admUnitRepository.existsByNameAndIdNotAndDeletedAtIsNull(inputDTO.getName(), id)) {
            throw new BusinessRuleException("exception.admunit.duplicated");
        }

        admUnitMapper.updateModelFromInput(inputDTO, existing);
        AdmUnit updatedModel = admUnitRepository.update(existing, id);
        return admUnitMapper.toResponse(updatedModel);
    }

    /**
     * Performs a logical delete on an administrative unit by setting its deletion timestamp
     * and recording the current context username acting upon it.
     *
     * @param id the target administrative unit ID to be deactivated
     * @throws BusinessRuleException if the target unit is not found
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Executing logical delete for administrative unit ID: {}", id);
        AdmUnit existing = admUnitRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException("exception.admunit.notfound");
        }

        existing.setDeletedAt(LocalDateTime.now());

        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy("SYSTEM");

        OidcUserInfo currentUser = (OidcUserInfo) SecurityUtils.getCurrentUser();

        if (currentUser != null && currentUser.getClaims().get("preferred_username") != null) {
            existing.setDeletedBy((String) currentUser.getClaims().get("preferred_username"));
        }

        admUnitRepository.delete(existing);
    }
}
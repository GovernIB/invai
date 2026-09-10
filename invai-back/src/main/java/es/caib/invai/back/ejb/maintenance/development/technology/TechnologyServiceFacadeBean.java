package es.caib.invai.back.ejb.maintenance.development.technology;

import es.caib.invai.back.interna.maintenance.development.technology.DTO.TechnologyInputDTO;
import es.caib.invai.back.interna.maintenance.development.technology.DTO.TechnologyOutputDTO;
import es.caib.invai.back.service.mapper.maintenance.development.technology.TechnologyMapper;
import es.caib.invai.back.persistence.repository.maintenance.development.technology.TechnologyCriteria;
import es.caib.invai.back.persistence.repository.maintenance.development.technology.TechnologyRepository;
import es.caib.invai.back.persistence.repository.application.development.technology.AppTechnologyRepository;
import es.caib.invai.back.service.facade.maintenance.development.technology.TechnologyService;
import es.caib.invai.back.service.model.maintenance.development.technology.Technology;
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
 * Facade service implementation for administrative catalog Technologies.
 *
 * @since 1.0.2
 */
@Service
@Slf4j
@Transactional
public class TechnologyServiceFacadeBean implements TechnologyService {

    /** Mapper responsible for converting between Technology domain models, entities, and DTOs. */
    @Autowired
    private TechnologyMapper technologyMapper;

    /** Outbound port used to persist and retrieve Technology domain models. */
    @Autowired
    private TechnologyRepository technologyRepository;

    /** Repository used to check for existing application-technology dependencies before deletion. */
    @Autowired
    private AppTechnologyRepository appTechnologyRepository;

    /**
     * Retrieves a single technology by its identifier.
     *
     * @param id the technology identifier
     * @return the matching technology as an output DTO
     * @throws BusinessRuleException if no technology exists with the given ID
     */
    @Override
    @Transactional(readOnly = true)
    public TechnologyOutputDTO getById(Long id) {
        log.debug("Facade: Fetching technology by ID: {}", id);
        Technology technology = technologyRepository.findById(id);

        if (technology == null) {
            throw new BusinessRuleException(Constants.ERR_TECHNOLOGYCATALOG_NOT_FOUND);
        }

        return technologyMapper.toResponse(technology);
    }

    /**
     * Retrieves a paginated list of technologies matching the given filter criteria.
     *
     * @param filter   the search criteria to apply
     * @param pageable the pagination and sorting configuration
     * @return a page of matching technologies as output DTOs
     */
    @Override
    @Transactional(readOnly = true)
    public Page<TechnologyOutputDTO> getAll(TechnologyCriteria filter, Pageable pageable) {
        log.debug("Facade: Fetching technologies via pagination boundaries");
        Page<Technology> domainPage = technologyRepository.findAll(filter, pageable);
        return domainPage.map(technologyMapper::toResponse);
    }

    /**
     * Creates a new technology record after sanitizing the input and validating name uniqueness.
     *
     * @param inputDTO the data for the technology to create
     * @return the created technology as an output DTO
     * @throws BusinessRuleException if a non-deleted technology already exists with the same name
     */
    @Override
    public TechnologyOutputDTO create(TechnologyInputDTO inputDTO) {
        log.info("Facade: Creating new technology record with name: {}", inputDTO.getName());

        Utils.sanitize(inputDTO);

        if (technologyRepository.existsByNameAndDeletedAtIsNull(inputDTO.getName())) {
            throw new BusinessRuleException(Constants.ERR_TECHNOLOGYCATALOG_DUPLICATED);
        }

        Technology model = technologyMapper.toModelFromInput(inputDTO);

        Technology savedModel = technologyRepository.create(model);
        return technologyMapper.toResponse(savedModel);
    }

    /**
     * Updates an existing technology record after sanitizing the input and validating name uniqueness.
     *
     * @param id       the identifier of the technology to update
     * @param inputDTO the new data for the technology
     * @return the updated technology as an output DTO
     * @throws BusinessRuleException if the technology does not exist, or another non-deleted
     *                                technology already has the same name
     */
    @Override
    public TechnologyOutputDTO update(Long id, TechnologyInputDTO inputDTO) {
        log.info("Facade: Updating technology with ID: {}", id);

        Technology existing = technologyRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_TECHNOLOGYCATALOG_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        if (technologyRepository.existsByNameAndIdNotAndDeletedAtIsNull(inputDTO.getName(), id)) {
            throw new BusinessRuleException(Constants.ERR_TECHNOLOGYCATALOG_DUPLICATED);
        }

        technologyMapper.updateModelFromInput(inputDTO, existing);
        return technologyMapper.toResponse(technologyRepository.update(existing, id));
    }

    /**
     * Logically deletes a technology by marking its deletion timestamp and author.
     *
     * @param id the identifier of the technology to delete
     * @throws BusinessRuleException if the technology does not exist, is already deleted,
     *                                or is still referenced by application technology assignments
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Logically deleting technology with ID: {}", id);
        Technology existing = technologyRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_TECHNOLOGYCATALOG_NOT_FOUND);
        }

        if (existing.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_TECHNOLOGYCATALOG_NOT_ACTIVE);
        }

        if (appTechnologyRepository.existsByTechnologyId(id)) {
            throw new BusinessRuleException(Constants.ERR_TECHNOLOGYCATALOG_DELETE_HAS_DEPENDENCIES);
        }

        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy(Utils.resolveCurrentUsername());

        technologyRepository.delete(existing);
    }

    /**
     * Reactivates a previously logically-deleted technology by clearing its deletion metadata.
     *
     * @param id the identifier of the technology to reactivate
     * @return the reactivated technology as an output DTO
     * @throws BusinessRuleException if the technology does not exist or is already active
     */
    @Override
    public TechnologyOutputDTO reactivate(Long id) {
        log.info("Facade: Reactivating technology with ID: {}", id);
        Technology existing = technologyRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_TECHNOLOGYCATALOG_NOT_FOUND);
        }

        if (existing.getDeletedAt() == null) {
            throw new BusinessRuleException(Constants.ERR_TECHNOLOGYCATALOG_ACTIVE);
        }

        existing.setDeletedAt(null);
        existing.setDeletedBy(null);

        Technology updatedModel = technologyRepository.update(existing, id);
        return technologyMapper.toResponse(updatedModel);
    }
}

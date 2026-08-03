package es.caib.invai.api.ejb;

import es.caib.invai.api.interna.maintenance.technology.DTO.TechnologyInputDTO;
import es.caib.invai.api.interna.maintenance.technology.DTO.TechnologyOutputDTO;
import es.caib.invai.api.service.mapper.TechnologyMapper;
import es.caib.invai.api.persistence.repository.technology.TechnologyCriteria;
import es.caib.invai.api.persistence.repository.technology.TechnologyRepository;
import es.caib.invai.api.persistence.repository.application.development.technology.AppTechnologyRepository;
import es.caib.invai.api.service.facade.TechnologyService;
import es.caib.invai.api.service.model.Technology;
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
 * Facade service implementation for administrative catalog Technologies.
 *
 * @since 1.0.2
 */
@Service
@Slf4j
@Transactional
public class TechnologyServiceFacadeBean implements TechnologyService {

    @Autowired
    private TechnologyMapper technologyMapper;

    @Autowired
    private TechnologyRepository technologyRepository;

    @Autowired
    private AppTechnologyRepository appTechnologyRepository;

    @Override
    @Transactional(readOnly = true)
    public TechnologyOutputDTO getById(Long id) {
        log.info("Facade: Fetching technology by ID: {}", id);
        Technology technology = technologyRepository.findById(id);

        if (technology == null) {
            throw new BusinessRuleException(Constants.ERR_TECHNOLOGYCATALOG_NOT_FOUND);
        }

        return technologyMapper.toResponse(technology);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TechnologyOutputDTO> getAll(TechnologyCriteria filter, Pageable pageable) {
        log.info("Facade: Fetching technologies via pagination boundaries");
        Page<Technology> domainPage = technologyRepository.findAll(filter, pageable);
        return domainPage.map(technologyMapper::toResponse);
    }

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

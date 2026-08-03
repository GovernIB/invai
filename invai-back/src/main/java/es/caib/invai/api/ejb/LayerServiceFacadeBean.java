package es.caib.invai.api.ejb;

import es.caib.invai.api.interna.maintenance.layer.DTO.LayerInputDTO;
import es.caib.invai.api.interna.maintenance.layer.DTO.LayerOutputDTO;
import es.caib.invai.api.service.mapper.LayerMapper;
import es.caib.invai.api.persistence.repository.layer.LayerCriteria;
import es.caib.invai.api.persistence.repository.layer.LayerRepository;
import es.caib.invai.api.persistence.repository.application.development.technology.AppTechnologyRepository;
import es.caib.invai.api.persistence.repository.technology.TechnologyRepository;
import es.caib.invai.api.service.facade.LayerService;
import es.caib.invai.api.service.model.Layer;
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
 * Facade service implementation for administrative architecture Layers.
 *
 * @since 1.0.2
 */
@Service
@Slf4j
@Transactional
public class LayerServiceFacadeBean implements LayerService {

    @Autowired
    private LayerMapper layerMapper;

    @Autowired
    private LayerRepository layerRepository;

    @Autowired
    private TechnologyRepository technologyRepository;

    @Autowired
    private AppTechnologyRepository appTechnologyRepository;

    @Override
    @Transactional(readOnly = true)
    public LayerOutputDTO getById(Long id) {
        log.info("Facade: Fetching layer by ID: {}", id);
        Layer layer = layerRepository.findById(id);

        if (layer == null) {
            throw new BusinessRuleException(Constants.ERR_LAYER_NOT_FOUND);
        }

        return layerMapper.toResponse(layer);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LayerOutputDTO> getAll(LayerCriteria filter, Pageable pageable) {
        log.info("Facade: Fetching layers via pagination boundaries");
        Page<Layer> domainPage = layerRepository.findAll(filter, pageable);
        return domainPage.map(layerMapper::toResponse);
    }

    @Override
    public LayerOutputDTO create(LayerInputDTO inputDTO) {
        log.info("Facade: Creating new layer record with name: {}", inputDTO.getName());

        Utils.sanitize(inputDTO);

        if (layerRepository.existsByNameAndDeletedAtIsNull(inputDTO.getName())) {
            throw new BusinessRuleException(Constants.ERR_LAYER_DUPLICATED);
        }

        Layer model = layerMapper.toModelFromInput(inputDTO);

        Layer savedModel = layerRepository.create(model);
        return layerMapper.toResponse(savedModel);
    }

    @Override
    public LayerOutputDTO update(Long id, LayerInputDTO inputDTO) {
        log.info("Facade: Updating layer with ID: {}", id);

        Layer existing = layerRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_LAYER_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        if (layerRepository.existsByNameAndIdNotAndDeletedAtIsNull(inputDTO.getName(), id)) {
            throw new BusinessRuleException(Constants.ERR_LAYER_DUPLICATED);
        }

        layerMapper.updateModelFromInput(inputDTO, existing);
        return layerMapper.toResponse(layerRepository.update(existing, id));
    }

    @Override
    public void delete(Long id) {
        log.info("Facade: Logically deleting layer with ID: {}", id);
        Layer existing = layerRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_LAYER_NOT_FOUND);
        }

        if (existing.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_LAYER_NOT_ACTIVE);
        }

        if (technologyRepository.existsByLayerId(id) || appTechnologyRepository.existsByLayerId(id)) {
            throw new BusinessRuleException(Constants.ERR_LAYER_DELETE_HAS_DEPENDENCIES);
        }

        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy(Utils.resolveCurrentUsername());

        layerRepository.delete(existing);
    }

    @Override
    public LayerOutputDTO reactivate(Long id) {
        log.info("Facade: Reactivating layer with ID: {}", id);
        Layer existing = layerRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_LAYER_NOT_FOUND);
        }

        if (existing.getDeletedAt() == null) {
            throw new BusinessRuleException(Constants.ERR_LAYER_ACTIVE);
        }

        existing.setDeletedAt(null);
        existing.setDeletedBy(null);

        Layer updatedModel = layerRepository.update(existing, id);
        return layerMapper.toResponse(updatedModel);
    }
}

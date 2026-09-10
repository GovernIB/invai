package es.caib.invai.back.ejb.application.development.technology;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.development.technology.DTO.AppTechnologyInputDTO;
import es.caib.invai.back.interna.application.development.technology.DTO.AppTechnologyOutputDTO;
import es.caib.invai.back.persistence.repository.application.development.technology.AppTechnologyCriteria;
import es.caib.invai.back.persistence.repository.application.development.technology.AppTechnologyRepository;
import es.caib.invai.back.service.facade.application.development.technology.AppTechnologyService;
import es.caib.invai.back.service.mapper.application.development.technology.AppTechnologyMapper;
import es.caib.invai.back.service.model.application.development.technology.AppTechnology;
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
 * Facade service for the "AppTechnology" list: technology stack entries (layer, technology, version,
 * architecture) that hang many-to-one off a single
 * {@link es.caib.invai.back.service.model.application.development.core.AppDevelopment} record (via
 * {@code appDevelopmentId}) — unlike {@code AppDevelopment} itself, which is a single record per
 * application. No uniqueness check on create: a development can have any number of technology
 * entries, including duplicates for the same layer/technology.
 *
 * @since 1.0.2
 */
@Service
@Slf4j
@Transactional
public class AppTechnologyServiceFacadeBean implements AppTechnologyService {

    /** Mapper between {@link AppTechnology}, its entity, and the technology input/output DTOs. */
    @Autowired
    private AppTechnologyMapper appTechnologyMapper;

    /** Repository handling persistence of {@link AppTechnology} records. */
    @Autowired
    private AppTechnologyRepository appTechnologyRepository;

    /**
     * Fetches the technology entries assigned to a single development, filtered by {@code criteria}
     * and paged. {@code appDevelopmentId} is always applied regardless of {@code criteria}, so this
     * never returns technology records belonging to a different development module.
     *
     * @param appDevelopmentId identifier of the owning development record
     * @param criteria         optional additional filters (status, search), see {@link AppTechnologyCriteria}
     * @param pageable         pagination and sorting parameters
     * @return a page of mapped {@link AppTechnologyOutputDTO} results
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AppTechnologyOutputDTO> getAll(Long appDevelopmentId, AppTechnologyCriteria criteria, Pageable pageable) {
        log.debug("Facade: Fetching paged technology records for Development ID: {}", appDevelopmentId);
        Page<AppTechnology> domainPage = appTechnologyRepository.findAll(appDevelopmentId, criteria, pageable);
        return domainPage.map(appTechnologyMapper::toResponse);
    }

    /**
     * Creates a new technology stack entry linked to a development module. No check is made for an
     * existing entry with the same layer/technology, so duplicates are allowed.
     *
     * @param inputDTO the development reference, layer, technology, version and architecture
     * @return the mapped output DTO for the newly created record
     */
    @Override
    public AppTechnologyOutputDTO create(AppTechnologyInputDTO inputDTO) {
        log.info("Facade: Creating new technology entry for Development ID: {}", inputDTO.getAppDevelopmentId());

        Utils.sanitize(inputDTO);

        AppTechnology domainModel = appTechnologyMapper.toModelFromInput(inputDTO);
        AppTechnology savedModel = appTechnologyRepository.create(domainModel);
        return appTechnologyMapper.toResponse(savedModel);
    }

    /**
     * Updates an existing technology record in place. Does not check {@code deletedAt}, so a
     * soft-deleted record can still be updated through this method.
     *
     * @param id       the technology record's own identifier
     * @param inputDTO the replacement field values to merge onto the existing record
     * @return the mapped output DTO reflecting the applied changes
     * @throws BusinessRuleException if no technology record exists with this id
     */
    @Override
    public AppTechnologyOutputDTO update(Long id, AppTechnologyInputDTO inputDTO) {
        log.info("Facade: Updating technology ID: {}", id);

        AppTechnology existingModel = appTechnologyRepository.findById(id);
        if (existingModel == null) {
            throw new BusinessRuleException(Constants.ERR_TECHNOLOGY_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        appTechnologyMapper.updateModelFromInput(inputDTO, existingModel);
        return appTechnologyMapper.toResponse(appTechnologyRepository.update(existingModel, id));
    }

    /**
     * Soft-deletes a technology record: stamps {@code deletedAt}/{@code deletedBy} and persists
     * them, without removing the row.
     *
     * @param id the technology record's own identifier
     * @throws BusinessRuleException if no technology record exists with this id, or it is already
     * soft-deleted ({@code deletedAt} already set)
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Logically deleting technology ID: {}", id);

        AppTechnology existingModel = appTechnologyRepository.findById(id);
        if (existingModel == null) {
            throw new BusinessRuleException(Constants.ERR_TECHNOLOGY_NOT_FOUND);
        }

        if (existingModel.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_TECHNOLOGY_NOT_ACTIVE);
        }

        existingModel.setDeletedAt(LocalDateTime.now());
        existingModel.setDeletedBy(Utils.resolveCurrentUsername());

        appTechnologyRepository.delete(existingModel);
    }
}

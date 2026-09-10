package es.caib.invai.back.ejb.application.development.provider;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.development.provider.DTO.AppProviderInputDTO;
import es.caib.invai.back.interna.application.development.provider.DTO.AppProviderOutputDTO;
import es.caib.invai.back.persistence.repository.application.development.provider.AppProviderCriteria;
import es.caib.invai.back.persistence.repository.application.development.provider.AppProviderRepository;
import es.caib.invai.back.service.facade.application.development.provider.AppProviderService;
import es.caib.invai.back.service.mapper.application.development.provider.AppProviderMapper;
import es.caib.invai.back.service.model.application.development.provider.AppProvider;
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
 * Facade service for the "AppProvider" list: provider (company) assignments that hang many-to-one
 * off a single {@link es.caib.invai.back.service.model.application.development.core.AppDevelopment}
 * record (via {@code appDevelopmentId}) — unlike {@code AppDevelopment} itself, which is a single
 * record per application. No uniqueness check on create: a development can have any number of
 * provider assignments, including duplicates.
 *
 * @since 1.0.2
 */
@Service
@Slf4j
@Transactional
public class AppProviderServiceFacadeBean implements AppProviderService {

    /** Mapper between {@link AppProvider}, its entity, and the provider input/output DTOs. */
    @Autowired
    private AppProviderMapper appProviderMapper;

    /** Repository handling persistence of {@link AppProvider} records. */
    @Autowired
    private AppProviderRepository appProviderRepository;

    /**
     * Fetches the providers assigned to a single development, filtered by {@code criteria} and paged.
     * {@code appDevelopmentId} is always applied regardless of {@code criteria}, so this never
     * returns provider records belonging to a different development module.
     *
     * @param appDevelopmentId identifier of the owning development record
     * @param criteria         optional additional filters (status, search), see {@link AppProviderCriteria}
     * @param pageable         pagination and sorting parameters
     * @return a page of mapped {@link AppProviderOutputDTO} results
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AppProviderOutputDTO> getAll(Long appDevelopmentId, AppProviderCriteria criteria, Pageable pageable) {
        log.debug("Facade: Fetching paged provider records for Development ID: {}", appDevelopmentId);
        Page<AppProvider> domainPage = appProviderRepository.findAll(appDevelopmentId, criteria, pageable);
        return domainPage.map(appProviderMapper::toResponse);
    }

    /**
     * Creates a new provider assignment linked to a development module. No check is made for an
     * existing assignment with the same company/role, so duplicates are allowed.
     *
     * @param inputDTO the development reference, company name, role, and contract dates
     * @return the mapped output DTO for the newly created record
     */
    @Override
    public AppProviderOutputDTO create(AppProviderInputDTO inputDTO) {
        log.info("Facade: Creating new provider assignment for Development ID: {} and company: {}",
                inputDTO.getAppDevelopmentId(), inputDTO.getCompanyName());

        Utils.sanitize(inputDTO);

        AppProvider domainModel = appProviderMapper.toModelFromInput(inputDTO);
        AppProvider savedModel = appProviderRepository.create(domainModel);
        return appProviderMapper.toResponse(savedModel);
    }

    /**
     * Updates an existing provider record in place. Does not check {@code deletedAt}, so a
     * soft-deleted record can still be updated through this method.
     *
     * @param id       the provider record's own identifier
     * @param inputDTO the replacement field values to merge onto the existing record
     * @return the mapped output DTO reflecting the applied changes
     * @throws BusinessRuleException if no provider record exists with this id
     */
    @Override
    public AppProviderOutputDTO update(Long id, AppProviderInputDTO inputDTO) {
        log.info("Facade: Updating provider ID: {}", id);

        AppProvider existingModel = appProviderRepository.findById(id);
        if (existingModel == null) {
            throw new BusinessRuleException(Constants.ERR_PROVIDER_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        appProviderMapper.updateModelFromInput(inputDTO, existingModel);
        return appProviderMapper.toResponse(appProviderRepository.update(existingModel, id));
    }

    /**
     * Soft-deletes a provider record: stamps {@code deletedAt}/{@code deletedBy} and persists them,
     * without removing the row.
     *
     * @param id the provider record's own identifier
     * @throws BusinessRuleException if no provider record exists with this id, or it is already
     * soft-deleted ({@code deletedAt} already set)
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Logically deleting provider ID: {}", id);

        AppProvider existingModel = appProviderRepository.findById(id);
        if (existingModel == null) {
            throw new BusinessRuleException(Constants.ERR_PROVIDER_NOT_FOUND);
        }

        if (existingModel.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_PROVIDER_NOT_ACTIVE);
        }

        existingModel.setDeletedAt(LocalDateTime.now());
        existingModel.setDeletedBy(Utils.resolveCurrentUsername());

        appProviderRepository.delete(existingModel);
    }
}

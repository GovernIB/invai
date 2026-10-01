package es.caib.invai.back.ejb.application.integration.core;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.integration.core.DTO.AppIntegrationInputDTO;
import es.caib.invai.back.interna.application.integration.core.DTO.AppIntegrationOutputDTO;
import es.caib.invai.back.persistence.repository.application.core.ApplicationRepository;
import es.caib.invai.back.persistence.repository.application.integration.core.AppIntegrationRepository;
import es.caib.invai.back.service.facade.application.integration.core.AppIntegrationService;
import es.caib.invai.back.service.mapper.application.integration.core.AppIntegrationMapper;
import es.caib.invai.back.service.model.application.integration.core.AppIntegration;
import es.caib.invai.back.utils.Constants;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Facade service implementing CRUD for the "Integracio" anchor. Like its
 * AppData/AppSecurity/AppAccessibility counterparts, this anchor is auto-created when the owning
 * {@code Application} is created (see {@code ApplicationServiceFacadeBean}); {@link #create}
 * exists mainly to backfill applications created before this anchor existed, and rejects the
 * request if the target application already has an active anchor.
 *
 * @since 1.0.5
 */
@Service
@Slf4j
@Transactional
public class AppIntegrationServiceFacadeBean implements AppIntegrationService {

    /** Mapper converting between AppIntegration domain models, entities, and DTOs. */
    @Autowired
    private AppIntegrationMapper appIntegrationMapper;

    /** Repository handling persistence of the "Integracio" anchor linked to an application. */
    @Autowired
    private AppIntegrationRepository appIntegrationRepository;

    /** Repository port used to validate the referenced application actually exists. */
    @Autowired
    private ApplicationRepository applicationRepository;

    @Override
    @Transactional(readOnly = true)
    public AppIntegrationOutputDTO getById(Long id) {
        log.debug("Facade: Fetching application integration anchor for ID: {}", id);
        AppIntegration domain = appIntegrationRepository.findById(id);
        return domain == null ? null : appIntegrationMapper.toResponse(domain);
    }

    @Override
    public AppIntegrationOutputDTO create(AppIntegrationInputDTO inputDTO) {
        log.info("Facade: Persisting new application integration anchor for application ID: {}", inputDTO.getApplicationId());

        if (applicationRepository.findById(inputDTO.getApplicationId()) == null) {
            throw new BusinessRuleException(Constants.ERR_APP_NOT_FOUND);
        }

        AppIntegration existing = appIntegrationRepository.findByApplicationId(inputDTO.getApplicationId());
        if (existing != null && existing.getDeletedAt() == null) {
            throw new BusinessRuleException(Constants.ERR_APP_INTEGRATION_ALREADY_EXISTS);
        }

        Utils.sanitize(inputDTO);

        AppIntegration domainModel = appIntegrationMapper.toModelFromInput(inputDTO);
        AppIntegration savedModel = appIntegrationRepository.create(domainModel);
        return appIntegrationMapper.toResponse(savedModel);
    }

    @Override
    public AppIntegrationOutputDTO update(Long id, AppIntegrationInputDTO inputDTO) {
        log.info("Facade: Updating application integration anchor ID: {}", id);

        AppIntegration existingModel = appIntegrationRepository.findById(id);
        if (existingModel == null) {
            throw new BusinessRuleException(Constants.ERR_APP_INTEGRATION_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        appIntegrationMapper.updateModelFromInput(inputDTO, existingModel);
        return appIntegrationMapper.toResponse(appIntegrationRepository.update(existingModel, id));
    }

    @Override
    public void delete(Long id) {
        log.info("Facade: Logically deleting application integration anchor ID: {}", id);

        AppIntegration existingModel = appIntegrationRepository.findById(id);
        if (existingModel == null) {
            throw new BusinessRuleException(Constants.ERR_APP_INTEGRATION_NOT_FOUND);
        }

        existingModel.setDeletedAt(LocalDateTime.now());
        existingModel.setDeletedBy(Utils.resolveCurrentUsername());

        appIntegrationRepository.delete(existingModel);
    }
}

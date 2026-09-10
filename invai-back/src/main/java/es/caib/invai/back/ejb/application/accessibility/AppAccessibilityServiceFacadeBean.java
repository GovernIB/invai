package es.caib.invai.back.ejb.application.accessibility;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.accessibility.DTO.AppAccessibilityInputDTO;
import es.caib.invai.back.interna.application.accessibility.DTO.AppAccessibilityOutputDTO;
import es.caib.invai.back.persistence.repository.application.accessibility.AppAccessibilityRepository;
import es.caib.invai.back.service.facade.application.accessibility.AppAccessibilityService;
import es.caib.invai.back.service.mapper.application.accessibility.AppAccessibilityMapper;
import es.caib.invai.back.service.model.application.accessibility.AppAccessibility;
import es.caib.invai.back.utils.Constants;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Facade service implementing CRUD for the "AppAccessibility" (Accessibilitat) tab anchor.
 * Unlike its AppDevelopment/AppSecurity/AppInformationSystemDb/AppResponsibleAuthorized
 * counterparts, this anchor is never auto-created when an application is created or updated —
 * {@link #create} must be called explicitly. A missing anchor is treated by
 * {@code ApplicationServiceFacadeBean#isAccessibilityIncomplete} as an incomplete "Accessibilitat"
 * tab. At most one active (non soft-deleted) anchor is allowed per application, enforced by
 * {@link #create}.
 *
 * @since 1.0.4
 */
@Service
@Slf4j
@Transactional
public class AppAccessibilityServiceFacadeBean implements AppAccessibilityService {

    /** Mapper converting between AppAccessibility domain models, entities, and DTOs. */
    @Autowired
    private AppAccessibilityMapper appAccessibilityMapper;

    /** Repository handling persistence of the "AppAccessibility" tab linked to an application. */
    @Autowired
    private AppAccessibilityRepository appAccessibilityRepository;

    /**
     * Fetches the accessibility anchor by its own primary key. Unlike {@link #update} and
     * {@link #delete}, this does not throw when no such anchor exists: the MapStruct mapper
     * simply returns {@code null} for a {@code null} input model, so the caller gets back a
     * {@code null} DTO rather than a "not found" error.
     *
     * @param id primary key of the accessibility anchor
     * @return the mapped anchor, or {@code null} if no anchor exists with this ID
     */
    @Override
    @Transactional(readOnly = true)
    public AppAccessibilityOutputDTO getById(Long id) {
        log.debug("Facade: Fetching application accessibility anchor for ID: {}", id);
        AppAccessibility domain = appAccessibilityRepository.findById(id);
        return appAccessibilityMapper.toResponse(domain);
    }

    /**
     * Creates a new accessibility anchor for the application referenced by
     * {@code inputDTO.getApplicationId()}. Rejects the request if that application already has an
     * active (non soft-deleted) anchor; a previously soft-deleted anchor for the same application
     * does not block creating a new one.
     *
     * @param inputDTO creation payload; {@code applicationId} identifies the owning application
     * @return the newly created anchor, mapped to its outbound representation
     * @throws BusinessRuleException if the target application already has an active accessibility anchor
     */
    @Override
    public AppAccessibilityOutputDTO create(AppAccessibilityInputDTO inputDTO) {
        log.info("Facade: Persisting new application accessibility anchor for application ID: {}", inputDTO.getApplicationId());

        AppAccessibility existing = appAccessibilityRepository.findByApplicationId(inputDTO.getApplicationId());
        if (existing != null && existing.getDeletedAt() == null) {
            throw new BusinessRuleException(Constants.ERR_APP_ACCESSIBILITY_ALREADY_EXISTS);
        }

        Utils.sanitize(inputDTO);

        AppAccessibility domainModel = appAccessibilityMapper.toModelFromInput(inputDTO);
        AppAccessibility savedModel = appAccessibilityRepository.create(domainModel);
        return appAccessibilityMapper.toResponse(savedModel);
    }

    /**
     * Applies the given payload onto the accessibility anchor identified by {@code id}. Does not
     * check whether the anchor is currently soft-deleted — unlike {@link #delete}, a
     * previously-deleted anchor can still be updated through this method.
     *
     * @param id       primary key of the accessibility anchor to update
     * @param inputDTO payload with the new field values
     * @return the updated anchor, mapped to its outbound representation
     * @throws BusinessRuleException if no anchor exists with the given ID
     */
    @Override
    public AppAccessibilityOutputDTO update(Long id, AppAccessibilityInputDTO inputDTO) {
        log.info("Facade: Updating application accessibility anchor ID: {}", id);

        AppAccessibility existingModel = appAccessibilityRepository.findById(id);
        if (existingModel == null) {
            throw new BusinessRuleException(Constants.ERR_APP_ACCESSIBILITY_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        appAccessibilityMapper.updateModelFromInput(inputDTO, existingModel);
        return appAccessibilityMapper.toResponse(appAccessibilityRepository.update(existingModel, id));
    }

    /**
     * Soft-deletes the accessibility anchor identified by {@code id}, stamping {@code deletedAt}/
     * {@code deletedBy} rather than removing the row.
     *
     * @param id primary key of the accessibility anchor to delete
     * @throws BusinessRuleException if no anchor exists with the given ID, or it is already soft-deleted
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Logically deleting application accessibility anchor ID: {}", id);

        AppAccessibility existingModel = appAccessibilityRepository.findById(id);
        if (existingModel == null) {
            throw new BusinessRuleException(Constants.ERR_APP_ACCESSIBILITY_NOT_FOUND);
        }

        if (existingModel.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_APP_ACCESSIBILITY_NOT_ACTIVE);
        }

        existingModel.setDeletedAt(LocalDateTime.now());
        existingModel.setDeletedBy(Utils.resolveCurrentUsername());

        appAccessibilityRepository.delete(existingModel);
    }
}

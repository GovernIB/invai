package es.caib.invai.back.ejb.application.development.core;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.development.core.DTO.DevelopmentInputDTO;
import es.caib.invai.back.interna.application.development.core.DTO.DevelopmentOutputDTO;
import es.caib.invai.back.persistence.repository.application.development.core.AppDevelopmentRepository;
import es.caib.invai.back.service.facade.application.development.core.AppDevelopmentService;
import es.caib.invai.back.service.mapper.application.development.core.AppDevelopmentMapper;
import es.caib.invai.back.service.model.application.development.core.AppDevelopment;
import es.caib.invai.back.utils.Constants;
import es.caib.invai.back.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Facade service for the "AppDevelopment" tab: the single development record attached 1-to-1 to an
 * {@link es.caib.invai.back.service.model.application.core.Application} (see {@code applicationId} on
 * {@link AppDevelopment}). Unlike {@link es.caib.invai.back.ejb.application.development.provider.AppProviderServiceFacadeBean}
 * and {@link es.caib.invai.back.ejb.application.development.technology.AppTechnologyServiceFacadeBean},
 * which manage lists hanging off a development record, this facade has no list-scoped {@code getAll}:
 * {@link #create} refuses a second active record for the same application, and {@link #getById} looks
 * the record up by its own primary key rather than by application id.
 *
 * @since 1.0.2
 */
@Service
@Slf4j
@Transactional
public class AppDevelopmentServiceFacadeBean implements AppDevelopmentService {

    /** Mapper between {@link AppDevelopment}, its entity, and the development input/output DTOs. */
    @Autowired
    private AppDevelopmentMapper appDevelopmentMapper;

    /** Repository handling persistence of {@link AppDevelopment} records. */
    @Autowired
    private AppDevelopmentRepository appDevelopmentRepository;

    /**
     * Fetches a development record by its own primary key (not the parent application's id).
     * Unlike {@link #update} and {@link #delete}, this does not check existence itself: if no
     * record matches, {@code findById} returns {@code null} and the generated mapper's null-check
     * passes that straight through, so this method silently returns {@code null} rather than
     * throwing {@link BusinessRuleException}.
     *
     * @param id the development record's own identifier
     * @return the mapped output DTO, or {@code null} if no record has this id
     */
    @Override
    @Transactional(readOnly = true)
    public DevelopmentOutputDTO getById(Long id) {
        log.debug("Facade: Fetching development record for ID: {}", id);
        AppDevelopment domain = appDevelopmentRepository.findById(id);
        return appDevelopmentMapper.toResponse(domain);
    }

    /**
     * Creates the development record for an application. Enforces the 1-to-1 relationship: if an
     * active (non-soft-deleted) development record already exists for {@code inputDTO.applicationId},
     * creation is rejected rather than creating a second one. A previously soft-deleted record for
     * the same application does not block this, since only {@code existing.getDeletedAt() == null}
     * triggers the check.
     *
     * @param inputDTO the application, environment and lookup references for the new record
     * @return the mapped output DTO for the newly created record
     * @throws BusinessRuleException if the target application already has an active development record
     */
    @Override
    public DevelopmentOutputDTO create(DevelopmentInputDTO inputDTO) {
        log.info("Facade: Creating new development for Application ID: {} and Environment ID: {}",
                inputDTO.getApplicationId(), inputDTO.getEnvironmentId());

        AppDevelopment existing = appDevelopmentRepository.findByApplicationId(inputDTO.getApplicationId());
        if (existing != null && existing.getDeletedAt() == null) {
            throw new BusinessRuleException(Constants.ERR_DEVELOPMENT_ALREADY_EXISTS);
        }

        Utils.sanitize(inputDTO);

        AppDevelopment domainModel = appDevelopmentMapper.toModelFromInput(inputDTO);
        AppDevelopment savedModel = appDevelopmentRepository.create(domainModel);
        return appDevelopmentMapper.toResponse(savedModel);
    }

    /**
     * Updates an existing development record in place. Unlike {@link #create}, this does not check
     * {@code deletedAt}, so a soft-deleted record can still be updated through this method.
     *
     * @param id       the development record's own identifier
     * @param inputDTO the replacement field values to merge onto the existing record
     * @return the mapped output DTO reflecting the applied changes
     * @throws BusinessRuleException if no development record exists with this id
     */
    @Override
    public DevelopmentOutputDTO update(Long id, DevelopmentInputDTO inputDTO) {
        log.info("Facade: Updating development ID: {}", id);

        AppDevelopment existingModel = appDevelopmentRepository.findById(id);
        if (existingModel == null) {
            throw new BusinessRuleException(Constants.ERR_DEVELOPMENT_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        appDevelopmentMapper.updateModelFromInput(inputDTO, existingModel);
        return appDevelopmentMapper.toResponse(appDevelopmentRepository.update(existingModel, id));
    }

    /**
     * Soft-deletes a development record: stamps {@code deletedAt}/{@code deletedBy} and persists
     * them, without removing the row.
     *
     * @param id the development record's own identifier
     * @throws BusinessRuleException if no development record exists with this id, or it is already
     * soft-deleted ({@code deletedAt} already set)
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Logically deleting development ID: {}", id);

        AppDevelopment existingModel = appDevelopmentRepository.findById(id);
        if (existingModel == null) {
            throw new BusinessRuleException(Constants.ERR_DEVELOPMENT_NOT_FOUND);
        }

        if (existingModel.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_DEVELOPMENT_NOT_ACTIVE);
        }

        existingModel.setDeletedAt(LocalDateTime.now());
        existingModel.setDeletedBy(Utils.resolveCurrentUsername());

        appDevelopmentRepository.delete(existingModel);
    }
}

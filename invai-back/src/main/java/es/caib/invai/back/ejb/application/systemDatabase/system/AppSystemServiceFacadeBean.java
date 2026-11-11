package es.caib.invai.back.ejb.application.systemDatabase.system;

import es.caib.invai.back.interna.application.system_database.system.DTO.AppSystemInputDTO;
import es.caib.invai.back.interna.application.system_database.system.DTO.AppSystemOutputDTO;
import es.caib.invai.back.persistence.repository.application.system_database.system.AppSystemCriteria;
import es.caib.invai.back.persistence.repository.application.system_database.system.AppSystemRepository;
import es.caib.invai.back.service.mapper.application.system_database.system.AppSystemMapper;
import es.caib.invai.back.service.facade.application.system_database.system.AppSystemService;
import es.caib.invai.back.service.model.application.system_database.system.AppSystem;
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
import java.util.Objects;

/**
 * Facade service implementation for managing application-to-system mapping core distributions.
 * Orchestrates transactional mechanics, model mapping mutations, and validation constraints verification.
 *
 * @since 1.0.1
 */
@Service
@Slf4j
@Transactional
public class AppSystemServiceFacadeBean implements AppSystemService {

    /** MapStruct mapper handling transformations between entities, domain models, and DTO layouts. */
    @Autowired
    private AppSystemMapper appSystemMapper;

    /** Infrastructure outbound repository port managing relational lifecycle data operations. */
    @Autowired
    private AppSystemRepository appSystemRepository;

    /**
     * Streams partitioned chunk metrics using pagination layout boundaries.
     *
     * @param pageable pagination layout boundaries and sorting rules configuration
     * @return a partitioned matrix page containing mapped output definitions
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AppSystemOutputDTO> getAll(Long informationSystemDbId, AppSystemCriteria criteria, Pageable pageable) {
        log.debug("Facade: Fetching application-system links via pagination boundaries for informationSystemDbId ID: {}", informationSystemDbId);
        Page<AppSystem> domainPage = appSystemRepository.findAll(informationSystemDbId, criteria, pageable);
        return domainPage.map(appSystemMapper::toResponse);
    }

    /**
     * Registers a unique link tracking profile connecting an information system database grouping to host system infrastructure.
     * Performs sanitization and validates relational integrity rules before commit hooks trigger.
     * A link that already exists but was logically deleted is reactivated instead of creating a duplicate.
     *
     * @param inputDTO properties dataset containing link mappings and configuration variables
     * @return the newly created (or reactivated) snapshot parameters state model
     * @throws BusinessRuleException if an active link already exists for the identical information system database and system
     */
    @Override
    public AppSystemOutputDTO create(AppSystemInputDTO inputDTO) {
        log.info("Facade: Mapping new infrastructure link assignment configuration for Information System DB ID: {} to System ID: {}",
                inputDTO.getInformationSystemDbId(), inputDTO.getSystemId());

        Utils.sanitize(inputDTO);

        AppSystem existingModel = appSystemRepository.findByInformationSystemDbIdAndSystemId(
                inputDTO.getInformationSystemDbId(), inputDTO.getSystemId());
        if (Objects.nonNull(existingModel)) {
            if (Objects.isNull(existingModel.getDeletedAt())) {
                throw new BusinessRuleException(Constants.ERR_APPLICATIONSYSTEM_LINK_DUPLICATED);
            }
            log.info("Facade: Reactivating the logically deleted link ID: {} instead of creating a new one", existingModel.getId());
            existingModel.setDeletedAt(null);
            existingModel.setDeletedBy(null);
            return appSystemMapper.toResponse(appSystemRepository.update(existingModel, existingModel.getId()));
        }

        AppSystem model = appSystemMapper.toModelFromInput(inputDTO);
        AppSystem savedModel = appSystemRepository.create(model);
        return appSystemMapper.toResponse(savedModel);
    }

    /**
     * Executes soft deactivation over targeted application system relationships.
     * Updates structural tracking audit traces to mirror administrative termination states.
     *
     * @param id persistent tracking row database reference index targeting removal execution paths
     * @throws BusinessRuleException if target data element cannot be resolved or has already undergone soft deactivation routines
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Logically soft deleting application system linking tracking index row for ID: {}", id);
        AppSystem existing = appSystemRepository.findById(id);

        if (Objects.isNull(existing)) {
            throw new BusinessRuleException(Constants.ERR_APPLICATIONSYSTEM_NOT_FOUND);
        }

        if (Objects.nonNull(existing.getDeletedAt())) {
            throw new BusinessRuleException(Constants.ERR_APPLICATIONSYSTEM_NOT_ACTIVE);
        }

        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy(Utils.resolveCurrentUsername());

        appSystemRepository.delete(existing);
    }
}

package es.caib.invai.back.ejb.application.system_database.database;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.application.system_database.database.DTO.AppDatabaseInputDTO;
import es.caib.invai.back.interna.application.system_database.database.DTO.AppDatabaseOutputDTO;
import es.caib.invai.back.persistence.repository.application.system_database.database.AppDatabaseCriteria;
import es.caib.invai.back.persistence.repository.application.system_database.database.AppDatabaseRepository;
import es.caib.invai.back.service.facade.application.system_database.database.AppDatabaseService;
import es.caib.invai.back.service.mapper.application.system_database.database.AppDatabaseMapper;
import es.caib.invai.back.service.model.application.system_database.database.AppDatabase;
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
 * Facade service implementation for managing application-to-database mapping core distributions.
 * Orchestrates transactional mechanics, model mapping mutations, and validation constraints verification.
 *
 * @since 1.0.2
 */
@Service
@Slf4j
@Transactional
public class AppDatabaseServiceFacadeBean implements AppDatabaseService {

    /** MapStruct mapper handling transformations between entities, domain models, and DTO layouts. */
    @Autowired
    private AppDatabaseMapper appDatabaseMapper;

    /** Infrastructure outbound repository port managing relational lifecycle data operations. */
    @Autowired
    private AppDatabaseRepository appDatabaseRepository;

    /**
     * Streams partitioned chunk metrics using pagination layout boundaries, scoped to a single
     * parent application. Never returns database links belonging to other applications.
     *
     * @param informationSystemDbId mandatory parent application identifier scoping the result set
     * @param pageable      pagination layout boundaries and sorting rules configuration
     * @return a partitioned matrix page containing mapped output definitions
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AppDatabaseOutputDTO> getAll(Long informationSystemDbId, AppDatabaseCriteria criteria, Pageable pageable) {
        log.info("Facade: Executing dynamic search pattern pipeline across application database components for informationSystemDb ID: {}", informationSystemDbId);
        Page<AppDatabase> domainPage = appDatabaseRepository.findAll(informationSystemDbId, criteria, pageable);
        return domainPage.map(appDatabaseMapper::toResponse);
    }

    /**
     * Registers a unique link tracking profile connecting an information system database grouping to database infrastructure.
     * Performs sanitization and validates relational integrity rules before commit hooks trigger.
     *
     * @param inputDTO properties dataset containing link mappings and configuration variables
     * @return the newly created snapshot parameters state model
     * @throws BusinessRuleException if a mapping assignment already exists for the identical information system database, database, and environment triplet
     */
    @Override
    public AppDatabaseOutputDTO create(AppDatabaseInputDTO inputDTO) {
        log.info("Facade: Initializing deployment checks before persisting application registry tracking metadata for Information System DB ID: {} to DB ID: {}",
                inputDTO.getInformationSystemDbId(), inputDTO.getDatabaseId());

        Utils.sanitize(inputDTO);

        if (appDatabaseRepository.existsByInformationSystemDbIdAndDatabaseId(
                inputDTO.getInformationSystemDbId(), inputDTO.getDatabaseId())) {
            log.error("Facade: Conflict encountered. Compound layout matching criteria already exists in active register");
            throw new BusinessRuleException(Constants.ERR_APP_DATABASE_DUPLICATED);
        }

        AppDatabase domainModel = appDatabaseMapper.toModelFromInput(inputDTO);
        AppDatabase savedModel = appDatabaseRepository.create(domainModel);
        return appDatabaseMapper.toResponse(savedModel);
    }

    /**
     * Modifies mutable tracking link variables belonging to an active existing metadata allocation mapping.
     *
     * @param id       targeted structural identifier element index
     * @param inputDTO property data variables mapping structural items to be merged
     * @return current modified configuration state properties details wrapper
     * @throws BusinessRuleException if target record is missing, logically deactivated, or if updating the fields violates uniqueness constraints
     */
    @Override
    public AppDatabaseOutputDTO update(Long id, AppDatabaseInputDTO inputDTO) {
        log.info("Facade: Initiating security validation rules check prior to executing transaction merge on ID: {}", id);

        AppDatabase existingModel = appDatabaseRepository.findById(id);
        if (existingModel == null) {
            throw new BusinessRuleException(Constants.ERR_APP_DATABASE_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        if (appDatabaseRepository.existsByUniqueCombinationExcludingId(
                inputDTO.getInformationSystemDbId(), inputDTO.getDatabaseId(), id)) {
            log.error("Facade: Mutation failed due to uniqueness collision across target composite layout matrices");
            throw new BusinessRuleException(Constants.ERR_APP_DATABASE_DUPLICATED);
        }

        appDatabaseMapper.updateModelFromInput(inputDTO, existingModel);
        return appDatabaseMapper.toResponse(appDatabaseRepository.update(existingModel, id));
    }

    /**
     * Executes soft deactivation over targeted application database relationships.
     * Updates structural tracking audit traces to mirror administrative termination states.
     *
     * @param id persistent tracking row database reference index targeting removal execution paths
     * @throws BusinessRuleException if target data element cannot be resolved or has already undergone soft deactivation routines
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Evaluating infrastructure components prior to setting logical deletion properties for target: {}", id);

        AppDatabase existingModel = appDatabaseRepository.findById(id);
        if (existingModel == null) {
            throw new BusinessRuleException(Constants.ERR_APP_DATABASE_NOT_FOUND);
        }

        if (existingModel.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_APP_DATABASE_NOT_ACTIVE);
        }

        existingModel.setDeletedAt(LocalDateTime.now());
        existingModel.setDeletedBy(Utils.resolveCurrentUsername());

        appDatabaseRepository.delete(existingModel);
        log.info("Facade: Completed logical deprecation process tree successfully for entity tracking mapping");
    }
}

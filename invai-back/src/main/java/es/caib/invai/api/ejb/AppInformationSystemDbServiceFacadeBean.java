package es.caib.invai.api.ejb;

import es.caib.invai.api.exception.BusinessRuleException;
import es.caib.invai.api.interna.application.system_database.core.DTO.AppInformationSystemDbInputDTO;
import es.caib.invai.api.interna.application.system_database.core.DTO.AppInformationSystemDbOutputDTO;
import es.caib.invai.api.persistence.repository.application.system_database.core.AppInformationSystemDbRepository;
import es.caib.invai.api.service.facade.AppInformationSystemDbService;
import es.caib.invai.api.service.mapper.AppInformationSystemDbMapper;
import es.caib.invai.api.service.model.AppInformationSystemDb;
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
 * Facade service implementation for managing application-to-information-system-database groupings.
 * Orchestrates transactional mechanics, model mapping mutations, and validation constraints verification.
 *
 * @since 1.0.2
 */
@Service
@Slf4j
@Transactional
public class AppInformationSystemDbServiceFacadeBean implements AppInformationSystemDbService {

    /**
     * MapStruct mapper handling transformations between entities, domain models, and DTO layouts.
     */
    @Autowired
    private AppInformationSystemDbMapper appInformationSystemDbMapper;

    /**
     * Infrastructure outbound repository port managing relational lifecycle data operations.
     */
    @Autowired
    private AppInformationSystemDbRepository appInformationSystemDbRepository;

    /**
     * Streams partitioned chunk metrics using pagination layout boundaries, scoped to a single
     * parent application. Never returns groupings belonging to other applications.
     *
     * @param id mandatory parent application identifier scoping the result set
     * @return a partitioned matrix page containing mapped output definitions
     */
    @Override
    @Transactional(readOnly = true)
    public AppInformationSystemDbOutputDTO getById(Long id) {
        log.info("Facade: Fetching paged application information system database groupings for informationSystemDb ID: {}", id);
        AppInformationSystemDb domain = appInformationSystemDbRepository.findById(id);
        return (appInformationSystemDbMapper.toResponse(domain));
    }

    /**
     * Registers a new information system database grouping owned by a corporate application.
     *
     * @param inputDTO property dataset containing the application reference and observation notes
     * @return the newly created snapshot parameters state model
     */
    @Override
    public AppInformationSystemDbOutputDTO create(AppInformationSystemDbInputDTO inputDTO) {

        Utils.sanitize(inputDTO);

        AppInformationSystemDb domainModel = appInformationSystemDbMapper.toModelFromInput(inputDTO);
        AppInformationSystemDb savedModel = appInformationSystemDbRepository.create(domainModel);
        return appInformationSystemDbMapper.toResponse(savedModel);
    }

    /**
     * Modifies mutable tracking variables belonging to an active existing grouping.
     *
     * @param id       targeted structural identifier element index
     * @param inputDTO property data variables mapping structural items to be merged
     * @return current modified configuration state properties details wrapper
     * @throws BusinessRuleException if the target record is missing or logically deactivated
     */
    @Override
    public AppInformationSystemDbOutputDTO update(Long id, AppInformationSystemDbInputDTO inputDTO) {
        log.info("Facade: Updating application information system database ID: {}", id);

        AppInformationSystemDb existingModel = appInformationSystemDbRepository.findById(id);
        if (existingModel == null) {
            throw new BusinessRuleException(Constants.ERR_APP_INFORMATION_SYSTEM_DB_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        appInformationSystemDbMapper.updateModelFromInput(inputDTO, existingModel);
        return appInformationSystemDbMapper.toResponse(appInformationSystemDbRepository.update(existingModel, id));
    }

    /**
     * Executes soft deactivation over the targeted application information system database grouping.
     *
     * @param id persistent tracking row database reference index targeting removal execution paths
     * @throws BusinessRuleException if target data element cannot be resolved or has already undergone soft deactivation
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Logically deleting application information system database ID: {}", id);

        AppInformationSystemDb existingModel = appInformationSystemDbRepository.findById(id);
        if (existingModel == null) {
            throw new BusinessRuleException(Constants.ERR_APP_INFORMATION_SYSTEM_DB_NOT_FOUND);
        }

        if (existingModel.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_APP_INFORMATION_SYSTEM_DB_NOT_ACTIVE);
        }

        existingModel.setDeletedAt(LocalDateTime.now());
        existingModel.setDeletedBy(Utils.resolveCurrentUsername());

        appInformationSystemDbRepository.delete(existingModel);
    }
}

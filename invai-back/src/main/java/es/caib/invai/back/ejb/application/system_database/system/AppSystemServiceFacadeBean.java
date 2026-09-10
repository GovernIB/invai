package es.caib.invai.back.ejb.application.system_database.system;

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
     *
     * @param inputDTO properties dataset containing link mappings and configuration variables
     * @return the newly created snapshot parameters state model
     * @throws BusinessRuleException if a mapping assignment already exists for the identical information system database, system, and environment triplet
     */
    @Override
    public AppSystemOutputDTO create(AppSystemInputDTO inputDTO) {
        log.info("Facade: Mapping new infrastructure link assignment configuration for Information System DB ID: {} to System ID: {}",
                inputDTO.getInformationSystemDbId(), inputDTO.getSystemId());

        Utils.sanitize(inputDTO);

        if (appSystemRepository.existsByInformationSystemDbAndSystem(
                inputDTO.getInformationSystemDbId(), inputDTO.getSystemId())) {
            throw new BusinessRuleException(Constants.ERR_APPLICATIONSYSTEM_LINK_DUPLICATED);
        }

        AppSystem model = appSystemMapper.toModelFromInput(inputDTO);
        AppSystem savedModel = appSystemRepository.create(model);
        return appSystemMapper.toResponse(savedModel);
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
    public AppSystemOutputDTO update(Long id, AppSystemInputDTO inputDTO) {
        log.info("Facade: Modifying metadata links matching active tracker index for ID: {}", id);

        AppSystem existing = appSystemRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_APPLICATIONSYSTEM_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        if (appSystemRepository.existsByInformationSystemDbAndSystemAndIdNot(
                inputDTO.getInformationSystemDbId(), inputDTO.getSystemId(), id)) {
            throw new BusinessRuleException(Constants.ERR_APPLICATIONSYSTEM_LINK_OWNED_BY_OTHER);
        }

        appSystemMapper.updateModelFromInput(inputDTO, existing);
        return appSystemMapper.toResponse(appSystemRepository.update(existing, id));
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

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_APPLICATIONSYSTEM_NOT_FOUND);
        }

        if (existing.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_APPLICATIONSYSTEM_NOT_ACTIVE);
        }

        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy(Utils.resolveCurrentUsername());

        appSystemRepository.delete(existing);
    }
}

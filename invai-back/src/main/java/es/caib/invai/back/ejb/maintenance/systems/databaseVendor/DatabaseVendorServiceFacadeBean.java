package es.caib.invai.back.ejb.maintenance.systems.databaseVendor;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.maintenance.systems.databaseVendor.DTO.DatabaseVendorInputDTO;
import es.caib.invai.back.interna.maintenance.systems.databaseVendor.DTO.DatabaseVendorOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.systems.databaseVendor.DatabaseVendorCriteria;
import es.caib.invai.back.persistence.repository.maintenance.systems.databaseVendor.DatabaseVendorRepository;
import es.caib.invai.back.service.facade.maintenance.systems.databaseVendor.DatabaseVendorService;
import es.caib.invai.back.service.mapper.maintenance.systems.databaseVendor.DatabaseVendorMapper;
import es.caib.invai.back.service.model.maintenance.systems.databaseVendor.DatabaseVendor;
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
 * Facade service implementation for managing database vendor/type catalog configurations.
 * Enforces transactional safety bounds and verifies logical deletion metrics.
 *
 * @since 1.0.2
 */
@Service
@Slf4j
@Transactional
public class DatabaseVendorServiceFacadeBean implements DatabaseVendorService {

    /** Mapper converting between DatabaseVendor domain models, entities, and DTOs. */
    @Autowired
    private DatabaseVendorMapper databaseVendorMapper;

    /** Outbound port used to persist and query database vendor/type catalog records. */
    @Autowired
    private DatabaseVendorRepository databaseVendorRepository;

    /**
     * Retrieves an active database vendor by its unique identifier.
     *
     * @param id the unique database vendor record identifier
     * @return the mapped {@link DatabaseVendorOutputDTO} response
     * @throws BusinessRuleException if no matching record exists
     */
    @Override
    @Transactional(readOnly = true)
    public DatabaseVendorOutputDTO getById(Long id) {
        log.debug("Facade: Fetching database vendor by ID: {}", id);
        DatabaseVendor databaseVendor = databaseVendorRepository.findById(id);

        if (databaseVendor == null) {
            throw new BusinessRuleException(Constants.ERR_DATABASEVENDOR_NOT_FOUND);
        }

        return databaseVendorMapper.toResponse(databaseVendor);
    }

    /**
     * Retrieves a paginated list of database vendors matching the given search criteria.
     *
     * @param filter   dynamic search criteria and full-text keyword filters
     * @param pageable pagination and sorting parameters
     * @return a page of mapped {@link DatabaseVendorOutputDTO} results
     */
    @Override
    @Transactional(readOnly = true)
    public Page<DatabaseVendorOutputDTO> getAll(DatabaseVendorCriteria filter, Pageable pageable) {
        log.debug("Facade: Fetching database vendors via pagination boundaries");
        Page<DatabaseVendor> domainPage = databaseVendorRepository.findAll(filter, pageable);
        return domainPage.map(databaseVendorMapper::toResponse);
    }

    /**
     * Validates and registers a new database vendor/type catalog record.
     * Enforces name uniqueness among active records.
     *
     * @param inputDTO the data describing the vendor to create
     * @return the persisted vendor mapped into an {@link DatabaseVendorOutputDTO}
     * @throws BusinessRuleException if a record with the same name already exists
     */
    @Override
    public DatabaseVendorOutputDTO create(DatabaseVendorInputDTO inputDTO) {
        log.info("Facade: Creating new database vendor record with name: {}", inputDTO.getName());

        Utils.sanitize(inputDTO);

        if (databaseVendorRepository.existsByName(inputDTO.getName())) {
            throw new BusinessRuleException(Constants.ERR_DATABASEVENDOR_DUPLICATED);
        }

        DatabaseVendor model = databaseVendorMapper.toModelFromInput(inputDTO);
        DatabaseVendor savedModel = databaseVendorRepository.create(model);
        return databaseVendorMapper.toResponse(savedModel);
    }

    /**
     * Updates the modifiable fields of an existing database vendor record.
     *
     * @param id       the identifier of the record to update
     * @param inputDTO the new data to apply to the record
     * @return the updated vendor mapped into an {@link DatabaseVendorOutputDTO}
     * @throws BusinessRuleException if the record does not exist, or if the new name conflicts with another record
     */
    @Override
    public DatabaseVendorOutputDTO update(Long id, DatabaseVendorInputDTO inputDTO) {
        log.info("Facade: Updating database vendor with ID: {}", id);

        DatabaseVendor existing = databaseVendorRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_DATABASEVENDOR_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        if (databaseVendorRepository.existsByNameAndIdNot(inputDTO.getName(), id)) {
            throw new BusinessRuleException(Constants.ERR_DATABASEVENDOR_OWNED_BY_OTHER);
        }

        databaseVendorMapper.updateModelFromInput(inputDTO, existing);
        return databaseVendorMapper.toResponse(databaseVendorRepository.update(existing, id));
    }

    /**
     * Logically soft-deletes a database vendor record.
     *
     * @param id the identifier of the record to deactivate
     * @throws BusinessRuleException if the record does not exist or is already inactive
     */
    @Override
    public void delete(Long id) {
        log.info("Facade: Logically deleting database vendor with ID: {}", id);
        DatabaseVendor existing = databaseVendorRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_DATABASEVENDOR_NOT_FOUND);
        }

        if (existing.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_DATABASEVENDOR_NOT_ACTIVE);
        }

        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy(Utils.resolveCurrentUsername());

        databaseVendorRepository.delete(existing);
    }

    /**
     * Reactivates a logically soft-deleted database vendor record back to active state.
     *
     * @param id the identifier of the record to reactivate
     * @return the reactivated vendor mapped into an {@link DatabaseVendorOutputDTO}
     * @throws BusinessRuleException if the record does not exist or is already active
     */
    @Override
    public DatabaseVendorOutputDTO reactivate(Long id) {
        log.info("Facade: Reactivating database vendor with ID: {}", id);
        DatabaseVendor existing = databaseVendorRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_DATABASEVENDOR_NOT_FOUND);
        }

        if (existing.getDeletedAt() == null) {
            throw new BusinessRuleException(Constants.ERR_DATABASEVENDOR_ACTIVE);
        }

        existing.setDeletedAt(null);
        existing.setDeletedBy(null);

        return databaseVendorMapper.toResponse(databaseVendorRepository.update(existing, id));
    }
}

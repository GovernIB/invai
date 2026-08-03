package es.caib.invai.api.ejb;

import es.caib.invai.api.exception.BusinessRuleException;
import es.caib.invai.api.interna.maintenance.databaseVendor.DTO.DatabaseVendorInputDTO;
import es.caib.invai.api.interna.maintenance.databaseVendor.DTO.DatabaseVendorOutputDTO;
import es.caib.invai.api.persistence.repository.databaseVendor.DatabaseVendorCriteria;
import es.caib.invai.api.persistence.repository.databaseVendor.DatabaseVendorRepository;
import es.caib.invai.api.service.facade.DatabaseVendorService;
import es.caib.invai.api.service.mapper.DatabaseVendorMapper;
import es.caib.invai.api.service.model.DatabaseVendor;
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
 * Facade service implementation for managing database vendor/type catalog configurations.
 * Enforces transactional safety bounds and verifies logical deletion metrics.
 *
 * @since 1.0.2
 */
@Service
@Slf4j
@Transactional
public class DatabaseVendorServiceFacadeBean implements DatabaseVendorService {

    @Autowired
    private DatabaseVendorMapper databaseVendorMapper;

    @Autowired
    private DatabaseVendorRepository databaseVendorRepository;

    @Override
    @Transactional(readOnly = true)
    public DatabaseVendorOutputDTO getById(Long id) {
        log.info("Facade: Fetching database vendor by ID: {}", id);
        DatabaseVendor databaseVendor = databaseVendorRepository.findById(id);

        if (databaseVendor == null) {
            throw new BusinessRuleException(Constants.ERR_DATABASEVENDOR_NOT_FOUND);
        }

        return databaseVendorMapper.toResponse(databaseVendor);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DatabaseVendorOutputDTO> getAll(DatabaseVendorCriteria filter, Pageable pageable) {
        log.info("Facade: Fetching database vendors via pagination boundaries");
        Page<DatabaseVendor> domainPage = databaseVendorRepository.findAll(filter, pageable);
        return domainPage.map(databaseVendorMapper::toResponse);
    }

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

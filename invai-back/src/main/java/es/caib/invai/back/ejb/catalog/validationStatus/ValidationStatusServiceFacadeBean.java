package es.caib.invai.back.ejb.catalog.validationStatus;

import es.caib.invai.back.interna.catalog.validationStatus.DTO.ValidationStatusOutputDTO;
import es.caib.invai.back.persistence.repository.catalog.validationStatus.ValidationStatusRepository;
import es.caib.invai.back.service.facade.catalog.validationStatus.ValidationStatusService;
import es.caib.invai.back.service.mapper.catalog.validationStatus.ValidationStatusMapper;
import es.caib.invai.back.service.model.catalog.validationStatus.ValidationStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Facade service implementation for reading the validation status lookup dictionary.
 *
 * @since 1.0.5
 */
@Service
@Slf4j
@Transactional
public class ValidationStatusServiceFacadeBean implements ValidationStatusService {

    /** Mapper used to translate domain models into outbound response DTOs. */
    @Autowired
    private ValidationStatusMapper validationStatusMapper;

    /** Repository port used to read the validation status lookup dictionary. */
    @Autowired
    private ValidationStatusRepository validationStatusRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ValidationStatusOutputDTO> getAll() {
        log.debug("Facade: Fetching every validation status lookup entry");
        List<ValidationStatus> statuses = validationStatusRepository.findAll();
        return validationStatusMapper.toResponseList(statuses);
    }
}

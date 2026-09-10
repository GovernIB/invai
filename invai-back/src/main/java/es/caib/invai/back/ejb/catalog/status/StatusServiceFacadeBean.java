package es.caib.invai.back.ejb.catalog.status;

import es.caib.invai.back.interna.catalog.status.DTO.StatusOutputDTO;
import es.caib.invai.back.persistence.repository.catalog.status.StatusRepository;
import es.caib.invai.back.service.facade.catalog.status.StatusService;
import es.caib.invai.back.service.mapper.catalog.status.StatusMapper;
import es.caib.invai.back.service.model.catalog.status.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Facade service implementation for reading the Status lookup dictionary.
 *
 * @since 1.0.3
 */
@Service
@Slf4j
@Transactional
public class StatusServiceFacadeBean implements StatusService {

    /** Mapper used to translate domain models into outbound response DTOs. */
    @Autowired
    private StatusMapper statusMapper;

    /** Repository port used to read the status lookup dictionary. */
    @Autowired
    private StatusRepository statusRepository;

    @Override
    @Transactional(readOnly = true)
    public List<StatusOutputDTO> getAll() {
        log.debug("Facade: Fetching every status lookup entry");
        List<Status> statuses = statusRepository.findAll();
        return statusMapper.toResponseList(statuses);
    }
}

package es.caib.invai.back.ejb.catalog.dir3Status;

import es.caib.invai.back.interna.catalog.dir3Status.DTO.Dir3StatusOutputDTO;
import es.caib.invai.back.persistence.repository.catalog.dir3Status.Dir3StatusRepository;
import es.caib.invai.back.service.facade.catalog.dir3Status.Dir3StatusService;
import es.caib.invai.back.service.mapper.catalog.dir3Status.Dir3StatusMapper;
import es.caib.invai.back.service.model.catalog.dir3Status.Dir3Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Facade service implementation for reading the DIR3 validation status lookup dictionary.
 *
 * @since 1.0.5
 */
@Service
@Slf4j
@Transactional
public class Dir3StatusServiceFacadeBean implements Dir3StatusService {

    /** Mapper used to translate domain models into outbound response DTOs. */
    @Autowired
    private Dir3StatusMapper dir3StatusMapper;

    /** Repository port used to read the DIR3 status lookup dictionary. */
    @Autowired
    private Dir3StatusRepository dir3StatusRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Dir3StatusOutputDTO> getAll() {
        log.debug("Facade: Fetching every DIR3 status lookup entry");
        List<Dir3Status> statuses = dir3StatusRepository.findAll();
        return dir3StatusMapper.toResponseList(statuses);
    }
}

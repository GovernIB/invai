package es.caib.invai.api.ejb;

import es.caib.invai.api.interna.maintenance.serverType.DTO.ServerTypeOutputDTO;
import es.caib.invai.api.persistence.repository.serverType.ServerTypeRepository;
import es.caib.invai.api.service.facade.ServerTypeService;
import es.caib.invai.api.service.mapper.ServerTypeMapper;
import es.caib.invai.api.service.model.ServerType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Facade service implementation for reading the Server Type lookup dictionary.
 *
 * @since 1.0.2
 */
@Service
@Slf4j
@Transactional
public class ServerTypeServiceFacadeBean implements ServerTypeService {

    @Autowired
    private ServerTypeMapper serverTypeMapper;

    @Autowired
    private ServerTypeRepository serverTypeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ServerTypeOutputDTO> getAll() {
        log.info("Facade: Fetching every server type lookup entry");
        List<ServerType> serverTypes = serverTypeRepository.findAll();
        return serverTypeMapper.toResponseList(serverTypes);
    }
}

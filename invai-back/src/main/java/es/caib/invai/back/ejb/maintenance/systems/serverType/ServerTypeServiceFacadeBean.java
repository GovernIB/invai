package es.caib.invai.back.ejb.maintenance.systems.serverType;

import es.caib.invai.back.interna.maintenance.systems.serverType.DTO.ServerTypeOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.systems.serverType.ServerTypeRepository;
import es.caib.invai.back.service.facade.maintenance.systems.serverType.ServerTypeService;
import es.caib.invai.back.service.mapper.maintenance.systems.serverType.ServerTypeMapper;
import es.caib.invai.back.service.model.maintenance.systems.serverType.ServerType;
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

    /** Mapper used to convert between ServerType domain models and their DTO representations. */
    @Autowired
    private ServerTypeMapper serverTypeMapper;

    /** Repository providing read access to the Server Type lookup entries. */
    @Autowired
    private ServerTypeRepository serverTypeRepository;

    /**
     * Retrieves every entry of the Server Type lookup dictionary.
     *
     * @return the full list of server types mapped into {@link ServerTypeOutputDTO} instances
     */
    @Override
    @Transactional(readOnly = true)
    public List<ServerTypeOutputDTO> getAll() {
        log.info("Facade: Fetching every server type lookup entry");
        List<ServerType> serverTypes = serverTypeRepository.findAll();
        return serverTypeMapper.toResponseList(serverTypes);
    }
}

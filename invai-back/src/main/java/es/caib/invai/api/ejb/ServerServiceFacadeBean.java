package es.caib.invai.api.ejb;

import es.caib.invai.api.exception.BusinessRuleException;
import es.caib.invai.api.interna.maintenance.server.DTO.ServerInputDTO;
import es.caib.invai.api.interna.maintenance.server.DTO.ServerOutputDTO;
import es.caib.invai.api.persistence.repository.server.ServerCriteria;
import es.caib.invai.api.persistence.repository.server.ServerRepository;
import es.caib.invai.api.service.facade.ServerService;
import es.caib.invai.api.service.mapper.ServerMapper;
import es.caib.invai.api.service.model.Server;
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
 * Facade service implementation for managing host server core configurations.
 * Enforces transactional safety bounds and verifies logical deletion metrics.
 *
 * @since 1.0.2
 */
@Service
@Slf4j
@Transactional
public class ServerServiceFacadeBean implements ServerService {

    @Autowired
    private ServerMapper serverMapper;

    @Autowired
    private ServerRepository serverRepository;

    @Override
    @Transactional(readOnly = true)
    public ServerOutputDTO getById(Long id) {
        log.info("Facade: Fetching server by ID: {}", id);
        Server server = serverRepository.findById(id);

        if (server == null) {
            throw new BusinessRuleException(Constants.ERR_SERVER_NOT_FOUND);
        }

        return serverMapper.toResponse(server);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServerOutputDTO> getAll(ServerCriteria filter, Pageable pageable) {
        log.info("Facade: Fetching servers via pagination boundaries");
        Page<Server> domainPage = serverRepository.findAll(filter, pageable);
        return domainPage.map(serverMapper::toResponse);
    }

    @Override
    public ServerOutputDTO create(ServerInputDTO inputDTO) {
        log.info("Facade: Creating new server record with name: {}", inputDTO.getName());

        Utils.sanitize(inputDTO);

        if (serverRepository.existsByName(inputDTO.getName())) {
            throw new BusinessRuleException(Constants.ERR_SERVER_DUPLICATED);
        }

        Server model = serverMapper.toModelFromInput(inputDTO);
        Server savedModel = serverRepository.create(model);
        return serverMapper.toResponse(savedModel);
    }

    @Override
    public ServerOutputDTO update(Long id, ServerInputDTO inputDTO) {
        log.info("Facade: Updating server with ID: {}", id);

        Server existing = serverRepository.findById(id);
        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_SERVER_NOT_FOUND);
        }

        Utils.sanitize(inputDTO);

        if (serverRepository.existsByNameAndIdNot(inputDTO.getName(), id)) {
            throw new BusinessRuleException(Constants.ERR_SERVER_OWNED_BY_OTHER);
        }

        serverMapper.updateModelFromInput(inputDTO, existing);
        return serverMapper.toResponse(serverRepository.update(existing, id));
    }

    @Override
    public void delete(Long id) {
        log.info("Facade: Logically deleting server with ID: {}", id);
        Server existing = serverRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_SERVER_NOT_FOUND);
        }

        if (existing.getDeletedAt() != null) {
            throw new BusinessRuleException(Constants.ERR_SERVER_NOT_ACTIVE);
        }

        existing.setDeletedAt(LocalDateTime.now());
        existing.setDeletedBy(Utils.resolveCurrentUsername());

        serverRepository.delete(existing);
    }

    @Override
    public ServerOutputDTO reactivate(Long id) {
        log.info("Facade: Reactivating server with ID: {}", id);
        Server existing = serverRepository.findById(id);

        if (existing == null) {
            throw new BusinessRuleException(Constants.ERR_SERVER_NOT_FOUND);
        }

        if (existing.getDeletedAt() == null) {
            throw new BusinessRuleException(Constants.ERR_SERVER_ACTIVE);
        }

        existing.setDeletedAt(null);
        existing.setDeletedBy(null);

        return serverMapper.toResponse(serverRepository.update(existing, id));
    }
}

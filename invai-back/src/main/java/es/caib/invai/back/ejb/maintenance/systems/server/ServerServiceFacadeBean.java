package es.caib.invai.back.ejb.maintenance.systems.server;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.maintenance.systems.server.DTO.ServerInputDTO;
import es.caib.invai.back.interna.maintenance.systems.server.DTO.ServerOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.systems.server.ServerCriteria;
import es.caib.invai.back.persistence.repository.maintenance.systems.server.ServerRepository;
import es.caib.invai.back.service.facade.maintenance.systems.server.ServerService;
import es.caib.invai.back.service.mapper.maintenance.systems.server.ServerMapper;
import es.caib.invai.back.service.model.maintenance.systems.server.Server;
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
 * Facade service implementation for managing host server core configurations.
 * Enforces transactional safety bounds and verifies logical deletion metrics.
 *
 * @since 1.0.2
 */
@Service
@Slf4j
@Transactional
public class ServerServiceFacadeBean implements ServerService {

    /** Mapper used to convert between Server domain models and their DTO representations. */
    @Autowired
    private ServerMapper serverMapper;

    /** Repository providing persistence operations for Server records. */
    @Autowired
    private ServerRepository serverRepository;

    /**
     * Retrieves a server by its unique database identifier.
     *
     * @param id the unique server record identifier
     * @return the mapped {@link ServerOutputDTO} response payload
     * @throws BusinessRuleException if no server matches the given identifier
     */
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

    /**
     * Retrieves a paginated list of servers matching the given filter criteria.
     *
     * @param filter   the search criteria used to narrow the results
     * @param pageable the pagination and sorting parameters
     * @return a page of mapped {@link ServerOutputDTO} results
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ServerOutputDTO> getAll(ServerCriteria filter, Pageable pageable) {
        log.info("Facade: Fetching servers via pagination boundaries");
        Page<Server> domainPage = serverRepository.findAll(filter, pageable);
        return domainPage.map(serverMapper::toResponse);
    }

    /**
     * Validates and creates a new server record.
     *
     * @param inputDTO the data used to create the new server
     * @return the persisted server mapped into a {@link ServerOutputDTO}
     * @throws BusinessRuleException if a server with the same name already exists
     */
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

    /**
     * Validates and updates an existing server record with the given input.
     *
     * @param id       the identifier of the server to update
     * @param inputDTO the data used to update the server
     * @return the updated server mapped into a {@link ServerOutputDTO}
     * @throws BusinessRuleException if the server does not exist or the name is already owned by another server
     */
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

    /**
     * Logically deletes (soft-deletes) an active server record.
     *
     * @param id the identifier of the server to delete
     * @throws BusinessRuleException if the server does not exist or is already deleted
     */
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

    /**
     * Reactivates a logically deleted server record back to active state.
     *
     * @param id the identifier of the server to reactivate
     * @return the reactivated server mapped into a {@link ServerOutputDTO}
     * @throws BusinessRuleException if the server does not exist or is already active
     */
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

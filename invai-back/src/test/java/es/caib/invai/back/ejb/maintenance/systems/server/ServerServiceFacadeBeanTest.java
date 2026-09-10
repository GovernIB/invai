package es.caib.invai.back.ejb.maintenance.systems.server;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.maintenance.systems.server.DTO.ServerInputDTO;
import es.caib.invai.back.interna.maintenance.systems.server.DTO.ServerOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.systems.server.ServerCriteria;
import es.caib.invai.back.persistence.repository.maintenance.systems.server.ServerRepository;
import es.caib.invai.back.service.mapper.maintenance.systems.server.ServerMapper;
import es.caib.invai.back.service.model.maintenance.systems.server.Server;
import es.caib.invai.back.utils.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ServerServiceFacadeBean}, exercising every branch of its business rules
 * with the {@link ServerRepository} and {@link ServerMapper} collaborators fully mocked.
 */
@ExtendWith(MockitoExtension.class)
class ServerServiceFacadeBeanTest {

    @Mock
    private ServerMapper serverMapper;

    @Mock
    private ServerRepository serverRepository;

    @InjectMocks
    private ServerServiceFacadeBean serverServiceFacadeBean;

    private Server activeServer;

    @BeforeEach
    void setUp() {
        activeServer = new Server();
        activeServer.setId(1L);
        activeServer.setName("srv-01");
    }

    @Test
    void getById_found_returnsMappedResponse() {
        ServerOutputDTO expected = new ServerOutputDTO();
        when(serverRepository.findById(1L)).thenReturn(activeServer);
        when(serverMapper.toResponse(activeServer)).thenReturn(expected);

        ServerOutputDTO result = serverServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_notFound_throwsBusinessRuleException() {
        when(serverRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> serverServiceFacadeBean.getById(99L));
        assertEquals(Constants.ERR_SERVER_NOT_FOUND, ex.getMessage());
    }

    @Test
    void getAll_delegatesToRepositoryAndMapsPage() {
        ServerCriteria criteria = new ServerCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<Server> domainPage = new PageImpl<>(List.of(activeServer));
        ServerOutputDTO mapped = new ServerOutputDTO();
        when(serverRepository.findAll(criteria, pageable)).thenReturn(domainPage);
        when(serverMapper.toResponse(activeServer)).thenReturn(mapped);

        Page<ServerOutputDTO> result = serverServiceFacadeBean.getAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(mapped, result.getContent().get(0));
    }

    @Test
    void create_uniqueName_persistsAndReturnsResponse() {
        ServerInputDTO inputDTO = new ServerInputDTO();
        inputDTO.setName("New Server");
        inputDTO.setEnvironmentId(1L);
        inputDTO.setServerTypeId(2L);
        Server model = new Server();
        Server saved = new Server();
        ServerOutputDTO response = new ServerOutputDTO();
        when(serverRepository.existsByName("New Server")).thenReturn(false);
        when(serverMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(serverRepository.create(model)).thenReturn(saved);
        when(serverMapper.toResponse(saved)).thenReturn(response);

        ServerOutputDTO result = serverServiceFacadeBean.create(inputDTO);

        assertEquals(response, result);
    }

    @Test
    void create_duplicateName_throwsBusinessRuleException() {
        ServerInputDTO inputDTO = new ServerInputDTO();
        inputDTO.setName("Taken");
        inputDTO.setEnvironmentId(1L);
        inputDTO.setServerTypeId(2L);
        when(serverRepository.existsByName("Taken")).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> serverServiceFacadeBean.create(inputDTO));
        assertEquals(Constants.ERR_SERVER_DUPLICATED, ex.getMessage());
        verify(serverRepository, never()).create(any());
    }

    @Test
    void update_notFound_throwsBusinessRuleException() {
        ServerInputDTO inputDTO = new ServerInputDTO();
        inputDTO.setName("X");
        inputDTO.setEnvironmentId(1L);
        inputDTO.setServerTypeId(2L);
        when(serverRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> serverServiceFacadeBean.update(99L, inputDTO));
        assertEquals(Constants.ERR_SERVER_NOT_FOUND, ex.getMessage());
    }

    @Test
    void update_duplicateName_throwsBusinessRuleException() {
        ServerInputDTO inputDTO = new ServerInputDTO();
        inputDTO.setName("Taken");
        inputDTO.setEnvironmentId(1L);
        inputDTO.setServerTypeId(2L);
        when(serverRepository.findById(1L)).thenReturn(activeServer);
        when(serverRepository.existsByNameAndIdNot("Taken", 1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> serverServiceFacadeBean.update(1L, inputDTO));
        assertEquals(Constants.ERR_SERVER_OWNED_BY_OTHER, ex.getMessage());
        verify(serverMapper, never()).updateModelFromInput(any(), any());
    }

    @Test
    void update_valid_updatesAndReturnsResponse() {
        ServerInputDTO inputDTO = new ServerInputDTO();
        inputDTO.setName("Updated");
        inputDTO.setEnvironmentId(1L);
        inputDTO.setServerTypeId(2L);
        Server updated = new Server();
        ServerOutputDTO response = new ServerOutputDTO();
        when(serverRepository.findById(1L)).thenReturn(activeServer);
        when(serverRepository.existsByNameAndIdNot("Updated", 1L)).thenReturn(false);
        when(serverRepository.update(activeServer, 1L)).thenReturn(updated);
        when(serverMapper.toResponse(updated)).thenReturn(response);

        ServerOutputDTO result = serverServiceFacadeBean.update(1L, inputDTO);

        verify(serverMapper).updateModelFromInput(inputDTO, activeServer);
        assertEquals(response, result);
    }

    @Test
    void delete_notFound_throwsBusinessRuleException() {
        when(serverRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> serverServiceFacadeBean.delete(99L));
        assertEquals(Constants.ERR_SERVER_NOT_FOUND, ex.getMessage());
    }

    @Test
    void delete_alreadyInactive_throwsBusinessRuleException() {
        activeServer.setDeletedAt(LocalDateTime.now());
        when(serverRepository.findById(1L)).thenReturn(activeServer);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> serverServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_SERVER_NOT_ACTIVE, ex.getMessage());
    }

    @Test
    void delete_valid_softDeletesServer() {
        when(serverRepository.findById(1L)).thenReturn(activeServer);

        serverServiceFacadeBean.delete(1L);

        assertNotNull(activeServer.getDeletedAt());
        verify(serverRepository, times(1)).delete(activeServer);
    }

    @Test
    void reactivate_notFound_throwsBusinessRuleException() {
        when(serverRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> serverServiceFacadeBean.reactivate(99L));
        assertEquals(Constants.ERR_SERVER_NOT_FOUND, ex.getMessage());
    }

    @Test
    void reactivate_alreadyActive_throwsBusinessRuleException() {
        when(serverRepository.findById(1L)).thenReturn(activeServer);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> serverServiceFacadeBean.reactivate(1L));
        assertEquals(Constants.ERR_SERVER_ACTIVE, ex.getMessage());
    }

    @Test
    void reactivate_inactive_reactivatesServer() {
        activeServer.setDeletedAt(LocalDateTime.now());
        activeServer.setDeletedBy("someone");
        Server reactivated = new Server();
        ServerOutputDTO response = new ServerOutputDTO();
        when(serverRepository.findById(1L)).thenReturn(activeServer);
        when(serverRepository.update(eq(activeServer), eq(1L))).thenReturn(reactivated);
        when(serverMapper.toResponse(reactivated)).thenReturn(response);

        ServerOutputDTO result = serverServiceFacadeBean.reactivate(1L);

        assertNull(activeServer.getDeletedAt());
        assertNull(activeServer.getDeletedBy());
        assertEquals(response, result);
    }
}

package es.caib.invai.back.ejb.maintenance.systems.system;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.maintenance.systems.system.DTO.SystemInputDTO;
import es.caib.invai.back.interna.maintenance.systems.system.DTO.SystemOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.systems.server.ServerRepository;
import es.caib.invai.back.persistence.repository.maintenance.systems.system.SystemCriteria;
import es.caib.invai.back.persistence.repository.maintenance.systems.system.SystemRepository;
import es.caib.invai.back.service.mapper.maintenance.systems.system.SystemMapper;
import es.caib.invai.back.service.model.maintenance.systems.server.Server;
import es.caib.invai.back.service.model.maintenance.systems.serverType.ServerType;
import es.caib.invai.back.service.model.maintenance.systems.system.System;
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
 * Unit tests for {@link SystemServiceFacadeBean}, exercising every branch of its business rules
 * with the {@link SystemRepository}, {@link SystemMapper}, and {@link ServerRepository}
 * collaborators fully mocked.
 */
@ExtendWith(MockitoExtension.class)
class SystemServiceFacadeBeanTest {

    @Mock
    private SystemMapper systemMapper;

    @Mock
    private SystemRepository systemRepository;

    @Mock
    private ServerRepository serverRepository;

    @InjectMocks
    private SystemServiceFacadeBean systemServiceFacadeBean;

    private System activeSystem;
    private Server applicationServer;

    @BeforeEach
    void setUp() {
        activeSystem = new System();
        activeSystem.setId(1L);
        activeSystem.setInstance("INST1");
        activeSystem.setPort(1521);
        activeSystem.setVersion("1.0");

        ServerType applicationType = new ServerType();
        applicationType.setCode(Constants.SERVER_TYPE_CODE_APPLICATION);
        applicationServer = new Server();
        applicationServer.setId(10L);
        applicationServer.setServerType(applicationType);
    }

    private SystemInputDTO buildInputDTO() {
        SystemInputDTO inputDTO = new SystemInputDTO();
        inputDTO.setServerId(10L);
        inputDTO.setInstance("INST1");
        inputDTO.setPort(1521);
        inputDTO.setVersion("1.0");
        return inputDTO;
    }

    @Test
    void getById_found_returnsMappedResponse() {
        SystemOutputDTO expected = new SystemOutputDTO();
        when(systemRepository.findById(1L)).thenReturn(activeSystem);
        when(systemMapper.toResponse(activeSystem)).thenReturn(expected);

        SystemOutputDTO result = systemServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_notFound_throwsBusinessRuleException() {
        when(systemRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> systemServiceFacadeBean.getById(99L));
        assertEquals(Constants.ERR_SYSTEM_NOT_FOUND, ex.getMessage());
    }

    @Test
    void getAll_delegatesToRepositoryAndMapsPage() {
        SystemCriteria criteria = new SystemCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<System> domainPage = new PageImpl<>(List.of(activeSystem));
        SystemOutputDTO mapped = new SystemOutputDTO();
        when(systemRepository.findAll(criteria, pageable)).thenReturn(domainPage);
        when(systemMapper.toResponse(activeSystem)).thenReturn(mapped);

        Page<SystemOutputDTO> result = systemServiceFacadeBean.getAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(mapped, result.getContent().get(0));
    }

    @Test
    void create_valid_persistsAndReturnsResponse() {
        SystemInputDTO inputDTO = buildInputDTO();
        System model = new System();
        System saved = new System();
        SystemOutputDTO response = new SystemOutputDTO();
        when(serverRepository.findById(10L)).thenReturn(applicationServer);
        when(systemRepository.existsByServerIdAndInstance(10L, "INST1")).thenReturn(false);
        when(systemMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(systemRepository.create(model)).thenReturn(saved);
        when(systemMapper.toResponse(saved)).thenReturn(response);

        SystemOutputDTO result = systemServiceFacadeBean.create(inputDTO);

        assertEquals(response, result);
    }

    @Test
    void create_serverNotFound_skipsTypeCheckAndProceeds() {
        SystemInputDTO inputDTO = buildInputDTO();
        System model = new System();
        System saved = new System();
        SystemOutputDTO response = new SystemOutputDTO();
        when(serverRepository.findById(10L)).thenReturn(null);
        when(systemRepository.existsByServerIdAndInstance(10L, "INST1")).thenReturn(false);
        when(systemMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(systemRepository.create(model)).thenReturn(saved);
        when(systemMapper.toResponse(saved)).thenReturn(response);

        SystemOutputDTO result = systemServiceFacadeBean.create(inputDTO);

        assertEquals(response, result);
    }

    @Test
    void create_serverTypeNotApplication_throwsBusinessRuleException() {
        SystemInputDTO inputDTO = buildInputDTO();
        ServerType databaseType = new ServerType();
        databaseType.setCode(Constants.SERVER_TYPE_CODE_DATABASE);
        Server databaseServer = new Server();
        databaseServer.setId(10L);
        databaseServer.setServerType(databaseType);
        when(serverRepository.findById(10L)).thenReturn(databaseServer);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> systemServiceFacadeBean.create(inputDTO));
        assertEquals(Constants.ERR_SYSTEM_SERVER_TYPE_INVALID, ex.getMessage());
        verify(systemRepository, never()).create(any());
    }

    @Test
    void create_serverTypeMissing_throwsBusinessRuleException() {
        SystemInputDTO inputDTO = buildInputDTO();
        Server serverWithoutType = new Server();
        serverWithoutType.setId(10L);
        when(serverRepository.findById(10L)).thenReturn(serverWithoutType);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> systemServiceFacadeBean.create(inputDTO));
        assertEquals(Constants.ERR_SYSTEM_SERVER_TYPE_INVALID, ex.getMessage());
        verify(systemRepository, never()).create(any());
    }

    @Test
    void create_duplicateServerIdAndInstance_throwsBusinessRuleException() {
        SystemInputDTO inputDTO = buildInputDTO();
        when(serverRepository.findById(10L)).thenReturn(applicationServer);
        when(systemRepository.existsByServerIdAndInstance(10L, "INST1")).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> systemServiceFacadeBean.create(inputDTO));
        assertEquals(Constants.ERR_SYSTEM_NAME_AND_INSTANCE_DUPLICATED, ex.getMessage());
        verify(systemRepository, never()).create(any());
    }

    @Test
    void update_notFound_throwsBusinessRuleException() {
        SystemInputDTO inputDTO = buildInputDTO();
        when(systemRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> systemServiceFacadeBean.update(99L, inputDTO));
        assertEquals(Constants.ERR_SYSTEM_NOT_FOUND, ex.getMessage());
    }

    @Test
    void update_serverTypeNotApplication_throwsBusinessRuleException() {
        SystemInputDTO inputDTO = buildInputDTO();
        ServerType databaseType = new ServerType();
        databaseType.setCode(Constants.SERVER_TYPE_CODE_DATABASE);
        Server databaseServer = new Server();
        databaseServer.setId(10L);
        databaseServer.setServerType(databaseType);
        when(systemRepository.findById(1L)).thenReturn(activeSystem);
        when(serverRepository.findById(10L)).thenReturn(databaseServer);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> systemServiceFacadeBean.update(1L, inputDTO));
        assertEquals(Constants.ERR_SYSTEM_SERVER_TYPE_INVALID, ex.getMessage());
        verify(systemMapper, never()).updateModelFromInput(any(), any());
    }

    @Test
    void update_duplicateServerIdAndInstance_throwsBusinessRuleException() {
        SystemInputDTO inputDTO = buildInputDTO();
        when(systemRepository.findById(1L)).thenReturn(activeSystem);
        when(serverRepository.findById(10L)).thenReturn(applicationServer);
        when(systemRepository.existsByServerIdAndInstanceAndId(10L, "INST1", 1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> systemServiceFacadeBean.update(1L, inputDTO));
        assertEquals(Constants.ERR_SYSTEM_NAME_AND_INSTANCE_OWNED_BY_OTHER, ex.getMessage());
        verify(systemMapper, never()).updateModelFromInput(any(), any());
    }

    @Test
    void update_valid_updatesAndReturnsResponse() {
        SystemInputDTO inputDTO = buildInputDTO();
        System updated = new System();
        SystemOutputDTO response = new SystemOutputDTO();
        when(systemRepository.findById(1L)).thenReturn(activeSystem);
        when(serverRepository.findById(10L)).thenReturn(applicationServer);
        when(systemRepository.existsByServerIdAndInstanceAndId(10L, "INST1", 1L)).thenReturn(false);
        when(systemRepository.update(activeSystem, 1L)).thenReturn(updated);
        when(systemMapper.toResponse(updated)).thenReturn(response);

        SystemOutputDTO result = systemServiceFacadeBean.update(1L, inputDTO);

        verify(systemMapper).updateModelFromInput(inputDTO, activeSystem);
        assertEquals(response, result);
    }

    @Test
    void delete_notFound_throwsBusinessRuleException() {
        when(systemRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> systemServiceFacadeBean.delete(99L));
        assertEquals(Constants.ERR_SYSTEM_NOT_FOUND, ex.getMessage());
    }

    @Test
    void delete_alreadyInactive_throwsBusinessRuleException() {
        activeSystem.setDeletedAt(LocalDateTime.now());
        when(systemRepository.findById(1L)).thenReturn(activeSystem);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> systemServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_SYSTEM_NOT_ACTIVE, ex.getMessage());
    }

    @Test
    void delete_valid_softDeletesSystem() {
        when(systemRepository.findById(1L)).thenReturn(activeSystem);

        systemServiceFacadeBean.delete(1L);

        assertNotNull(activeSystem.getDeletedAt());
        verify(systemRepository, times(1)).delete(activeSystem);
    }

    @Test
    void reactivate_notFound_throwsBusinessRuleException() {
        when(systemRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> systemServiceFacadeBean.reactivate(99L));
        assertEquals(Constants.ERR_SYSTEM_NOT_FOUND, ex.getMessage());
    }

    @Test
    void reactivate_alreadyActive_throwsBusinessRuleException() {
        when(systemRepository.findById(1L)).thenReturn(activeSystem);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> systemServiceFacadeBean.reactivate(1L));
        assertEquals(Constants.ERR_SYSTEM_ACTIVE, ex.getMessage());
    }

    @Test
    void reactivate_inactive_reactivatesSystem() {
        activeSystem.setDeletedAt(LocalDateTime.now());
        activeSystem.setDeletedBy("someone");
        System reactivated = new System();
        SystemOutputDTO response = new SystemOutputDTO();
        when(systemRepository.findById(1L)).thenReturn(activeSystem);
        when(systemRepository.update(eq(activeSystem), eq(1L))).thenReturn(reactivated);
        when(systemMapper.toResponse(reactivated)).thenReturn(response);

        SystemOutputDTO result = systemServiceFacadeBean.reactivate(1L);

        assertNull(activeSystem.getDeletedAt());
        assertNull(activeSystem.getDeletedBy());
        assertEquals(response, result);
    }
}

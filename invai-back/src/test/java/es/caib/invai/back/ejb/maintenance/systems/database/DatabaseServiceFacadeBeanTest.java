package es.caib.invai.back.ejb.maintenance.systems.database;

import es.caib.invai.back.exception.BusinessRuleException;
import es.caib.invai.back.interna.maintenance.systems.database.DTO.DatabaseInputDTO;
import es.caib.invai.back.interna.maintenance.systems.database.DTO.DatabaseOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.systems.database.DatabaseCriteria;
import es.caib.invai.back.persistence.repository.maintenance.systems.database.DatabaseRepository;
import es.caib.invai.back.persistence.repository.maintenance.systems.server.ServerRepository;
import es.caib.invai.back.service.mapper.maintenance.systems.database.DatabaseMapper;
import es.caib.invai.back.service.model.maintenance.systems.database.Database;
import es.caib.invai.back.service.model.maintenance.systems.server.Server;
import es.caib.invai.back.service.model.maintenance.systems.serverType.ServerType;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DatabaseServiceFacadeBean}, exercising every branch of its business rules
 * with the {@link DatabaseRepository}, {@link DatabaseMapper}, and {@link ServerRepository}
 * collaborators fully mocked.
 */
@ExtendWith(MockitoExtension.class)
class DatabaseServiceFacadeBeanTest {

    @Mock
    private DatabaseMapper databaseMapper;

    @Mock
    private DatabaseRepository databaseRepository;

    @Mock
    private ServerRepository serverRepository;

    @InjectMocks
    private DatabaseServiceFacadeBean databaseServiceFacadeBean;

    private Database activeDatabase;
    private Server databaseTypedServer;

    @BeforeEach
    void setUp() {
        ServerType serverType = new ServerType();
        serverType.setCode(Constants.SERVER_TYPE_CODE_DATABASE);

        databaseTypedServer = new Server();
        databaseTypedServer.setId(10L);
        databaseTypedServer.setServerType(serverType);

        activeDatabase = new Database();
        activeDatabase.setId(1L);
        activeDatabase.setServer(databaseTypedServer);
        activeDatabase.setService("invai_db");
    }

    @Test
    void getById_found_returnsMappedResponse() {
        DatabaseOutputDTO expected = new DatabaseOutputDTO();
        when(databaseRepository.findById(1L)).thenReturn(activeDatabase);
        when(databaseMapper.toResponse(activeDatabase)).thenReturn(expected);

        DatabaseOutputDTO result = databaseServiceFacadeBean.getById(1L);

        assertEquals(expected, result);
    }

    @Test
    void getById_notFound_throwsBusinessRuleException() {
        when(databaseRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> databaseServiceFacadeBean.getById(99L));
        assertEquals(Constants.ERR_DATABASE_NOT_FOUND, ex.getMessage());
    }

    @Test
    void getAll_delegatesToRepositoryAndMapsPage() {
        DatabaseCriteria criteria = new DatabaseCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<Database> domainPage = new PageImpl<>(List.of(activeDatabase));
        DatabaseOutputDTO mapped = new DatabaseOutputDTO();
        when(databaseRepository.findAll(criteria, pageable)).thenReturn(domainPage);
        when(databaseMapper.toResponse(activeDatabase)).thenReturn(mapped);

        Page<DatabaseOutputDTO> result = databaseServiceFacadeBean.getAll(criteria, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(mapped, result.getContent().get(0));
    }

    @Test
    void create_invalidServerType_throwsBusinessRuleException() {
        DatabaseInputDTO inputDTO = new DatabaseInputDTO();
        inputDTO.setServerId(10L);
        inputDTO.setService("invai_db");

        ServerType nonDatabaseType = new ServerType();
        nonDatabaseType.setCode("APPLICATION");
        Server appServer = new Server();
        appServer.setId(10L);
        appServer.setServerType(nonDatabaseType);
        when(serverRepository.findById(10L)).thenReturn(appServer);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> databaseServiceFacadeBean.create(inputDTO));
        assertEquals(Constants.ERR_DATABASE_SERVER_TYPE_INVALID, ex.getMessage());
        verify(databaseRepository, never()).create(any());
    }

    @Test
    void create_serverTypeNull_throwsBusinessRuleException() {
        DatabaseInputDTO inputDTO = new DatabaseInputDTO();
        inputDTO.setServerId(10L);
        inputDTO.setService("invai_db");

        Server serverWithoutType = new Server();
        serverWithoutType.setId(10L);
        serverWithoutType.setServerType(null);
        when(serverRepository.findById(10L)).thenReturn(serverWithoutType);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> databaseServiceFacadeBean.create(inputDTO));
        assertEquals(Constants.ERR_DATABASE_SERVER_TYPE_INVALID, ex.getMessage());
    }

    @Test
    void create_serverNotFound_skipsTypeValidationAndProceeds() {
        DatabaseInputDTO inputDTO = new DatabaseInputDTO();
        inputDTO.setServerId(404L);
        inputDTO.setService("invai_db");
        Database model = new Database();
        Database saved = new Database();
        DatabaseOutputDTO response = new DatabaseOutputDTO();
        when(serverRepository.findById(404L)).thenReturn(null);
        when(databaseRepository.existsByServerAndService(404L, "invai_db")).thenReturn(false);
        when(databaseMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(databaseRepository.create(model)).thenReturn(saved);
        when(databaseMapper.toResponse(saved)).thenReturn(response);

        DatabaseOutputDTO result = databaseServiceFacadeBean.create(inputDTO);

        assertEquals(response, result);
    }

    @Test
    void create_duplicateServerAndService_throwsBusinessRuleException() {
        DatabaseInputDTO inputDTO = new DatabaseInputDTO();
        inputDTO.setServerId(10L);
        inputDTO.setService("invai_db");
        when(serverRepository.findById(10L)).thenReturn(databaseTypedServer);
        when(databaseRepository.existsByServerAndService(10L, "invai_db")).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> databaseServiceFacadeBean.create(inputDTO));
        assertEquals(Constants.ERR_DATABASE_DUPLICATED, ex.getMessage());
        verify(databaseRepository, never()).create(any());
    }

    @Test
    void create_valid_persistsAndReturnsResponse() {
        DatabaseInputDTO inputDTO = new DatabaseInputDTO();
        inputDTO.setServerId(10L);
        inputDTO.setService("invai_db");
        Database model = new Database();
        Database saved = new Database();
        DatabaseOutputDTO response = new DatabaseOutputDTO();
        when(serverRepository.findById(10L)).thenReturn(databaseTypedServer);
        when(databaseRepository.existsByServerAndService(10L, "invai_db")).thenReturn(false);
        when(databaseMapper.toModelFromInput(inputDTO)).thenReturn(model);
        when(databaseRepository.create(model)).thenReturn(saved);
        when(databaseMapper.toResponse(saved)).thenReturn(response);

        DatabaseOutputDTO result = databaseServiceFacadeBean.create(inputDTO);

        assertEquals(response, result);
    }

    @Test
    void update_notFound_throwsBusinessRuleException() {
        DatabaseInputDTO inputDTO = new DatabaseInputDTO();
        when(databaseRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> databaseServiceFacadeBean.update(99L, inputDTO));
        assertEquals(Constants.ERR_DATABASE_NOT_FOUND, ex.getMessage());
    }

    @Test
    void update_invalidServerType_throwsBusinessRuleException() {
        DatabaseInputDTO inputDTO = new DatabaseInputDTO();
        inputDTO.setServerId(20L);
        inputDTO.setService("invai_db");
        when(databaseRepository.findById(1L)).thenReturn(activeDatabase);

        ServerType nonDatabaseType = new ServerType();
        nonDatabaseType.setCode("APPLICATION");
        Server appServer = new Server();
        appServer.setId(20L);
        appServer.setServerType(nonDatabaseType);
        when(serverRepository.findById(20L)).thenReturn(appServer);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> databaseServiceFacadeBean.update(1L, inputDTO));
        assertEquals(Constants.ERR_DATABASE_SERVER_TYPE_INVALID, ex.getMessage());
    }

    @Test
    void update_duplicateOwnedByOther_throwsBusinessRuleException() {
        DatabaseInputDTO inputDTO = new DatabaseInputDTO();
        inputDTO.setServerId(10L);
        inputDTO.setService("other_db");
        when(databaseRepository.findById(1L)).thenReturn(activeDatabase);
        when(serverRepository.findById(10L)).thenReturn(databaseTypedServer);
        when(databaseRepository.existsByServerAndServiceAndIdNot(10L, "other_db", 1L)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> databaseServiceFacadeBean.update(1L, inputDTO));
        assertEquals(Constants.ERR_DATABASE_SERVER_AND_SERVICE_OWNED_BY_OTHER, ex.getMessage());
        verify(databaseMapper, never()).updateModelFromInput(any(), any());
    }

    @Test
    void update_valid_updatesAndReturnsResponse() {
        DatabaseInputDTO inputDTO = new DatabaseInputDTO();
        inputDTO.setServerId(10L);
        inputDTO.setService("invai_db_updated");
        Database updated = new Database();
        DatabaseOutputDTO response = new DatabaseOutputDTO();
        when(databaseRepository.findById(1L)).thenReturn(activeDatabase);
        when(serverRepository.findById(10L)).thenReturn(databaseTypedServer);
        when(databaseRepository.existsByServerAndServiceAndIdNot(10L, "invai_db_updated", 1L)).thenReturn(false);
        when(databaseRepository.update(activeDatabase, 1L)).thenReturn(updated);
        when(databaseMapper.toResponse(updated)).thenReturn(response);

        DatabaseOutputDTO result = databaseServiceFacadeBean.update(1L, inputDTO);

        verify(databaseMapper).updateModelFromInput(inputDTO, activeDatabase);
        assertEquals(response, result);
    }

    @Test
    void delete_notFound_throwsBusinessRuleException() {
        when(databaseRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> databaseServiceFacadeBean.delete(99L));
        assertEquals(Constants.ERR_DATABASE_NOT_FOUND, ex.getMessage());
    }

    @Test
    void delete_alreadyInactive_throwsBusinessRuleException() {
        activeDatabase.setDeletedAt(LocalDateTime.now());
        when(databaseRepository.findById(1L)).thenReturn(activeDatabase);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> databaseServiceFacadeBean.delete(1L));
        assertEquals(Constants.ERR_DATABASE_NOT_ACTIVE, ex.getMessage());
    }

    @Test
    void delete_valid_softDeletesDatabase() {
        when(databaseRepository.findById(1L)).thenReturn(activeDatabase);

        databaseServiceFacadeBean.delete(1L);

        assertNotNull(activeDatabase.getDeletedAt());
        verify(databaseRepository, times(1)).delete(activeDatabase);
    }

    @Test
    void reactivate_notFound_throwsBusinessRuleException() {
        when(databaseRepository.findById(99L)).thenReturn(null);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> databaseServiceFacadeBean.reactivate(99L));
        assertEquals(Constants.ERR_DATABASE_NOT_FOUND, ex.getMessage());
    }

    @Test
    void reactivate_alreadyActive_throwsBusinessRuleException() {
        when(databaseRepository.findById(1L)).thenReturn(activeDatabase);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> databaseServiceFacadeBean.reactivate(1L));
        assertEquals(Constants.ERR_DATABASE_ACTIVE, ex.getMessage());
    }

    @Test
    void reactivate_inactive_reactivatesDatabase() {
        activeDatabase.setDeletedAt(LocalDateTime.now());
        activeDatabase.setDeletedBy("someone");
        Database reactivated = new Database();
        DatabaseOutputDTO response = new DatabaseOutputDTO();
        when(databaseRepository.findById(1L)).thenReturn(activeDatabase);
        when(databaseRepository.update(activeDatabase, 1L)).thenReturn(reactivated);
        when(databaseMapper.toResponse(reactivated)).thenReturn(response);

        DatabaseOutputDTO result = databaseServiceFacadeBean.reactivate(1L);

        assertNull(activeDatabase.getDeletedAt());
        assertNull(activeDatabase.getDeletedBy());
        assertEquals(response, result);
    }
}

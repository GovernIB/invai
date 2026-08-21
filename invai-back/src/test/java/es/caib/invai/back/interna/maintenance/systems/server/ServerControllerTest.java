package es.caib.invai.back.interna.maintenance.systems.server;

import es.caib.invai.back.interna.maintenance.systems.server.DTO.ServerInputDTO;
import es.caib.invai.back.interna.maintenance.systems.server.DTO.ServerOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.systems.server.ServerCriteria;
import es.caib.invai.back.service.facade.maintenance.systems.server.ServerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ServerController}, verifying that every endpoint delegates to
 * {@link ServerService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class ServerControllerTest {

    @Mock
    private ServerService serverService;

    private ServerController serverController;

    @BeforeEach
    void setUp() {
        serverController = new ServerController(serverService);
    }

    @Test
    void getById_returnsOkWithServiceResult() {
        ServerOutputDTO dto = new ServerOutputDTO();
        when(serverService.getById(1L)).thenReturn(dto);

        ResponseEntity<ServerOutputDTO> response = serverController.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void getAll_returnsOkWithPagedServiceResult() {
        ServerCriteria filter = new ServerCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<ServerOutputDTO> page = new PageImpl<>(List.of(new ServerOutputDTO()));
        when(serverService.getAll(filter, pageable)).thenReturn(page);

        ResponseEntity<Page<ServerOutputDTO>> response = serverController.getAll(filter, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
    }

    @Test
    void create_returnsCreatedWithServiceResult() {
        ServerInputDTO inputDTO = new ServerInputDTO();
        inputDTO.setName("srv-01");
        inputDTO.setEnvironmentId(1L);
        inputDTO.setServerTypeId(2L);
        ServerOutputDTO dto = new ServerOutputDTO();
        when(serverService.create(inputDTO)).thenReturn(dto);

        ResponseEntity<ServerOutputDTO> response = serverController.create(inputDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void update_returnsOkWithServiceResult() {
        ServerInputDTO inputDTO = new ServerInputDTO();
        inputDTO.setName("srv-01");
        inputDTO.setEnvironmentId(1L);
        inputDTO.setServerTypeId(2L);
        ServerOutputDTO dto = new ServerOutputDTO();
        when(serverService.update(1L, inputDTO)).thenReturn(dto);

        ResponseEntity<ServerOutputDTO> response = serverController.update(1L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void delete_returnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = serverController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(serverService).delete(1L);
    }

    @Test
    void reactivate_returnsOkWithServiceResult() {
        ServerOutputDTO dto = new ServerOutputDTO();
        when(serverService.reactivate(1L)).thenReturn(dto);

        ResponseEntity<ServerOutputDTO> response = serverController.reactivate(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }
}

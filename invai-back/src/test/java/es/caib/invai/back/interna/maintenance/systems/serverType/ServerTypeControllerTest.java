package es.caib.invai.back.interna.maintenance.systems.serverType;

import es.caib.invai.back.interna.maintenance.systems.serverType.DTO.ServerTypeOutputDTO;
import es.caib.invai.back.service.facade.maintenance.systems.serverType.ServerTypeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ServerTypeController}, verifying that the sole read-only endpoint
 * delegates to {@link ServerTypeService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class ServerTypeControllerTest {

    @Mock
    private ServerTypeService serverTypeService;

    private ServerTypeController serverTypeController;

    @BeforeEach
    void setUp() {
        serverTypeController = new ServerTypeController(serverTypeService);
    }

    @Test
    void getAll_returnsOkWithServiceResult() {
        List<ServerTypeOutputDTO> list = List.of(new ServerTypeOutputDTO());
        when(serverTypeService.getAll()).thenReturn(list);

        ResponseEntity<List<ServerTypeOutputDTO>> response = serverTypeController.getAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(list, response.getBody());
    }
}

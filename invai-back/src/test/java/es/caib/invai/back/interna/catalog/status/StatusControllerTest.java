package es.caib.invai.back.interna.catalog.status;

import es.caib.invai.back.interna.catalog.status.DTO.StatusOutputDTO;
import es.caib.invai.back.service.facade.catalog.status.StatusService;
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
 * Unit tests for {@link StatusController}, verifying that the sole read-only endpoint
 * delegates to {@link StatusService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class StatusControllerTest {

    @Mock
    private StatusService statusService;

    private StatusController statusController;

    @BeforeEach
    void setUp() {
        statusController = new StatusController(statusService);
    }

    @Test
    void getAll_returnsOkWithServiceResult() {
        List<StatusOutputDTO> list = List.of(new StatusOutputDTO());
        when(statusService.getAll()).thenReturn(list);

        ResponseEntity<List<StatusOutputDTO>> response = statusController.getAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(list, response.getBody());
    }
}

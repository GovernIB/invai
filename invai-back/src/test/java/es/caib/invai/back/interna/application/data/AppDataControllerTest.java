package es.caib.invai.back.interna.application.data;

import es.caib.invai.back.interna.application.data.DTO.AppDataInputDTO;
import es.caib.invai.back.interna.application.data.DTO.AppDataOutputDTO;
import es.caib.invai.back.service.facade.application.data.AppDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AppDataController}, verifying that every endpoint delegates to
 * {@link AppDataService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class AppDataControllerTest {

    @Mock
    private AppDataService appDataService;

    private AppDataController appDataController;

    @BeforeEach
    void setUp() {
        appDataController = new AppDataController(appDataService);
    }

    @Test
    void getById_returnsOkWithServiceResult() {
        AppDataOutputDTO dto = new AppDataOutputDTO();
        when(appDataService.getById(1L)).thenReturn(dto);

        ResponseEntity<AppDataOutputDTO> response = appDataController.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void create_returnsCreatedWithServiceResult() {
        AppDataInputDTO inputDTO = new AppDataInputDTO(10L, "Observacio", null, false, null, false);
        AppDataOutputDTO dto = new AppDataOutputDTO();
        when(appDataService.create(inputDTO)).thenReturn(dto);

        ResponseEntity<AppDataOutputDTO> response = appDataController.create(inputDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void update_returnsOkWithServiceResult() {
        AppDataInputDTO inputDTO = new AppDataInputDTO(10L, "Observacio actualitzada", null, false, null, false);
        AppDataOutputDTO dto = new AppDataOutputDTO();
        when(appDataService.update(1L, inputDTO)).thenReturn(dto);

        ResponseEntity<AppDataOutputDTO> response = appDataController.update(1L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void delete_returnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = appDataController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(appDataService).delete(1L);
    }
}

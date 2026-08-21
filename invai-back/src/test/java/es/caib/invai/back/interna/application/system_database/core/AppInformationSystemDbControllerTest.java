package es.caib.invai.back.interna.application.system_database.core;

import es.caib.invai.back.interna.application.system_database.core.DTO.AppInformationSystemDbInputDTO;
import es.caib.invai.back.interna.application.system_database.core.DTO.AppInformationSystemDbOutputDTO;
import es.caib.invai.back.service.facade.application.system_database.core.AppInformationSystemDbService;
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
 * Unit tests for {@link AppInformationSystemDbController}, verifying that every endpoint
 * delegates to {@link AppInformationSystemDbService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class AppInformationSystemDbControllerTest {

    @Mock
    private AppInformationSystemDbService appInformationSystemDbService;

    private AppInformationSystemDbController appInformationSystemDbController;

    @BeforeEach
    void setUp() {
        appInformationSystemDbController = new AppInformationSystemDbController(appInformationSystemDbService);
    }

    @Test
    void getAllInformationSystemDbId_returnsOkWithServiceResult() {
        AppInformationSystemDbOutputDTO dto = new AppInformationSystemDbOutputDTO();
        when(appInformationSystemDbService.getById(10L)).thenReturn(dto);

        ResponseEntity<AppInformationSystemDbOutputDTO> response = appInformationSystemDbController.getAllInformationSystemDbId(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void create_returnsCreatedWithServiceResult() {
        AppInformationSystemDbInputDTO inputDTO = new AppInformationSystemDbInputDTO(10L, "Notes");
        AppInformationSystemDbOutputDTO dto = new AppInformationSystemDbOutputDTO();
        when(appInformationSystemDbService.create(inputDTO)).thenReturn(dto);

        ResponseEntity<AppInformationSystemDbOutputDTO> response = appInformationSystemDbController.create(inputDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void update_returnsOkWithServiceResult() {
        AppInformationSystemDbInputDTO inputDTO = new AppInformationSystemDbInputDTO(10L, "Notes");
        AppInformationSystemDbOutputDTO dto = new AppInformationSystemDbOutputDTO();
        when(appInformationSystemDbService.update(1L, inputDTO)).thenReturn(dto);

        ResponseEntity<AppInformationSystemDbOutputDTO> response = appInformationSystemDbController.update(1L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void delete_returnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = appInformationSystemDbController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(appInformationSystemDbService).delete(1L);
    }
}

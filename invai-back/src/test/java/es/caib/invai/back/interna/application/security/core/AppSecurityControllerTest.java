package es.caib.invai.back.interna.application.security.core;

import es.caib.invai.back.interna.application.security.core.DTO.AppSecurityInputDTO;
import es.caib.invai.back.interna.application.security.core.DTO.AppSecurityOutputDTO;
import es.caib.invai.back.service.facade.application.security.core.AppSecurityService;
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
 * Unit tests for {@link AppSecurityController}, verifying that every endpoint
 * delegates to {@link AppSecurityService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class AppSecurityControllerTest {

    @Mock
    private AppSecurityService appSecurityService;

    private AppSecurityController appSecurityController;

    @BeforeEach
    void setUp() {
        appSecurityController = new AppSecurityController(appSecurityService);
    }

    @Test
    void getAllSecurityId_returnsOkWithServiceResult() {
        AppSecurityOutputDTO dto = new AppSecurityOutputDTO();
        when(appSecurityService.getById(10L)).thenReturn(dto);

        ResponseEntity<AppSecurityOutputDTO> response = appSecurityController.getAllSecurityId(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void create_returnsCreatedWithServiceResult() {
        AppSecurityInputDTO inputDTO = new AppSecurityInputDTO(10L, "Notes");
        AppSecurityOutputDTO dto = new AppSecurityOutputDTO();
        when(appSecurityService.create(inputDTO)).thenReturn(dto);

        ResponseEntity<AppSecurityOutputDTO> response = appSecurityController.create(inputDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void update_returnsOkWithServiceResult() {
        AppSecurityInputDTO inputDTO = new AppSecurityInputDTO(10L, "Notes");
        AppSecurityOutputDTO dto = new AppSecurityOutputDTO();
        when(appSecurityService.update(1L, inputDTO)).thenReturn(dto);

        ResponseEntity<AppSecurityOutputDTO> response = appSecurityController.update(1L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void delete_returnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = appSecurityController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(appSecurityService).delete(1L);
    }
}

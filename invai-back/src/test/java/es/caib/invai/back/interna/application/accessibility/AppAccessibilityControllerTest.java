package es.caib.invai.back.interna.application.accessibility;

import es.caib.invai.back.interna.application.accessibility.DTO.AppAccessibilityInputDTO;
import es.caib.invai.back.interna.application.accessibility.DTO.AppAccessibilityOutputDTO;
import es.caib.invai.back.service.facade.application.accessibility.AppAccessibilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AppAccessibilityController}, verifying that every endpoint
 * delegates to {@link AppAccessibilityService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class AppAccessibilityControllerTest {

    @Mock
    private AppAccessibilityService appAccessibilityService;

    private AppAccessibilityController appAccessibilityController;

    @BeforeEach
    void setUp() {
        appAccessibilityController = new AppAccessibilityController(appAccessibilityService);
    }

    private AppAccessibilityInputDTO buildInputDTO() {
        return new AppAccessibilityInputDTO(10L, 400L, 401L, "https://invai.example.test",
                Boolean.TRUE, "APP INVAI Android", "PDF antic", "Observacio",
                LocalDateTime.of(2026, 1, 1, 0, 0));
    }

    @Test
    void getById_returnsOkWithServiceResult() {
        AppAccessibilityOutputDTO dto = new AppAccessibilityOutputDTO();
        when(appAccessibilityService.getById(1L)).thenReturn(dto);

        ResponseEntity<AppAccessibilityOutputDTO> response = appAccessibilityController.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void create_returnsCreatedWithServiceResult() {
        AppAccessibilityInputDTO inputDTO = buildInputDTO();
        AppAccessibilityOutputDTO dto = new AppAccessibilityOutputDTO();
        when(appAccessibilityService.create(inputDTO)).thenReturn(dto);

        ResponseEntity<AppAccessibilityOutputDTO> response = appAccessibilityController.create(inputDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void update_returnsOkWithServiceResult() {
        AppAccessibilityInputDTO inputDTO = buildInputDTO();
        AppAccessibilityOutputDTO dto = new AppAccessibilityOutputDTO();
        when(appAccessibilityService.update(1L, inputDTO)).thenReturn(dto);

        ResponseEntity<AppAccessibilityOutputDTO> response = appAccessibilityController.update(1L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void delete_returnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = appAccessibilityController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(appAccessibilityService).delete(1L);
    }
}

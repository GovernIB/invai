package es.caib.invai.back.interna.application.development.core;

import es.caib.invai.back.interna.application.development.core.DTO.DevelopmentInputDTO;
import es.caib.invai.back.interna.application.development.core.DTO.DevelopmentOutputDTO;
import es.caib.invai.back.service.facade.application.development.core.AppDevelopmentService;
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
 * Unit tests for {@link AppDevelopmentController}, verifying that every endpoint delegates to
 * {@link AppDevelopmentService} and returns the expected HTTP status code. This is a 1:1 child entity
 * scoped to a parent application: it is addressed by its own record ID via {@code getById}, not a
 * paginated per-application listing, and has no {@code reactivate} endpoint.
 */
@ExtendWith(MockitoExtension.class)
class AppDevelopmentControllerTest {

    @Mock
    private AppDevelopmentService appDevelopmentService;

    private AppDevelopmentController appDevelopmentController;

    @BeforeEach
    void setUp() {
        appDevelopmentController = new AppDevelopmentController(appDevelopmentService);
    }

    @Test
    void getById_returnsOkWithServiceResult() {
        DevelopmentOutputDTO dto = new DevelopmentOutputDTO();
        when(appDevelopmentService.getById(10L)).thenReturn(dto);

        ResponseEntity<DevelopmentOutputDTO> response = appDevelopmentController.getById(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void create_returnsCreatedWithServiceResult() {
        DevelopmentInputDTO inputDTO = new DevelopmentInputDTO(1L, 2L, 3L, "code", 4L, LocalDateTime.now(), "obs");
        DevelopmentOutputDTO dto = new DevelopmentOutputDTO();
        when(appDevelopmentService.create(inputDTO)).thenReturn(dto);

        ResponseEntity<DevelopmentOutputDTO> response = appDevelopmentController.create(inputDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void update_returnsOkWithServiceResult() {
        DevelopmentInputDTO inputDTO = new DevelopmentInputDTO(1L, 2L, 3L, "code", 4L, LocalDateTime.now(), "obs");
        DevelopmentOutputDTO dto = new DevelopmentOutputDTO();
        when(appDevelopmentService.update(1L, inputDTO)).thenReturn(dto);

        ResponseEntity<DevelopmentOutputDTO> response = appDevelopmentController.update(1L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void delete_returnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = appDevelopmentController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(appDevelopmentService).delete(1L);
    }
}

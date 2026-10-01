package es.caib.invai.back.interna.application.development.webContext;

import es.caib.invai.back.interna.application.development.webContext.DTO.AppWebContextInputDTO;
import es.caib.invai.back.interna.application.development.webContext.DTO.AppWebContextOutputDTO;
import es.caib.invai.back.persistence.repository.application.security.webContext.AppWebContextCriteria;
import es.caib.invai.back.service.facade.application.development.webContext.AppWebContextService;
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
 * Unit tests for {@link AppWebContextController} (Development's view): full CRUD, verifying
 * delegation to {@link AppWebContextService} and the expected HTTP status codes.
 */
@ExtendWith(MockitoExtension.class)
class AppWebContextControllerTest {

    @Mock
    private AppWebContextService appWebContextService;

    private AppWebContextController appWebContextController;

    @BeforeEach
    void setUp() {
        appWebContextController = new AppWebContextController(appWebContextService);
    }

    @Test
    void getAllByAppDevelopmentId_returnsOkWithPagedServiceResult() {
        AppWebContextCriteria criteria = new AppWebContextCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<AppWebContextOutputDTO> page = new PageImpl<>(List.of(new AppWebContextOutputDTO()));
        when(appWebContextService.getAll(15L, criteria, pageable)).thenReturn(page);

        ResponseEntity<Page<AppWebContextOutputDTO>> response = appWebContextController.getAllByAppDevelopmentId(15L, criteria, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
    }

    @Test
    void create_returnsCreatedWithServiceResult() {
        AppWebContextInputDTO inputDTO = new AppWebContextInputDTO(15L, 7L, 9L, "obs", null);
        AppWebContextOutputDTO dto = new AppWebContextOutputDTO();
        when(appWebContextService.create(inputDTO)).thenReturn(dto);

        ResponseEntity<AppWebContextOutputDTO> response = appWebContextController.create(inputDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void update_returnsOkWithServiceResult() {
        AppWebContextInputDTO inputDTO = new AppWebContextInputDTO(15L, 7L, 9L, "obs", null);
        AppWebContextOutputDTO dto = new AppWebContextOutputDTO();
        when(appWebContextService.update(1L, inputDTO)).thenReturn(dto);

        ResponseEntity<AppWebContextOutputDTO> response = appWebContextController.update(1L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void delete_returnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = appWebContextController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(appWebContextService).delete(1L);
    }
}

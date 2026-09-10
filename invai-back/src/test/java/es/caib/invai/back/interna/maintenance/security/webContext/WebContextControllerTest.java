package es.caib.invai.back.interna.maintenance.security.webContext;

import es.caib.invai.back.interna.maintenance.security.webContext.DTO.WebContextInputDTO;
import es.caib.invai.back.interna.maintenance.security.webContext.DTO.WebContextOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.security.webContext.WebContextCriteria;
import es.caib.invai.back.service.facade.maintenance.security.webContext.WebContextService;
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
 * Unit tests for {@link WebContextController}, verifying that every endpoint delegates to
 * {@link WebContextService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class WebContextControllerTest {

    @Mock
    private WebContextService webContextService;

    private WebContextController webContextController;

    @BeforeEach
    void setUp() {
        webContextController = new WebContextController(webContextService);
    }

    @Test
    void getById_returnsOkWithServiceResult() {
        WebContextOutputDTO dto = new WebContextOutputDTO();
        when(webContextService.getById(1L)).thenReturn(dto);

        ResponseEntity<WebContextOutputDTO> response = webContextController.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void getAll_returnsOkWithPagedServiceResult() {
        WebContextCriteria filter = new WebContextCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<WebContextOutputDTO> page = new PageImpl<>(List.of(new WebContextOutputDTO()));
        when(webContextService.getAll(filter, pageable)).thenReturn(page);

        ResponseEntity<Page<WebContextOutputDTO>> response = webContextController.getAll(filter, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
    }

    @Test
    void create_returnsCreatedWithServiceResult() {
        WebContextInputDTO inputDTO = new WebContextInputDTO("Name", "Name ES");
        WebContextOutputDTO dto = new WebContextOutputDTO();
        when(webContextService.create(inputDTO)).thenReturn(dto);

        ResponseEntity<WebContextOutputDTO> response = webContextController.create(inputDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void update_returnsOkWithServiceResult() {
        WebContextInputDTO inputDTO = new WebContextInputDTO("Name", "Name ES");
        WebContextOutputDTO dto = new WebContextOutputDTO();
        when(webContextService.update(1L, inputDTO)).thenReturn(dto);

        ResponseEntity<WebContextOutputDTO> response = webContextController.update(1L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void delete_returnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = webContextController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(webContextService).delete(1L);
    }

    @Test
    void reactivate_returnsOkWithServiceResult() {
        WebContextOutputDTO dto = new WebContextOutputDTO();
        when(webContextService.reactivate(1L)).thenReturn(dto);

        ResponseEntity<WebContextOutputDTO> response = webContextController.reactivate(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }
}

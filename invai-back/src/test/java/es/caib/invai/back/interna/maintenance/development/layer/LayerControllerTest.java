package es.caib.invai.back.interna.maintenance.development.layer;

import es.caib.invai.back.interna.maintenance.development.layer.DTO.LayerInputDTO;
import es.caib.invai.back.interna.maintenance.development.layer.DTO.LayerOutputDTO;
import es.caib.invai.back.persistence.repository.maintenance.development.layer.LayerCriteria;
import es.caib.invai.back.service.facade.maintenance.development.layer.LayerService;
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
 * Unit tests for {@link LayerController}, verifying that every endpoint delegates to
 * {@link LayerService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class LayerControllerTest {

    @Mock
    private LayerService layerService;

    private LayerController layerController;

    @BeforeEach
    void setUp() {
        layerController = new LayerController(layerService);
    }

    @Test
    void getById_returnsOkWithServiceResult() {
        LayerOutputDTO dto = new LayerOutputDTO();
        when(layerService.getById(1L)).thenReturn(dto);

        ResponseEntity<LayerOutputDTO> response = layerController.getById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void getAll_returnsOkWithPagedServiceResult() {
        LayerCriteria filter = new LayerCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<LayerOutputDTO> page = new PageImpl<>(List.of(new LayerOutputDTO()));
        when(layerService.getAll(filter, pageable)).thenReturn(page);

        ResponseEntity<Page<LayerOutputDTO>> response = layerController.getAll(filter, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
    }

    @Test
    void create_returnsCreatedWithServiceResult() {
        LayerInputDTO inputDTO = new LayerInputDTO("Presentation");
        LayerOutputDTO dto = new LayerOutputDTO();
        when(layerService.create(inputDTO)).thenReturn(dto);

        ResponseEntity<LayerOutputDTO> response = layerController.create(inputDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void update_returnsOkWithServiceResult() {
        LayerInputDTO inputDTO = new LayerInputDTO("Presentation");
        LayerOutputDTO dto = new LayerOutputDTO();
        when(layerService.update(1L, inputDTO)).thenReturn(dto);

        ResponseEntity<LayerOutputDTO> response = layerController.update(1L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void delete_returnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = layerController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(layerService).delete(1L);
    }

    @Test
    void reactivate_returnsOkWithServiceResult() {
        LayerOutputDTO dto = new LayerOutputDTO();
        when(layerService.reactivate(1L)).thenReturn(dto);

        ResponseEntity<LayerOutputDTO> response = layerController.reactivate(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }
}

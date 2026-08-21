package es.caib.invai.back.interna.catalog.modality;

import es.caib.invai.back.interna.catalog.modality.DTO.ModalityOutputDTO;
import es.caib.invai.back.service.facade.catalog.modality.ModalityService;
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
 * Unit tests for {@link ModalityController}, verifying that the sole read-only endpoint
 * delegates to {@link ModalityService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class ModalityControllerTest {

    @Mock
    private ModalityService modalityService;

    private ModalityController modalityController;

    @BeforeEach
    void setUp() {
        modalityController = new ModalityController(modalityService);
    }

    @Test
    void getAll_returnsOkWithServiceResult() {
        List<ModalityOutputDTO> list = List.of(new ModalityOutputDTO());
        when(modalityService.getAll()).thenReturn(list);

        ResponseEntity<List<ModalityOutputDTO>> response = modalityController.getAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(list, response.getBody());
    }
}

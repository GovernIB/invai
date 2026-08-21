package es.caib.invai.back.interna.catalog.standardAdaption;

import es.caib.invai.back.interna.catalog.standardAdaption.DTO.StandardAdaptionOutputDTO;
import es.caib.invai.back.service.facade.catalog.standardAdaption.StandardAdaptionService;
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
 * Unit tests for {@link StandardAdaptionController}, verifying that the sole read-only endpoint
 * delegates to {@link StandardAdaptionService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class StandardAdaptionControllerTest {

    @Mock
    private StandardAdaptionService standardAdaptionService;

    private StandardAdaptionController standardAdaptionController;

    @BeforeEach
    void setUp() {
        standardAdaptionController = new StandardAdaptionController(standardAdaptionService);
    }

    @Test
    void getAll_returnsOkWithServiceResult() {
        List<StandardAdaptionOutputDTO> list = List.of(new StandardAdaptionOutputDTO());
        when(standardAdaptionService.getAll()).thenReturn(list);

        ResponseEntity<List<StandardAdaptionOutputDTO>> response = standardAdaptionController.getAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(list, response.getBody());
    }
}

package es.caib.invai.back.interna.catalog.responsibleType;

import es.caib.invai.back.interna.catalog.responsibleType.DTO.ResponsibleTypeOutputDTO;
import es.caib.invai.back.service.facade.catalog.responsibleType.ResponsibleTypeService;
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
 * Unit tests for {@link ResponsibleTypeController}, verifying that the sole read-only endpoint
 * delegates to {@link ResponsibleTypeService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class ResponsibleTypeControllerTest {

    @Mock
    private ResponsibleTypeService responsibleTypeService;

    private ResponsibleTypeController responsibleTypeController;

    @BeforeEach
    void setUp() {
        responsibleTypeController = new ResponsibleTypeController(responsibleTypeService);
    }

    @Test
    void getAll_returnsOkWithServiceResult() {
        List<ResponsibleTypeOutputDTO> list = List.of(new ResponsibleTypeOutputDTO());
        when(responsibleTypeService.getAll()).thenReturn(list);

        ResponseEntity<List<ResponsibleTypeOutputDTO>> response = responsibleTypeController.getAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(list, response.getBody());
    }
}

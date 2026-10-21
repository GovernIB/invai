package es.caib.invai.back.interna.catalog.validationStatus;

import es.caib.invai.back.interna.catalog.validationStatus.DTO.ValidationStatusOutputDTO;
import es.caib.invai.back.service.facade.catalog.validationStatus.ValidationStatusService;
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
 * Unit tests for {@link ValidationStatusController}, verifying that the sole read-only endpoint
 * delegates to {@link ValidationStatusService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class ValidationStatusControllerTest {

    @Mock
    private ValidationStatusService validationStatusService;

    private ValidationStatusController validationStatusController;

    @BeforeEach
    void setUp() {
        validationStatusController = new ValidationStatusController(validationStatusService);
    }

    @Test
    void getAll_returnsOkWithServiceResult() {
        List<ValidationStatusOutputDTO> list = List.of(new ValidationStatusOutputDTO());
        when(validationStatusService.getAll()).thenReturn(list);

        ResponseEntity<List<ValidationStatusOutputDTO>> response = validationStatusController.getAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(list, response.getBody());
    }
}

package es.caib.invai.back.interna.catalog.dir3Status;

import es.caib.invai.back.interna.catalog.dir3Status.DTO.Dir3StatusOutputDTO;
import es.caib.invai.back.service.facade.catalog.dir3Status.Dir3StatusService;
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
 * Unit tests for {@link Dir3StatusController}, verifying that the sole read-only endpoint
 * delegates to {@link Dir3StatusService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class Dir3StatusControllerTest {

    @Mock
    private Dir3StatusService dir3StatusService;

    private Dir3StatusController dir3StatusController;

    @BeforeEach
    void setUp() {
        dir3StatusController = new Dir3StatusController(dir3StatusService);
    }

    @Test
    void getAll_returnsOkWithServiceResult() {
        List<Dir3StatusOutputDTO> list = List.of(new Dir3StatusOutputDTO());
        when(dir3StatusService.getAll()).thenReturn(list);

        ResponseEntity<List<Dir3StatusOutputDTO>> response = dir3StatusController.getAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(list, response.getBody());
    }
}

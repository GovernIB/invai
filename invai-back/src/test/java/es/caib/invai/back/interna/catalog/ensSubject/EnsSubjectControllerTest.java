package es.caib.invai.back.interna.catalog.ensSubject;

import es.caib.invai.back.interna.catalog.ensSubject.DTO.EnsSubjectOutputDTO;
import es.caib.invai.back.service.facade.catalog.ensSubject.EnsSubjectService;
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
 * Unit tests for {@link EnsSubjectController}, verifying that the sole read-only endpoint
 * delegates to {@link EnsSubjectService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class EnsSubjectControllerTest {

    @Mock
    private EnsSubjectService ensSubjectService;

    private EnsSubjectController ensSubjectController;

    @BeforeEach
    void setUp() {
        ensSubjectController = new EnsSubjectController(ensSubjectService);
    }

    @Test
    void getAll_returnsOkWithServiceResult() {
        List<EnsSubjectOutputDTO> list = List.of(new EnsSubjectOutputDTO());
        when(ensSubjectService.getAll()).thenReturn(list);

        ResponseEntity<List<EnsSubjectOutputDTO>> response = ensSubjectController.getAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(list, response.getBody());
    }
}

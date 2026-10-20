package es.caib.invai.back.interna.application.responsibleAuthorized.dir3;

import es.caib.invai.back.interna.application.responsibleAuthorized.dir3.DTO.Dir3ManualValidateInputDTO;
import es.caib.invai.back.interna.application.responsibleAuthorized.dir3.DTO.Dir3ValidationOutputDTO;
import es.caib.invai.back.service.facade.application.responsibleAuthorized.dir3.Dir3ValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link Dir3ValidationController}, verifying that the endpoint delegates to
 * {@link Dir3ValidationService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class Dir3ValidationControllerTest {

    @Mock
    private Dir3ValidationService dir3ValidationService;

    private Dir3ValidationController dir3ValidationController;

    @BeforeEach
    void setUp() {
        dir3ValidationController = new Dir3ValidationController(dir3ValidationService);
    }

    @Test
    void validateManually_returnsOkWithServiceResult() {
        Dir3ManualValidateInputDTO inputDTO = new Dir3ManualValidateInputDTO("Motiu");
        Dir3ValidationOutputDTO dto = new Dir3ValidationOutputDTO();
        when(dir3ValidationService.validateManually(500L, inputDTO)).thenReturn(dto);

        ResponseEntity<Dir3ValidationOutputDTO> response = dir3ValidationController.validateManually(500L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }
}

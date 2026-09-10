package es.caib.invai.back.interna.catalog.securityLevel;

import es.caib.invai.back.interna.catalog.securityLevel.DTO.SecurityLevelOutputDTO;
import es.caib.invai.back.service.facade.catalog.securityLevel.SecurityLevelService;
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
 * Unit tests for {@link SecurityLevelController}, verifying that the sole read-only endpoint
 * delegates to {@link SecurityLevelService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class SecurityLevelControllerTest {

    @Mock
    private SecurityLevelService securityLevelService;

    private SecurityLevelController securityLevelController;

    @BeforeEach
    void setUp() {
        securityLevelController = new SecurityLevelController(securityLevelService);
    }

    @Test
    void getAll_returnsOkWithServiceResult() {
        List<SecurityLevelOutputDTO> list = List.of(new SecurityLevelOutputDTO());
        when(securityLevelService.getAll()).thenReturn(list);

        ResponseEntity<List<SecurityLevelOutputDTO>> response = securityLevelController.getAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(list, response.getBody());
    }
}

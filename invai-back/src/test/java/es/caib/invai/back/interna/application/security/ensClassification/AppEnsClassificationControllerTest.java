package es.caib.invai.back.interna.application.security.ensClassification;

import es.caib.invai.back.interna.application.security.ensClassification.DTO.AppEnsClassificationInputDTO;
import es.caib.invai.back.interna.application.security.ensClassification.DTO.AppEnsClassificationOutputDTO;
import es.caib.invai.back.persistence.repository.application.security.ensClassification.AppEnsClassificationCriteria;
import es.caib.invai.back.service.facade.application.security.ensClassification.AppEnsClassificationService;
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
 * Unit tests for {@link AppEnsClassificationController}, verifying that every endpoint delegates to
 * {@link AppEnsClassificationService} and returns the expected HTTP status code.
 */
@ExtendWith(MockitoExtension.class)
class AppEnsClassificationControllerTest {

    @Mock
    private AppEnsClassificationService appEnsClassificationService;

    private AppEnsClassificationController appEnsClassificationController;

    @BeforeEach
    void setUp() {
        appEnsClassificationController = new AppEnsClassificationController(appEnsClassificationService);
    }

    @Test
    void getAllByAppSecurityId_returnsOkWithPagedServiceResult() {
        AppEnsClassificationCriteria criteria = new AppEnsClassificationCriteria();
        Pageable pageable = Pageable.unpaged();
        Page<AppEnsClassificationOutputDTO> page = new PageImpl<>(List.of(new AppEnsClassificationOutputDTO()));
        when(appEnsClassificationService.getAll(10L, criteria, pageable)).thenReturn(page);

        ResponseEntity<Page<AppEnsClassificationOutputDTO>> response = appEnsClassificationController.getAllByAppSecurityId(10L, criteria, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(page, response.getBody());
    }

    @Test
    void create_returnsCreatedWithServiceResult() {
        AppEnsClassificationInputDTO inputDTO = new AppEnsClassificationInputDTO();
        inputDTO.setAppSecurityId(5L);
        AppEnsClassificationOutputDTO dto = new AppEnsClassificationOutputDTO();
        when(appEnsClassificationService.create(inputDTO)).thenReturn(dto);

        ResponseEntity<AppEnsClassificationOutputDTO> response = appEnsClassificationController.create(inputDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void update_returnsOkWithServiceResult() {
        AppEnsClassificationInputDTO inputDTO = new AppEnsClassificationInputDTO();
        inputDTO.setAppSecurityId(5L);
        AppEnsClassificationOutputDTO dto = new AppEnsClassificationOutputDTO();
        when(appEnsClassificationService.update(1L, inputDTO)).thenReturn(dto);

        ResponseEntity<AppEnsClassificationOutputDTO> response = appEnsClassificationController.update(1L, inputDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(dto, response.getBody());
    }

    @Test
    void delete_returnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = appEnsClassificationController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(appEnsClassificationService).delete(1L);
    }
}
